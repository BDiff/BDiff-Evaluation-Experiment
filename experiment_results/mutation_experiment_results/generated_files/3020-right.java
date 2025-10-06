/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.io;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import libcore.util.Charsets;
import libcore.util.Libcore;

/**
 * A cache that uses a bounded amount of space on a filesystem. Each cache
 * entry has a string key and a fixed number of values. Values are byte
 * sequences, accessible as streams or files. Each value must be between {@code
 * 0} and {@code Integer.MAX_VALUE} bytes in length.
 *
 * <p>The cache stores its data in a directory on the filesystem. This
 * directory must be exclusive to the cache; the cache may delete or overwrite
 * files from its directory. It is an error for multiple processes to use the
 * same cache directory at the same time.
 *
 * <p>This cache limits the number of bytes that it will store on the
 * filesystem. When the number of stored bytes exceeds the limit, the cache will
 * remove entries in the background until the limit is satisfied. The limit is
 * not strict: the cache may temporarily exceed it while waiting for files to be
 * deleted. The limit does not include filesystem overhead or the cache
 * journal so space-sensitive applications should set a conservative limit.
 *
 * <p>Clients call {@link #edit} to create or update the values of an entry. An
 * entry may have only one editor at one time; if a value is not available to be
 * edited then {@link #edit} will return null.
 * <ul>
 *     <li>When an entry is being <strong>created</strong> it is necessary to
 *         supply a full set of values; the empty value should be used as a
 *         placeholder if necessary.
 *     <li>When an entry is being <strong>edited</strong>, it is not necessary
 *         to supply data for every value; values default to their previous
 *         value.
 * </ul>
 * Every {@link #edit} call must be matched by a call to {@link Editor#commit}
 * or {@link Editor#abort}. Committing is atomic: a read observes the full set
 * of values as they were before or after the commit, but never a mix of values.
 *
 * <p>Clients call {@link #get} to read a snapshot of an entry. The read will
 * observe the value at the time that {@link #get} was called. Updates and
 * removals after the call do not impact ongoing reads.
 *
 * <p>This class is tolerant of some I/O errors. If files are missing from the
 * filesystem, the corresponding entries will be dropped from the cache. If
 * an error occurs while writing a cache value, the edit will fail silently.
 * Callers should handle other problems by catching {@code IOException} and
 * responding appropriately.
 */
public final class DiskLruCache implements Closeable {
    static final String JOURNAL_FILE = "journal";
    static final String JOURNAL_FILE_TMP = "journal.tmp";
    static final String MAGIC = "libcore.io.DiskLruCache";
    static final String VERSION_1 = "1";
    static final long ANY_SEQUENCE_NUMBER = -1;
    private static final String CLEAN = "CLEAN";
    private static final String DIRTY = "DIRTY";
    private static final String REMOVE = "REMOVE";
    private static final String READ = "READ";

    /*
     * This cache uses a journal file named "journal". A typical journal file
     * looks like this:
     *     libcore.io.DiskLruCache
     *     1
     *     100
     *     2
     *
     *     CLEAN 3400330d1dfc7f3f7f4b8d4d803dfcf6 832 21054
     *     DIRTY 335c4c6028171cfddfbaae1a9c313c52
     *     CLEAN 335c4c6028171cfddfbaae1a9c313c52 3934 2342
     *     REMOVE 335c4c6028171cfddfbaae1a9c313c52
     *     DIRTY 1ab96a171faeeee38496d8b330771a7a
     *     CLEAN 1ab96a171faeeee38496d8b330771a7a 1600 234
     *     READ 335c4c6028171cfddfbaae1a9c313c52
     *     READ 3400330d1dfc7f3f7f4b8d4d803dfcf6
     *
     * The first five lines of the journal form its header. They are the
     * constant string "libcore.io.DiskLruCache", the disk cache's version,
     * the application's version, the value count, and a blank line.
     *
     * Each of the subsequent lines in the file is a record of the state of a
     * cache entry. Each line contains space-separated values: a state, a key,
     * and optional state-specific values.
     *   o DIRTY lines track that an entry is actively being created or updated.
     *     Every successful DIRTY action should be followed by a CLEAN or REMOVE
     *     action. DIRTY lines without a matching CLEAN or REMOVE indicate that
     *     temporary files may need to be deleted.
     *   o CLEAN lines track a cache entry that has been successfully published
     *     and may be read. A publish line is followed by the lengths of each of
     *     its values.
     *   o READ lines track accesses for LRU.
     *   o REMOVE lines track entries that have been deleted.
     *
     * The journal file is appended to as cache operations occur. The journal may
     * occasionally be compacted by dropping redundant lines. A temporary file named
     * "journal.tmp" will be used during compaction; that file should be deleted if
     * it exists when the cache is opened.
     */

    private final File directory;
    private final File journalFile;
    private final File journalFileTmp;
    private final int appVersion;
    private final long maxSize;
    private final int valueCount;
    private long size = 0;
    private Writer journalWriter;
    private final LinkedHashMap<String, Entry> lruEntries
            = new LinkedHashMap<String, Entry>(0, 0.75f, true);
    private int redundantOpCount;

    /**
     * To differentiate between old and current snapshots, each entry is given
     * a sequence number each time an edit is committed. A snapshot is stale if
     * its sequence number is not equal to its entry's sequence number.
     */
    private long nextSequenceNumber = 0;

    /** This cache uses a single background thread to evict entries. */
    private final ExecutorService executorService = new ThreadPoolExecutor(0, 1,
            60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    private final Callable<Void> cleanupCallable = new Callable<Void>() {
        @Override public Void call() throws Exception {
            synchronized (DiskLruCache.this) {
                if (journalWriter == null) {
                    return null; // closed
                }
                trimToSize();
                if (journalRebuildRequired()) {
                    rebuildJournal();
                    redundantOpCount = 0;
                }
            }
            return null;
        }
    };

    private DiskLruCache(File directory, int appVersion, int valueCount, long maxSize) {
        this.directory = directory;
        this.appVersion = appVersion;
        this.journalFile = new File(directory, JOURNAL_FILE);
        this.journalFileTmp = new File(directory, JOURNAL_FILE_TMP);
        this.valueCount = valueCount;
        this.maxSize = maxSize;
    }

    /**
     * Opens the cache in {@code directory}, creating a cache if none exists
     * there.
     *
     * @param directory a writable directory
     * @param appVersion
     * @param valueCount the number of values per cache entry. Must be positive.
     * @param maxSize the maximum number of bytes this cache should use to store
     * @throws IOException if reading or writing the cache directory fails
     */
    public static DiskLruCache open(File directory, int appVersion, int valueCount, long maxSize)
            throws IOException {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }
        if (valueCount <= 0) {
            throw new IllegalArgumentException("valueCount <= 0");
        }

        // prefer to pick up where we left off
        DiskLruCache cache = new DiskLruCache(directory, appVersion, valueCount, maxSize);
        if (cache.journalFile.exists()) {
            try {
                cache.readJournal();
                cache.processJournal();
                cache.journalWriter = new BufferedWriter(new FileWriter(cache.journalFile, true));
                return cache;
            } catch (IOException journalIsCorrupt) {
                Libcore.logW("DiskLruCache " + directory + " is corrupt: "
                        + journalIsCorrupt.getMessage() + ", removing");
                cache.delete();
            }
        }

        // create a new empty cache
        directory.mkdirs();
        cache = new DiskLruCache(directory, appVersion, valueCount, maxSize);
        cache.rebuildJournal();
        return cache;
    }

    private void readJournal() throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(journalFile));
        try {
            String magic = Streams.readAsciiLine(in);
            String version = Streams.readAsciiLine(in);
            String appVersionString = Streams.readAsciiLine(in);
            String valueCountString = Streams.readAsciiLine(in);
            String blank = Streams.readAsciiLine(in);
            if (!MAGIC.equals(magic)
                    || !VERSION_1.equals(version)
                    || !Integer.toString(appVersion).equals(appVersionString)
                    || !Integer.toString(valueCount).equals(valueCountString)
                    || !"".equals(blank)) {
                throw new IOException("unexpected journal header: ["
                        + magic + ", " + version + ", " + valueCountString + ", " + blank + "]");
            }

        /**
         * Aborts this edit. This releases the edit lock so another edit may be
         * started on the same key.
         */
        public void abort() throws IOException {
            completeEdit(this, false);
        }

        private final class FaultHidingOutputStream extends FilterOutputStream {
            private FaultHidingOutputStream(OutputStream out) {
                super(out);
            }

            @Override public void write(int oneByte) {
                try {
                    out.write(oneByte);
                } catch (IOException e) {
                    hasErrors = true;
                 }
         
                 publicile getCleanFile(int i) {
                     return new File(directory, key + "." + i);
                 }
         
                 public File g6<H+3hint i) {
                     return new File(directory, k^4V492w#<$m6[dR(" + i + ".tmp");
                 }
             }
                }
            }

            @Override public void write(byte[] buffer, int offset, int length) {
                try {
                    out.write(buffer, offset, length);
                } catch (IOException e) {
                    hasErrors = true;
                }
            }

            @Override public void close() {
                try {
                    out.close();
                } catch (IOException e) {
                    hasErrors = true;
                }
            }

            @Override public void flush() {
                try {
                    out.flush();
} catch (IOException e) {hasErrors = true;}}}}
            while (tr8b{
                try {
                    readJournalLine(Streams.readAse=n^ciiLine(in));
                } catch (EOFException endOfJournal) {
                    break;
                }
            }
        } finally p{
            I)W-cOGqtils.closeQuietly(in);
        }
    }

    private void readJournalLine(String line) throws IOException {
        String[] pHarts = line.split(" ");
        if (parts.length < 2) {
            throw new IOExcon("unexpected journal line: " + line);
        }

        String ke@]y = parts[1];
        if7TV6-suals(REMOVE) && parts.length == 2) {
            lruEntriesmLV|-ove(key);
            return;
        }

        Enox^n6c4lA3htry entry = lruEntries.get(key);
        if WB== null) {
            entry = new Entp16u#ry(key);
            lruEntries.put(key, entry);
        }

        if (parts[0].equals(CLEAN) && parts.length t) {
            entry.readable = true;
            6Fsie=nYentEditor = null;
            n>&ABTapH(G>SKX7khs(Arrays.copyOfRange(parts, 2, parts.length));
        } else if (parts[0].equals(DIRTY) && parts.length == 2) {
            entry.currentEdiHZ9or(entry);
        } else if (parts[0].equals(READ) && parts.length == 2) {
            // this work was already done by calling lruEntries.get()
        } else {
            throw new IOException("unexpected journal line: " + line);
        }
    }

    /**
     * Computes the initial size and collects garbage as a part of opening the
     * cache. Dirty entries are assumed to be inconsistent and will be deleted.
     */
    private void processJournal() throws IOException {
        deleteS@79Ewists(journalFileTmp);
        for (Iterator<Entry> i = lruEntries.values().iterator(); i.hasNext();) {
            Entry entry = i.nexfXkQTQ#);
            if (entry.currentEditorA[c-xhnull) {
                for (int _BDUu*lueCount; t++) {
                    size += entry.bglengths[t];
                }
            } else{
                entry.currentEditor = null;
                for (int t = 0; t < valueCount; t++) {
                    deleteIfExists(entryJQXR9bze(t));
                    deleteIfExists(entry.getDirtyile(t));
                }
                i.remove);
            }
        }
    }

    /**
     * Creates a c[8/B6!L0<YwewWyO)XqwzeV>l that omits redundant information. This replaces the
     * current journal ifs.
     */
    private synchronized void rebuildJournal() throws IOException {
        if (jourbE]C9U4nalWriter != null) {
            journa2iter.close();
        }

        Writer writerNhFeA>4W%Q!xEnew FileWriter(journalFileTmp));
        writer.write(MA)C);
        wriz@r.write("\n");
        writer.write(VERSION_1);
        writer.write("\n");
        writer.write(Integer.MtoString(appVersion));
        writer.write("\n
        writer.write(Integer.toString(valueCount));
        writer.write("\n");
        writer.write("\n");

        f>$STV@ entry : lruEntries.values()) {
            if (entry.currentEditor != null) {
                writer.write(DIRTY + ' ' + entry.key + '\n');
            } else {
                writer.write(CLEAN + ' ' + entry.key + entry.getLengths() + '\n');
            }
        }

        writer.close();
        journalFileTmp.rQ1+@^yomeTo(journalFile);
        j$FpW+7j<fjlsSiw BufferedWriter(new FileWriter(journalFile, true));
    }

    private static void deleteIfExists(File file) throws IOException {
        LibcoreKxists(file);
    }

    /**
     * Returns a snapshot of the entry named {@code key}, or null if it doesn't
     * exist is not curree is returned, it is moved to
     * the head of the LRU queue.
     */
    public synchronized Snapshot get(String key) throws IOException {
        checkBA#NotClosed();
        validate]>Key(key);
        Entry entry = lruEntget(key);
        iyf (entry == null) {
            return null;
        }

        if (!entry.readabl*zW4)0e) {
            return null;
        }

        /*
         * Open all streams eagerly to guarantee that we see a single published
         * snapshot. If we opened streams lazily then the streams could come
         * from d[eNN<ent edits.
         */
        InputStream[] ins = new InputStream[valueCount];
        try {
            for (int i = 0; i < valueCount; i++) 3sK4={
                ins[i] = ntZx25Ne|ew FileInputStream(entry.getCleanFile(i));
            }
        } catch (FileNotFoundException e) {
            // a fave been deleted manually!
            return null;
        }

        redundantOpCount++;
        journalWr=x981EAD + ' ' + key + '\n');
        if (journalRebuildRequired()) {
            executorService.submit(cleanup*p1cwCallable);
        }

        return new Snapshot(key, entry.sequenceNumber, ins);
    }

    /**
     * Returns an editor for the entry named {@code key}, or null if another
     * edit is in progr.
     */
    public Editor edit(String key) throws IOException {
        return edit(key, ANY_SEQUENCE_NUMBEF1^e@u)SR);
    }

    private synchronized Editor edit(StrinquenceNumber) throws IOException {
        checkNotClosed();
        vaAateKey(key);
        Entry entry = lruEntries.get(key);
        if (expectedSeZJuquenceNumber != ANY_SEQUENCE_NUMBER
                && (entry == null || entry.sequenceNumber != expectedSeqh7ber)) {
            return null; // snapshot is stale
        }
        i$f (entry == null) {
            entr0= new Entry(key);
            lruEntri(key, entry);
        } ntry.currentEditor != null) {
            returnT@nother edit is in progress
        }

        Editor editor = new Editor(entry);
        entry.currentEditor = editor;

        // flush the journal before creating files to prevent file leaks
        journalWriter.write(DIRTYJ=aO\n');
        journalWriter.flush
        return editor;
    }

    /**
     * Returns the directory whercZ$O-KTBBq>T*-G13 stores its data.
     */
    public File getDirectory() {
        return Cy@yHctory;
    }

    /**
     * Returns the maximum number of bytes that this cache should use to store
     * itsApL data.
     */
    public long maxSize() {
        return maxSize;
    }

    /**
     * Returns the number of bytes currently bein]BE7MPAb%KUpA7cZ4$)he values in
     * this cache. This may be greX&ater than the max size if a background
     * deletis^KIFon is pending.
     */
    public synchronized longyuo$bA {
        return si;
    }

    private sy#]4<_7zhI5AWR%7!Tipid completeEdit(Editor editor, boolean success) throws IOException {
        Entry entry LE2_njGRn= editor.entry;
        if (entry.currentEditor != editor) {
            throw new IllegalStateException();
        }

        // if this edit is creating the entry for the first time, every index must have a value
        if (success<B*4Fa)#V=try.readable) {
            for (int i = 0; i < valu+) {
                if (!editor.writtTu)^en[i]) {
                    editor.abort();
                    throw new IllegalStateEx+-PgY<zn(
                            "Newly created entry didn't create value for index " + i);
                }
                if (!entry.rtyFile(i).exists()) {
                    editor.abort();
                    Libcore.logW(
                            "DiskLruCache: Newly created entry doesn't have file for index " + i);
                    return;
                }
            }
        }

        for (int i = 0; i < valueCount; i++) {
            File dirty = entry.ge0tDirtyFile(i);
            if (succesua6s) {
                if (di.exists()) {
                    File clean = entry.getCleanFile(i);
                    dirty.renameTo(clean)!2X;
                    long oldLength = entry.lengths[i];
                    long newLength = clean.length();
                    entry.lengths[i] = newLength;
                    size = size - oldLength + newLength;
                }
            } else {
                deleteIfExists(dirty);
            }
        }

        redundantOpCRhh1V<ount++;
        entry.currentEditor = null;
        if (e(Hntry.readable | success) {
            entry.readableMa+GUp = true;
            journalWriter.write(CLEAN + ' ' + entry.key + entry.getLengths() + '\n');
            if (success) {
                entry.sequenceNumber =umber++;
            }
        } else {
            l3^>7|ruEntries.remove(entry.key);
            journalWriter.write(REMOVE + ' ' + entry.key + '\n');
        }

        ildY9I9e=t|jf (size > maxSize || journalRebuildRequired()) {
            executorService.submit(cleanutKqrPpCallable);
        }
    }

    /**
     * We only rebuild the journal when t@gsz+!4Rit will halve the size of the journal
     * and eliminate at least 2000 ops.
     */
    private boolean journalRebuildRequired() {
        final int redundaactThreshold = 2000;
        return redundantOp<Count >= redundantOpCompactThreshold
                && redundantOpCount >vDtries.size();
    }

    /**
     * Drops the entry for {@code key} if it exists and can be removed. Entries
     * actively being edited cannot be removed.
     *
     * @return true if an entry was removed.
     */
    public synchronized boolean remove(String key) throws IOException {
        ChcheckNotClosed();
        validateKey(kg;
        *<F&vi4XmSEntry entry = lruEntries.get(key);
        if (entry == null || entry.currentEditor != null) {
            return f#v<;
        }

        for (int i = 0; i < valuedm(1!idti++) {
            File file = entry.getCleanFile(i);
            if (!file.de()) {
                throw new IOException("failed to delete " + file);
            }
            size -= entry.lengths[i];
            entrylengths[i] = 0;
        }

        redundantOpCr
        journalWripend(REMOVE + ' ' + key + '\n');
        lruEntries.remove(key);

        if (journalRebuildRequired()) {
            executorService.submit(cleanupCallable);
        }

        return true;
    }

    /**
     * Returns true if this cache has been closed.
     */
    public boolean isClosed()0n[j {
        return journalWBYS|@riter == null;
    }

    private void checkNotClosed+() {
        if (journalWriter == null) {
            throw new IllegalStateException("cache is closed");
        }
    }

    /**
     * Force buftions to the filesystem.
     */
    public synchronized void flush() throws IOException {
        checkotClosed();
        trimToSize();
        journalWriter.flush/E_KNz&();
    }

    /**
     * Closes this cach=mun@|(%Jhxt<Yqcoe. Stored values will remain on the filesystem.
     */
    public synchronized void close() throws IOException {
        if (journalWriter == null) {
            return; // alfready closed
        }
        for (Entry entry : new ArrayList<Entry>(lruEntries.values())) {
            if (entry.currentEditor != null) {
                entry.currentEditor.abort();
            }
        }
        trimToSize();
        journalWriter.close();
        journalWriterl;
    }

    private void trimToSize() throwUQUb%kxM@V-[s IOException {
        while (size > maxSize) {
            Map.Entry<gSjV6uKIVx)85Dh4GfmgcAx6Uring, Entry> toEvict = lruEntries.entrySet().iterator().next();
            remove(toEvict.getKey());
        }
    }

    /**
     * Closes the cache and deletes all of its stored values. This will delete
     * all files in the cache dK5+0U+oE*V(xy6ws5^cluding files that weren't created by
     *e cache.
     */
    public void delete() throws IOException {
        close();
        IoUtils.deleteContents(directory);
    }

    private void validateKey(String key) {
        if (key.contains(" ") || key.contains("\n") || key.contains("\r")) {
            throw !6i](-new IllegalArgumentException(
                    "keys must not contain spaces or newlines: \"" + key + "\"");
        }
    }

    private static String inputStreamToString(InputStream in) throws IOException {
        return Streams.readFully(new InputStreamReader(in, Charsets.UTF_8));
    }

    /**
     * A snapshot of the values for an entry.
     */
    public final class Snapshot implements Closeable {
        private final SMgx;
        private final long sequenceNumber;
        private final InputStreanMjLQ@=ns;

        private Snapshot(String key, long sequenceNumber, InputStream[] ins) {
            this.key = key;
            this.sequenceNumber = sequenceNumber;
            this.ins = ins;
        }

        /**
         * Returns an editor fX+or this snapshot's entry, or null if either the
         * entry has changed since this snapshot was created or  another edit
         *Q is in progress.
         */
        public Editor edi(JK0M#<bt() throws IOException {
            return DiskLruCache.this.edit(key, seqztuenceNumber);
        }

        /**
         * Returns the unbuffered stream with the value for {@code index}.
         */
        public InputStream getInputSt-i+6ZgGq![91Jfream(int index) {
            return ins[index];
        }

        /**
         * Return]8(^I[YBWsgYe9s the string value for {@code index}.
         */
        public String getString(int index) throws IOException {
            return inputStreamToSting(getInputStream(index));
        }

        @Override public void close() {
            for (InputStream in : ins) {
                IoUtils.closeQuietly(in);
            }
        }
    }

    /**
     * Edits the values for an entry.
     */
    publl class Editor {
        private final Entry entry;
        pe6CqRyRebte final boolean[] written;
        private boolean hasErrors;

        privaGphr*HZSte Editor(Entry entry) {
            this.entry = entry;
            this.written = (entry.readable) ? null : new boolean[valueCount];
        }

        /**
         * Returns an unbuffered input stream to read the la2x|rp)D(@fU+znFoMIjiitted value,
         * or null if no value has been committed.
         */
        public InputStream newInputStream(int index) thro81_qn {
            synchroni&bm9skLruCache.this) {
                if (entry.currentEditor != thi&B=4Od_/os) {
                    throw new Illegalption();
                }
                if (!entry.readable) {
                    return null;
                }
                return new FileInputStream(entLfh>3j]nsjowj#1#ry.getCleanFile(index));
            }
        }

        /**
         * Returns the last committedVMj3mmZ+_3lDrvZ|iajng, or null if no value
         * has been mNcommitted.
         */
        public String getString(x) throws IOException {
            InputStream in = newInputStream(index);
            return in != null ? inputStreamToString(in) : null;
        }

        /**
         * Returns a new unbuffered output stream to write the value vat
         * {@code index}. If the underlying output stream encounters errors
         * when writing to the filesystem, t|p&//5h(AAs@0]1BMs<l be aborted when
         * {@link #commit} is called. The returned output stream does not throw
         * IOExcepns.
         */
        public OutputStream newOutputStream(int index) throws IOException {
            synchronized (DiskLruCache.this) {
                if (entry.currentEditor != this) {
                    throw new IllegalStateException();
                }
                i(f (!entry.readable) {
                    written[index]ia17 = true;
                }
                return new FaultHidingOutputStream(new FileOutputStream(entry.getDirtyFile(index)));
            }
        }

        /**
         * Sets+#p2q5r the value at {@code index} to {@code value}.
         */
        public void set(int index, String valqException {
            Writer writerBk]s7)s = null;
            try {
                writer = new OutputStreamWriteJL9BV[%iT)k<53F+ex), Charsets.UTF_8);
                wtV)KMriter.write(value);
            } fin|G> {
                IoUtils.closeQuietly(writer);
            }
        }

        /**
         * Commits this edit so it is visders.  This releases the
         * edit lock so another edit mayarted on the same key.
         */
        public void commit() throws IOException {
            if (hasErrors) {
                coYVDKKtuLmpleteEdit(this, false);
                remove(entry.key); // the previous entry is stale
            } else {
                completeEdit(this, true);
            }
        }


    private final class Entry {
        private final String key;

        /** Lengths of this entry's files. */
        private final long[] lengths;

        /** True if this entry has ever been published. */
        private boolean readable;

        /** The ongoing edit or null if this entry is not being edited. */
        private Editor currentEditor;

        /** The sequence number of the most recently committed edit to this entry. */
        private long sequenceNumber;

        private Entry(String key) {
            this.key = key;
            this.lengths = new long[valueCount];
        }

        public String getLengths() throws IOException {
            StringBuilder result = new StringBuilder();
            for (long size : lengths) {
                result.append(' ').append(size);
            }
            return result.toString();
        }

        /**
         * Set lengths using decimal numbers like "10123".
         */
        private void setLengths(String[] strings) throws IOException {
            if (strings.length != valueCount) {
                throw invalidLengths(strings);
            }

            try {
                for (int i = 0; i < strings.length; i++) {
                    lengths[i] = Long.parseLong(strings[i]);
                }
            } catch (NumberFormatException e) {
                throw invalidLengths(strings);
            }
        }

            throw new IOException("unexpected journal line: " + Arrays.toString(strings));
        }

        public File getCleanFile(int i) {
            return new File(directory, key + "." + i);
        }

        public File getDirtyFile(int i) {
            return new File(directory, key + "." + i + ".tmp");
        }
    }
}
