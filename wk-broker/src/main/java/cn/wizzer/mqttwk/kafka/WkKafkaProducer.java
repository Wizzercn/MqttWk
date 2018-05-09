package cn.wizzer.mqttwk.kafka;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;

import java.util.Properties;

/**
 * Created by wizzer on 2018/5/8.
 */
@IocBean(create = "init", depose = "close")
public class WkKafkaProducer {
    private final static Log log = Logs.get();
    @Inject
    private PropertiesProxy conf;
    protected KafkaProducer kafkaProducer;

    @SuppressWarnings("unchecked")
    public void send(String topic, String key, String value) {
        ProducerRecord<String, String> data = new ProducerRecord<>(topic, key, value);
        kafkaProducer.send(data,
                new Callback() {
                    public void onCompletion(RecordMetadata metadata, Exception e) {
                        if (e != null) {
                            e.printStackTrace();
                            log.error(e.getMessage(), e);
                        } else {
                            log.info("The offset of the record we just sent is: " + metadata.offset());
                        }
                    }
                });
    }

    public Properties getProperties() {
        Properties properties = new Properties();
        for (String key : conf.keySet()) {
            if (key.startsWith("kafka.")) {
                properties.put(key.substring("kafka.".length()), conf.get(key));
            }
        }
        return properties;
    }

    public void init() {
        kafkaProducer = new KafkaProducer(getProperties());
    }

    public void close() {
        if (kafkaProducer != null) {
            try {
                kafkaProducer.close();
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage(), e);
            }
        }
    }
}
