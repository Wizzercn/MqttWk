package cn.wizzer.iot.mqtt.server.store.starter;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.nutz.boot.annotation.PropDoc;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

import java.util.Properties;

/**
 * Created by wizzer on 2018
 */
@IocBean(create = "init", depose = "close")
public class StoreStarter {
    @Inject
    protected PropertiesProxy conf;
    protected KafkaProducer kafkaProducer;
    protected static final String PRE = "mqttwk.broker.";

    @PropDoc(group = "broker", value = "实例名称", need = true, defaultValue = "mqttwk")
    public static final String PROP_INSTANCENAME = PRE + "id";

    @PropDoc(group = "broker", value = "是否启用kafka消息转发", need = true, defaultValue = "false")
    public static final String PROP_KAFKA_BROKER_ENABLED = PRE + "kafka.broker-enabled";

    @PropDoc(group = "broker", value = "kafka地址 127.0.0.1:9092,127.0.0.1:9093", need = true, defaultValue = "127.0.0.1:9092")
    public static final String PROP_KAFKA_SERVERS = PRE + "kafka.bootstrap.servers";

    @PropDoc(group = "broker", value = "all:必须等待回令 -1:不等待", defaultValue = "all")
    public static final String PROP_KAFKA_ACKS = PRE + "kafka.acks";

    @PropDoc(group = "broker", value = "重试次数", type = "int", defaultValue = "0")
    public static final String PROP_KAFKA_RETRIES = PRE + "kafka.retries";

    @PropDoc(group = "broker", value = "批量提交大小", type = "int", defaultValue = "16384")
    public static final String PROP_KAFKA_BATCHSIZE = PRE + "kafka.batch.size";

    @PropDoc(group = "broker", value = "提交延迟等待时间(等待时间内可以追加提交)", type = "int", defaultValue = "1")
    public static final String PROP_KAFKA_LINGERMS = PRE + "kafka.linger.ms";

    @PropDoc(group = "broker", value = "缓存大小(Bit) 默认:64MB", type = "int", defaultValue = "67108864")
    public static final String PROP_KAFKA_BUFFERMEMORY = PRE + "kafka.buffer.memory";

    @PropDoc(group = "broker", value = "key序列化方法", defaultValue = "org.apache.kafka.common.serialization.StringSerializer")
    public static final String PROP_KAFKA_KEYSERIALIZER = PRE + "kafka.key.serializer";

    @PropDoc(group = "broker", value = "value序列化方法", defaultValue = "org.apache.kafka.common.serialization.StringSerializer")
    public static final String PROP_KAFKA_VALUESERIALIZER = PRE + "kafka.value.serializer";

    @PropDoc(group = "broker", value = "分发策略", defaultValue = "cn.wizzer.iot.mqtt.server.store.kafka.SimplePartitioner")
    public static final String PROP_KAFKA_PARTITIONERCLASS = PRE + "kafka.partitioner.class";

    public Properties getProperties() {
        Properties properties = new Properties();
        for (String key : conf.keySet()) {
            if (key.startsWith("mqttwk.broker.kafka.")) {
                properties.put(key.substring("mqttwk.broker.kafka.".length()), conf.get(key));
            }
        }
        return properties;
    }

    @IocBean
    public KafkaProducer kafkaProducer() {
        return this.kafkaProducer;
    }

    public void init() throws Exception {
        if (conf.getBoolean(PROP_KAFKA_BROKER_ENABLED, false)) {
            this.kafkaProducer = new KafkaProducer(getProperties());
        }
    }

    public void close() throws Exception {
        if (this.kafkaProducer != null) {
            this.kafkaProducer.close();
        }
    }
}
