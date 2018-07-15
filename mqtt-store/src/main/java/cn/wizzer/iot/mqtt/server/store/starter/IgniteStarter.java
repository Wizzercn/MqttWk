package cn.wizzer.iot.mqtt.server.store.starter;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteMessaging;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.logger.slf4j.Slf4jLogger;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.nutz.boot.annotation.PropDoc;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wizzer on 2018
 */
@IocBean(create = "init")
public class IgniteStarter {
    @Inject
    protected PropertiesProxy conf;
    protected Ignite ignite;
    protected static final String PRE = "mqttwk.broker.";

    @PropDoc(group = "ignite", value = "Ignite实例名称", need = true, defaultValue = "mqttwk")
    public static final String PROP_INSTANCENAME = PRE + "id";

    @PropDoc(group = "ignite", value = "集群方式:基于组播或静态IP配置", need = true, defaultValue = "true")
    public static final String PROP_ENABLEMULTICASTGROUP = PRE + "enable-multicast-group";

    @PropDoc(group = "ignite", value = "组播地址", defaultValue = "239.255.255.255")
    public static final String PROP_MULTICASTGROUP = PRE + "multicast-group";

    @PropDoc(group = "ignite", value = "静态IP地址 ,号分隔")
    public static final String PROP_STATICIPADDRESSES = PRE + "static-ip-addresses";

    @PropDoc(group = "ignite", value = "持久化缓存内存初始化大小(MB), 默认值: 64", defaultValue = "64")
    public static final String PROP_PERSISTENCEINITIALSIZE = PRE + "cache.persistence-size";

    @PropDoc(group = "ignite", value = "持久化缓存占用内存最大值(MB), 默认值: 128", defaultValue = "128")
    public static final String PROP_PERSISTENCEMAXSIZE = PRE + "cache.persistence-max-size";

    @PropDoc(group = "ignite", value = "持久化磁盘存储路径", defaultValue = "128")
    public static final String PROP_PERSISTENCESTOREPATH = PRE + "cache.persistence-path";

    @PropDoc(group = "ignite", value = "非持久化缓存内存初始化大小(MB), 默认值: 64", defaultValue = "64")
    public static final String PROP_NOTPERSISTENCEINITIALSIZE = PRE + "cache.notpersistence-size";

    @PropDoc(group = "ignite", value = "非持久化缓存占用内存最大值(MB), 默认值: 128", defaultValue = "128")
    public static final String PROP_NOTPERSISTENCEMAXSIZE = PRE + "cache.notpersistence-max-size";

    //以下部分只是为了打印propDoc
    @PropDoc(group = "broker", value = "SSL端口号, 默认8885端口", type = "int", defaultValue = "8885")
    public static final String PROP_SSLPORT = PRE + "ssl-port";

    @PropDoc(group = "broker", value = "WebSocket SSL端口号, 默认9995端口", type = "int", defaultValue = "9995")
    public static final String PROP_WEBSOCKETSSLPORT = PRE + "websocket-ssl-port";

    @PropDoc(group = "broker", value = "WebSocket Path值, 默认值 /mqtt", defaultValue = "/mqtt")
    public static final String PROP_WEBSOCKETPATH = PRE + "websocket-path";

    @PropDoc(group = "broker", value = "SSL密钥文件密码")
    public static final String PROP_SSLPASSWORD = PRE + "ssl-password";

    @PropDoc(group = "broker", value = "心跳时间(秒), 默认60秒, 该值可被客户端连接时相应配置覆盖", type = "int", defaultValue = "60")
    public static final String PROP_KEEPALIVE = PRE + "keep-alive";

    @PropDoc(group = "broker", value = "是否开启Epoll模式, 默认关闭", type = "boolean", defaultValue = "false")
    public static final String PROP_USEEPOLL = PRE + "use-epoll";

    @PropDoc(group = "broker", value = "Sokcet参数, 存放已完成三次握手请求的队列最大长度, 默认511长度", type = "int", defaultValue = "511")
    public static final String PROP_SOBACKLOG = PRE + "so-backlog";

    @PropDoc(group = "broker", value = "Socket参数, 是否开启心跳保活机制, 默认开启", type = "boolean", defaultValue = "true")
    public static final String PROP_SOKEEPALIVE = PRE + "so-keep-alive";

    public void init() throws Exception {

        IgniteConfiguration igniteConfiguration = new IgniteConfiguration();
        // Ignite实例名称
        igniteConfiguration.setIgniteInstanceName(conf.get(this.PROP_INSTANCENAME, "mqttwk"));
        // Ignite日志
        Logger logger = LoggerFactory.getLogger("org.apache.ignite");
        igniteConfiguration.setGridLogger(new Slf4jLogger(logger));
        // 非持久化数据区域
        DataRegionConfiguration notPersistence = new DataRegionConfiguration().setPersistenceEnabled(false)
                .setInitialSize(conf.getInt(PROP_NOTPERSISTENCEINITIALSIZE, 64) * 1024 * 1024)
                .setMaxSize(conf.getInt(PROP_NOTPERSISTENCEMAXSIZE, 128) * 1024 * 1024).setName("not-persistence-data-region");
        // 持久化数据区域
        DataRegionConfiguration persistence = new DataRegionConfiguration().setPersistenceEnabled(true)
                .setInitialSize(conf.getInt(PROP_PERSISTENCEINITIALSIZE, 64) * 1024 * 1024)
                .setMaxSize(conf.getInt(PROP_PERSISTENCEMAXSIZE, 128) * 1024 * 1024).setName("persistence-data-region");
        DataStorageConfiguration dataStorageConfiguration = new DataStorageConfiguration().setDefaultDataRegionConfiguration(notPersistence)
                .setDataRegionConfigurations(persistence)
                .setWalArchivePath(Strings.isNotBlank(conf.get(this.PROP_PERSISTENCESTOREPATH)) ? conf.get(this.PROP_PERSISTENCESTOREPATH) : null)
                .setWalPath(Strings.isNotBlank(conf.get(this.PROP_PERSISTENCESTOREPATH)) ? conf.get(this.PROP_PERSISTENCESTOREPATH) : null)
                .setStoragePath(Strings.isNotBlank(conf.get(this.PROP_PERSISTENCESTOREPATH)) ? conf.get(this.PROP_PERSISTENCESTOREPATH) : null);
        igniteConfiguration.setDataStorageConfiguration(dataStorageConfiguration);
        // 集群, 基于组播或静态IP配置
        TcpDiscoverySpi tcpDiscoverySpi = new TcpDiscoverySpi();
        if (conf.getBoolean(this.PROP_ENABLEMULTICASTGROUP, true)) {
            TcpDiscoveryMulticastIpFinder tcpDiscoveryMulticastIpFinder = new TcpDiscoveryMulticastIpFinder();
            tcpDiscoveryMulticastIpFinder.setMulticastGroup(conf.get(this.PROP_MULTICASTGROUP, "239.255.255.255"));
            tcpDiscoverySpi.setIpFinder(tcpDiscoveryMulticastIpFinder);
        } else {
            TcpDiscoveryVmIpFinder tcpDiscoveryVmIpFinder = new TcpDiscoveryVmIpFinder();
            tcpDiscoveryVmIpFinder.setAddresses(conf.getList(this.PROP_STATICIPADDRESSES, ","));
            tcpDiscoverySpi.setIpFinder(tcpDiscoveryVmIpFinder);
        }
        igniteConfiguration.setDiscoverySpi(tcpDiscoverySpi);
        Ignite ignite = Ignition.start(igniteConfiguration);
        ignite.cluster().active(true);
        this.ignite = ignite;
    }

    @IocBean
    public Ignite ignite() throws Exception {
        return this.ignite;
    }

    @IocBean
    public IgniteCache messageIdCache() throws Exception {
        CacheConfiguration cacheConfiguration = new CacheConfiguration().setDataRegionName("not-persistence-data-region")
                .setCacheMode(CacheMode.PARTITIONED).setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL).setName("messageIdCache");
        return ignite().getOrCreateCache(cacheConfiguration);
    }

    @IocBean
    public IgniteCache retainMessageCache() throws Exception {
        CacheConfiguration cacheConfiguration = new CacheConfiguration().setDataRegionName("persistence-data-region")
                .setCacheMode(CacheMode.PARTITIONED).setName("retainMessageCache");
        return ignite().getOrCreateCache(cacheConfiguration);
    }

    @IocBean
    public IgniteCache subscribeNotWildcardCache() throws Exception {
        CacheConfiguration cacheConfiguration = new CacheConfiguration().setDataRegionName("persistence-data-region")
                .setCacheMode(CacheMode.PARTITIONED).setName("subscribeNotWildcardCache");
        return ignite().getOrCreateCache(cacheConfiguration);
    }

    @IocBean
    public IgniteCache subscribeWildcardCache() throws Exception {
        CacheConfiguration cacheConfiguration = new CacheConfiguration().setDataRegionName("persistence-data-region")
                .setCacheMode(CacheMode.PARTITIONED).setName("subscribeWildcardCache");
        return ignite().getOrCreateCache(cacheConfiguration);
    }

    @IocBean
    public IgniteCache dupPublishMessageCache() throws Exception {
        CacheConfiguration cacheConfiguration = new CacheConfiguration().setDataRegionName("persistence-data-region")
                .setCacheMode(CacheMode.PARTITIONED).setName("dupPublishMessageCache");
        return ignite().getOrCreateCache(cacheConfiguration);
    }

    @IocBean
    public IgniteCache dupPubRelMessageCache() throws Exception {
        CacheConfiguration cacheConfiguration = new CacheConfiguration().setDataRegionName("persistence-data-region")
                .setCacheMode(CacheMode.PARTITIONED).setName("dupPubRelMessageCache");
        return ignite().getOrCreateCache(cacheConfiguration);
    }

    @IocBean
    public IgniteMessaging igniteMessaging() throws Exception {
        return ignite().message(ignite().cluster().forRemotes());
    }
}
