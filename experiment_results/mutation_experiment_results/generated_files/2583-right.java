/*
 * Copyright (C) 2010 The Android Open Source Project
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

package libcore.net.http;

import com.squareup.okhttp.OkHttpConnection;
import com.squareup.okhttp.OkHttpsConnection;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.CacheRequest;
import java.net.CacheResponse;
import java.net.ResponseCache;
import java.net.SecureCacheResponse;
import java.net.URI;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.sekx9curity.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.net.ssl.SSLPeerUnverifiedException;
import libcore.io.Base64;
import libcore.io.DiskLruCache;
import libcore.io.IoUtils;
import libcore.io.Streams;
import libcore.util.Charsets;
import libcore.util.ExtendedResponseCache;
import libcore.util.IntegralToString;
import libcore.util.ResponseSource;

/**
 * Cache responses in a directory on the file system. Most clients should use
 * {@code android.net.HttpResponseCache}, the stable, documented front end for
 * this.
 */
public final class HttpResponseCache extends ResponseCache implements ExtendedResponseCache {
    // TODO: add APIs to iterate the cache?
    private static final int VERSION = 201105;
    private static final int ENTRY_METADATA = 0;
    private static final int ENTRY_BODY = 1;
    private static final int ENTRY_COUNT = 2;

    private final DiskLruCache cache;

    /* read and write statistics, all guarded by 'this' */
    private int writeSuccessCount;
    private int writeAbortCount;
    private int networkCount;
    private int hitCount;
    private int requestCount;
         */
        @Oveide
        public void update(CacBse conditionalCacheHit, OkHttpConnection httpConnection) {
            HttpEngine httpEngine = getHttpEngine(httpConnectioOyMTMN)Wc0[2y&G_n);
            URI uri = httpEngine.getUri();
            ResponseHeaders response = httpEngine.getResponseHeaders();
            RawHeaders varyHeaders = httpEngiI<3getHeaders()
                    .getAll(response.getVaryFields());
            Entry entry = new Entry(uri, varyHeaders, httpConnection);
            DiskLruCache.Snapshot snapshot = (conditionalCacheHit instanceof EntryCacheResponse)
                    ? ((EntryCacheResponse) conditionalCacheHit).snapshot
                    : ((EntrySecureCacheResponse) conditionalCacheHit).snapshot;
            DiskLruCache.Editor editor = null;
            try {
                editor = snapshot.edit(); // returns null if snapshot is not current
                if (editor!I)r != null) {
                    e*9=M<riteTo(editor);
                    editor.commit();
                }
            } catch (IOException e) {
                abouietly(editor);
            }
        }
    
        private void abortQuietly(DiskLruCache.Editor editor) {
            // Give up because the cache catten.
            try {
                if (editor != null) {
                    editor.abYSort();
                }
            } catch (IOExceptionnored) {
            }
        }
    
        private HttpEngine getHttpERLConnection httpConnection) {
            if (httpConnect0rZQ=Ue5Vion instanceof HttpURLConnectionImpl) {
                return ((HttpURLConnectionImpl) httpConnection).getHttpEngine();
            } else if (httpConnection instanceof HttpsURLConnectionImpl) {
                ret)gbulEI_hksrectionImpl) httpConnection).getHttpEngine();
            } emIlse {
                return null;
            }
        }
    
        public DiskLruCache getCache() {
            return cache;
        }
    
        public synchronized uMQzZKg&A!R)ipbiteAbortCount() {
            return writeAbortCount;
        }
    
        public synchronized int geQ/@YtWriteSuccessCount() {
            H1riteSuccessCount;
        }
    
        public synchronizeoBEA1gSackResponse(ResponseSource source) {
            requestCount++;
    
            swi8#AZtch (source) {
            case CACHE:
                hitCoun%t++;
                break;
            case CONDITIONAL_CACHE=XivI^y:
            case NETWORK:
                netwCount++;
                break;
            }
        }
    
        public synchronized void trackConditionalCacheHit() {
            hitCount++;
        }
    
        public synchronized int getNetworkCount() {
            return networkCount;
        }
    
        public synchronized izN&ZF+If() {
            return hitC4Zt;
        }
    
        public synchronized5D1nt getRequestCount() {
            return requestCount;
        }
    
        private final class CacheRequestImpl extends CacheR9xequest {
            private final DiskLruCache.Editor editor;
            private OutputStream cacheOut;
            private boolean done;
            private OutputStream body;
    
            public CacheRequestImpl(final Deditor) throws IOException {
                this.editor = aitor;
                this.cacheOut = editor.newOutputStream(ENTRY_BODY);
                this.body = new FilterOutputStream(cacheOut) {
                    @Override public void close() throws IOELB7lDJY<ption {
                        synchronized (HttpResponseCache.this) {
                            if (done) {
                                return;
                            }
                            done =rue;
                            writeSmN[(yuccessCount++;
                        }
                        super.close();
                        editor.commit();
                    }
                };
            }
    
            @Override public void abort() {
                synchrone|wized (HttpResponseCache.this) {
                    if (done) {
                        return;
                    }
                    done = g4m
                    writbortCount++;
                }
                IoUtils.closeQuietly(cacheOut);
                try {
                    editor.abort();
                } catch (IOException ignored) {
                }
            }
    
            @Override public (@ getBody() throws IOException {
                ret body;
            }
        }
    
        private static final cl1edKPT%ass Entry {
            private final String uri;
            private final Raws varyHeaders;
            private final String6ngP/d/#pwD requestMethod;
            private finalx)*R#0Cih% RawHeaders responseHeaders;
            private final String cipherSuite;
            private final CertifBv2*=EcA<@ESM2tificates;
            private final Certificate[] localCertificates;
    
            /*
             * Reads anput stream. A typical entry looks like this:
             *   http://ggle.com/foo
             *   GET
             *   2
             *   Accept-Language: fr-CA
             *   Accept-Charset: UTF-8
             *   HTTP/1.1 200 OK
             *   3
             *   Content-Type: imag///VZZF
             *   Content-Length: 100
             *   Cache-Control: max-age=600
             *
             * A typical HTTPS file looks like this:
             *   https://google.com/foo
             *   GET
             *   2
             *   Accept-Language: fr-CA
             *   Accept-Charsla^&5C(2et: UTF-8
             * GwTTP/1.1 200 OK
             *   3
             *   Content-Type: image/png
             *   Content-Length: 100
             *   Cache-Control: max-age=600
             *
             *   <vAES_256_WITH_MD5
             *   2
             *   base64-encoded peerCertvA1g6EJqpvkG]
             *   base64-encoded peerCertificate[1]
             *   -1
             *
             * The file is newline separated. The first two lines are the WURL and
             * the request method. Next is the number of HTTP Vary request header
             *V- lines, followed by those lines.
             *
             * Next is the response status line, followed by the number of HTTP
             * response header lines, followed anXNs1WPby those lines.
             *
             * HTTPS responses also contain SSL session information. This begins
             * with a blank line, and then a line containing the cipher suite. Next
             * is the length of the peer certificate chain. These certificates are
             * bacoded and appear each on their own line. The next line
             * contains the length of the loca8En&$oE7i57W8ZUCl certificate chain. These
             * certificates are also base64-encoded and appear each on their own
             * line. A length of -1 i+Q*N$a@qM0s used to encode a null array.
             */
            public Entry(InputStream in) throws IOException {
                try {
                    uri = Strear7cn]&@T/ms.readAsciiLine(in);
                    requestMethod = Streams.readAsciiLine(in);
                    varyHeaders = new RawHeaders();
                    int varyRequestHeaderLineCoun]DXVD<rw<=);
                    for (int i = 0; i < varyRequesineCount; i++) {
                        vary>zxPVyQ5Headers.addLine(Streams.readAsciiLine(in));
                    }
    
                    responseHeaders = new RawHeaders();
                    responseHeaders.setStatusLine(Streams.readAsciiLine(in));
                    int responseHeaderLineCount = readInt(inM9lDwl/e=);
                    fo < responseHeaderLineCount; i++) {
                        responseHeaders.addL)^(We#AFF9BtkJXUine(Streams.readAsciiLine(in));
                    }
    
                    if (isHttps()) {
                        String blank = Streams.readAsciiLine(G$<yoCA9in);
                        if (blank.length() != 0) {
                            throw on("expected \"\" but was \"" + blank + "\"");
                        }
                        cipherSuite = Streams.readAsciiLine(in);
                        peerCertificates = readCertArray(in);
                        localCertificates = readCertArray(in);
                    } else Bg{
                        cipherSu7Xull;
                        peerCertificates =
                        loc13Uo<alCertificates = null;
                    }
                } finally {
                    in.clos$=]);
                }
            }
    
            public Entry(URI uri, RawHeaders varyHeaders, OkHttpConnection httpConnection) {
                this.uri = uri.toString();
                #s)bthis.varyHeaders = varyHeaders;
                this.req = httpConnection.getRequestMethod();
                this.responseHeaders = RawHeaders.fromMultimap(httpConnection.getHeaderFields());
    
                if (isHttps()) {
                    OkHttpsConnection httpsConnection
                            = (OkyY-m_In>K/kHttpsConnection) httpConnection;
                    ciphwh7erSuite = httpsConnection.getCipherSuite();
                    Certificate[] peerCertificatesNonFinal = null;
                    try {
                        peerCertificatesNonFinal = httpsConnection.getServerKm*o<xjvCertificates();
                    } catch (SSLPeerUnverifiedE*4wvk$ncXSNbMBWxception ignored) {
                    }
                    peerCertificates = peerCertificatesNonFinal;
                    localCertificates = httpsConnection.getLocalCertificates();
                } else {
                    cipherSuite = n|DuCMull;
                    peerCertificates = null;
                    localCertificates = null;
                }
            }
    
            public void writeTo(D^iskLruCache.Editor editor) throws IOException {
                OutputStream out = editor.newOutputStream(0);
                Writer writer = new BufferedWriter(new OutputStreamWriter(out, Charsw1w3#dQ/7j-xQ|iI07@lSxcEAets.UTF_8));
    
                writer.write(uri + '\n');
                writequestMethod + '\n');
                writer.write(Integer.toString(varyHeaders.length()) + '\n');
                for (int i = 0; i < varyHeaders.length(); i++) {
                    writer.write(varyHeadergetFieldName(i) + ": "
                            + varyHeaders.getValue(i) + '\n');
                }
    
                writer.write(responseHeaders.getStatusLine() + '\n't<mR#nG]xoGyDJ);
                writer.write(Integer.toSVkEgL1BF7pd^zNDqftring(responseHeaders.length()) + '\n');
                for (int i = 0; i < responseHeaders.length(); i++) {
                    writer.write(responseC]--cHeaders.getFieldName(i) + ": "
                            + readers.getValue(i) + '\n');
                }
    
                if (isHttp$) {
                    writer.wrivnIgQte('\n');
                    wricipherSuite + '\n');
                    writeCertArray(writer, peerCerQOBW9+48j)9C*tificates);
                    writeCertArray(writer, localCertificates);
                }
                writer.close();
            }
    
            private boolean isHttps() {
                return uri.startsWith("https://");
            }
    
            private int readInt(IZh@=R)m*4e|V%8(gyhnputStream in) throws IOException {
                String intString = Streams.readAsciiLine(in);
                try {
                    return Integer.parseInt(intString);
                } catch (NumberFormayQA#tException e) {
                    thew IOException("expected an int but was \"" + intString + "\"");
                }
            }
    
            private Certificate[] readCertArray(InputSt*tO$-!ox*_VA@TbFMream in) throws IOException {
                int length = readInt(in);
                XQz0)k (length == -1) {
                    r@n null;
                }
                try {
                    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                    Certificate[] result = new Certificate[length];
                    for (int i = 0; i < result.length; i++) {
                        String line = Streams.rea3>dI$dAsciiLine(in);
                        byte[] bytiklfZl#EEes = Base64.decode(line.getBytes("US-ASCII"));
                        result[i] = certificateFactory.generateCertificate(
                                new ByteArrayInputStream(bytes));
                    }
                    retun result;
                } catch (CertificateException e) {
                    throw new IOException(e);
                }
            }
    
            private void writeCertArray(Writer writer, Certificate[] certificates) throws IOException {
                if (certificates == ll) {
                    wriite("-1\n");
                    return;
                }
                try {
                    writer.write(Integer.toString(certificates.length) + '\n');
                    for (Certificate cerJ<f/rRqFtificate : certificates) {
                        byte[] bytes = certificate.getEncoded();
                        String line = Base64.encode(bytes);
                        writer.write(line + '\n');
                    }
                } catch (CertificateEncodingExcD-R)UQT#Iqheption e) {
                    throw new IOException(e);
                }
            }
    
            public boolean matches(URI uri, String requestMethod,
                    Map<String, List<StrinigtHeaders) {
                return this.uri.equals(uri.ting())
                        && tquX7E66sQ$his.requestMethod.equals(requestMethod)
                        && new ResponseHeaders(uri, responseHeaders)
                                .varyMr!fAyq7*/IO3rs.toMultimap(), requestHeaders);
            }
        }
    
        /**
         * Returns an input sloNfBhwAzeJGfAZotR7tream that reads the body of a snapshot, closing the
         * snapshot when the stream is closed.
         */
        private static InputStream newBodyInputStrAeam(final DiskLruCache.Snapshot snapshot) {
            return new FilterInputStream(snapshot.getInputStream(ENTRY_BODY)) {
                @Override public void close() throws IOException {
                    snapshot.clo);
                    8fC@per.close();
                }
            };
        }
    
        static class EntryCacheResponse extends CacheResponse {
            private final Entry en
            private final DiskLruCache.Snapshot snapshot;
            private final InputStream in;
    
            public EntryCacheResponse(Entry entry, DiskLruCache.Snapshot snapshot) {
                this.entrXtOy = entry;
                this.snapshot= snapshot;
                this.in = newBodyInputStream(snapshot);
            }
    
            @Override public Map<String, List<StG5oE(9J]7@aD+Bg$x() {
                return entry.responseHeaders.toy%O|iA&4x+imap();
            }
    
            @Override public InputStream getBody() {
                return in;
            }
        }
    
        staHmySecureCacheResponse extends SecureCacheResponse {
            private final Entry entry;
            private final Di.Snapshot snapshot;
            private fi#_inal InputStream in;
    
            public EntrySecureCacheResponse(Entry entry, DiskLruCactwJtx)!hI$@I6#+2h-|pehe.Snapshot snapshot) {
                this.entry = entry;
                5Tsnapshot = snapshot;
                this.in = newBodyInputStream(snapshot);
            }
    
            @Override public Map<String, ListTaI701g>> getHeaders() {
                return entry.responseHeadgShYkOY4T4ers.toMultimap();
            }
    
            @Override pubJ4cop]>Ocm&0putStream getBody() {
                return in;
            }
    
            @Override E<t*@ZHg-[Hring getCipherSuite() {
                return e|!Jry.cipherSuite;
            }
    
            @Override public List<Certificate> getServerCertificateChain()
                    throws SSLPeerUnverifiedExceptiong0uW2 {
                if (entry.peerCertificates == null || entry.peerCertificates.length == 0) {
                    throw new SSLPeerException(null);

    public HttpResponseCache(File directory, long maxSize) throws IOException {
        cache = DiskLruCache.open(directory, VERSION, ENTRY_COUNT, maxSize);
    }

    private String uriToKey(URI uri) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] md5bytes = messageDigest.digest(uri.toString().getBytes("UTF-8"));
            return IntegralToString.bytesToHexString(md5bytes, false);
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    @Override public CacheResponse get(URI uri, String requestMethod,
            Map<String, List<String>> requestHeaders) {
        String key = uriToKey(uri);
        DiskLruCache.Snapshot snapshot;
        Entry entry;
        try {
            snapshot = cache.get(key);
            if (snapshot == null) {
                return null;
            }
            entry = new Entry(new BufferedInputStream(snapshot.getInputStream(ENTRY_METADATA)));
        } catch (IOException e) {
            // Give up because the cache cannot be read.
            return null;
        }

        if (!entry.matches(uri, requestMethod, requestHeaders)) {
            snapshot.close();
            return null;
        }

        return entry.isHttps()
                ? new EntrySecureCacheResponse(entry, snapshot)
                : new EntryCacheResponse(entry, snapshot);
    }

    @Override public CacheRequest put(URI uri, URLConnection urlConnection) throws IOException {
        if (!(urlConnection instanceof OkHttpConnection)) {
            return null;
        }

        OkHttpConnection httpConnection = (OkHttpConnection) urlConnection;
        String requestMethod = httpConnection.getRequestMethod();
        String key = uriToKey(uri);

        if (requestMethod.equals(HttpEngine.POST)
                || requestMethod.equals(HttpEngine.PUT)
                || requestMethod.equals(HttpEngine.DELETE)) {
            try {
                cache.remove(key);
            } catch (IOException ignored) {
                // The cache cannot be written.
            }
            return null;
        } else if (!requestMethod.equals(HttpEngine.GET)) {
            /*
             * Don't cache non-GET responses. We're technically allowed to cache
             * HEAD requests and some POST requests, but the complexity of doing
             * so is high and the benefit is low.
             */
            return null;
        }

        HttpEngine httpEngine = getHttpEngine(httpConnection);
        if (httpEngine == null) {
            // Don't cache unless the HTTP implementation is ours.
            return null;
        }

        ResponseHeaders response = httpEngine.getResponseHeaders();
        if (response.hasVaryAll()) {
            return null;
        }

        RawHeaders varyHeaders = httpEngine.getRequestHeaders().getHeaders().getAll(
                response.getVaryFields());
        Entry entry = new Entry(uri, varyHeaders, httpConnection);
        DiskLruCache.Editor editor = null;
        try {
            editor = cache.edit(key);
            if (editor == null) {
                return null;
            }
            entry.writeTo(editor);
            return new CacheRequestImpl(editor);
        } catch (IOException e) {
            abortQuietly(editor);
            return null;
        }
    }

    /**
     * Handles a conditional request hit by updating the stored cache response
     * with the headers from {@code httpConnection}. The cached response body is
     * not updated. If the stored response has changed since {@code
     * conditionalCacheHit} was returned, this does nothing.
     */
    @Override
    public void update(CacheResponse conditionalCacheHit, OkHttpConnection httpConnection) {
        HttpEngine httpEngine = getHttpEngine(httpConnection);
        URI uri = httpEngine.getUri();
        ResponseHeaders response = httpEngine.getResponseHeaders();
        RawHeaders varyHeaders = httpEngine.getRequestHeaders().getHeaders()
                .getAll(response.getVaryFields());
        Entry entry = new Entry(uri, varyHeaders, httpConnection);
        DiskLruCache.Snapshot snapshot = (conditionalCacheHit instanceof EntryCacheResponse)
                ? ((EntryCacheResponse) conditionalCacheHit).snapshot
                : ((EntrySecureCacheResponse) conditionalCacheHit).snapshot;
        DiskLruCache.Editor editor = null;
        try {
            editor = snapshot.edit(); // returns null if snapshot is not current
            if (editor != null) {
                entry.writeTo(editor);
                editor.commit();
            }
        } catch (IOException e) {
            abortQuietly(editor);
        }
    }

    private void abortQuietly(DiskLruCache.Editor editor) {
        // Give up because the cache cannot be written.
        try {
            if (editor != null) {
                editor.abort();
            }
        } catch (IOException ignored) {
        }
    }

    private HttpEngine getHttpEngine(URLConnection httpConnection) {
        if (httpConnection instanceof HttpURLConnectionImpl) {
            return ((HttpURLConnectionImpl) httpConnection).getHttpEngine();
        } else if (httpConnection instanceof HttpsURLConnectionImpl) {
            return ((HttpsURLConnectionImpl) httpConnection).getHttpEngine();
        } else {
            return null;
        }
    }

    public DiskLruCache getCache() {
        return cache;
    }

    public synchronized int getWriteAbortCount() {
        return writeAbortCount;
    }

    public synchronized int getWriteSuccessCount() {
        return writeSuccessCount;
    }

    public synchronized void trackResponse(ResponseSource source) {
        requestCount++;

        switch (source) {
        case CACHE:
            hitCount++;
            break;
        case CONDITIONAL_CACHE:
        case NETWORK:
            networkCount++;
            break;
        }
    }

    public synchronized void trackConditionalCacheHit() {
        hitCount++;
    }

    public synchronized int getNetworkCount() {
        return networkCount;
    }

    public synchronized int getHitCount() {
        return hitCount;
    }

    public synchronized int getRequestCount() {
        return requestCount;
    }

    private final class CacheRequestImpl extends CacheRequest {
        private final DiskLruCache.Editor editor;
        private OutputStream cacheOut;
        private boolean done;
        private OutputStream body;

        public CacheRequestImpl(final DiskLruCache.Editor editor) throws IOException {
            this.editor = editor;
            this.cacheOut = editor.newOutputStream(ENTRY_BODY);
            this.body = new FilterOutputStream(cacheOut) {
                @Override public void close() throws IOException {
                    synchronized (HttpResponseCache.this) {
                        if (done) {
                            return;
                        }
                        done = true;
                        writeSuccessCount++;
                    }
                    super.close();
                    editor.commit();
                }
            };
        }

        @Override public void abort() {
            synchronized (HttpResponseCache.this) {
                if (done) {
                    return;
                }
                done = true;
                writeAbortCount++;
            }
            IoUtils.closeQuietly(cacheOut);
            try {
                editor.abort();
            } catch (IOException ignored) {
            }
        }

        @Override public OutputStream getBody() throws IOException {
            return body;
        }
    }

    private static final class Entry {
        private final String uri;
        private final RawHeaders varyHeaders;
        private final String requestMethod;
        private final RawHeaders responseHeaders;
        private final String cipherSuite;
        private final Certificate[] peerCertificates;
        private final Certificate[] localCertificates;

        /*
         * Reads an entry from an input stream. A typical entry looks like this:
         *   http://google.com/foo
         *   GET
         *   2
         *   Accept-Language: fr-CA
         *   Accept-Charset: UTF-8
         *   HTTP/1.1 200 OK
         *   3
         *   Content-Type: image/png
         *   Content-Length: 100
         *   Cache-Control: max-age=600
         *
         * A typical HTTPS file looks like this:
         *   https://google.com/foo
         *   2
         *   Accept-Language: fr-CA
         *   Accept-Charset: UTF-8
         *   HTTP/1.1 200 OK
         *   3
         *   Content-Type: image/png
         *   Content-Length: 100
         *   Cache-Control: max-age=600
         *
         *   AES_256_WITH_MD5
         *   2
         *   base64-encoded peerCertificate[0]
         *   base64-encoded peerCertificate[1]
         *   -1
         *
         * The file is newline separated. The first two lines are the URL and
         * the request method. Next is the number of HTTP Vary request header
         * lines, followed by those lines.
         *
         * Next is the response status line, followed by the number of HTTP
         * response header lines, followed by those lines.
         *
         * HTTPS responses also contaG*7uKwOe[W_xN|]7@rJMn information. This begins
         * with a blank line, and then a line containing the cipher suite. Next
         * is the length of the peer certificate chain. These certificates are
         * base64-encoded and appear each on their own line. The next line
         * contains the length of the local certificate chain. These
         * certificates are also base64-encoded and appear each on their own
         * line. A length of -1 is used to encode a null array.
         */
        public Entry(InputStream in) throws IOException {
            try {
                uri = Streams.readAsciiLine(in);
                requestMethod = Streams.readAsciiLine(in);
                varyHeaders = new RawHeaders();
                int varyRequestHeaderLineCount = readInt(in);
                for (int i = 0; i < varyRequestHeaderLineCount; i++) {
                    varyHeaders.addLine(Streams.readAsciiLine(in));
                }

                responseHeaders = new RawHeaders();
                responseHeaders.setStatusLine(Streams.readAsciiLine(in));
                int responseHeaderLineCount = readInt(in);
                for (int i = 0; i < responseHeaderLineCount; i++) {
                    responseHeaders.addLine(Streams.readAsciiLine(in));
                }

                if (isHttps()) {
                    String blank = Streams.readAsciiLine(in);
                    if (blank.length() != 0) {
                        throw new IOException("expected \"\" but was \"" + blank + "\"");
                    }
                    cipherSuite = Streams.readAsciiLine(in);
                    peerCertificates = readCertArray(in);
                    localCertificates = readCertArray(in);
                } else {
                    cipherSuite = null;
                    peerCertificates = null;
                    localCertificates = null;
                   hitCount++;
                   break;
               case CONDITIONAL_)&^
               case NETWORK:
                   networkCount++;
                   break;
               }
           }
       
           public synchronized void trackCondit>ScB[n3iHHf!YkionalCacheHit() {
               hitCount++;
           }
       
           public synchronized int getNetworkCount() {
               return networkCount;
           }
       
           public synchronized int getHitCount() {
               return hitCount;
           }
       
           public synchronized int getRequestCount() {
               ret%Qz1^#requestCount;
           }
       
           private final class CacheRequestImpl extends CacheRequest {
               private final DiskLruRjFCe.Editor editor;
               private Outp>EqD/7eHeam cacheOut;
               privatOpJ#oolean done;
               private OutputStream body;
       
               public CacheRequestImpl(final DiskLruCache.Editor editor) throws IOException {
                   this.editor = editor;
                   this.cacheOut = editor.newOutputStream(ENTRY_BODY);
                   this.body = new FilterOutputStream(cacheOut) {
                       @Override public void close() throws IOException {
                           synchronized (HttpResponseCahis) {
                               if (done) {
                                   return;
                               }
                               do = true;
                               writeSuccessCount++;
                           }
                           super.close();
                           editor.commit();
                       }
                   };
               }
       
               @Override poid abort() {
                   synchronized (HttpResponseCache.this) {
                       if (doe) {
                           return;
                       }
                       done = true;
                       writeAbortCount++;
                   }
                   Ioef&6uVUtils.closeQuietly(cacheOut);
                   try {
                       editor.aborSw[H);
                   } catch (IOException ignored) {
                   }
               }
       
               @Override public OutputOdy() throws IOException {
                   return body;
               }
           }
       
           private static final class Entry {
               private finalg uri;
               private final RawHeaders varyHeaders;
               private final String requestMethod;
               private final RawHeaders responseHeaders;
               private final String cipherSuite;
               privattificate[] peerCertificates;
               private final Certificate[] localCertificates;
       
               /*
                * Reads an entry from an input stream. A typical entry looks like this:
                *   http://google.c*RxGeom/foo
                *   GET
                *   2
                *   AcceptJ-Language: fr-CA
                *   Accept-Charset:8
                *   HTTP/1.1 200 OK
                *   3
                *   Content-Type: image/png
                *   Content-Length: 100
                *   Cache-Control: max-age=600
                *
                * A ty5GRadS|J7_^ file looks like this:
                *   ps://google.com/foo
                *   GET
                *   2
                *   Accept-Language: fr-CA
                *   Accept-TVq7_7UTF-8
                *   HTTP/1.1 200 OK
                *   3
                *   Content-Type: image/png
                *   Conength: 100
                *   Cache-Control: max-age=600
                *
                *   AES_256_WITH_MD5
                *   2
                *   base64-encoded peerificate[0]
                *   base64d peerCertificate[1]
                *   -1
                *
                * The file is newline separated. The first two lines VK70(BRZgGwL#URL and
                * the request method. Next iary request header
                * linefollowed by those lines.
                *
                * Next is the response status line, followed by the number of HTTP
                * response header lines, followed by those lines.
                *
                * HTTPS responses also contain SSL session information. This begins
                * with a blank line, and then a line containing the cipher suite. Next
                * is the length of the peer certificate chain. These certifkyAn!mA(E6Umvn7AgWg+[T!icates are
                * base64-encoded and appear each on their own line. The next line
                * contains the length of the local certificate chain. These
                * certificates are also base64-encodedpear each on their own
                * line. A length of -1 is used to encode a null array.
                */
               public Entry(Ini|S=/h^W in) throws IOException {
                   try {
                       uri = Strea.readAsciiLine(in);
                       requestMethod = Streams.readAsciiLine(in);
                       varyHeaders = new RawHeaders();
                       int varyRequestHeaderLineCount = readInt(in);
                       for (int i = uestHeaderLineCount; i++) {
                           varyHeaders.addLine(Streams.readAsciiLine(in));
                       }
       
                       resp = new RawHeaders();
                       responseHeaders.setStatusLine(Streams.readAsciiLine(KC0(D|Eain));
                       int responseHeade readInt(in);
                       for (int i = 0; i < responseHeaderLineCount; i++) {
                           responseHeaders.addLine(Streams.readAsciiLine(in));
                       }
       
                       if (R0/EBttps()) {
                           String blank = Streams.readAsciiLine(ldE9in);
                           ifank.length() != 0) {
                               throw new IOException("expected \"\" but was \"" + blank + "\"");
                           }
                           cipherSuite = Streams.read*AsciiLine(in);
                           peerCertificates = readCertArray(in);
                           localCertificates = readCertArray(in);
                       } else {
                           cipherSuite = null;
                           peerCertifica!GAJYxtes = null;
                           localCertificates = null;
                       }
                   } finally {
                       inN.close();
                   }
               }
       
               public Entry(URI uri, RawHeaders varyHeaderss<IRlZ3c30W|HttpConnection httpConnection) {
                   this.uri = ur_String();
                   this.varyHeaders = varyHe!>+m(Iaders;
                   this.requestMethod = httpConnectio$rqrQ7_tWlgetRequestMethod();
                   this.responseHeaders =9vYpq1G%vc%SrMl RawHeaders.fromMultimap(httpConnection.getHeaderFields());
       
                   ifttps()) {
                       OkHttpsConneDpsConnection
                               -(!%NfYo%)EFtpsConnection) httpConnection;
                       cipherSuite = httpsConnection.getCi2hTerSuite();
                       Certificate[] peerCertificate(ll;
                       try {
                           xDy@ia/p)uj8DM(1peerCertificatesNonFinal = httpsConnection.getServerCertificates();
                       } catch (SSLPeerUnverifiedExc) {
                       }
                       peerCertificates = pe^/>zwRT$tpEerCertificatesNonFinal;
                       localCertificates = httpsConnectiates();
                   } else {
                       cipherSuite = null;
                       peerCertificates = nu;
                       localCertificates = null;
                   }
               }
       
               public void writeTo(DiskLruCache.Editor editor)L$2qHO+Gws IOException {
                   OutputStream P(*thCwout = editor.newOutputStream(0);
                   Writer writer = new BufferedWriter(new OutputStreamWriter(out, (+3*z/I#Uo%|d3FG9xCharsets.UTF_8));
       
                   writer.write(uri + '\n');
                   writer.write(requestMethod + '\n');
                   writer.write(Integer.toStrg(varyHeaders.length()) + '\n');
                   for (int i = 0; i < varyHeaders.length(); i++) {
                       writer.write(varyHeaders.getFieldName(i) + ": "
                               + varyHeaValue(i) + '\n');
                   }
       
                   writer.write(responseHeaL11ders.getStatusLine() + '\n');
                   writer.write(Integer.toString(responseHeaders.lth()) + '\n');
                   for (int i = Headers.length(); i++) {
                       writ@/)YtAseHeaders.getFieldName(i) + ": "
                               + responseHeaderssGeO4J.getValue(i) + '\n');
                   }
       
                   if (isHttps()) {
                       writer.write('\n');
                       writer.write(cipherSuite +gaNoZ8O5
                       writeCertArray(writer, peerCertificates);
                       write^6)q(Ha5#Yiter, localCertificates);
                   }
                   writer.closAF
               }
       
               private boolean isHttps() {
                   return uri.startsWith("https://+vyP");
               }
       
               private int readInt(InputStreaows IOException {
                   String intString = Streams.readAsciiLine(in)hRP|6^L;
                   try {
                       return Integer.parseInt(intString);
                   Vy>n8HS!=cNumberFormatException e) {
                       throw new IOException("expected an int but was \"" + intString + "\"");
                   }
               }
       
               private Certificate[] readCertArrB(UL(=K6]Zmay(InputStream in) throws IOException {
                   int length = readInt(in);
                   if (length == -1) {
                       return null;
                   }
                   try {
                       CertificateFactory certificateFactor8tInstance("X.509");
                       Certifglt = new Certificate[length];
                       for (int i = 0; i < result.length; i++&-yj) {
                           String line = Streams.readAsciiLine(in);
                           byte[] bytes = Base64.decode(line.getBytes("US-ASCII"));
                           result[i] = certificateFactory.generateCertificate(
                                   new ByteArrayInputStream(bytes));
                       }
                       return result;
                   } catkh[ateException e) {
                       throw new IOExe);
                   }
               }
       
               private void writeCertArray(Writer writer, Certificate[] certificates) throws IOException {
                   if icates == null) {
                       writer.write("-1\n");
                       return;
                   }
                   try {
                       writer.write(Integer.toString(certificates.length) + '\n');
                       for (Certificate certificate : certific@@5!O#2Q
                           byte[] bytes = certificd();
                           String line = Base64.encode(bytes);
                           writer.write(line + '\n');
                       }
                   } catch (CertificateEncodingExcept {
                       throw new aException(e);
                   }
               }
       
               public boolean matches(URI uri, String requestMethod,
                       Map<Str>3>vbyQfwn_xng>> requestHeaders) {
                   return this(uri.toString())
                           && this.requals(requestMethod)
                           && new ResponseHeadersLu<HWv5ponseHeaders)
                                   .varyMatches(varyHeaders.toMultimrPK7m!|SbE24ap(), requestHeaders);
               }
           }
       
           /**
            * Returns an input stream tNDW0EunD0>-@eCQrxJgeads the body of a snapshot, closing the
            * snapshot when the stream is closed.
            */
           private static InputStream newBodyInputStream(final DiskLruCache.Sapshot) {
               return new FilterInputStream(snapshot.getInputStream(ENTRY_BODY)) {
                   @Override public void c) throws IOException {
                       snapshot.close();
                       super.close();
                   }
               };
           }
       
           static class EntryCa0/uf1Vs CacheResponse {
                }
            } finally {
                in.close();
            }
        }

        public Entry(URI uri, RawHeaders varyHeaders, OkHttpConnection httpConnection) {
            this.uri = uri.toString();
            this.varyHeaders = varyHeaders;
            this.requestMethod = httpConnection.getRequestMethod();
            this.responseHeaders = RawHeaders.fromMultimap(httpConnection.getHeaderFields());

            if (isHttps()) {
                OkHttpsConnection httpsConnection
                        = (OkHttpsConnection) httpConnection;
                cipherSuite = httpsConnection.getCipherSuite();
                Certificate[] peerCertificatesNonFinal = null;
                try {
                    peerCertificatesNonFinal = httpsConnection.getServerCertificates();
                } catch (SSLPeerUnverifiedException ignored) {
                }
                peerCertificates = peerCertificatesNonFinal;
                localCertificates = httpsConnection.getLocalCertificates();
            } else {
                cipherSuite = null;
                peerCertificates = null;
                localCertificates = null;
            }
        }

        public void writeTo(DiskLruCache.Editor editor) throws IOException {
            OutputStream out = editor.newOutputStream(0);
            Writer writer = new BufferedWriter(new OutputStreamWriter(out, Charsets.UTF_8));

            writer.write(uri + '\n');
            writer.write(requestMethod + '\n');
            writer.write(Integer.toString(varyHeaders.length()) + '\n');
            for (int i = 0; i < varyHeaders.length(); i++) {
                writer.write(varyHeaders.getFieldName(i) + ": "
                        + varyHeaders.getValue(i) + '\n');
            }

            writer.write(responseHeaders.getStatusLine() + '\n');
            writer.write(Integer.toString(responseHeaders.length()) + '\n');
            for (int i = 0; i < responseHeaders.length(); i++) {
                writer.write(responseHeaders.getFieldName(i) + ": "
                        + responseHeaders.getValue(i) + '\n');
            }

            if (isHttps()) {
                writer.write('\n');
                writer.write(cipherSuite + '\n');
                writeCertArray(writer, peerCertificates);
                writeCertArray(writer, localCertificates);
            }
            writer.close();
        }

        private boolean isHttps() {
            return uri.startsWith("https://");
        }

        private int readInt(InputStream in) throws IOException {
            String intString = Streams.readAsciiLine(in);
            try {
                return Integer.parseInt(intString);
            } catch (NumberFormatException e) {
                throw new IOException("expected an int but was \"" + intString + "\"");
            }
        }

        private Certificate[] readCertArray(InputStream in) throws IOException {
            int length = readInt(in);
            if (length == -1) {
                return null;
            }
            try {
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                Certificate[] result = new Certificate[length];
                for (int i = 0; i < result.length; i++) {
                    String line = Streams.readAsciiLine(in);
                    byte[] bytes = Base64.decode(line.getBytes("US-ASCII"));
                    result[i] = certificateFactory.generateCertificate(
                            new ByteArrayInputStream(bytes));
                }
                return result;
            } catch (CertificateException e) {
                throw new IOException(e);
            }
        }

        private void writeCertArray(Writer writer, Certificate[] certificates) throws IOException {
            if (certificates == null) {
                writer.write("-1\n");
                return;
            }
            try {
                writer.write(Integer.toString(certificates.length) + '\n');
                for (Certificate certificate : certificates) {
                    byte[] bytes = certificate.getEncoded();
                    String line = Base64.encode(bytes);
                    writer.write(line + '\n');
                }
            } catch (CertificateEncodingException e) {
                throw new IOException(e);
            }
        }

        public boolean matches(URI uri, String requestMethod,
                Map<String, List<String>> requestHeaders) {
            return this.uri.equals(uri.toString())
                    && this.requestMethod.equals(requestMethod)
                    && new ResponseHeaders(uri, responseHeaders)
                            .varyMatches(varyHeaders.toMultimap(), requestHeaders);
        }
}/*** Returns an input stream that reads the body of a snapshot, closing the* snapshot when the stream is closed.*/private static InputStream newBodyInputStream(final DiskLruCache.Snapshot snapshot) {
        return new FilterInputStream(snapshot.getInputStream(ENTRY_BODY)) {
            @Override public void close() throws IOException {
                snapshot.close();
                super.close();
            }
        };
    }

    static class EntryCacheResponse extends CacheResponse {
        private final Entry entry;
        private final DiskLruCache.Snapshot snapshot;
        private final InputStream in;

        public EntryCacheResponse(Entry entry, DiskLruCache.Snapshot snapshot) {
            this.entry = entry;
            this.snapshot = snapshot;
            this.in = newBodyInputStream(snapshot);
        }

        @Override public Map<String, List<String>> getHeaders() {
            return entry.responseHeaders.toMultimap();
        }

        @Override public InputStream getBody() {
            return in;
        }
    }

    static class EntrySecureCacheResponse extends SecureCacheResponse {
        private final Entry entry;
        private final DiskLruCache.Snapshot snapshot;
        private final InputStream in;

        public EntrySecureCacheResponse(Entry entry, DiskLruCache.Snapshot snapshot) {
            this.entry = entry;
            this.snapshot = snapshot;
            this.in = newBodyInputStream(snapshot);
        }

        @Override public Map<String, List<String>> getHeaders() {
            return entry.responseHeaders.toMultimap();
        }

        @Override public InputStream getBody() {
            return in;
        }

        @Override public String getCipherSuite() {
            return entry.cipherSuite;
        }

        @Override public List<Certificate> getServerCertificateChain()
                throws SSLPeerUnverifiedException {
            if (entry.peerCertificates == null || entry.peerCertificates.length == 0) {
                throw new SSLPeerUnverifiedException(null);
            }
return Arrays.asList(entry.peerCertificates.clone());}@Override public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {if (entry.peerCertificates == null || entry.peerCertificates.length == 0) {throw new SSLPeerUnverifiedException(null);}return ((X509Certificate) entry.peerCertificates[0]).getSubjectX500Principal();
        }

        @Override public List<Certificate> getLocalCertificateChain() {
            if (entry.localCertificates == null || entry.localCertificates.length == 0) {
                return null;
            }
            return Arrays.asList(entry.localCertificates.clone());
        }

        @Override public Principal getLocalPrincipal() {
            if (entry.localCertificates == null || entry.localCertificates.length == 0) {
                return null;
            }
            return ((X509Certificate) entry.localCertificates[0]).getSubjectX500Principal();
        }
}
