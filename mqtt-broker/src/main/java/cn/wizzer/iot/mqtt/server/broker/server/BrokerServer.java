/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.wizzer.iot.mqtt.server.broker.server;

import cn.wizzer.iot.mqtt.server.broker.config.BrokerProperties;
import cn.wizzer.iot.mqtt.server.broker.handler.BrokerHandler;
import cn.wizzer.iot.mqtt.server.broker.listener.MqttServerListener;
import cn.wizzer.iot.mqtt.server.broker.protocol.ProtocolProcess;
import org.nutz.boot.AppContext;
import org.nutz.ioc.Ioc;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.GroupContext;
import org.tio.core.exception.AioDecodeException;
import org.tio.core.intf.Packet;
import org.tio.core.ssl.SslConfig;
import org.tio.server.ServerGroupContext;
import org.tio.server.TioServer;
import org.tio.server.intf.ServerAioHandler;
import org.tio.server.intf.ServerAioListener;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.KeyStore;

/**
 * Netty启动Broker
 */
@IocBean(create = "start", depose = "stop")
public class BrokerServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrokerServer.class);

    @Inject
    private BrokerProperties brokerProperties;

    @Inject
    private ProtocolProcess protocolProcess;

    protected TioServer tioServer;
    @Inject("refer:$ioc")
    private Ioc ioc;

    private SslConfig sslConfig;

    private ChannelContext channel;

    private ChannelContext websocketChannel;

    public void start() throws Exception {
        LOGGER.info("Initializing {} MQTT Broker ...", "[" + brokerProperties.getId() + "]");
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("keystore/server.jks");
        sslConfig = SslConfig.forServer(inputStream, null,brokerProperties.getSslPassword());
        mqttServer();
//		websocketServer();
//		LOGGER.info("MQTT Broker {} is up and running. Open SSLPort: {} WebSocketSSLPort: {}", "[" + brokerProperties.getId() + "]", brokerProperties.getSslPort(), brokerProperties.getWebsocketSslPort());
    }


    public void stop() {
//		LOGGER.info("Shutdown {} MQTT Broker ...", "[" + brokerProperties.getId() + "]");
//		bossGroup.shutdownGracefully();
//		bossGroup = null;
//		workerGroup.shutdownGracefully();
//		workerGroup = null;
//		channel.closeFuture().syncUninterruptibly();
//		channel = null;
//		websocketChannel.closeFuture().syncUninterruptibly();
//		websocketChannel = null;
//		LOGGER.info("MQTT Broker {} shutdown finish.", "[" + brokerProperties.getId() + "]");
    }

    @IocBean(name = "serverGroupContext")
    public ServerGroupContext getServerGroupContext() throws Exception {
        ServerGroupContext serverGroupContext = new ServerGroupContext(ioc.get(BrokerHandler.class), ioc.get(MqttServerListener.class));
        serverGroupContext.setName(brokerProperties.getId());
        serverGroupContext.setHeartbeatTimeout(brokerProperties.getKeepAlive());
        serverGroupContext.setSslConfig(sslConfig);
        return serverGroupContext;
    }

    @IocBean
    public TioServer getAioServer(@Inject ServerGroupContext serverGroupContext) {
        return new TioServer(serverGroupContext);
    }

    private void mqttServer() throws Exception {
        tioServer = ioc.getByType(TioServer.class);
        tioServer.start(brokerProperties.getSslHost(), brokerProperties.getSslPort());
    }

    private void websocketServer() throws Exception {
//		ServerBootstrap sb = new ServerBootstrap();
//		sb.group(bossGroup, workerGroup)
//			.channel(brokerProperties.isUseEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
//			// handler在初始化时就会执行
//			.handler(new LoggingHandler(LogLevel.INFO))
//			.childHandler(new ChannelInitializer<SocketChannel>() {
//				@Override
//				protected void initChannel(SocketChannel socketChannel) throws Exception {
//					ChannelPipeline channelPipeline = socketChannel.pipeline();
//					// Netty提供的心跳检测
//					channelPipeline.addFirst("idle", new IdleStateHandler(0, 0, brokerProperties.getKeepAlive()));
//					// Netty提供的SSL处理
//					SSLEngine sslEngine = sslContext.newEngine(socketChannel.alloc());
//					sslEngine.setUseClientMode(false);        // 服务端模式
//					sslEngine.setNeedClientAuth(false);        // 不需要验证客户端
//					channelPipeline.addLast("ssl", new SslHandler(sslEngine));
//					// 将请求和应答消息编码或解码为HTTP消息
//					channelPipeline.addLast("http-codec", new HttpServerCodec());
//					// 将HTTP消息的多个部分合成一条完整的HTTP消息
//					channelPipeline.addLast("aggregator", new HttpObjectAggregator(1048576));
//					// 将HTTP消息进行压缩编码
//					channelPipeline.addLast("compressor ", new HttpContentCompressor());
//					channelPipeline.addLast("protocol", new WebSocketServerProtocolHandler(brokerProperties.getWebsocketPath(), "mqtt,mqttv3.1,mqttv3.1.1", true, 65536));
//					channelPipeline.addLast("mqttWebSocket", new MqttWebSocketCodec());
//					channelPipeline.addLast("decoder", new MqttDecoder());
//					channelPipeline.addLast("encoder", MqttEncoder.INSTANCE);
//					channelPipeline.addLast("broker", new BrokerHandler(protocolProcess));
//				}
//			})
//			.option(ChannelOption.SO_BACKLOG, brokerProperties.getSoBacklog())
//			.childOption(ChannelOption.SO_KEEPALIVE, brokerProperties.isSoKeepAlive());
//		websocketChannel = sb.bind(brokerProperties.getWebsocketSslPort()).sync().channel();
    }

}
