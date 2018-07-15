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
@IocBean
public class IgniteStarter {
    @Inject
    protected PropertiesProxy conf;

    protected static final String PRE = "mqttwk.broker.";

    @PropDoc(group = "ignite", value = "Ignite实例名称", need = true, defaultValue = "mqttwk")
    public static final String instanceName = PRE + "id";

    @PropDoc(group = "ignite", value = "集群方式:基于组播或静态IP配置", need = true, defaultValue = "true")
    public static final String enableMulticastGroup = PRE + "enable-multicast-group";

    @PropDoc(group = "ignite", value = "组播地址", defaultValue = "239.255.255.255")
    public static final String multicastGroup = PRE + "multicast-group";

    @PropDoc(group = "ignite", value = "静态IP地址 ,号分隔")
    public static final String staticIpAddresses = PRE + "static-ip-addresses";

    @PropDoc(group = "ignite", value = "持久化缓存内存初始化大小(MB), 默认值: 64", defaultValue = "64")
    public static final String persistenceInitialSize = PRE + "cache.persistence-size";

    @PropDoc(group = "ignite", value = "持久化缓存占用内存最大值(MB), 默认值: 128", defaultValue = "128")
    public static final String persistenceMaxSize = PRE + "cache.persistence-max-size";

    @PropDoc(group = "ignite", value = "持久化磁盘存储路径", defaultValue = "128")
    public static final String persistenceStorePath = PRE + "cache.persistence-path";

    @PropDoc(group = "ignite", value = "非持久化缓存内存初始化大小(MB), 默认值: 64", defaultValue = "64")
    public static final String NotPersistenceInitialSize = PRE + "cache.notpersistence-size";

    @PropDoc(group = "ignite", value = "非持久化缓存占用内存最大值(MB), 默认值: 128", defaultValue = "128")
    public static final String NotPersistenceMaxSize = PRE + "cache.notpersistence-max-size";

    @IocBean
    public Ignite ignite() throws Exception {
        IgniteConfiguration igniteConfiguration = new IgniteConfiguration();
        // Ignite实例名称
        igniteConfiguration.setIgniteInstanceName(conf.get(this.instanceName, "mqttwk"));
        // Ignite日志
        Logger logger = LoggerFactory.getLogger("org.apache.ignite");
        igniteConfiguration.setGridLogger(new Slf4jLogger(logger));
        // 非持久化数据区域
        DataRegionConfiguration notPersistence = new DataRegionConfiguration().setPersistenceEnabled(false)
                .setInitialSize(conf.getInt(NotPersistenceInitialSize, 64) * 1024 * 1024)
                .setMaxSize(conf.getInt(NotPersistenceMaxSize, 128) * 1024 * 1024).setName("not-persistence-data-region");
        // 持久化数据区域
        DataRegionConfiguration persistence = new DataRegionConfiguration().setPersistenceEnabled(true)
                .setInitialSize(conf.getInt(persistenceInitialSize, 64) * 1024 * 1024)
                .setMaxSize(conf.getInt(persistenceMaxSize, 128) * 1024 * 1024).setName("persistence-data-region");
        DataStorageConfiguration dataStorageConfiguration = new DataStorageConfiguration().setDefaultDataRegionConfiguration(notPersistence)
                .setDataRegionConfigurations(persistence)
                .setWalArchivePath(Strings.isNotBlank(conf.get(this.persistenceStorePath)) ? conf.get(this.persistenceStorePath) : null)
                .setWalPath(Strings.isNotBlank(conf.get(this.persistenceStorePath)) ? conf.get(this.persistenceStorePath) : null)
                .setStoragePath(Strings.isNotBlank(conf.get(this.persistenceStorePath)) ? conf.get(this.persistenceStorePath) : null);
        igniteConfiguration.setDataStorageConfiguration(dataStorageConfiguration);
        // 集群, 基于组播或静态IP配置
        TcpDiscoverySpi tcpDiscoverySpi = new TcpDiscoverySpi();
        if (conf.getBoolean(this.enableMulticastGroup, true)) {
            TcpDiscoveryMulticastIpFinder tcpDiscoveryMulticastIpFinder = new TcpDiscoveryMulticastIpFinder();
            tcpDiscoveryMulticastIpFinder.setMulticastGroup(this.multicastGroup);
            tcpDiscoverySpi.setIpFinder(tcpDiscoveryMulticastIpFinder);
        } else {
            TcpDiscoveryVmIpFinder tcpDiscoveryVmIpFinder = new TcpDiscoveryVmIpFinder();
            tcpDiscoveryVmIpFinder.setAddresses(conf.getList(this.staticIpAddresses, ","));
            tcpDiscoverySpi.setIpFinder(tcpDiscoveryVmIpFinder);
        }
        igniteConfiguration.setDiscoverySpi(tcpDiscoverySpi);
        Ignite ignite = Ignition.start(igniteConfiguration);
        ignite.cluster().active(true);
        return ignite;
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
