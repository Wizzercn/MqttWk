/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.wizzer.iot.mqtt.server.broker.internal;

import cn.wizzer.iot.mqtt.server.broker.packet.MqttPacket;
import cn.wizzer.iot.mqtt.server.broker.service.TioService;
import cn.wizzer.iot.mqtt.server.common.message.IMessageIdService;
import cn.wizzer.iot.mqtt.server.common.session.ISessionStoreService;
import cn.wizzer.iot.mqtt.server.common.subscribe.ISubscribeStoreService;
import cn.wizzer.iot.mqtt.server.common.subscribe.SubscribeStore;
import cn.wizzer.iot.mqtt.tio.codec.*;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * 内部通信, 基于发布-订阅范式
 */
@IocBean
public class InternalCommunication {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalCommunication.class);
    @Inject
    private PropertiesProxy conf;

    @Inject
    private KafkaProducer kafkaProducer;

    @Inject
    private ISessionStoreService sessionStoreService;

    @Inject
    private ISubscribeStoreService subscribeStoreService;

    @Inject
    private IMessageIdService messageIdService;

    @Inject
    private TioService tioService;

    public void internalSend(InternalMessage internalMessage) {
        ProducerRecord<String, InternalMessage> data = new ProducerRecord<>(conf.get("mqttwk.broker.kafka.producer.topic", "mqtt_publish"), internalMessage.getTopic(), internalMessage);
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

    private void sendPublishMessage(String topic, MqttQoS mqttQoS, byte[] messageBytes, boolean retain, boolean dup) {
        List<SubscribeStore> subscribeStores = subscribeStoreService.search(topic);
        subscribeStores.forEach(subscribeStore -> {
            if (sessionStoreService.containsKey(subscribeStore.getClientId())) {
                // 订阅者收到MQTT消息的QoS级别, 最终取决于发布消息的QoS和主题订阅的QoS
                MqttQoS respQoS = mqttQoS.value() > subscribeStore.getMqttQoS() ? MqttQoS.valueOf(subscribeStore.getMqttQoS()) : mqttQoS;
                if (respQoS == MqttQoS.AT_MOST_ONCE) {
                    MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
                            new MqttPublishVariableHeader(topic, 0), ByteBuffer.wrap(messageBytes));
                    LOGGER.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}", subscribeStore.getClientId(), topic, respQoS.value());
                    MqttPacket mqttPacket = new MqttPacket();
                    mqttPacket.setMqttMessage(publishMessage);
                    tioService.send(sessionStoreService.get(subscribeStore.getClientId()).getChannelId(), mqttPacket);
                }
                if (respQoS == MqttQoS.AT_LEAST_ONCE) {
                    int messageId = messageIdService.getNextMessageId();
                    MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
                            new MqttPublishVariableHeader(topic, messageId), ByteBuffer.wrap(messageBytes));
                    LOGGER.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}, messageId: {}", subscribeStore.getClientId(), topic, respQoS.value(), messageId);
                    MqttPacket mqttPacket = new MqttPacket();
                    mqttPacket.setMqttMessage(publishMessage);
                    tioService.send(sessionStoreService.get(subscribeStore.getClientId()).getChannelId(), mqttPacket);
                }
                if (respQoS == MqttQoS.EXACTLY_ONCE) {
                    int messageId = messageIdService.getNextMessageId();
                    MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
                            new MqttPublishVariableHeader(topic, messageId), ByteBuffer.wrap(messageBytes));
                    LOGGER.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}, messageId: {}", subscribeStore.getClientId(), topic, respQoS.value(), messageId);
                    MqttPacket mqttPacket = new MqttPacket();
                    mqttPacket.setMqttMessage(publishMessage);
                    tioService.send(sessionStoreService.get(subscribeStore.getClientId()).getChannelId(), mqttPacket);
                }
            }
        });
    }

}
