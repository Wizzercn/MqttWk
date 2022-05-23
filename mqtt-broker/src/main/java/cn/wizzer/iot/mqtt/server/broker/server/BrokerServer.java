/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.wizzer.iot.mqtt.server.broker.server;

import cn.wizzer.iot.mqtt.server.broker.codec.MqttWebSocketCodec;
import cn.wizzer.iot.mqtt.server.broker.config.BrokerProperties;
import cn.wizzer.iot.mqtt.server.broker.handler.BrokerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.nutz.boot.starter.ServerFace;
import org.nutz.ioc.Ioc;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

/**
 * 启动Broker
 */
@IocBean
public class BrokerServer implements ServerFace {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrokerServer.class);
    @Inject
    private BrokerProperties brokerProperties;
    @Inject("refer:$ioc")
    private Ioc ioc;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private SslContext sslContext;
    private Channel channel;
    private Channel websocketChannel;
    private ChannelGroup channelGroup;
    private Map<String, ChannelId> channelIdMap;

    public void start() throws Exception {
        LOGGER.info("Initializing {} MQTT Broker ...", "[" + brokerProperties.getId() + "]");
        channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        channelIdMap = new HashMap<>();
        bossGroup = brokerProperties.getUseEpoll() ? new EpollEventLoopGroup(brokerProperties.getBossGroup_nThreads()) : new NioEventLoopGroup(brokerProperties.getBossGroup_nThreads());
        workerGroup = brokerProperties.getUseEpoll() ? new EpollEventLoopGroup(brokerProperties.getWorkerGroup_nThreads()) : new NioEventLoopGroup(brokerProperties.getWorkerGroup_nThreads());
        if (brokerProperties.getSslEnabled()) {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("keystore/server.pfx");
            keyStore.load(inputStream, brokerProperties.getSslPassword().toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, brokerProperties.getSslPassword().toCharArray());
            sslContext = SslContextBuilder.forServer(kmf).build();
        }
        mqttServer();
        if (brokerProperties.getWebsocketEnabled()) {
            websocketServer();
            LOGGER.info("MQTT Broker {} is up and running. Open Port: {} WebSocketPort: {}", "[" + brokerProperties.getId() + "]", brokerProperties.getPort(), brokerProperties.getWebsocketPort());
        } else {
            LOGGER.info("MQTT Broker {} is up and running. Open Port: {} ", "[" + brokerProperties.getId() + "]", brokerProperties.getPort());
        }
    }

    public void stop() {
        LOGGER.info("Shutdown {} MQTT Broker ...", "[" + brokerProperties.getId() + "]");
        channelGroup = null;
        channelIdMap = null;
        bossGroup.shutdownGracefully();
        bossGroup = null;
        workerGroup.shutdownGracefully();
        workerGroup = null;
        channel.closeFuture().syncUninterruptibly();
        channel = null;
        websocketChannel.closeFuture().syncUninterruptibly();
        websocketChannel = null;
        LOGGER.info("MQTT Broker {} shutdown finish.", "[" + brokerProperties.getId() + "]");
    }

    @IocBean(name = "channelGroup")
    public ChannelGroup getChannels() {
        return this.channelGroup;
    }

    @IocBean(name = "channelIdMap")
    public Map<String, ChannelId> getChannelIdMap() {
        return this.channelIdMap;
    }

    private void mqttServer() throws Exception {
        ServerBootstrap sb = new ServerBootstrap();
        sb.group(bossGroup, workerGroup)
                .channel(brokerProperties.getUseEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                // handler在初始化时就会执行
                .handler(new LoggingHandler(LogLevel.INFO))
                // childHandler会在客户端成功connect后才执行
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline channelPipeline = socketChannel.pipeline();
                        // Netty提供的心跳检测
                        channelPipeline.addFirst("idle", new IdleStateHandler(0, 0, brokerProperties.getKeepAlive()));
                        // Netty提供的SSL处理
                        if (brokerProperties.getSslEnabled()) {
                            SSLEngine sslEngine = sslContext.newEngine(socketChannel.alloc());
                            sslEngine.setUseClientMode(false);        // 服务端模式
                            sslEngine.setNeedClientAuth(false);        // 不需要验证客户端
                            channelPipeline.addLast("ssl", new SslHandler(sslEngine));
                        }
                        channelPipeline.addLast("decoder", new MqttDecoder());
                        channelPipeline.addLast("encoder", MqttEncoder.INSTANCE);
                        channelPipeline.addLast("broker", ioc.get(BrokerHandler.class));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, brokerProperties.getSoBacklog())
                .childOption(ChannelOption.SO_KEEPALIVE, brokerProperties.getSoKeepAlive());
        if (Strings.isNotBlank(brokerProperties.getHost())) {
            channel = sb.bind(brokerProperties.getHost(), brokerProperties.getPort()).sync().channel();
        } else {
            channel = sb.bind(brokerProperties.getPort()).sync().channel();
        }
    }

    private void websocketServer() throws Exception {
        ServerBootstrap sb = new ServerBootstrap();
        sb.group(bossGroup, workerGroup)
                .channel(brokerProperties.getUseEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                // handler在初始化时就会执行
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline channelPipeline = socketChannel.pipeline();
                        // Netty提供的心跳检测
                        channelPipeline.addFirst("idle", new IdleStateHandler(0, 0, brokerProperties.getKeepAlive()));
                        // Netty提供的SSL处理
                        if (brokerProperties.getSslEnabled()) {
                            SSLEngine sslEngine = sslContext.newEngine(socketChannel.alloc());
                            sslEngine.setUseClientMode(false);        // 服务端模式
                            sslEngine.setNeedClientAuth(false);        // 不需要验证客户端
                            channelPipeline.addLast("ssl", new SslHandler(sslEngine));
                        }
                        // 将请求和应答消息编码或解码为HTTP消息
                        channelPipeline.addLast("http-codec", new HttpServerCodec());
                        // 将HTTP消息的多个部分合成一条完整的HTTP消息
                        channelPipeline.addLast("aggregator", new HttpObjectAggregator(1048576));
                        // 将HTTP消息进行压缩编码
                        channelPipeline.addLast("compressor ", new HttpContentCompressor());
                        channelPipeline.addLast("protocol", new WebSocketServerProtocolHandler(brokerProperties.getWebsocketPath(), "mqtt,mqttv3.1,mqttv3.1.1", true, 65536));
                        channelPipeline.addLast("mqttWebSocket", new MqttWebSocketCodec());
                        channelPipeline.addLast("decoder", new MqttDecoder());
                        channelPipeline.addLast("encoder", MqttEncoder.INSTANCE);
                        channelPipeline.addLast("broker", ioc.get(BrokerHandler.class));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, brokerProperties.getSoBacklog())
                .childOption(ChannelOption.SO_KEEPALIVE, brokerProperties.getSoKeepAlive());
        if (Strings.isNotBlank(brokerProperties.getHost())) {
            websocketChannel = sb.bind(brokerProperties.getHost(), brokerProperties.getWebsocketPort()).sync().channel();
        } else {
            websocketChannel = sb.bind(brokerProperties.getWebsocketPort()).sync().channel();
        }
    }
}
