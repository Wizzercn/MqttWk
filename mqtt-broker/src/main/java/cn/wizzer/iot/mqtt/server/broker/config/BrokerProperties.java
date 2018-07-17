/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.wizzer.iot.mqtt.server.broker.config;

import org.nutz.boot.annotation.PropDoc;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

/**
 * 服务配置
 */
@IocBean(create = "init")
public class BrokerProperties {
    @Inject
    private PropertiesProxy conf;
    protected static final String PRE = "mqttwk.broker.";
    /**
     * Broker唯一标识, 默认mqttwk
     */
    private String id;

    //	@PropDoc(group = "broker", value = "Broker唯一标识", need = true, defaultValue = "mqttwk")
    public static final String _id = PRE + "id";

    /**
     * SSL启动的IP地址, 默认127.0.0.1
     */
    private String host;
    @PropDoc(group = "broker", value = "服务启动的IP", defaultValue = "127.0.0.1")
    public static final String PROP_HOST = PRE + "host";
    /**
     * SSL端口号, 默认8885端口
     */
    private int port;
    @PropDoc(group = "broker", value = "端口号, 默认8885端口", type = "int", defaultValue = "8885")
    public static final String PROP_PORT = PRE + "port";
    /**
     * WebSocket SSL端口号, 默认9995端口
     */
    private int websocketPort;
    @PropDoc(group = "broker", value = "WebSocket 端口号, 默认9995端口", type = "int", defaultValue = "9995")
    public static final String PROP_WEBSOCKETPORT = PRE + "websocket-port";
    /**
     * WebSocket Path值, 默认值 /mqtt
     */
    private String websocketPath;
    @PropDoc(group = "broker", value = "WebSocket Path值, 默认值 /mqtt", defaultValue = "/mqtt")
    public static final String PROP_WEBSOCKETPATH = PRE + "websocket-path";
    /**
     * SSL是否启用
     */
    private boolean sslEnabled;
    @PropDoc(group = "broker", value = "SSL是否启用", type = "boolean", defaultValue = "true")
    public static final String PROP_SSLENABLED = PRE + "ssl-enabled";
    /**
     * SSL密钥文件密码
     */
    private String sslPassword;
    @PropDoc(group = "broker", value = "SSL密钥文件密码")
    public static final String PROP_SSLPASSWORD = PRE + "ssl-password";
    /**
     * 心跳时间(秒), 默认60秒, 该值可被客户端连接时相应配置覆盖
     */
    private int keepAlive;
    @PropDoc(group = "broker", value = "心跳时间(秒), 默认60秒, 该值可被客户端连接时相应配置覆盖", type = "int", defaultValue = "60")
    public static final String PROP_KEEPALIVE = PRE + "keep-alive";
    /**
     * 转发kafka主题
     */
    private String producerTopic;
    @PropDoc(group = "broker", value = "kafka转发topic", defaultValue = "mqtt_publish")
    public static final String PROP_KAFKA_PRODUCERTOPIC = PRE + "kafka.producer.topic";

    /**
     * 接收kafka主题
     */
    private String consumerTopic;
    @PropDoc(group = "broker", value = "kafka订阅topic", defaultValue = "mqtt_subscribe")
    public static final String PROP_KAFKA_CONSUMERTOPIC = PRE + "kafka.consumer.topic";

    public void init() {
        this.id = conf.get(_id, "mqttwk");
        this.host = conf.get(PROP_HOST, "127.0.0.1");
        this.port = conf.getInt(PROP_PORT, 8885);
        this.websocketPort = conf.getInt(PROP_WEBSOCKETPORT, 9995);
        this.websocketPath = conf.get(PROP_WEBSOCKETPATH, "/mqtt");
        this.sslEnabled = conf.getBoolean(PROP_SSLENABLED, true);
        this.sslPassword = conf.get(PROP_SSLPASSWORD);
        this.keepAlive = conf.getInt(PROP_KEEPALIVE, 60);
        this.producerTopic = conf.get(PROP_KAFKA_PRODUCERTOPIC, "mqtt_publish");
        this.consumerTopic = conf.get(PROP_KAFKA_CONSUMERTOPIC, "mqtt_subscribe");

    }

    public String getId() {
        return id;
    }

    public BrokerProperties setId(String id) {
        this.id = id;
        return this;
    }

    public String getHost() {
        return host;
    }

    public BrokerProperties setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public BrokerProperties setPort(int port) {
        this.port = port;
        return this;
    }

    public int getWebsocketPort() {
        return websocketPort;
    }

    public BrokerProperties setWebsocketPort(int websocketPort) {
        this.websocketPort = websocketPort;
        return this;
    }

    public String getWebsocketPath() {
        return websocketPath;
    }

    public BrokerProperties setWebsocketPath(String websocketPath) {
        this.websocketPath = websocketPath;
        return this;
    }

    public boolean getSslEnabled() {
        return sslEnabled;
    }

    public BrokerProperties setSslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
        return this;
    }

    public String getSslPassword() {
        return sslPassword;
    }

    public BrokerProperties setSslPassword(String sslPassword) {
        this.sslPassword = sslPassword;
        return this;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public BrokerProperties setKeepAlive(int keepAlive) {
        this.keepAlive = keepAlive;
        return this;
    }

    public String getProducerTopic() {
        return producerTopic;
    }

    public BrokerProperties setProducerTopic(String producerTopic) {
        this.producerTopic = producerTopic;
        return this;
    }

    public String getConsumerTopic() {
        return consumerTopic;
    }

    public BrokerProperties setConsumerTopic(String consumerTopic) {
        this.consumerTopic = consumerTopic;
        return this;
    }
}
