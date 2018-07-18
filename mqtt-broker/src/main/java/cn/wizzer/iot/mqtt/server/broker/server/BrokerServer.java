/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.wizzer.iot.mqtt.server.broker.server;

import cn.wizzer.iot.mqtt.server.broker.config.BrokerProperties;
import cn.wizzer.iot.mqtt.server.broker.handler.BrokerHandler;
import cn.wizzer.iot.mqtt.server.broker.handler.WsBrokerHandler;
import cn.wizzer.iot.mqtt.server.broker.listener.MqttServerListener;
import cn.wizzer.iot.mqtt.server.broker.listener.WsServerListener;
import org.nutz.boot.starter.ServerFace;
import org.nutz.ioc.Ioc;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ssl.SslConfig;
import org.tio.server.ServerGroupContext;
import org.tio.server.TioServer;

import java.io.InputStream;

/**
 * t-io启动Broker
 */
@IocBean
public class BrokerServer implements ServerFace {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrokerServer.class);
    @Inject
    private BrokerProperties brokerProperties;
    @Inject("refer:$ioc")
    private Ioc ioc;

    private SslConfig sslConfig;

    public void start() throws Exception {
        LOGGER.info("Initializing {} MQTT Broker ...", "[" + brokerProperties.getId() + "]");
        //如果启用SSL
        if (brokerProperties.getSslEnabled()) {
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("keystore/server.jks");
            sslConfig = SslConfig.forServer(inputStream, null, brokerProperties.getSslPassword());
        }
        mqttServer();
        //如果启用websocket通信
        if (brokerProperties.getWebsockeenabled()) {
            websocketServer();
        }
        LOGGER.info("MQTT Broker {} is up and running. Open Port: {} WebSocketPort: {}", "[" + brokerProperties.getId() + "]", brokerProperties.getPort(), brokerProperties.getWebsocketPort());
    }


    public void stop() {
        ioc.get(TioServer.class, "tioServer").stop();
        //如果启用websocket通信
        if (brokerProperties.getWebsockeenabled()) {
            ioc.get(TioServer.class, "wsTioServer").stop();
        }
    }

    @IocBean(name = "serverGroupContext")
    public ServerGroupContext getServerGroupContext() throws Exception {
        ServerGroupContext serverGroupContext = new ServerGroupContext(ioc.get(BrokerHandler.class), ioc.get(MqttServerListener.class));
        serverGroupContext.setName(brokerProperties.getId());
        serverGroupContext.setHeartbeatTimeout(brokerProperties.getKeepAlive());
        if (brokerProperties.getSslEnabled())
            serverGroupContext.setSslConfig(sslConfig);
        return serverGroupContext;
    }

    @IocBean(name = "wsServerGroupContext")
    public ServerGroupContext getWsServerGroupContext() throws Exception {
        ServerGroupContext serverGroupContext = new ServerGroupContext(ioc.get(WsBrokerHandler.class), ioc.get(WsServerListener.class));
        serverGroupContext.setName(brokerProperties.getId());
        serverGroupContext.setHeartbeatTimeout(brokerProperties.getKeepAlive());
        if (brokerProperties.getSslEnabled())
            serverGroupContext.setSslConfig(sslConfig);
        return serverGroupContext;
    }

    @IocBean(name = "tioServer")
    public TioServer getTioServer(@Inject ServerGroupContext serverGroupContext) {
        return new TioServer(serverGroupContext);
    }


    @IocBean(name = "wsTioServer")
    public TioServer getWsTioServer(@Inject ServerGroupContext wsServerGroupContext) {
        return new TioServer(wsServerGroupContext);
    }

    private void mqttServer() throws Exception {
        ioc.get(TioServer.class, "tioServer").start(brokerProperties.getHost(), brokerProperties.getPort());
    }

    private void websocketServer() throws Exception {
        ioc.get(TioServer.class, "wsTioServer").start(brokerProperties.getHost(), brokerProperties.getWebsocketPort());
    }

}
