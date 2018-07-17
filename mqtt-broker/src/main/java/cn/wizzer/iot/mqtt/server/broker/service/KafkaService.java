package cn.wizzer.iot.mqtt.server.broker.service;

import cn.hutool.core.util.HexUtil;
import cn.wizzer.iot.mqtt.server.broker.config.BrokerProperties;
import cn.wizzer.iot.mqtt.server.broker.internal.InternalMessage;
import cn.wizzer.iot.mqtt.server.common.message.RetainMessageStore;
import cn.wizzer.iot.mqtt.server.store.message.RetainMessageStoreService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.nutz.aop.interceptor.async.Async;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created by wizzer on 2018
 */
@IocBean
public class KafkaService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaService.class);
    @Inject
    private KafkaProducer kafkaProducer;
    @Inject
    private KafkaConsumer kafkaConsumer;
    @Inject
    private BrokerProperties brokerProperties;
    @Inject
    private RetainMessageStoreService retainMessageStoreService;

    @Async
    public void internalSend(InternalMessage internalMessage) {
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

    @Async
    public void consumerMessage() {
        kafkaConsumer.subscribe(Arrays.asList(brokerProperties.getConsumerTopic()));
        while (true) {
            ConsumerRecords<String, String> records = kafkaConsumer.poll(200);
            for (ConsumerRecord<String, String> record : records) {
                RetainMessageStore retainMessageStore = new RetainMessageStore().setTopic(record.key()).setMqttQoS(0)
                        .setMessageBytes(HexUtil.decodeHex(record.value()));
                retainMessageStoreService.put(record.key(), retainMessageStore);
                LOGGER.debug("offset = {}, key = {}, value = {}", record.offset(), record.key(), record.value());
            }
        }
    }
}
