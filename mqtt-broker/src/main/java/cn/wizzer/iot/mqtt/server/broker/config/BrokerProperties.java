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
     * SSL端口号, 默认8885端口
     */
    private int sslPort;
    @PropDoc(group = "broker", value = "SSL端口号, 默认8885端口", type = "int", defaultValue = "8885")
    public static final String _sslPort = PRE + "sslPort";
    /**
     * WebSocket SSL端口号, 默认9995端口
     */
    private int websocketSslPort;
    @PropDoc(group = "broker", value = "WebSocket SSL端口号, 默认9995端口", type = "int", defaultValue = "9995")
    public static final String _websocketSslPort = PRE + "websocketSslPort";
    /**
     * WebSocket Path值, 默认值 /mqtt
     */
    private String websocketPath;
    @PropDoc(group = "broker", value = "WebSocket Path值, 默认值 /mqtt", defaultValue = "/mqtt")
    public static final String _websocketPath = PRE + "websocketPath";
    /**
     * SSL密钥文件密码
     */
    private String sslPassword;
    @PropDoc(group = "broker", value = "SSL密钥文件密码")
    public static final String _sslPassword = PRE + "sslPassword";
    /**
     * 心跳时间(秒), 默认60秒, 该值可被客户端连接时相应配置覆盖
     */
    private int keepAlive;
    @PropDoc(group = "broker", value = "心跳时间(秒), 默认60秒, 该值可被客户端连接时相应配置覆盖", type = "int", defaultValue = "60")
    public static final String _keepAlive = PRE + "keepAlive";
    /**
     * 是否开启Epoll模式, 默认关闭
     */
    private boolean useEpoll;
    @PropDoc(group = "broker", value = "是否开启Epoll模式, 默认关闭", type = "boolean", defaultValue = "false")
    public static final String _useEpoll = PRE + "useEpoll";
    /**
     * Sokcet参数, 存放已完成三次握手请求的队列最大长度, 默认511长度
     */
    private int soBacklog;
    @PropDoc(group = "broker", value = "Sokcet参数, 存放已完成三次握手请求的队列最大长度, 默认511长度", type = "int", defaultValue = "511")
    public static final String _soBacklog = PRE + "soBacklog";
    /**
     * Socket参数, 是否开启心跳保活机制, 默认开启
     */
    private boolean soKeepAlive = true;
    @PropDoc(group = "broker", value = "Socket参数, 是否开启心跳保活机制, 默认开启", type = "boolean", defaultValue = "true")
    public static final String _soKeepAlive = PRE + "soKeepAlive";
    /**
     * 集群配置, 是否基于组播发现, 默认开启
     */
    private boolean enableMulticastGroup;
    public static final String _enableMulticastGroup = PRE + "enable-multicast-group";
    /**
     * 集群配置, 基于组播发现
     */
    private String multicastGroup;
    public static final String _multicastGroup = PRE + "multicast-group";
    /**
     * 集群配置, 当组播模式禁用时, 使用静态IP开启配置集群
     */
    private String staticIpAddresses;
    public static final String _staticIpAddresses = PRE + "static-ip-addresses";

    public void init() {
        this.id = conf.get(_id, "mqttwk");
        this.sslPort = conf.getInt(_sslPort, 8885);
        this.websocketSslPort = conf.getInt(_websocketSslPort, 9995);
        this.websocketPath = conf.get(_websocketPath, "/mqtt");
        this.sslPassword = conf.get(_sslPassword);
        this.keepAlive = conf.getInt(_keepAlive, 60);
        this.useEpoll = conf.getBoolean(_useEpoll, false);
        this.soBacklog = conf.getInt(_soBacklog, 511);
        this.soKeepAlive = conf.getBoolean(_soKeepAlive, true);
        this.enableMulticastGroup = conf.getBoolean(_enableMulticastGroup, true);
        this.multicastGroup = conf.get(_multicastGroup, "239.255.255.255");
        this.staticIpAddresses = conf.get(_staticIpAddresses);
    }

    public String getId() {
        return id;
    }

    public BrokerProperties setId(String id) {
        this.id = id;
        return this;
    }

    public int getSslPort() {
        return sslPort;
    }

    public BrokerProperties setSslPort(int sslPort) {
        this.sslPort = sslPort;
        return this;
    }

    public int getWebsocketSslPort() {
        return websocketSslPort;
    }

    public BrokerProperties setWebsocketSslPort(int websocketSslPort) {
        this.websocketSslPort = websocketSslPort;
        return this;
    }

    public String getWebsocketPath() {
        return websocketPath;
    }

    public BrokerProperties setWebsocketPath(String websocketPath) {
        this.websocketPath = websocketPath;
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

    public boolean isUseEpoll() {
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

    public boolean isSoKeepAlive() {
        return soKeepAlive;
    }

    public BrokerProperties setSoKeepAlive(boolean soKeepAlive) {
        this.soKeepAlive = soKeepAlive;
        return this;
    }

    public boolean isEnableMulticastGroup() {
        return enableMulticastGroup;
    }

    public BrokerProperties setEnableMulticastGroup(boolean enableMulticastGroup) {
        this.enableMulticastGroup = enableMulticastGroup;
        return this;
    }

    public String getMulticastGroup() {
        return multicastGroup;
    }

    public BrokerProperties setMulticastGroup(String multicastGroup) {
        this.multicastGroup = multicastGroup;
        return this;
    }

    public String getStaticIpAddresses() {
        return staticIpAddresses;
    }

    public BrokerProperties setStaticIpAddresses(String staticIpAddresses) {
        this.staticIpAddresses = staticIpAddresses;
        return this;
    }
}