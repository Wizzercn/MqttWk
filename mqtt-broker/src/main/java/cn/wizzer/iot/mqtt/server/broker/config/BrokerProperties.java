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
     * t-io是否开启集群
     */
    private boolean clusterEnabled;
    @PropDoc(group = "broker", value = "是否开启集群模式, 默认false", type = "boolean", defaultValue = "false")
    public static final String PROP_CLUSTERON = PRE + "cluster-on";
    /**
     * WebSocket SSL端口号, 默认9995端口
     */
    private int websocketPort;
    @PropDoc(group = "broker", value = "WebSocket 端口号, 默认9995端口", type = "int", defaultValue = "9995")
    public static final String PROP_WEBSOCKETPORT = PRE + "websocket-port";
    /**
     * WebSocket 是否启用
     */
    private boolean websocketEnabled;
    @PropDoc(group = "broker", value = "WebSocket 是否启用", type = "boolean", defaultValue = "false")
    public static final String PROP_WEBSOCKETENABLED = PRE + "websocket-enabled";
    /**
     * WebSocket 路径
     */
    private String websocketPath;
    @PropDoc(group = "broker", value = "WebSocket 访问路径, 默认 /mqtt", defaultValue = "/mqtt")
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
     * 是否开启Epoll模式, 默认关闭
     */
    private boolean useEpoll;
    @PropDoc(group = "broker", value = "是否开启Epoll模式, 默认关闭", type = "boolean", defaultValue = "false")
    public static final String PROP_USEEPOLL = PRE + "use-epoll";
    /**
     * Sokcet参数, 存放已完成三次握手请求的队列最大长度, 默认511长度
     */
    private int soBacklog;
    @PropDoc(group = "broker", value = "Sokcet参数, 存放已完成三次握手请求的队列最大长度, 默认511长度", type = "int", defaultValue = "511")
    public static final String PROP_SOBACKLOG = PRE + "so-backlog";
    /**
     * Socket参数, 是否开启心跳保活机制, 默认开启
     */
    private boolean soKeepAlive;
    @PropDoc(group = "broker", value = "是否开启心跳保活机制, 默认开启", type = "boolean", defaultValue = "true")
    public static final String PROP_SOKEEPALIVE = PRE + "so-keep-alive";
    /**
     * 转发kafka主题
     */
    private String producerTopic;
    @PropDoc(group = "broker", value = "kafka转发topic", defaultValue = "mqtt_publish")
    public static final String PROP_KAFKA_PRODUCERTOPIC = PRE + "kafka.producer.topic";
    /**
     * MQTT.Connect消息必须通过用户名密码验证
     */
    private boolean mqttPasswordMust;
    @PropDoc(group = "broker", value = "Connect消息必须通过用户名密码验证, 默认true", type = "boolean", defaultValue = "true")
    public static final String PROP_MQTTPASSWORDMUST = PRE + "mqtt-password-must";
    /**
     * 是否启用kafka消息转发
     */
    private boolean kafkaBrokerEnabled;
    public static final String PROP_KAFKA_BROKER_ENABLED = PRE + "kafka.broker-enabled";


    public void init() {
        this.id = conf.get(_id, "mqttwk");
        this.host = conf.get(PROP_HOST, "127.0.0.1");
        this.port = conf.getInt(PROP_PORT, 8885);
        this.websocketPort = conf.getInt(PROP_WEBSOCKETPORT, 9995);
        this.websocketEnabled = conf.getBoolean(PROP_WEBSOCKETENABLED, false);
        this.websocketPath = conf.get(PROP_WEBSOCKETPATH, "/mqtt");
        this.sslEnabled = conf.getBoolean(PROP_SSLENABLED, true);
        this.sslPassword = conf.get(PROP_SSLPASSWORD);
        this.keepAlive = conf.getInt(PROP_KEEPALIVE, 60);
        this.producerTopic = conf.get(PROP_KAFKA_PRODUCERTOPIC, "mqtt_publish");
        this.mqttPasswordMust = conf.getBoolean(PROP_MQTTPASSWORDMUST, true);
        this.clusterEnabled = conf.getBoolean(PROP_CLUSTERON, false);
        this.kafkaBrokerEnabled = conf.getBoolean(PROP_KAFKA_BROKER_ENABLED, false);
        this.useEpoll = conf.getBoolean(PROP_USEEPOLL, false);
        this.soBacklog = conf.getInt(PROP_SOBACKLOG, 511);
        this.soKeepAlive = conf.getBoolean(PROP_SOKEEPALIVE, true);
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

    public boolean getClusterEnabled() {
        return clusterEnabled;
    }

    public BrokerProperties setClusterEnabled(boolean clusterEnabled) {
        this.clusterEnabled = clusterEnabled;
        return this;
    }

    public int getWebsocketPort() {
        return websocketPort;
    }

    public BrokerProperties setWebsocketPort(int websocketPort) {
        this.websocketPort = websocketPort;
        return this;
    }

    public boolean getWebsocketEnabled() {
        return websocketEnabled;
    }

    public BrokerProperties setWebsocketEnabled(boolean websocketEnabled) {
        this.websocketEnabled = websocketEnabled;
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

    public boolean getMqttPasswordMust() {
        return mqttPasswordMust;
    }

    public BrokerProperties setMqttPasswordMust(boolean mqttPasswordMust) {
        this.mqttPasswordMust = mqttPasswordMust;
        return this;
    }

    public boolean getKafkaBrokerEnabled() {
        return kafkaBrokerEnabled;
    }

    public BrokerProperties setKafkaBrokerEnabled(boolean kafkaBrokerEnabled) {
        this.kafkaBrokerEnabled = kafkaBrokerEnabled;
        return this;
    }

    public boolean getUseEpoll() {
        return useEpoll;
    }

    public BrokerProperties setUseEpoll(boolean useEpoll) {
        this.useEpoll = useEpoll;
        return this;
    }

    public int getSoBacklog() {
        return soBacklog;
    }

    public BrokerProperties setSoBacklog(int soBacklog) {
        this.soBacklog = soBacklog;
        return this;
    }

    public boolean getSoKeepAlive() {
        return soKeepAlive;
    }

    public BrokerProperties setSoKeepAlive(boolean soKeepAlive) {
        this.soKeepAlive = soKeepAlive;
        return this;
    }

}
