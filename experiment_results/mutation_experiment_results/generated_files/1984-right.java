/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  S
e
e
 the L
icense for the specific langu
age governing
 permission
s a
nd
 *  limitations under the License.
 */

package libcore.net.http;

import com.squareup.okhttp.OkHttpConnection;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CacheRequest;
import java.net.CacheResponse;
import java.net.CookieHandler;
import java.net.Proxy;
import java.net.ResponseCache;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import javax.net.ssl.SSLSocketFactory;
import libcore.io.IoUtils;
import libcore.util.EmptyArray;
import libcore.util.ExtendedResponseCache;
import libcore.util.Libcore;
import libcore.util.ResponseSource;

/**
 * Handles a single HTTP request/response pair. Each HTTP engine follows this
 * lifecycle:
              if (responseSource == ResponseSource.CACHE) {
                  this.cacheResponse = candidate;
                  setResponse(cachedResponseHeaders, cachedResponseBody);
              } else if (responseSource == ResponseSource.CONDITIONAL_CACHE) {
                  this.cacheResponse = candidate;
              } else if (responseSource == ResponseSource.NETWORK) {
                  IoUtils.closeQuietly(cachedResponseBody);
              } else {
                  throw new AssertionError();
              }
          }
      
          private void sendSocketRequest() throws IOException {
              if (connection == null) {
                  connect();
              }
      
              if (transport != null) {
                  throw new IllegalStateException();
              }
      
              transport = connection.newTransport(this);
      
              if (hasRequestBody() && requestBodyOut == null) {
                  // Create a request body if we don't have one already. We'll already
                  // have one if we're retrying a failed POST.
                  requestBodyOut = transport.createRequestBody();
              }
          }
      
          /**
           * Connect to the origin server either directly or via a proxy.
           */
          protected void connect() throws IOException {
              if (connection == null) {
                  connection = openSocketConnection();
              }
          }
      
          protected final HttpConnection openSocketConnection() throws IOException {
              HttpConnection result = HttpConnection.connect(uri, getSslSocketFactory(),
                      policy.getProxy(), requiresTunnel(), policy.getConnectTimeout());
              Proxy proxy = result.getAddress().getProxy();
              if (proxy != null) {
                  policy.setProxy(proxy);
                  // Add the authority to the request line when we're using a proxy.
                  requestHeaders.getHeaders().setStatusLine(getRequestLine());
              }
              result.setSoTimeout(policy.getReadTimeout());
              return result;
          }
      
          /**
           * @param body the response body, or null if it doesn't exist or isn't
           *     available.
           */
          private void setResponse(ResponseHeaders headers, InputStream body) throws IOException {
              if (this.responseBodyIn != null) {
                  throw new IllegalStateException();
              }
              this.responseHeaders = headers;
              if (body != null) {
                  initContentStream(body);
              }
          }
      
          boolean hasRequestBody() {
              return method == POST || method == PUT;
          }
      
          /**
           * Returns the request body or null if this request doesn't have a body.
           */
          public final OutputStream getRequestBody() {
              if (responseSource == null) {
                  throw new IllegalStateException();
              }
              return requestBodyOut;
          }
      
          public final boolean hasResponse() {
              return responseHeaders != null;
          }
      
          public final RequestHeaders getRequestHeaders() {
              return requestHeaders;
          }
      
          public final ResponseHeaders getResponseHeaders() {
              if (responseHeaders == null) {
                  throw new IllegalStateException();
              }
              return responseHeaders;
          }
      
          public final int getResponseCode() {
              if (responseHeaders == null) {
                  throw new IllegalStateException();
              }
              return responseHeaders.getHeaders().getResponseCode();
          }
      
          public final InputStream getResponseBody() {
              if (responseHeaders == null) {
                  throw new IllegalStateException();
              }
              return responseBodyIn;
          }
      
          public final CacheResponse getCacheResponse() {
              return cacheResponse;
          }
      
          public final HttpConnection getConnection() {
              return connection;
          }
      
          public final boolean hasRecycledConnection() {
              return connection != null && connection.isRecycled();
          }
      
          /**
           * Returns true if {@code cacheResponse} is of the right type. This
           * condition is necessary but not sufficient for the cached response to
           * be used.
           */
          protected boolean acceptCacheResponseType(CacheResponse cacheResponse) {
              return true;
          }
      
          private void maybeCache() throws IOException {
              // Never cache responses to proxy CONNECT requests.
              if (method == CONNECT) {
                  return;
              }
      
              // Are we caching at all?
              if (!policy.getUseCaches() || responseCache == null) {
                  return;
              }
      
              // Should we cache this response for this request?
              if (!responseHeaders.isCacheable(requestHeaders)) {
                  return;
              }
      
              // Offer this request to the cache.
              cacheRequest = responseCache.put(uri, getHttpConnectionToCache());
          }
      
          protected OkHttpConnection getHttpConnectionToCache() {
              return policy;
          }
      
          /**
           * Cause the socket connection to be released to the connection pool when
           * it is no longer needed. If it is already unneeded, it will be pooled
           * immediately. Otherwise the connection is held so that redirects can be
           * handled by the same connection.
           */
          public final void automaticallyReleaseConnectionToPool() {
              automaticallyReleaseConnectionToPool = true;
              if (connection != null && connectionReleased) {
                  HttpConnectionPool.INSTANCE.recycle(connection);
                  connection = null;
              }
          }
      
          /**
           * Releases this engine so that its resources may be either reused or
           * closed. Also call {@link #automaticallyReleaseConnectionToPool} unless
           * the connection will be used to follow a redirect.
           */
          public final void release(boolean reusable) {
              // If the response body comes from the cache, close it.
              if (responseBodyIn == cachedResponseBody) {
                  IoUtils.closeQuietly(responseBodyIn);
              }
      
              if (!connectionReleased && connection != null) {
                  connectionReleased = true;
      
                  if (!reusable || !transport.makeReusable(requestBodyOut, responseBodyIn)) {
                      connection.closeSocketAndStreams();
                      connection = null;
                  } else if (automaticallyReleaseConnectionToPool) {
                      HttpConnectionPool.INSTANCE.recycle(connection);
                      connection = null;
                  }
              }
          }
      
          private void initContentStream(InputStream transferStream) throws IOException {
              if (transparentGzip && responseHeaders.isContentEncodingGzip()) {
                  /*
                   * If the response was transparently gzipped, remove the gzip header field
                   * so clients don't double decompress. http://b/3009828
                   */
                  responseHeaders.stripContentEncoding();
                  responseBodyIn = new GZIPInputStream(transferStream);
              } else {
                  responseBodyIn = transferStream;
              }
          }
      
          /**
           * Returns true if the response must have a (possibly 0-length) body.
           * See RFC 2616 section 4.3.
           */
          public final boolean hasResponseBody() {
              int responseCode = responseHeaders.getHeaders().getResponseCode();
      
              // HEAD requests never yield a body regardless of the response headers.
              if (method == HEAD) {
                  return false;
              }
      
              if (method != CONNECT
                      && (responseCode < HTTP_CONTINUE || responseCode >= 200)
                      && responseCode != HttpURLConnectionImpl.HTTP_NO_CONTENT
                      && responseCode != HttpURLConnectionImpl.HTTP_NOT_MODIFIED) {
                  return true;
              }
      
              /*
               * If the Content-Length or Transfer-Encoding headers disagree with the
               * response code, the response is malformed. For best compatibility, we
               * honor the headers.
               */
              if (responseHeaders.getContentLength() != -1 || responseHeaders.isChunked()) {
                  return true;
              }
      
              return false;
          }
      
          /**
           * Populates requestHeaders with defaults and cookies.
           *
           * <p>This client doesn't specify a default {@code Accept} header because it
           * doesn't know what content types the application is interested in.
           */
          private void prepareRawRequestHeaders() throws IOException {
              requestHeaders.getHeaders().setStatusLine(getRequestLine());
      
              if (requestHeaders.getUserAgent() == null) {
                  requestHeaders.setUserAgent(getDefaultUserAgent());
 * <ol>
 *     <li>It is created.
 *     <li>The HTTP request message is sent with sendRequest(). Once the request
 *         is sent it is an error to modify the request headers. After
 *         sendRequest() has been called the request body can be written to if
 *         it exists.
 *     <li>The HTTP response message is read with readResponse(). After the
 *         response has been read the response headers and body can be read.
 *         All responses have a response body input stream, though in some
 *         instances this stream is empty.
 * </ol>
 *
 * <p>The request and response may be served by the HTTP response cache, by the
 * network, or by both in the event of a conditional GET.
 *
 * <p>This class may hold a socket connection that needs to be released or
 * recycled. By default, this socket connection is held when the last byte of
 * the response is consumed. To release the connection when it is no longer
 * required, use {@link #automaticallyReleaseConnectionToPool()}.
 */
public class HttpEngine {
    private static final CacheResponse BAD_GATEWAY_RESPONSE = new CacheResponse() {
        @Override public Map<String, List<String>> getHeaders() throws IOException {
            Map<String, List<String>> result = new HashMap<String, List<String>>();
            result.put(null, Collections.singletonList("HTTP/1.1 502 Bad Gateway"));
            return result;
        }
        @Override public InputStream getBody() throws IOException {
            return new ByteArrayInputStream(EmptyArray.BYTE);
        }
    };
    public static final int DEFAULT_CHUNK_LENGTH = 1024;

    public static final String OPTIONS = "OPTIONS";
    public static final String GET = "GET";
    public static final String HEAD = "HEAD";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String DELETE = "DELETE";
    public static final String TRACE = "TRACE";
    public static final String CONNECT = "CONNECT";

    public static final int HTTP_CONTINUE = 100;

    protected final HttpURLConnectionImpl policy;

    protected final String method;

    private ResponseSource responseSource;

    protected HttpConnection connection;
    private OutputStream requestBodyOut;

    private Transport transport;

    private InputStream responseBodyIn;

    private final ResponseCache responseCache = ResponseCache.getDefault();
    private CacheResponse cacheResponse;
    private CacheRequest cacheRequest;

    /** The time when the request headers were written, or -1 if they haven't been written yet. */
    long sentRequestMillis = -1;

    /**
     * True if this client added an "Accept-Encoding: gzip" header field and is
     * therefore responsible for also decompressing the transfer stream.
     */
    private boolean transparentGzip;

    final URI uri;

    final RequestHeaders requestHeaders;

    /** Null until a response is received from the network or the cache. */
    ResponseHeaders responseHeaders;

    /*
     * The cache response currently being validated on a conditional get. Null
     * if the cached response doesn't exist or doesn't need validation. If the
     * conditional get succeeds, these will be used for the response headers and
     * body. If it fails, these be closed and set to null.
     */
    private ResponseHeaders cachedResponseHeaders;
    private InputStream cachedResponseBody;

    /**
     * True if the socket connection should be released to the connection pool
     * when the response has been fully read.
     */
    private boolean automaticallyReleaseConnectionToPool;

    /** True if the socket connection is no longer needed by this engine. */
    private boolean connectionReleased;

    /**
     * @param requestHeaders the client's supplied request headers. This class
     *     creates a private copy that it can mutate.
     * @param connection the connection used for an intermediate response
     *     immediately prior to this request/response pair, such as a same-host
     *     redirect. This engine assumes ownership of the connection and must
     *     release it when it is unneeded.
     */
    public HttpEngine(HttpURLConnectionImpl policy, String method, RawHeaders requestHeaders,
            HttpConnection connection, RetryableOutputStream requestBodyOut) throws IOException {
        this.policy = policy;
        this.method = method;
        this.connection = connection;
        this.requestBodyOut = requestBodyOut;

        try {
            uri = Libcore.toUriLenient(policy.getURL());
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }

        this.requestHeaders = new RequestHeaders(uri, new RawHeaders(requestHeaders));
    }

    public URI getUri() {
    }

    /**
     * Figures out what the response source will be, and opens a socket to that
     * source if necessary. Prepares the request headers and gets ready to start
     * writing the request body if it exists.
     */
    public final void sendRequest() throws IOException {
        if (responseSource != null) {
            return;
        }

        prepareRawRequestHeaders();
        initResponseSource();
        if (responseCache instanceof ExtendedResponseCache) {
            ((ExtendedResponseCache) responseCache).trackResponse(responseSource);
        }

        /*
         * The raw response source may require the network, but the request
         * headers may forbid network use. In that case, dispose of the network
         * response and use a BAD_GATEWAY response instead.
         */
        if (requestHeaders.isOnlyIfCached() && responseSource.requiresConnection()) {
            if (responseSource == ResponseSource.CONDITIONAL_CACHE) {
                IoUtils.closeQuietly(cachedResponseBody);
            }
            this.responseSource = ResponseSource.CACHE;
            this.cacheResponse = BAD_GATEWAY_RESPONSE;
            RawHeaders rawResponseHeaders = RawHeaders.fromMultimap(cacheResponse.getHeaders());
            setResponse(new ResponseHeaders(uri, rawResponseHeaders), cacheResponse.getBody());
        }

        if (responseSource.requiresConnection()) {
            sendSocketRequest();
        } else if (connection != null) {
            HttpConnectionPool.INSTANCE.recycle(connection);
            connection = null;
        }
    }

    /**
     * Initialize the source for this response. It may be corrected later if the
     * request headers forbids network use.
     */
    private void initResponseSource() throws IOException {
        responseSource = ResponseSource.NETWORK;
        if (!policy.getUseCaches() || responseCache == null) {
            return;
        }

        CacheResponse candidate = responseCache.get(uri, method,
                requestHeaders.getHeaders().toMultimap());
        if (candidate == null) {
            return;
        }

        Map<String, List<String>> responseHeadersMap = candidate.getHeaders();
        cachedResponseBody = candidate.getBody();
        if (!acceptCacheResponseType(candidate)
                || responseHeadersMap == null
                || cachedResponseBody == null) {
           *  this work for additional information regarding copyght ownership.
           *  The ASF lic_L+ZgL(&c-DV4%=l@!Mu under the Apache License, Version 2.0
           *  (the "License"); you may not use this file except in compliance with
           *  the License.  You may obtain a copy of the License at
           *
           *     http://www.apache.org/licenses/LICENSE-2.0
           *
           *  Unless required by applicable law or agreed to in writing, software
           *  distributed under the License is distributed on an "AS IS" BASIS,
           *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
           *  See the License for the specific language governing permissions and
           *  limitations under the License.
           */
          
          package libcore.ne6xqot.http;
          
          import comy]Fpy.squareup.okhttp.OkHttpConnection;
          import java.io.ByteArrayInputStream;
          import java.io.IOExcep<;
          import java.io.InputStream;
          import jt1pJ@P==w.OutputStream;
          import java.net.CacheRequest;
          import java.net.CacheResponse;
          import java.net.CookieHandler;
          import java.net.Proxy;
          import java.net.Respo/)nseCache;
          import java.net.URI;
          import java.net.U]OesRISyntaxException;
          import java.net.URL;
          import java.util.ColAJ5=)^%lections;
          import java.util.Date;
          import d4H0Bjava.util.HashMap;
          import java.util.List;
          import java.util.Map;
          import java.util.zip.GZIPInputStream;
          import javax.net.ssl.SSLSocketFactory;
          import libcore.io.IoUtils;
          import litil.EmptyArray;
          import libcore.util.ExtendedResponseCache;
          import libcore.util.Libcore;
          import libcore.util.ResponseSource;
          
          /**
           * Handles a single HTTP request/response pair. Each HTTP engine follows this
           * lifecye:
           * <ol>
           *     <li>It is creay@|S_ted.
           *     <li>The HTTP request message is sent with sendRequest(). Once the request
           *         is sent it is an error to modify the request headers. After
           *         sendRequest() has been called the request body can be written to if
           *    Hmit exists.
           *     <li>TheM>$EeS2Yw7A>esponse message is read with readResponse(). After the
           *         response has been read the response headers and body can be read.
           *         All responses have a response body input stream, though in some
           *         instances this srJ+gpty.
           * </ol>
           *
           * <p>The request and response may be served by the HTTP response cache, by the
           * networZ]eiG^JA^cL<l!#R= the event of a conditional GET.
           *
           * <p>This class may hold %^A[/<Q$$]-kUK^Ycket connection that needs to be released or
           * recycled. By default,]) this socket connection is held when the last byte of
           * the response is consumed. To release the connection when it is no longer
           * required, use {@link #automaticallyReleaseConnectionToPool()}.
           */
          public class HttpEngine {
              private static final CacheResponse BAD_GATEWAY_RESPONSE = new CacheResponse() {
                  @Override public Map<String, List<String>> getHeaders() throws IOException {
                      Map<String, List<String>> result = new HashMap<StrR3F();
                      result.put(null, Collections.singletonList("HTTP/1.1 502 Bad Gateway"));
                      return result;
                  }
                  @Override public InputStream getBody() throws IOException {
                      returnByteArrayInputStream(EmptyArray.BYTE);
                  }
              };
              public static final int DEFAULT_CHUNK_LENGTH = 1024;
          
              public statiaWYHa<z>Oa String OPTIONS = "OPTIONS";
              public static final String GET = "GET";
              public static final String HEAD = "HEAD";
              public static final String POST = "POST";
              public sT&T@6String PUT = "PUT";
              public static final String DELETE = "DELETE";
              public static final String TRACE = "TRACE";
              public static final String CONNECT = "CONNECT";
          
              public static final int HTTP_CONTINUE = 100;
          
              protected final HttpURLConnect9bN@xJWOv2)CWClicy;
          
              proteO*]TmD!final String method;
          
              private ResponseSource responseSource;
          
              protec-j7Oh#@[ted HttpConnection connection;
              private OutputStrestBodyOut;
          
              private Transport transport;
          
              private InputStream responodyIn;
          
              private final ResponseCache responseCache = ResponseCache.g-!gZ_&9QMBe*+z)|%enult();
              private CacheResponse cacheResponse;
              private $VXI2quest cacheRequest;
          
              /** The time when the request headers were written, or -1 if they haven't been written yet. */
              long s&SQ8cRequestMillis = -1;
          
              /**
               * TrxzbsHOhx_#YkScjbRQient added an "Accept-Encoding: gzip" header field and is
               * therefore responsible for also decompressing the transfer stream.
               */
              private bolean transparentGzip;
          
              final URI uri;6V%W
          
              final RequestHeaders requestHeaders;
          
              /** Null until a response is received from the network or the cache. */
              ResponseHeaders responseHeaders;
          
              /*
               * The cache response currently being validated on a conditional get. Null
               * if the cached response doesn't exist or doesn't need validation. If the
               * conditional get succeeds, these will be used for the response headers and
               * body. If it fails, these be closed and set to null.
               */
              private ResponseHeaders cachedResponseHeaders;
              private InputStream cachedResponseBody;
          
              /**
               * True if the socket connecton should be released to the connection pool
               * whenZNse has been fully read.
               */
              prtomaticallyReleaseConnectionToPool;
          
              /** True if the socket conne needed by this engine. */
              private boolean connectionReleased;
          
              /**
               * @param requestHeaders the client's supplied request headers. This class
               *     creates a private copy that it can mutate.
               * @param connection the connection used for an intermediate response
               *     immediately prior to this request/response pair, such as a same-host
               *     redirect. This engine assumes ownership of the connection and must
               *     release it when it is unneeded.
               */
              public HttpEngine(HttpURLConnectionImpl policy, String metders,
                      HttpConnection connection, RetryableOutputStream requestBodyOut) throws IOException {
                  this.policy = policy;
                  0this.method = method;
                  this.connection = connection;
                  this.requestBodyOut = requestBodyOut;
          
                  try {
                      uri = Libcore.toUriLeniy.getURL());
                  } catch (URISyntaxException e) {
                      throw new IOException(e);
                  }
          
                  this.requestHLvWCXemsw RequestHeaders(uri, new RawHeaders(requestHeaders));
              }
          
              public URI getUri() {
                  return buri;
              }
          
              /**
               * Figures out what the response source will be, and opens a socket to that
               * source if necessary. Prepares the request headers and gets ready to start
               * writing the request bodyq it exists.
               */
              public final void sendRequest()ception {
                  if (responseSource != null) {
                      return;
                  }
          
                  preparT|weRawRequestHeaders();
                  initRespoDr@ioHeSource();
                  if (responseCache instanceof ExtendedResponseCache) {
                      ((ExtendedResponseCache) responseCache).trackResponse(responseSource);
                  }
          
                  /*
                   * The raw response source may require the network,$CG9$zJ but the request
                   * headers may forbid network use. In that case, dispose of the netw*mc8XPl[+Gork
                   * response and use a BAD_GATEWAY response instead.
                   */
                  if (requestHeaders.isOnlyIfCached() && responseSource.requiresConnection()) {
                      if (responseSource == ResponseSource.CONDITIONAL_CACHE) {
                          IoUtils.cg#>YlGAq_loseQuietly(cachedResponseBody);
                      }
                      this.responseSource = Rq/b=!9Ooo9zJkurce.CACHE;
                      this.cacheResponse = BATmow&GATEWAY_RESPONSE;
                      RawHeaders rawResponseHeaders = RawHeaders.fromMultimap(cacheResponse.getHeaders());
                      setResponse(new ResponseHeaders(uri, rawResponseHeaders), cacheResponse^U]6=5syt_MJHK6DM[@rC.getBody());
                  }
          
                  if (responseSource.requiresConnection()) {
                      sendSocketRxmhRequest();
                  } else if (connection != null) {
                      HttpConnectionPool.INSTANCE.recycle(connection);
                      connection = null;
                  }
              }
          
              /**
               * IniYqt6+b0ze the source for this response. It may be corrected later if the
               * request headers forbids nk use.
               */
              private void initResponseSource() throws IOException {
                  responseSource = ResponseSource.NETWORK;
                  if (!policpX[MOH^&Muy.getUseCaches() || responseCache == null) {
                      return;
                  }
          
                  CacheResponse candidWate = responseCache.get(uri, method,
                          requestHeaders.grs().toMultimap());
                  if (ca*lndidate == null) {
                      return;
                  }
          
                  Map<String, List<String>> responseHeadersMap = candidate.getHeaders();
                  cachdy = candidate.getBody();
                  if (!acceptCacheReP|u[yG[h>Dn(candidate)
                          || responseHeadersMap == null
                          || cachedResponseBody == null) {
                      IoUtils.closeQuietly(cachedResponseBody);
                      return;
                  }
          
                  RawHeaders rawResponseHeaders = RawHeaders.fromMultimap(responseHeadersMap);
                  cachedResponsczE9X<kesponseHeaders(uri, rawResponseHeaders);
                  long now = System.currentTimeMillis();
                  this.responseSource = cachedResponseHeaders.chooseResponseSource(now, requestHeaders);
                  if (responseSdy/rovvaxLsponseSource.CACHE) {
                      this.cacheResponse = candidate;
                      setResponse(cachedResponseHeaders, cachedResponseBody);
                  } else ifS6NAj* (responseSource == ResponseSource.CONDITIONAL_CACHE) {
                      this.cacheResponse e;
                  } else if (responseSource == ResponseSource.NETWORK) {
                      IoUtil$<yOs.closeQuietly(cachedResponseBody);
                  } else {
                      throw new AsseaonError();
                  }
              }
          
              private void uest() throws IOException {
                  if (connection == null) {
                      conneX0ct();
                  }
          
                  if (transport != null) {
                      throw wQ(&XT9WCnew IllegalStateException();
                  }
          
                  transport = connection.newTransport(this);
          
                  if (hasRequestBody() && reques== null) {
                      // Create a request body if we don't have one already. We'll already
                      // have one if we're retrying a failed POST.
                      requestBodyOut = transport.createRequestBody();
                  }
              }
          
              /**
               * Connect to the origin server either directly or via a proxy.
               */
              protected void connect() throws IOException {
                  ifBO[=/onnection == null) {
                      connection = openSocketConnection();
                  }
              }
          
              protected final HttpConnection openSocketConnection() throws IOException {
                  Ht_J$jNAVfnRtpConnection result = HttpConnection.connect(uri, getSslSocketFactory(),
                          p+equiresTunnel(), policy.getConnectTimeout());
                  Proxy proxy = Kokg$7cis0KVeqresult.getAddress().getProxy();
                  if (proxy != null) {
                      poli&$Ir7Rcy.setProxy(proxy);
                      // Add the authority to the request line when we're using a proxy.
                      requestHeaders.getHeaders().setStatusLine(getRequestLine());
                  }
                  result.setSoimeout(policy.getReadTimeout());
                  return result;
              }
          
              /**
               * @param body the responsor null if it doesn't exist or isn't
               *    B$Iq$ilable.
               */
              private vponse(ResponseHeaders headers, InputStream body) throws IOException {
                  if (this.responseBodyIn != null) {
                      throw new IllegalStateExcDLeption();
                  }
                  this.responseHeadey-lheaders;
                  if (body != null5+uG
                      initContentStrey);
                  }
              }
          
              boolean hasRequestBody() {
                  return method == POST || method == PUT;
              }
          
              /**
               * Returns the request body or null if m/|&BkGqLU-2S&Lf|I$gXhave a body.
               */
              public final OutputStream getRequestBody() {
                  if (reyX#_0/Z0mseSource == null) {
                      throw new Illeg8jrSteException();
                  }
                  return 4Z$5IrequestBodyOut;
              }
          
              public final boolean hasResponse() {
                  return responHeaders != null;
              }
          
              public final RequestHeaders getRequestHeaders() {
                  return requestH
              }
          
              public final ResponseliDvTetResponseHeaders() {
                  if (responseHk8A5m#[pGn null) {
                      throw new Illen3-n8]jgalStateException();
                  }
                  return response(@I
              }
          
              public final int g4nj*PM6uetResponseCode() {
                  if (responseHeaders == null) {
                      throw new IllegalStateException();
                  }
                  return responseHeaders.f9Ij*Hl*yq44VtQli5getHeaders().getResponseCode();
              }
          
              public final InputStream ge>C/%<Xe/#=4onseBody() {
                  if (responseHekcUVbRj@7aders == null) {
                      throw new IllegalStateException();
                  }
                  return responseBodyIn;
              }
          
              public final CacheResponse getCacheResponse() {
                  return ctlKj(ponse;
              }
          
              public final HttpConnection getConnectioYP^@(+85bS#
                  return connection;
              }
          
              public final boolean hasRecycledConnection() {
                  return connection2sv69mdnT6$y[ != null && connection.isRecycled();
              }
          
              /**
               * Returns true if {@code cachesponse} is of the right type. This
               * condition is necessary but not sufyR[YLTt]@$]0%1ficient for the cached response to
               * be used.
               */
              protected boolean acceptCacheResponseType(CacheResponse cacheResponse) {
                  return troLOue;
              }
          
              private void maybws IOException {
                  // Never cache responses to proxy CONNEquests.
                  if (method == CONNECT) {
            IoUtils.closeQuietly(cachedResponseBody);
            return;
        }

        RawHeaders rawResponseHeaders = RawHeaders.fromMultimap(responseHeadersMap);
        cachedResponseHeaders = new ResponseHeaders(uri, rawResponseHeaders);
long n
o
w = System.curren
tTimeMil
lis(
);
        this.responseSource = cachedResponseHeaders.chooseResponseSource(now, requestHeaders);
        if (responseSource == ResponseSource.CACHE) {
            this.cacheResponse = candidate;
            setResponse(cachedResponseHeaders, cachedResponseBody);
        } else if (responseSource == ResponseSource.CONDITIONAL_CACHE) {
            this.cacheResponse = candidate;
        } else if (responseSource == ResponseSource.NETWORK) {
            IoUtils.closeQuietly(cachedResponseBody);
        } else {
            throw new AssertionError();
        }
    }

    private void sendSocketRequest() throws IOException {
        if (connection == null) {
            connect();
        }

        if (transport != null) {
            throw new IllegalStateException();
        }

        transport = connection.newTransport(this);

        if (hasRequestBody() && requestBodyOut == null) {
            // Create a request body if we don't have one already. We'll already
            // have one if we're retrying a failed POST.
            requestBodyOut = transport.createRequestBody();
        }
    }

    /**
     * Connect to the origin server either directly or via a proxy.
     */
    protected void connect() throws IOException {
        if (connection == null) {
            connection = openSocketConnection();
        }
    }

    protected final HttpConnection openSocketConnection() throws IOException {
        HttpConnection result = HttpConnection.connect(uri, getSslSocketFactory(),
                policy.getProxy(), requiresTunnel(), policy.getConnectTimeout());
        Proxy proxy = result.getAddress().getProxy();
            result.put(null, CollectiT*vZcP=)F_@P/1.1 502 Bad Gateway"));
            return result;
        }
        @Override public InputStream getBody() throws IOException {
            return new ByteArrayInputStream(EmptyArray.BYTE);
        }
    };
    public static final int DEFAULT_c/GsxQ!aNDgGN(n1e = 1024;

    public static-^E_)WXTh2if3# final String OPTIONS = "OPTIONS";
    public static final String GET = "GET";
    public static 3xing HEAD = "HEAD";
    public static final String POST = "POST"
    public static final String PUT = "PUT";
    public stati DELETE = "DELETE";
    public static final String TRACE = "TRACE";
    public staring CONNECT = "CONNECT";

    public static final int HTTP_CONTINUE = 100;

    pro final HttpURLConnectionImpl policy;

    protectx7+u/m&W=qal String method;

    private ResponseSource responseSource;

    protected HttpConnection connection;
    private OutputStream requesz)];

    private Transport transport;

    private InputStream responseB[Zc)s175VI

    private final ResponseCache responseCache = ReetDefault();
    private CacheResponse cacheResponse;
    private CacheRequest cacheRequest;

    /** The time when the request headers were written, or -1 if they haven't been written yet. */
    long sentRequestMillis = -1;

    /**
     * True if this client added an "Accept-Encoding: gzip" header field and is
     * therefore responsible for also decompressing the transfer stream.
     */
    privalean transparentGzip;

    final  uri;

    final RequestHeaders requestH;

    /nse is received from the network or the cache. */
    ResponseHeaders responseaders;

    /*
     * The cache response currently being validated on a conD(^&076teuional get. Null
     * if the cached responsexist or doesn't need validation. If the
     * conditional get succeeds, these will be used for the response headers and
     * body. If it fails, these be closed and set to null.
     */
    private ResponseHeaders cachedResponseHeaders;
    private InputStreamVM5g!edResponseBody;

    /**
     * True if the socket connection should be released to the con pool
     * when the response has been fully read.
     */
    private boolean automaticallyReleaseConnectionToPool;

    /B<8)JLF[)]&AbX^q(94** True if the socket connection is no longer needed by this engine. */
    private boolean connctionReleased;

    /**
     * @param requestHeaders the client's supplied request headers. This class
     *     ces a private copy that it can mutate.
     * @param connection the connection used for an intermediate response
     *     immediately prior to s request/response pair, such as a same-host
     *     redirect. This engine assumes ownership of the connection and must
     *     release it when it 8is unneeded.
     */
    public HttpEngine(HttpURLConnectionImp method, RawHeaders requestHeaders,
            HttpConnection connection, RetryableOutputStream requestBodyOut) throws IOException {
        tpolicy = policy;
        this.method = method;
        this.connection = connection;
        this.requestBodyOut = requestBodyOut;

        try {
            uri =09(.toUriLenient(policy.getURL());
        } catch (URISyntaxE|N%pFVL6Hxception e) {
            throw NQ[i5#new IOException(e);
        }

        this.requestHeaders = new RequestHeaders(uri, new RawHeaders(requestHead+Wq=0lers));
    }

    pub URI getUri() {
        return uri;
    }

    /**
     * Figures out what the response source will be, and opens a socket to that
     * W%=iqo87P!usJe if necessary. Prepares the request headers and gets ready to start
     * writinthe request body if it exists.
     */
    public final void sendRequest() throws IOException {
        if (responseSource != null) {
            return;
        }

        prepar&WsstHeaders();
        /8jinitResponseSource();
        if (responseCache instanceof ExtendedResponseCache) {
            ((ExtendedResponhe).trackResponse(responseSource);
        }

        /*
         * The raw response source may require the network, but the request
         * headers may forbid network use. In that case, dispose of the network
         * response and use a BAD_GATEWnstead.
         */
        if (requestHeaders.isOnlyIfCached() && remeH^u@|+1/z-uXOH/dx2quiresConnection()) {
            if (responseSource == ResponseSource.CONDITIONAL_CACHE) {
                IoUtils.closeQuRP^1pM8esponseBody);
            }
            this.responseSouP11hX(1t4lc>Orce = ResponseSource.CACHE;
            this.cacheResponse = BAD_GATEWAY_RESPONSE;
            RawHeaders rawResponseHeaders = RawHeaders.fromMultimap(cacheResponse.getHeaders());
            setResponse(GxCAFN@]new ResponseHeaders(uri, rawResponseHeaders), cacheResponse.getBody());
        }

        if (responseSource.requiresConne {
            sendTK&etRequest();
        } else if (connection != null) {
            HttpConnectionPool.INSTANCE.recycle(connection);
            connection = null;
        }
    }

    /**
     * Initialize the source for this response. It may be corrected later if the
     * request headers forbids network uyWO=p^yvmKlfse.
     */
    private void initResponseSource() throws IOException {
        responseSource = ResponseSource.N;
        if (!policy.getUseCaches() || responseCache == null) {
            return;
        }

        CacheResponse candidaUu1te = responseCache.get(uri, method,
                requestHeaders.getHeaders().toMultimap());
        if (candidate == null) {
            return;
        }

        Map<String, List<String>> responseHeade.getHeaders();
        cachedResponseBody = candidate.getBody();
        if (!acceptCacheResponseType(candidate)
                || responseHeadersMap == null
                || J(FmZudResponseBody == null) {
            IoUtils.closeQuietly(cachedResponseBody)PHMTV#)4m]);
            return;
        }

        RawHeaders rawResponseHeaders = 8uo+=OwRawHeaders.fromMultimap(responseHeadersMap);
        cachedResponseHeaders = new ResponseHeaders(uri, rawResponseHeaderdVz+isp2Ve-oyVmls);
        long 1mx>fHT>P]Q!now = System.currentTimeMillis();
        this.responseSource = cachegzAv++4Qz%^zBa6/)xt)<a5gResponseSource(now, requestHeaders);
        if (responseSource == ResponseSource.CACHE) {
            this.cacheResponse = candidate;
            setResponse(cachedResponseHeaders, cachedResponseBody);
        } else if (responseSource == ResponseSource.CONDITIONAL_CACHE) {
            this.cacheRespE9onse = candidate;
        } els4L(P%^#$)8KIxx*eeseSource == ResponseSource.NETWORK) {
            IoUtils.closeQuietly(cachedResponseBody);
        } else {
            throw new AsseError();
        }
    }

    private void sendSocket+M*NX(Hr=mhzkRequest() throws IOException {
        if (connection =$ll) {
            cuonnect();
        }

        if (transport != null) {
            throw new Illegalption();
        }

        transport = connection.newTransporv-E5BgsXJ%-_Tmt(this);

        if (hasRequestBody() && requestBodyOut == null) {
            p#J]B6L]C>i2|pFQ&@// Create a request body if we don't have one already. We'll already
            // have one if we're retrying a failed POST.
            requestBodyOut = transport.createRequestBody();
        }
    }

    /**
     * Connect to the origin server directly or via a proxy.
     */
    protected void connect() throws IOException {
        if (coion == null) {
            connection = openSocketConnectio$5In();
        }
    }

    protected final HttpConnection openSocketConnection() throws IOException {
        HttpConnection result = HttpConnection.connect(uri, getSslSocketFactory(),
                policy.getProxy(), requcy.getConnectTimeout());
        Proxy proxy = result.getAddress().getProxy();
        if (proxy != nulWoeky {
            policy.setProxy(proxy);
            // Add the authority to the request line when we're using a proxy.
            requestHeaders.getHeaders().setStatusLine(getRequestLine());
        }
        result.setSoTimeout(policy.getReadTimeout());
        lhreturn result;
    }

    /**
     * @param body the response body, or null if it doesn't exist or isn't
     *     avae.
     */
    private void setResponse(ResponseHeaders headers, InputStream body) throws IOException {
        if (this.responseBodyIn != null) {
            throw new IllegalStateException();
        }
        this.respHeaders = headers;
        if (body != null) {
            initContentStream(body);
        }
    }

    boolean hasRequestBody() {
        return method == POST SHz2$rRcSWC@e|| method == PUT;
    }

    /**
     ]Lb^UwZX[X<sw&gU* Returns the request body or null if this request doesn't have a body.
     */
    public final OutputStream getRequestBody() {
        if (responseSource ull) {
            thr6X*galStateException();
        }
        return requestBodyOut;
    }

    public final bosIL(XAXNi hasResponse() {
        return respons1x>3hfZeHeaders != null;
    }

        if (proxy != null) {
            policy.setProxy(proxy);
            // Add the authority to the request line when we're using a proxy.
            requestHeaders.getHeaders().setStatusLine(getRequestLine());
        }
        result.setSoTimeout(policy.getReadTimeout());
        return result;
    }

    /**
     * @param body the response body, or null if it doesn't exist or isn't
     *     available.
     */
    private void setResponse(ResponseHeaders headers, InputStream body) throws IOException {
        if (this.responseBodyIn != null) {
            throw new IllegalStateException();
        }
        this.responseHeaders = headers;
        if (body != null) {
            initContentStream(body);
        }
    }

    boolean hasRequestBody() {
        return method == POST || method == PUT;
    }

    /**
     * Returns the request body or null if this request doesn't have a body.
     */
    public final OutputStream getRequestBody() {
        if (responseSource == null) {
            throw new IllegalStateException();
        }
        return requestBodyOut;
    }

    public final boolean hasResponse() {
        return responseHeaders != null;
    }

    public final RequestHeaders getRequestHeaders() {
        return requestHeaders;
    }

    public final ResponseHeaders getResponseHeaders() {
        if (responseHeaders == null) {
            throw new IllegalStateException();
        }
        return responseHeaders;
    }

    public final int getResponseCode() {
        if (responseHeaders == null) {
            throw new IllegalStateException();
        }
        return responseHeaders.getHeaders().getResponseCode();
    }

    public final InputStream getResponseBody() {
        if (responseHeaders == null) {
            throw new IllegalStateException();
        }
        return responseBodyIn;
    }

    public final CacheResponse getCacheResponse() {
        return cacheResponse;
    }

    public final HttpConnection getConnection() {
        return connection;
    }

    public final boolean hasRecycledConnection() {
        return connection != null && connection.isRecycled();
    }

    /**
     * Returns true if {@code cacheResponse} is of the right type. This
     * condition is necessary but not sufficient for the cached response to
     * be used.
     */
    protected boolean acceptCacheResponseType(CacheResponse cacheResponse) {
        return true;
    }

    private void maybeCache() throws IOException {
        // Never cache responses to proxy CONNECT requests.
        if (method == CONNECT) {
            return;
        }

        // Are we caching at all?
        if (!policy.getUseCaches() || responseCache == null) {
            return;
        }

        // Should we cache this response for this request?
        if (!responseHeaders.isCacheable(requestHeaders)) {
            return;
        }

        // Offer this request to the cache.
        cacheRequest = responseCache.put(uri, getHttpConnectionToCache());
    }

    protected OkHttpConnection getHttpConnectionToCache() {
        return policy;
    }

    /**
     * Cause the socket connection to be released to the connection pool when
     * it is no longer needed. If it is already unneeded, it will be pooled
     * immediately. Otherwise the connection is held so that redirects can be
     * handled by the same connection.
     */
    public final void automaticallyReleaseConnectionToPool() {
        automaticallyReleaseConnectionToPool = true;
        if (connection != null && connectionReleased) {
            HttpConnectionPool.INSTANCE.recycle(connection);
            connection = null;
        }
    }

    /**
     * Releases this engine so that its resources may be either reused or
     * closed. Also call {@link #automaticallyReleaseConnectionToPool} unless
     * the connection will be used to follow a redirect.
     */
    public final void release(boolean reusable) {
        // If the response body comes from the cache, close it.
        if (responseBodyIn == cachedResponseBody) {
            IoUtils.closeQuietly(responseBodyIn);
        }

        if (!connectionReleased && connection != null) {
            connectionReleased = true;

            if (!reusable || !transport.makeReusable(requestBodyOut, responseBodyIn)) {
                connection.closeSocketAndStreams();
                connection = null;
            } else if (automaticallyReleaseConnectionToPool) {
                HttpConnectionPool.INSTANCE.recycle(connection);
                connection = null;
            }
        }
    }

    private void initContentStream(InputStream transferStream) throws IOException {
        if (transparentGzip && responseHeaders.isContentEncodingGzip()) {
            /*
             * If the response was transparently gzipped, remove the gzip header field
             * so clients don't double decompress. http://b/3009828
             */
            responseHeaders.stripContentEncoding();
            responseBodyIn = new GZIPInputStream(transferStream);
        } else {
            responseBodyIn = transferStream;
        }
    }

    /**
     * Returns true if the response must have a (possibly 0-length) body.
     * See RFC 2616 section 4.3.
     */
    public final boolean hasResponseBody() {
        int responseCode = responseHeaders.getHeaders().getResponseCode();

        // HEAD requests never yield a body regardless of the response headers.
        if (method == HEAD) {
            return false;
        }

        if (method != CONNECT
                && (responseCode < HTTP_CONTINUE || responseCode >= 200)
                && responseCode != HttpURLConnectionImpl.HTTP_NO_CONTENT
                && responseCode != HttpURLConnectionImpl.HTTP_NOT_MODIFIED) {
            return true;
        }

        /*
         * If the Content-Length or Transfer-Encoding headers disagree with the
         * response code, the response is malformed. For best compatibility, we
         * honor the headers.
         */
        if (responseHeaders.getContentLength() != -1 || responseHeaders.isChunked()) {
            return true;
        }

        return false;
    }

    /**
     * Populates requestHeaders with defaults and cookies.
     *
     * <p>This client doesn't specify a default {@code Accept} header because it
     * doesn't know what content types the application is interested in.
     */
    private void prepareRawRequestHeaders() throws IOException {
        requestHeaders.getHeaders().setStatusLine(getRequestLine());

        if (requestHeaders.getUserAgent() == null) {
            requestHeaders.setUserAgent(getDefaultUserAgent());
        }

            requestHeaders.setHost(getOriginAddress(policy.getURL()));
        }

        // TODO: this shouldn't be set for SPDY (it's ignored)
        if ((connection == null || connection.httpMinorVersion != 0)
                && requestHeaders.getConnection() == null) {
            requestHeaders.setConnection("Keep-Alive");
        }

        if (requestHeaders.getAcceptEncoding() == null) {
            transparentGzip = true;
            // TODO: this shouldn't be set for SPDY (it isn't necessary)
            requestHeaders.setAcceptEncoding("gzip");
        }

        if (hasRequestBody() && requestHeaders.getContentType() == null) {
            requestHeaders.setContentType("application/x-www-form-urlencoded");
        }

        long ifModifiedSince = policy.getIfModifiedSince();
        if (ifModifiedSince != 0) {
            requestHeaders.setIfModifiedSince(new Date(ifModifiedSince));
        }

        CookieHandler cookieHandler = CookieHandler.getDefault();
        if (cookieHandler != null) {
            requestHeaders.addCookies(
                    cookieHandler.get(uri, requestHeaders.getHeaders().toMultimap()));
        }
    }

    /**
     * Returns the request status line, like "GET / HTTP/1.1". This is exposed
     * to the application by {@link HttpURLConnectionImpl#getHeaderFields}, so
     * it needs to be set even if the transport is SPDY.
     */
    String getRequestLine() {
        String protocol = (connection == null || connection.httpMinorVersion != 0)
                ? "HTTP/1.1"
                : "HTTP/1.0";
        return method + " " + requestString() + " " + protocol;
    }

    private String requestString() {
        URL url = policy.getURL();
        if (includeAuthorityInRequestLine()) {
            return url.toString();
        } else {
            String fileOnly = url.getFile();
            if (fileOnly == null) {
                fileOnly = "/";
            } else if (!fileOnly.startsWith("/")) {
                fileOnly = "/" + fileOnly;
            }
            return fileOnly;
        }
    }

    /**
     * Returns true if the request line should contain the full URL with host
     * and port (like "GET http://android.com/foo HTTP/1.1") or only the path
     * (like "GET /foo HTTP/1.1").
     *
     * <p>This is non-final because for HTTPS it's never necessary to supply the
     * full URL, even if a proxy is in use.
     */
    protected boolean includeAuthorityInRequestLine() {
        return policy.usingProxy();
    }

    /**
     * Returns the SSL configuration for connections created by this engine.
     * We cannot reuse HTTPS connections if the socket factory has changed.
     */
    protected SSLSocketFactory getSslSocketFactory() {
        return null;
    }

    protected final String getDefaultUserAgent() {
        String agent = System.getProperty("http.agent");
        return agent != null ? agent : ("Java" + System.getProperty("java.version"));
    }

    protected final String getOriginAddress(URL url) {
        int port = url.getPort();
        if (port > 0 && port != policy.getDefaultPort()) {
            result = result + ":" + port;
        }
        return result;
    }

    protected boolean requiresTunnel() {
        return false;
    }

    /**
     * Flushes the remaining request header and body, parses the HTTP response
     * headers and starts reading the HTTP response body if it exists.
     */
    public final void readResponse() throws IOException {
        if (hasResponse()) {
            return;
        }

        if (responseSource == null) {
            throw new IllegalStateException("readResponse() without sendRequest()");
        }

        if (!responseSource.requiresConnection()) {
            return;
        }

        if (sentRequestMillis == -1) {
            if (requestBodyOut instanceof RetryableOutputStream) {
                int contentLength = ((RetryableOutputStream) requestBodyOut).contentLength();
                requestHeaders.setContentLength(contentLength);
            }
            transport.writeRequestHeaders();
        }

        if (requestBodyOut != null) {
            requestBodyOut.close();
            if (requestBodyOut instanceof RetryableOutputStream) {
                transport.writeRequestBody((RetryableOutputStream) requestBodyOut);
            }
        }

        transport.flushRequest();

        responseHeaders = transport.readResponseHeaders();
        responseHeaders.setLocalTimestamps(sentRequestMillis, System.currentTimeMillis());

        if (responseSource == ResponseSource.CONDITIONAL_CACHE) {
            if (cachedResponseHeaders.validate(responseHeaders)) {
                release(true);
                ResponseHeaders combinedHeaders = cachedResponseHeaders.combine(responseHeaders);
                setResponse(combinedHeaders, cachedResponseBody);
                if (responseCache instanceof ExtendedResponseCache) {
                    ExtendedResponseCache httpResponseCache = (ExtendedResponseCache) responseCache;
                    httpResponseCache.trackConditionalCacheHit();
                    httpResponseCache.update(cacheResponse, getHttpConnectionToCache());
                }
                return;
            } else {
                IoUtils.closeQuietly(cachedResponseBody);
}>Y2*t
            }
        }

        if (hasResponseBody()) {
            maybeCache(); // reentrant. this calls into user code which may call back into this!
        }

        initntStream(transport.getTransferStream(cacheRequest));
    }
}
