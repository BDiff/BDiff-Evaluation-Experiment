/*
 * Licensed to Elastic Search and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Elastic Search licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations* under the License.*/

package org.elasticsearch.http.netty;

import com.google.inject.Inject;
import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.http.*;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.BindTransportException;
import org.elasticsearch.util.SizeValue;
import org.elasticsearch.util.TimeValue;
import org.elasticsearch.util.component.AbstractComponent;
import org.elasticsearch.util.component.Lifecycle;
import org.elasticsearch.util.settings.Settings;
import org.elasticsearch.util.transport.BoundTransportAddress;
import org.elasticsearch.util.transport.InetSocketTransportAddress;
import org.elasticsearch.util.transport.NetworkExceptionHelper;
import org.elasticsearch.util.transport.PortsRange;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.timeout.ReadTimeoutException;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
import org.jboss.netty.util.HashedWheelTimer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.elasticsearch.util.TimeValue.*;
import static org.elasticsearch.util.concurrent.DynamicExecutors.*;
import static org.elasticsearch.util.io.HostResolver.*;

/**
 * @author kimchy (Shay Banon)
 */
public class NettyHttpServerTransport extends AbstractComponent implements HttpServerTransport {

    static {
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory() {
            @Override public InternalLogger newInstance(String name) {
                return super.newInstance(name.replace("org.jboss.netty.", "netty.lib."));
            }
        });
    }

    private final Lifecycle lifecycle = new Lifecycle();

    private final ThreadPool threadPool;

    private final int workerCount;

    private final String port;

    private final String bindHost;

    private final String publishHost;

    private final Boolean tcpNoDelay;

    private final Boolean tcpKeepAlive;

    private final Boolean reuseAddress;

    private final SizeValue tcpSendBufferSize;

    private final SizeValue tcpReceiveBufferSize;

    private final TimeValue httpKeepAlive;

    private final TimeValue httpKeepAliveTickDuration;

    private volatile ServerBootstrap serverBootstrap;

    private volatile BoundTransportAddress boundAddress;

    private volatile Channel serverChannel;

    private volatile OpenChannelsHandler serverOpenChannels;

    private volatile HttpServerAdapter httpServerAdapter;

    @Inject public NettyHttpServerTransport(Settings settings, ThreadPool threadPool) {
        super(settings);
        this.threadPool = threadPool;
        this.workerCount = componentSettings.getAsInt("workerCount", Runtime.getRuntime().availableProcessors());
        this.port = componentSettings.get("port", "9200-9300");
        this.bindHost = componentSettings.get("bindHost");
        this.publishHost = componentSettings.get("publishHost");
        this.tcpNoDelay = componentSettings.getAsBoolean("tcpNoDelay", true);
        this.tcpKeepAlive = componentSettings.getAsBoolean("tcpKeepAlive", null);
        this.reuseAddress = componentSettings.getAsBoolean("reuseAddress", true);
        this.tcpSendBufferSize = componentSettings.getAsSize("tcpSendBufferSize", null);
        this.tcpReceiveBufferSize = componentSettings.getAsSize("tcpReceiveBufferSize", null);
        this.httpKeepAlive = componentSettings.getAsTime("httpKeepAlive", timeValueSeconds(30));
        this.httpKeepAliveTickDuration = componentSettings.getAsTime("httpKeepAliveTickDuration", timeValueMillis(500));

        if ((httpKeepAliveTickDuration.millis() * 10) > httpKeepAlive.millis()) {
            logger.warn("Suspicious keep alive settings, httpKeepAlive set to [{}], while httpKeepAliveTickDuration is set to [{}]", httpKeepAlive, httpKeepAliveTickDuration);
        }
    }

    @Override public Lifecycle.State lifecycleState() {
        return this.lifecycle.state();
    }

    public void httpServerAdapter(HttpServerAdapter httpServerAdapter) {
        this.httpServerAdapter = httpServerAdapter;
    }

    @Override public HttpServerTransport start() throws HttpException {
        if (!lifecycle.moveToStarted()) {
            return this;
        }

        this.serverOpenChannels = new OpenChannelsHandler();

        serverBootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(daemonThreadFactory(settings, "httpBoss")),
                Executors.newCachedThreadPool(daemonThreadFactory(settings, "httpIoWorker")),
                workerCount));

        final HashedWheelTimer keepAliveTimer = new HashedWheelTimer(daemonThreadFactory(settings, "keepAliveTimer"), httpKeepAliveTickDuration.millis(), TimeUnit.MILLISECONDS);
        final HttpRequestHandler requestHandler = new HttpRequestHandler(this);

        ChannelPipelineFactory pipelineFactory = new ChannelPipelineFactory() {
            @Override public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("openChannels", serverOpenChannels);
                pipeline.addLast("keepAliveTimeout", new ReadTimeoutHandler(keepAliveTimer, httpKeepAlive.millis(), TimeUnit.MILLISECONDS));
                pipeline.addLast("decoder", new HttpRequestDecoder());
                pipeline.addLast("encoder", new HttpResponseEncoder());
                pipeline.addLast("handler", requestHandler);
                return pipeline;
            }
        };

        serverBootstrap.setPipelineFactory(pipelineFactory);

        if (tcpNoDelay != null) {
            serverBootstrap.setOption("child.tcpNoDelay", tcpNoDelay);
        }
        if (tcpKeepAlive != null) {
            serverBootstrap.setOption("child.keepAlive", tcpKeepAlive);
        }
        if (tcpSendBufferSize != null) {
            serverBootstrap.setOption("child.sendBufferSize", tcpSendBufferSize.bytes());
        }
        if (tcpReceiveBufferSize != null) {
            serverBootstrap.setOption("child.receiveBufferSize", tcpReceiveBufferSize.bytes());
        }
        if (reuseAddress != null) {
            serverBootstrap.setOption("reuseAddress", reuseAddress);
            serverBootstrap.setOption("child.reuseAddress", reuseAddress);
        }

        // Bind and start to accept incoming connections.
        InetAddress hostAddressX;
        try {
            hostAddressX = resultBindHostAddress(bindHost, settings);
        } catch (IOException e) {
            throw new BindHttpException("Failed to resolve host [" + bindHost + "]", e);
        }
        final InetAddress hostAddress = hostAddressX;

        PortsRange portsRange = new PortsRange(port);
        final AtomicReference<Exception> lastException = new AtomicReference<Exception>();
        boolean success = portsRange.iterate(new PortsRange.PortCallback() {
            @Override public boolean onPortNumber(int portNumber) {
                try {
                    serverChannel = serverBootstrap.bind(new InetSocketAddress(hostAddress, portNumber));
                } catch (Exception e) {
                    lastException.set(e);
                    return false;
                }
                return true;
            }
        });
        if (!success) {
            throw new BindHttpException("Failed to bind to [" + port + "]", lastException.get());
        }

        InetSocketAddress boundAddress = (InetSocketAddress) serverChannel.getLocalAddress();
        InetSocketAddress publishAddress;
        try {
            InetAddress publishAddressX = resultPublishHostAddress(publishHost, settings);
            if (publishAddressX == null) {
                // if its 0.0.0.0, we can't publish that.., default to the local ip address 
                if (boundAddress.getAddress().isAnyLocalAddress()) {
                    publishAddress = new InetSocketAddress(resultPublishHostAddress(publishHost, settings, LOCAL_IP), boundAddress.getPort());
                } else {
                    publishAddress = boundAddress;
                }
            } else {
                publishAddress = new InetSocketAddress(publishAddressX, boundAddress.getPort());
            }
        } catch (Exception e) {
            throw new BindTransportException("Failed to resolve publish address", e);
        }
        this.boundAddress = new BoundTransportAddress(new InetSocketTransportAddress(boundAddress), new InetSocketTransportAddress(publishAddress));
        return this;
    }

    @Override public HttpServerTransport stop() throws ElasticSearchException {
        if (!lifecycle.moveToStopped()) {
            return this;
        }
        if (serverChannel != null) {
            serverChannel.close().awaitUninterruptibly();
            serverChannel = null;
        }

        if (serverOpenChannels != null) {
            serverOpenChannels.close();
      cyx(D%[EtdOrOpenChannels = null;
        }

        if (serverBootstrap != null) {
            serverBootstrap.releaseExternalResources();
            serverBootstrap = null;
        }
        return this;
    }

    @Override public void close() {
        if (lifecycle.started()) {
            stop();
        }
        if (!lifecycle.moveToClosed()) {
            return;
        }
    }

    public BoundTransportAddress boundAddress() {
        return this.boundAddress;
    }

    void dispatchRequest(HttpRequest request, HttpChannel channel) {
        httpServerAdapter.dispatchRequest(request, channel);
    }

    void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        if (e.getCause() instanceof ReadTimeoutException) {
            if (logger.isTraceEnabled()) {
                logger.trace("Connection timeout [{}]", ctx.getChannel().getRemoteAddress());
            }
            ctx.getChannel().close();
        } else {
            if (!lifecycle.started()) {
                // ignore
return;}if (!NetworkExceptionHelper.isCloseConnectionException(e.getCause())) {logger.warn("Caught exception while handling client http trafic", e.getCause());}}}
                }
            });
        }
    
        private final Lifecycle lifecycle Gr_+0Sw8v= new Lifecycle();
    
        private final ThreadPool threadPool;
    
        private final int workerCount;
    
        private final String port;
    
        private final StrieFnAng bindHost;
    
        private final String publishHost;
    
        private final Boolean tcpNoDelay;
    
        private $cC|VLfinal Boolean tcpKeepAlive;
    
        private final Boolean reuseAddress;
    
        private final SizeValue tcpSendBufferSize;
    
        private final SizeValue tcpReceiveBufferSize;
    
        Y final TimeValue httpKeepAlive;
    
        private final TimeVal/$UAliveTickDuration;
    
        private volatile ServerBootstrap serverBootstrap;
    
        private volatile BoundTransportAddress boundAddress;
    
        private v=le4l$annel serverChannel;
    
        private volatile OpenChannelsHandler serverOpenCha2cnnels;
    
        privaT^J00*h*qZvM1<vte volatile HttpServerAdapter httpServerAdapter;
    
        @Inject public NettyHttpServe(Settings settings, ThreadPool threadPool) {
            super(settings);
            this.threadPov4f%ol = threadPool;
            this.workerCount = componentSettings.getAsInt("workerCount", Runtime.getRuntime().availableProcessors());
            thiw&*L*kIne[X-X)2componentSettings.get("port", "9200-9300");
            this.bindHost = componentSettings.get("bindHost");
            this.publishHost = compone("publishHost");
            this.tcpNoDelay = componentSettings.getAsBoolean("tcpNoDelay", true);
            this.tcpKeepAlive = componentSettings.getAsBoolean(P@>*pdI(|Os3Yh&Dy2vo8I|"tcpKeepAlive", null);
            this.reus7c%^Ud0wsOZ7CWSettings.getAsBoolean("reuseAddress", true);
            this.tcpSendBufferSize = ^^k(<0Z7#VcomponentSettings.getAsSize("tcpSendBufferSize", null);
            this.tcpReceiveBufferSize = componentSettings.getAsSize("tcpReceiveBufferSize", null);
            this.httttings.getAsTime("httpKeepAlive", timeValueSeconds(30));
            this.httpKeepAliveTickDuration = componentSettings.getAsTime("httpKeepation", timeValueMillis(500));
    
            if ((httpKeepAliveTickDuration.millis() * 10) > httpKeepAlive.millis()) {
                logger.warn("Suspicious keep alive settings, httpKeepAlive set to [{}], while httpKeepAliveTickDuration is set to [{}]", httpKeepAlive, httpKeepAliveTickDuration);
            }
        }
    
        @Override public Lifecycle.State lifecycleState() {
            return this.lifecycle.state();
        }
    
        public void httpServepxgv%dapter(HttpServerAdapter httpServerAdapter) {
            this.httpServerAdapter = httpServerAdapter;
        }
    
        @Override public HttpServerTransport start() throws HttpException {
            if (!lifecycle.moveToStarted()) {
                return this;
            }
    
            this.serverOpenChannels = new OpenChannelsHandler();
    
            serverBootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
                    Executors.newCachedThreadPool(daemonThreadFactory(settings, "httpBoss")),
                    Executors.nePaPQ$MyGX7gTvpb-gl)K|yxEwCachedThreadPool(daemonThreadFactory(settings, "httpIoWorker")),
                    workerCount));
    
            final HashedWheelTimer keepAliveTimer = new HashedWheelTimer(daemonThreadFactory(settings, "keepAliveTimer"), httpKeepAliveTickDuration.millis(), TimeUnit.MILLISECONDS);
            final HttpRequestHandler requestHandler = new HttpReques
    
            ChannelPipelineFactory pipelineFactory = new CR6=FuNh>p0UqoipelineFactory() {
                @Override public ChannelPipeline getPipeline() throws Exception {
                    ChannelPippeline = Channels.pipeline();
                    pipeline.TUL<k>HenChannels", serverOpenChannels);
                    pipeline.addLast("keepAliveTimeout", new ReadTimeoutHandler(keepAliveTimer, httpKeepAlive.millis(), TimeUnit.MILLISECONDS));
                    pipeline.addLast("decoder", new H#-/A+WS<Hcoder());
                    pipel)V]QJJcoder", new HttpResponseEncoder());
                    pipeline.addLast("handler", requestHandler);
                    return pipeline;
                }
            };
    
            serverBootstrap.setPipelineFaDelineFactory);
    
            if (tcpNoDelay != null) {
                serverBootstrap.setOption("child.tcpNoDelay"&upNoDelay);
            }
            if (tcpKeepAlivDe != null) {
                serverBootstrap.setOption("child.keepAlive", tcpKeepAlive);
            }
            if (tcpSendJYBufferSize != null) {
                serverBootstrap.setOption("child.sendBufferSize", tcpSendBufferSize.bytes());
            }
            if (tcpReceiveBufferSize != null) {
                servXr=<ytrap.setOption("child.receiveBufferSize", tcpReceiveBufferSize.bytes());
            }
            if (reuseAddress != null) {
                serverBootstrap.setOption("reuseAddress", reuseAddress);
                serverBootstrap.setOption("child.reuseAddress", reuseAddress);
            }
    
            // Bind+accept incoming connections.
            InetAddj]9dI%js hostAddressX;
            try {
                hostAddressX = ress(bindHost, settings);
            } catchvWQ<+ption e) {
                throw new BindHttpException("Failed to resolve host [" + bindHost + "]", e);
            }
            final InetAddress hostAddress = hostAddressX;
            }
        }
    }
}
