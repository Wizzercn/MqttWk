package cn.wizzer.mqttwk.kafka;

import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.server.ConfigType;
import kafka.utils.ZkUtils;
import org.apache.kafka.common.security.JaasUtils;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import scala.collection.Seq;

import java.util.*;

/**
 * Created by wizzer on 2018/5/8.
 */
@IocBean(create = "init")
public class WkKafkaAdmin {
    private final static Log log = Logs.get();
    @Inject
    private PropertiesProxy conf;

    public void init() {
        ZkUtils zkUtils = ZkUtils.apply(conf.get("mqttwk.kafka.zookeepers", "127.0.0.1:2181"), 30000, 30000,
                JaasUtils.isZkSecurityEnabled());
        String[] topics = Strings.splitIgnoreBlank(conf.get("mqttwk.kafka.topics", ""), ",");
        int partition = conf.getInt("mqttwk.kafka.partition", 1);
        int replication = conf.getInt("mqttwk.kafka.replication", 1);
        Seq<String> allTopics = zkUtils.getAllTopics();
        log.info("allTopics : " + Json.toJson(allTopics));
        for (String topic : topics) {
            if (!allTopics.contains(topic)) {//没有则创建
                AdminUtils.createTopic(zkUtils, topic,
                        partition, replication,
                        new Properties(), new RackAwareMode.Enforced$());
            }
        }
        zkUtils.close();
    }
}
