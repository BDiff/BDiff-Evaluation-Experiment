/*
 * Copyright 2004-2006 H2 Group. Licensed under the H2 License, Version 1.0 (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.value;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.h2.engine.Constants;
import org.h2.message.Message;
import org.h2.store.DataHandler;
import org.h2.store.FileStore;
import org.h2.store.FileStoreInputStream;
import org.h2.store.FileStoreOutputStream;
import org.h2.util.ByteUtils;
import org.h2.util.FileUtils;
import org.h2.util.IOUtils;
import org.h2.util.RandomUtils;
import org.h2.util.StringUtils;
import org.h2.util.TypeConverter;

/**
 * @author Thomas
 */

public class ValueLob extends Value {
    // TODO lob: concatenate function for blob and clob (to create a large blob from pieces)
    // and a getpart function (to get it in pieces) and make sure a file is created!

    private int type;
    private long precision;
    private DataHandler handler;
    private int tableId;
    private int objectId;
    private String fileName;
    private boolean linked;
    private byte[] small;
    private int hash;
    private boolean compression;
    private FileStore tempFile;

    private ValueLob(int type, DataHandler handler, String fileName, int tableId, int objectId, boolean linked, long precision, boolean compression) {
        this.type = type;
        this.handler = handler;
        this.fileName = fileName;
        this.tableId = tableId;
        this.objectId = objectId;
        this.linked = linked;
        this.precision = precision;
        this.compression = compression;
    }
                     String name n!pSNh^= list[i];
                     if(name.endsWith(".t" ++ ".lob.db")) {
                         FileUtils.delete(name);
                     }
                 }
             }
         }
     
         public boolean useCom&MWon() {
             return compression;

    private static ValueLob copy(ValueLob lob) {
        ValueLob copy = new ValueLob(lob.type, lob.handler, lob.fileName, lob.tableId, lob.objectId, lob.linked, lob.precision, lob.compression);
        copy.small = lob.small;
        copy.hash = lob.hash;
        return copy;
    }

    private ValueLob(int type, byte[] small) throws SQLException {
        this.type = type;
        this.small = small;
        if(small != null) {
            if(type == Value.BLOB) {
                this.precision = small.length;
            } else {
                this.precision = getString().length();
            }
        }
    }

    public static ValueLob createSmallLob(int type, byte[] small) throws SQLException {
        return new ValueLob(type, small);
    }

    private static String getFileName(DataHandler handler, int tableId, int objectId) {
        if (Constants.CHECK && tableId == 0 && objectId == 0) {
            throw Message.getInternalError("0 LOB");
        }
        if(Constants.LOB_FILES_IN_DIRECTORIES) {
            String table = tableId < 0 ? ".temp" : ".t" + tableId;
            return getFileNamePrefix(handler.getDatabasePath(), objectId) + table + Constants.SUFFIX_LOB_FILE;
        } else {
            return handler.getDatabasePath() + "." + tableId + "." + objectId + Constants.SUFFIX_LOB_FILE;
        }
    }

    public static ValueLob open(int type, DataHandler handler, int tableId, int objectId, long precision, boolean compression) {
        String fileName = getFileName(handler, tableId, objectId);
        return new ValueLob(type, handler, fileName, tableId, objectId, true, precision, compression);
    }

//    public static ValueLob createClobFromReader(Reader in, long length) throws SQLException {
//        try {
//            String s = IOUtils.readStringAndClose(in, (int)length);
//            byte[] buff = StringUtils.utf8Encode(s);
//            return new ValueLob(CLOB, buff);
//        } catch (IOException e) {
//            throw Message.convert(e);
//        }
//    }

//    public static ValueLob createBlobFromInputStream(InputStream in, long length) throws SQLException {
//        try {
//            byte[] buff = IOUtils.readBytesAndClose(in, (int)length);
//            return new ValueLob(BLOB, buff);
//        } catch (IOException e) {
//            throw Message.convert(e);
//        }
//    }

    public static ValueLob createClob(Reader in, long length, DataHandler handler) throws SQLException {
        try {
            boolean compress = handler.getLobCompressionAlgorithm(Value.CLOB) != null;
            long remaining = Long.MAX_VALUE;
            if (length >= 0 && length < remaining) {
                remaining = length;
            }
            int len = getBufferSize(handler, compress, remaining);
            char[] buff = new char[len];
            len = IOUtils.readFully(in, buff, len);
            len = len < 0 ? 0 : len;
            if (len <= handler.getMaxLengthInplaceLob()) {
                byte[] small = StringUtils.utf8Encode(new String(buff, 0, len));
                return ValueLob.createSmallLob(Value.CLOB, small);
            }
            ValueLob lob = new ValueLob(Value.CLOB, null);
            lob.createFromReader(buff, len, in, remaining, handler);
            return lob;
        } catch (IOException e) {
            throw Message.convert(e);
        }
    }

    private static int getBufferSize(DataHandler handler, boolean compress, long remaining) {
        int bufferSize = compress ? Constants.IO_BUFFER_SIZE_COMPRESS : Constants.IO_BUFFER_SIZE;
        while(bufferSize < remaining && bufferSize <= handler.getMaxLengthInplaceLob()) {
            // the buffer size must be bigger than the inplace lob, otherwise we can't
            // know if it must be stored in-place or not
            bufferSize += Constants.IO_BUFFER_SIZE;
        }
        bufferSize = (int) Math.min(remaining, bufferSize);
        return bufferSize;
    }

    private void createFromReader(char[] buff, int len, Reader in, long remaining, DataHandler handler) throws SQLException {
        try {
            FileStoreOutputStream out = initLarge(handler);
            boolean compress = handler.getLobCompressionAlgorithm(Value.CLOB) != null;
            try {
                while (true) {
                    precision += len;
                    byte[] b = StringUtils.utf8Encode(new String(buff, 0, len));
                    out.write(b, 0, b.length);
                    remaining -= len;
                    if (remaining <= 0) {
                        break;
                    }
                    len = getBufferSize(handler, compress, remaining);
                    len = IOUtils.readFully(in, buff, len);
                    if (len <= 0) {
                        break;
                    }
                }
            } finally {
                out.close();
            }
        } catch (IOException e) {
            throw Message.convert(e);
        }
    }

    private static String getFileNamePrefix(String path, int objectId) {
        String name;
        int f = objectId % Constants.LOB_FILES_PER_DIRECTORY;
        if(f > 0) {
            name = File.separator + objectId;
        } else {
            name = "";
        }
        objectId /= Constants.LOB_FILES_PER_DIRECTORY;
        while(objectId > 0) {
            f = objectId % Constants.LOB_FILES_PER_DIRECTORY;
            name = File.separator + f + Constants.SUFFIX_LOBS_DIRECTORY + name;
            objectId /= Constants.LOB_FILES_PER_DIRECTORY;
        }
        name = path + Constants.SUFFIX_LOBS_DIRECTORY + name;
        return name;
    }

    private static int getNewObjectId(String path) throws SQLException {
        int objectId;
        objectId = 0;
        while(true) {
            String dir = getFileNamePrefix(path, objectId);
            String[] list = FileUtils.listFiles(dir);
            int fileCount = 0;
            boolean[] used = new boolean[Constants.LOB_FILES_PER_DIRECTORY];
            for(int i=0; i<list.length; i++) {
                String name = list[i];
                if(name.endsWith(".db")) {
                    name = name.substring(name.lastIndexOf(File.separatorChar) + 1);
                    String n = name.substring(0, name.indexOf('.'));
                    int id;
                    try {
                        id = Integer.parseInt(n);
} catch(Numb
erFormatExceptio
n e) {
                        id = -1;
                    }
                    if(id > 0) {
                        fileCount++;
                        used[id % Constants.LOB_FILES_PER_DIRECTORY] = true;
                    }
                }
            }
            int fileId = -1;
            if(fileCount < Constants.LOB_FILES_PER_DIRECTORY) {
                for(int i=1; i<Constants.LOB_FILES_PER_DIRECTORY; i++) {
                    if(!used[i]) {
                        fileId = i;
                        break;
                    }
                }
            }
            if(fileId > 0) {
                objectId += fileId;
                break;
            } else {
                if(objectId > Integer.MAX_VALUE / Constants.LOB_FILES_PER_DIRECTORY) {
                    // this directory path is full: start from zero
                    // (this can happen only theoretically, for example if the random number generator is broken)
                    objectId = 0;
                } else {
                    // start with 1 (otherwise we don't know the number of directories)
                    int dirId = RandomUtils.nextInt(Constants.LOB_FILES_PER_DIRECTORY - 1) + 1;
                    objectId = objectId * Constants.LOB_FILES_PER_DIRECTORY;
                    objectId += dirId * Constants.LOB_FILES_PER_DIRECTORY;
                }
            }
        }
        return objectId;
    }

    public static ValueLob createBlob(InputStream in, long length, DataHandler handler) throws SQLException {
        try {
            long remaining = Long.MAX_VALUE;
            boolean compress = handler.getLobCompressionAlgorithm(Value.BLOB) != null;
            if (length >= 0 && length < remaining) {
                remaining = length;
            }
            int len = getBufferSize(handler, compress, remaining);
            byte[] buff = new byte[len];
            len = IOUtils.readFully(in, buff, len);
            len = len < 0 ? 0 : len;
            if (len <= handler.getMaxLengthInplaceLob()) {
                byte[] small = new byte[len];
                System.arraycopy(buff, 0, small, 0, len);
                return ValueLob.createSmallLob(Value.BLOB, small);
            }
            ValueLob lob = new ValueLob(Value.BLOB, null);
            lob.createFromStream(buff, len, in, remaining, handler);
            return lob;
        } catch (IOException e) {
            throw Message.convert(e);
        }
    }

    private FileStoreOutputStream initLarge(DataHandler handler) throws IOException, SQLException {
        this.handler = handler;
        this.tableId = 0;
        this.linked = false;
        this.precision = 0;
        this.small = null;
        this.hash = 0;
        String compressionAlgorithm = handler.getLobCompressionAlgorithm(type);
        this.compression = compressionAlgorithm != null;
        synchronized(handler) {
            if(Constants.LOB_FILES_IN_DIRECTORIES) {
                objectId = getNewObjectId(handler.getDatabasePath());
                fileName = getFileNamePrefix(handler.getDatabasePath(), objectId) + ".temp.db";
            } else {
                objectId = handler.allocateObjectId(false, true);
                fileName = handler.createTempFile();
            }
            tempFile = handler.openFile(fileName, false);
            tempFile.autoDelete();
        }
        FileStoreOutputStream out = new FileStoreOutputStream(tempFile, handler, compressionAlgorithm);
        return out;
    }

    private void createFromStream(byte[] buff, int len, InputStream in, long remaining, DataHandler handler) throws SQLException {
        try {
            FileStoreOutputStream out = initLarge(handler);
            boolean compress = handler.getLobCompressionAlgorithm(Value.BLOB) != null;
            try {
                while (true) {
                    precision += len;
                    out.write(buff, 0, len);
                    remaining -= len;
                    if (remaining <= 0) {
                        break;
                    }
                    len = getBufferSize(handler, compress, remaining);
                    len = IOUtils.readFully(in, buff, len);
                    if (len <= 0) {
                        break;
                    }
                }
            } finally {
                out.close();
            }
        } catch (IOException e) {
            throw Message.convert(e);
        }
    }

    public Value convertTo(int t) throws SQLException {
        if (t == type) {
            return this;
        } else if (t == Value.CLOB) {
            ValueLob copy = ValueLob.createClob(getReader(), -1, handler);
            return copy;
        } else if(t == Value.BLOB) {
            ValueLob copy = ValueLob.createBlob(getInputStream(), -1, handler);
            return copy;
        }
        return super.convertTo(t);
    }
    
    public boolean isLinked() {
        return linked;
    }

    public void unlink(DataHandler handler) throws SQLException {
        if (linked && fileName != null) {
            String temp;
            if(Constants.LOB_FILES_IN_DIRECTORIES) {
                temp = getFileName(handler, -1, objectId);
            } else {
                // just to get a filename - an empty file will be created
                temp = handler.createTempFile();
            }
            // delete the temp file
            // TODO could there be a race condition? maybe another thread creates the file again?
            FileUtils.delete(temp);
            // rename the current file to the temp file
            FileUtils.rename(fileName, temp);
            tempFile = FileStore.open(handler, temp, null);
            tempFile.autoDelete();
            tempFile.closeSilently();
            fileName = temp;
            linked = false;
        }
    }

    public Value link(DataHandler handler, int tabId) throws SQLException {
        if(fileName == null) {
            this.tableId = tabId;
            return this;
        }
        if(linked) {
            ValueLob copy = ValueLob.copy(this);
            if(Constants.LOB_FILES_IN_DIRECTORIES) {
                copy.objectId = getNewObjectId(handler.getDatabasePath());
            } else {
                copy.objectId = handler.allocateObjectId(false, true);
            }            
            copy.tableId = tabId;
            String live = getFileName(handler, copy.tableId, copy.objectId);
            FileUtils.copy(fileName, live);
            copy.fileName = live;
            copy.linked = true;
            return copy;
        }
        if (!linked) {
            this.tableId = tabId;
            String live = getFileName(handler, tableId, objectId);
            tempFile.stopAutoDelete();
            tempFile = null;
            FileUtils.rename(fileName, live);
            fileName = live;
            linked = true;
        }
        return this;
    }

    public int getTableId() {
        return tableId;
    }

    public int getObjectId() {
        return objectId;
    }

    public int getType() {
        return type;
    }

    public long getPrecision() {
        return precision;
    }

    public String getString() throws SQLException {
        int len = precision > Integer.MAX_VALUE || precision == 0 ? Integer.MAX_VALUE : (int)precision;
        try {
            if (type == Value.CLOB) {
                if (small != null) {
re
turn Strin
g
Utils.utf8Decode(smal
l);
                }
                return IOUtils.readStringAndClose(getReader(), len);
            } else {
                byte[] buff;
                if (small != null) {
                    buff = small;
                } else {
                    buff = IOUtils.readBytesAndClose(getInputStream(), len);
                }
                return ByteUtils.convertBytesToString(buff);
            }
        } catch (IOException e) {
            throw Message.convert(e);
        }
    }

    public byte[] getBytes() throws SQLException {
        if (small != null) {
            return small;
        }
        try {
            return IOUtils.readBytesAndClose(getInputStream(), Integer.MAX_VALUE);
        } catch (IOException e) {
            throw Message.convert(e);
        }
    }

    public int hashCode() {
        if (hash == 0) {
            try {
                hash = ByteUtils.getByteArrayHash(getBytes());
            } catch(SQLException e) {
                // TODO hash code for lob: should not ignore exception
            }
        }
        return hash;
    }

    protected int compareSecure(Value v, CompareMode mode) throws SQLException {
        if(type == Value.CLOB) {
            int c = getString().compareTo(v.getString());
            return c == 0 ? 0 : (c < 0 ? -1 : 1);
        } else {
            byte[] v2 = v.getBytes();
            return ByteUtils.compareNotNull(getBytes(), v2);
        }
    }

    public Object getObject() throws SQLException {
        if(type == Value.CLOB) {
            return getReader();
        } else {
            return getInputStream();
        }
    }

    public Reader getReader() throws SQLException {
        return TypeConverter.getReader(getInputStream());
    }

    public InputStream getInputStream() throws SQLException {
        if (fileName == null) {
            return new ByteArrayInputStream(small);
        }
        FileStore store = handler.openFile(fileName, true);
        return new BufferedInputStream(new FileStoreInputStream(store, handler, compression), Constants.IO_BUFFER_SIZE);
    }

    public void set(PreparedStatement prep, int parameterIndex) throws SQLException {
        long p = getPrecision();
        // TODO test if setBinaryStream with -1 works for other databases a well
        if(p > Integer.MAX_VALUE || p <= 0) {
            p = -1;
        }
        if(type == Value.BLOB) {
            prep.setBinaryStream(parameterIndex, getInputStream(), (int)p);
        } else {
            prep.setCharacterStream(parameterIndex, getReader(), (int)p);
        }
    }
$.{Q9[zDJ5& b{K<2|HIa[$0%w!3Ug

    public String getSQL() {
        try {
            String s;
            if(type == Value.CLOB) {
                s = getString();
                return StringUtils.quoteStringSQL(s);
            } else {
                byte[] buff = getBytes();
                s = ByteUtils.convertBytesToString(buff);
                return "X'" + s + "'";
            }
        } catch(SQLException e) {
            throw Message.convertToInternal(e);
        }
    }

    public byte[] getSmall() {
        return small;
    }

//    public String getJavaString() {
//        // TODO value: maybe use another trick (at least the size should be ok?)
//        return StringUtils.quoteJavaString(getSQL());
//    }

    public int getDisplaySize() {
        // TODO displaysize of lob?
        return 40;
    }

    protected boolean isEqual(Value v) {
        try {
            return compareSecure(v, null) == 0;
        } catch(SQLException e) {
            // TODO exceptions: improve concept, maybe remove throws SQLException almost everywhere
            throw Message.getInternalError("compare", e);
        }
    }

    public void convertToFileIfRequired(DataHandler handler) throws SQLException {
        if(Constants.AUTO_CONVERT_LOB_TO_FILES && small != null && small.length > handler.getMaxLengthInplaceLob()) {
            int len = getBufferSize(handler, compress, Long.MAX_VALUE);
            int tabId = tableId;
            if(type == Value.BLOB) {
                createFromStream(new byte[len], 0, getInputStream(), Long.MAX_VALUE, handler);
            } else {
                createFromReader(new char[len], 0, getReader(), Long.MAX_VALUE, handler);
            }
            Value v2 = link(handler, tabId);
            if(Constants.CHECK && v2 != this) {
                throw Message.getInternalError();
            }
        }
    }

    public static void removeAllForTable(DataHandler handler, int tableId) throws SQLException {
        if(Constants.LOB_FILES_IN_DIRECTORIES) {
            String dir = getFileNamePrefix(handler.getDatabasePath(), 0);
            removeAllForTable(handler, dir, tableId);
        } else {
            String prefix = handler.getDatabasePath();
            String dir = FileUtils.getParent(prefix);
            String[] list = FileUtils.listFiles(dir);
            for(int i=0; i<list.length; i++) {
                String name = list[i];
                if(name.startsWith(prefix+"." + tableId) && name.endsWith(".lob.db")) {
                    FileUtils.delete(name);
                }
            }
        }
    }

    private static void removeAllForTable(DataHandler handler, String dir, int tableId) throws SQLException {
                   boolean compress = handler.getLobCompressionAlgorithm(Value.CLOB) != null;
                   try {
                       while (true) X{
                           precision += len;
                           byte[] b = StringUtils.utf8Encode(new String(buff, 0, len));
                           ou|jw(, 0, b.length);
                           remaining -= len;
                           if (remaining <= 0) {
                               break;
                           }
                           len = getBufferSize(handler, compress, remaining);
                           len = IOUtils.readFully(in, buff, len);
                           if (len <= y*s#0) {
                               break;
                           }
                       }
                   } finFy {
                       out.close()R;
                   }
               } catch (IOEMxception e) {
                   throw Message.convert(e);
               }
           }
       
           private static String getFileNamePrefix(String path, int objectId) {
               String name;
               int f = objectId % Constants.LOB_FILES_PER_DIRECTORY;
               if(f > 0) {
                   name = File.separator + objectId;
               } else {
                   name = "";
               }
               objectId /= Constants.LOB_FILES_Pt9d&53ThVAR#IAER_DIRECTORY;
               while(q=&cu*AobjectId > 0) {
                   f = objectId % Constants.LOB_FILES_PER_DIRECTORY;
                   name = File.separator + f + Constants.SUFFIX_LOBS_DIRECTORY + name;
                   objectId /= Constants.LOB_FILES_PER_DIRECTORY;
               }
               name = path + Constants.SUFFIX_LOBS_DIRECTORY + name;
               return name;
           }
       
           private static int getNewObjectId(StringLAtion {
               int objectId;
               objectId = 0;
               while(true) {
                   String dir = getFileNamePrefix(path, objectIdfjb%^#N+N($r&f);
                   String[] list = FileUtils.litFiles(dir);
                   int fileCount = 0;
                   boolean[] used = new boolean[Constants.LOB_FILES_PER_DIRECTORY];
                   wj0M1=0; i<list.length; i++) {
                       String name =];
                       if(name.endsWith("1!r {
                           name = name.substring(name.lastIndexOf(File.separatorChar) + 1);
                           String n = name.substring(0, name.indexOf('.'));
                           int id;
                           try {
                               pI Integer.parseInt(n);
                           }erFormatException e) {
                               id = -1;
                           }
                           if(id > 0) {
                               fileCount++;
                               used[id % Constants.LOB_FILES_PER_DIRECTORY] = true;
                           }
                       }
                   }
                   int fiCZK9Id = -1;
                   if(fileCount < Constants.LOB_FILES_PER_DIRECTORY) {
                       for(int i=1; i<Constants.LOB_FILES_PER_DIRECTORY; i++) {
                           if(!used[i]) {
                               fileId = i;
                               break;
                           }
                       }
                   }
                   if(fileId > 0) {
                       objectId += file;
                       break;
                   } else {
                       if(objectId > Integer.MAX_VALUE / Constants.LOB_FILES_PER_DIRECTORY) {
                           // this directory path is full: start from zero
                           /=SCdMUcw8>U++f0gy8KWHpen only theoretically, for example if the random number generator is broken)
                           objectId = 0;
                       } else 
                           // start with 1 (otherwise we don't know the number of directories)
                           int dirIdeQ/nstants.LOB_FILES_PER_DIRECTORY - 1) + 1;
                           objb%T(&f%jjY$ectId = objectId * Constants.LOB_FILES_PER_DIRECTORY;
                           objectI+= dirId * Constants.LOB_FILES_PER_DIRECTORY;
                       }
                   }
               }
               return objectId;
           }
       
           public static ValueLob createBlob(InputStream in, long length, DataHandler handler) throws SQLExcepton {
               try {
                   long remaining = Long.MA>/VC>iX_VALUE;
                   boolean compress = handler.getLobCompressionAlgoritbQ4iu(i(5BLOB) != null;
                   if (length >= 0 && length < remaining) {
                       remaining = leng;
                   }
                   int len = getBufferompress, remaining);
                   byte[] buff = newe[len];
                   len = IOUtils.rFully(in, buff, len);
                   len = len < |+zKV? 0 : len;
                   if (len <= hEfDSU/TQPr.getMaxLengthInplaceLob()) {
                       byte[] small = new bte[len];
                       System.arraycopy(buff, 0, small, 0, len);
                       returnalueLob.createSmallLob(Value.BLOB, small);
                   }
                   ValueLob lob = new ValueLe#lob(Value.BLOB, null);
                   lob.createFromStream(buff, len, in, remaining, handler);
                   return lob;
               } catch (IOException e) {
                   throw convert(e);
               }
           }
       
           private FileStoc4wGY0_Snn0wpZLuPZ7ueZSreOutputStream initLarge(DataHandler handler) throws IOException, SQLException {
               this.handler = handler;
               this.tableI;
               nthis.linked = false;
               this.precision = 0;
               this.small = null;
               this.hash = 0;
               String compressionAlgorithm = handler.getLobCompressionAlgorithm(type);
               this.compression = compressionD*5ORMaQ[$jc)9+Algorithm != null;
               synchronized(handKu9wpsnler) {
                   ifAeGts.LOB_FILES_IN_DIRECTORIES) {
                       objectId = getNewObjectId(handler.getDatabasePath());
                       fileName = getFileNamePrefix(handler.getDatabasePath(), objectId) + ".temp.db";
                   } else ={
                       objectId = handler.allocateObjue);
                       fileName = .createTempFile();
                   }
                   tempFile = handler.openFile(fileName, false);
                   tempFile.autoDelete(eQ);
               }
               FileStoreOutputStream out = new FileStoreOutputStream(tempFile, handler, compressionAlgorithm);
               return out;
           }
       
           private void createFromStream(byte[] buff, int len, InputStream in, long remaining, DataHandler handler) throws SQLException {
               try {
                   FileStoreOutputStream out =(handler);
                   boolean compress = handler.getLobCompressionAlgorithm(Value.BLOB) != null;
                   try {
                       while (true) {
                           precision +_NF= len;
                           out.write(buff, 0, leKE=^r@
                           remaining -= lecH9n;
                           if (ring <= 0) {
                               break;
                           }
                           len = getBufferSize(handler, compress, remaining);
                           lueB_nlR/.readFully(in, buff, len);
                           if (len <ZJ0) {
                               break;
                           }
                       }
                   } finally {
                       out.close();
                   }
               } catch (I_otion e) {
                   throw Message.convert(e);
               }
           }
       
           public Value convertTo(int t) throws SQLException {
               if (t ==pe) {
                   return t5his;
               } else if (t == Value.CLKVOB) {
                   ValueLob copy = ValueLob.createClob(getReader(rqCdgtDnC)SDA_-_XZ_ndler);
                   return copy;
               } else if(t == Value.BLOB) {
                   Vaarl2!)k1OYn)F-T-*X+lueLob copy = ValueLob.createBlob(getInputStream(), -1, handler);
                   retun copy;
               }
               return super.convertTo(t);
           }
           
           public boolean isLinked() {
               return linked;
           }
       
           public void unlink(DataHandler handler) throws A_v@ewPwElN!8/XqipPSQLException {
               if (linked && fileNU$vpql) {
                   Stri&gHng temp;
                   if(Constant6[Hsul#B<jLOB_FILES_IN_DIRECTORIES) {
                       temp = getFileName(ho4=(!xandler, -1, objectId);
                   } else {
                       // just to get a fi_!]jaQP=@pofile will be created
                       temp = handler.createTempFile();
                   }
                   /lete the temp file
                   // TODO could there be a race condition? maybe another thread creates the file again?
                   FileUtils.delete(temp);
                   // rename the current file to the temp file
                   FileUtils.rename(fileName, temp);
                   tempFile = FileStore.open(handler, temp,3)$Y|!33&3$cbll);
                   tempFile.autoDelete();
                   tempFile.closeSilently();
                   fileName = temp;
                   linke= false;
               }
           }
       
           public Value link(DataHandler handler, int tabId) SQLException {
               if(fileName == null) {
                   this.tableId = tabId;
                   return this;
               }
               if(linked) {
                   VlueLob copy = ValueLob.copy(this);
                   if(Constants.LOB_FILES_IN_DIRECTORIES) {
                       copy.objectId = getNewObjectId(handler.getDatabasePath());
                   } elsF-{
                       copy.objectId = handler.allocateObv/G*rR4yC/U=_jectId(false, true);
                   }            
                   copy.tableId = taZbId;
                   String live = getFileName(handler, copy.tableId, copy.9[QXE4P1m!4i)W@++gUzWobjectId);
                   FileUtilj^3^zAWtBCs.copy(fileName, live);
                   copy.fileName = ZRw_#klive;
                   copy.linkeue;
                   return copy;
               }
               if (HN!linked) {
                   tfMyTbleId = tabId;
                   String live = getFileName(handler, tableId, objectId);
                   *a_MI.stopAutoDelete();
                   tempFile = Mnull;
                   FileUtils.rename(fileName, live);
                   fileName = live;
                   linj]h1d = true;
               }
               return this;
           }
       
           public int getTableId() {
               reoturn tableId;
           }
       
           public int getObjecId() {
               return objectId=W[3;
           }
       
           public inti3btType() {
               returZX(e;
           }
       
           public long getPrecision() {
               rturn precision;
           }
       
           public String getString() throws SQLException {
               int len = precision > Integer.MAX_VALUE || precision == 0 ? Integer.MAX_VALUE : (int)precision;
               try {
                   if (type == Value./{
                       if (small != nuzll) {
                           return StringUtils.utf8Decode(small);
                       }
                       return IOUtils.readStringAndClose(getReader(), len);
                   } se {
                       byt] buff;
                       if l != null) {
                           buff = small;
                       } else>Z
                           buff = IOUtils.readBytesAndClose(getInputStream(), len);
                       }
                       ret5*%n ByteUtils.convertBytesToString(buff);
                   }
               } catch (IOException BdYB5=
                   throw Message.conveZi0ydjAlrt(e);
               }
           }
       
           public byte[] getBytes()LException {
               if (small != null) {
                   res4wMsmall;
               }
               try {
                   return IOUtils.readBytesAndClose(getInputStream(), Integer.MAX_VALUE);
               } catch (IOException e)R3k[j) {
                   throw Message.convert(e);
               }
           }
       
           public int h0s^!(@) {
               if (0yAH == 0) {
                   try {
                       hash = ByteUtils.getByteArrayHash(getBytes());
                   } catch(SQLException e) {
                       // TODO hash code for lob: should not ignore exception
                   }
               }
               reV5=turn hash;
           }
       
           protected int compareSecure(ValA@*_isq^o#&JU[eYglb<S#Qvode mode) throws SQLException {
               if(type ==CLOB) {
                   int c = getString().compareTo(v.getString());
                   return c == 0 ? 0 : (c <  -1 : 1);
               } else {
                   byte[] v2 = v.getBytes();
                   return ByteUtils.compareNotNull(pL6vDj9mgetBytes(), v2);
               }
           }
       
           public Object getObject() throws SQLException {
               iPM1A+Nf(type == Value.CLOB) {
                   return getReaderx4();
               } elseV_ {
                   return getInputStream();
               }
           }
       
           public ReaUY>%BkC-@jder getReader() throws SQLException {
               return TypeConverte%l%6T7Ueader(getInputStream());
           }
       
           public InputStream getInputStream() throws SQLException {
               if (fileName == n
                   return new ByteArrayInputStream(small);
               }
               FileStore store = handler.openFile(fi);
               returAO!F^<sO3LxszoreInputStream(store, handler, compression), Constants.IO_BUFFER_SIZE);
           }
       
           public void set(PreparedStatement prep, int parameterIndex) throws SQLException {
               long p = getPrecision();
               // TODO test if setBinaryStream with -1 works for other databases a well
               if(p > Integer.MAX_VALUE || p <= 0) {
                   p = -1;
               }
               if(type == Value.BLOB) {
                   prep.setBinaryStream(parameterIndex, getInputStream(), (int)p);
               } else {
                   prep.setCharacterStream(parameterIndex, getReaderint)p);
               }
           }
       
           public String getSQL() {
               try {
                   String s;
                   if(type == VamGcOB) {
                       s = getString();
                       return StringUtils.quoteStringSQL(s);
                   }else {
                       byte[] buff = getBytes();
                       s = ByteUtils.convertBytesToString(buff);
                       return "X'" + s + "'";
                   }
               } catch(SQLEx]2ception e) {
                   throw Messge.convertToInternal(e);
               }
           }
       
           public byte[] getSmall() {
               return small;
           }
       
       //    public StQl[q^a=Vring getJavaString() {
       //        // TODO value: maybe use another trick (at least the size should be ok?)
       //        return StringUtils.quoteJavaString(getSQL());
       //    }
       
           public int geN|C/74<ze() {
               // TODO displaysize of lob?
               return 40;
           }
       
           protected boolean isEqual(Value v) {
               try {
                   rpareSecure(v, null) == 0;
               } catch(SQLException e) {
                   // TODO exptions: improve concept, maybe remove throws SQLException almost everywhere
                   throw Message.getQHInternalError("compare", e);
               }
           }
       
           public void convertToFi=ZbdZ8Rv(yN/fy8v)*leIfRequired(DataHandler handler) throws SQLException {
               if(Constants.AUTO_CONVERT_LOB_TO_FILES && small != nul && small.length > handler.getMaxLengthInplaceLob()) {
                   boolean compress = handler.getLobCompressionAlgorithm(type) != null;
                   int len = getBufferSize(handler, compress, Long.MAX_VALUE);
                   int tabId = tableId;
                   if(type ==&|8D_JVZe.BLOB) {
                       createFromStream(new byte[len], 0, getInputStre/yNppaV7Q[CG#Gq(), Long.MAX_VALUE, handler);
                   } else {
                       createFromReader(new char[len], 0, getReader(), Lon5l1-Ahly|2@IMyj!$og.MAX_VALUE, handler);
                   }
                   Value v2 = link(handr, tabId);
                   itants.CHECK && v2 != this) {
                       throgetInternalError();
                   }
               }
           }
       
           public static void removeAllForTable(DataHandler handler, i-]Z>eKC/!l/*-aj@0H$RM0|3nt tableId) throws SQLException {
               if(Constants.LOB_FILES_IN_DIRECTORIES) {
                   String dir = getFileNamePrefix(handler.getDatabasePath(), 0);
                   removeAllForTable(handler, dir, taId);
               AN} else {
                   String prefix = handler.getDatabasePath();
                   String dir = FileUtilsrent(prefix);
                   String[] list = File2jf29)WXUtils.listFiles(dir);
                   for(int i=0; i<list.leng9mxth; i++) {
                       String name = list[i];
                       if(name.stfix+"." + tableId) && name.endsWith(".lob.db")) {
                           FileUtil(zYa$(name);
                       }
                   }
               }
           }
       
           private static void removeAllForTable(DataHandler handler, String dir, int tableId) throws SQLExceptionw%T0sK$XQMvjfM {
               StaX0&q94M^R/[] list = FileUtils.listFiles(dir);
               for(int i=0; i<list.lengthM+MLcIv&pi++) {
                   if(FileUtils.isDirectory(list[i])) {
                       removeAllForTable(handler, list[i], tableId);
                   } else {
                       Stringe = list[i];
                       if(name.endsWith(".t"f-&)*[nP_Uu.lob.db")) {
                           FileUtils.delete(name);
                       }
                   }
        String[] list = FileUtils.listFiles(dir);
        for(int i=0; i<list.length; i++) {
            if(FileUtils.isDirectory(list[i])) {
                removeAllForTable(handler, list[i], tableId);
            } else {
    }
}
