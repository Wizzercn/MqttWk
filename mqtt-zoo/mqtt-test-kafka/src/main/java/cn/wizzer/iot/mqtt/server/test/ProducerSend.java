package cn.wizzer.iot.mqtt.server.test;

import cn.hutool.core.util.HexUtil;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.nutz.aop.interceptor.async.Async;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;

/**
 * Created by wizzer on 2018
 */
@IocBean
public class ProducerSend {
    private static final Log log = Logs.get();
    @Inject
    private KafkaProducer kafkaProducer;
    @Inject
    private PropertiesProxy conf;

    @Async
    public void send() {
        //kafka生产消息,转发到MQTT客户端
        byte[] d = "wizzer.cn".getBytes();
        ProducerRecord<String, String> data = new ProducerRecord<>(conf.get("mqttwk.broker.kafka.consumer.topic"), "/mqtt-spy/test/", HexUtil.encodeHexStr(d));
        while (true) {
            log.debug("ProducerSend:::"+new String(d));
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
            try {
                Thread.sleep(2000L);
            } catch (Exception e) {

            }
        }
    }
}
