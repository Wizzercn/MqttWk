package cn.wizzer.iot.mqtt.server.broker.service;

import cn.hutool.core.util.HexUtil;
import cn.wizzer.iot.mqtt.server.broker.config.BrokerProperties;
import cn.wizzer.iot.mqtt.server.broker.internal.InternalMessage;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.nutz.aop.interceptor.async.Async;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wizzer on 2018
 */
@IocBean
public class KafkaService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaService.class);
    @Inject
    private KafkaProducer kafkaProducer;
    @Inject
    private BrokerProperties brokerProperties;

    @Async
    public void send(InternalMessage internalMessage) throws Exception {
        //消息体转换为Hex字符串进行转发
        ProducerRecord<String, String> data = new ProducerRecord<>(brokerProperties.getProducerTopic(), internalMessage.getTopic(), HexUtil.encodeHexStr(internalMessage.getMessageBytes()));
        kafkaProducer.send(data,
                new Callback() {
                    public void onCompletion(RecordMetadata metadata, Exception e) {
                        if (e != null) {
                            e.printStackTrace();
                            LOGGER.error(e.getMessage(), e);
                        } else {
                            LOGGER.info("The offset of the record we just sent is: " + metadata.offset());
                        }
                    }
                });
    }

}
