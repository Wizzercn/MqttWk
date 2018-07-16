/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.wizzer.iot.mqtt.server.broker.internal;

import cn.wizzer.iot.mqtt.server.broker.packet.MqttPacket;
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
import org.nutz.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.Tio;
import org.tio.core.utils.ByteBufferUtils;

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
                    mqttPacket.setMqttFixedHeader(publishMessage.fixedHeader());
                    byte[] bytes1 = Lang.toBytes(publishMessage.variableHeader());
                    byte[] bytes2 = ByteBufferUtils.readBytes(publishMessage.payload(), publishMessage.payload().remaining());
                    byte[] bytes = new byte[bytes1.length + bytes2.length];
                    System.arraycopy(bytes1, 0, bytes, 0, bytes1.length);
                    System.arraycopy(bytes2, 0, bytes, bytes1.length, bytes2.length);
                    mqttPacket.setBody(bytes);
                    Tio.send(sessionStoreService.get(subscribeStore.getClientId()).getChannel(), mqttPacket);
                }
                if (respQoS == MqttQoS.AT_LEAST_ONCE) {
                    int messageId = messageIdService.getNextMessageId();
                    MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
                            new MqttPublishVariableHeader(topic, messageId), ByteBuffer.wrap(messageBytes));
                    LOGGER.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}, messageId: {}", subscribeStore.getClientId(), topic, respQoS.value(), messageId);
                    MqttPacket mqttPacket = new MqttPacket();
                    mqttPacket.setMqttFixedHeader(publishMessage.fixedHeader());
                    byte[] bytes1 = Lang.toBytes(publishMessage.variableHeader());
                    byte[] bytes2 = ByteBufferUtils.readBytes(publishMessage.payload(), publishMessage.payload().remaining());
                    byte[] bytes = new byte[bytes1.length + bytes2.length];
                    System.arraycopy(bytes1, 0, bytes, 0, bytes1.length);
                    System.arraycopy(bytes2, 0, bytes, bytes1.length, bytes2.length);
                    mqttPacket.setBody(bytes);
                    Tio.send(sessionStoreService.get(subscribeStore.getClientId()).getChannel(), mqttPacket);
                }
                if (respQoS == MqttQoS.EXACTLY_ONCE) {
                    int messageId = messageIdService.getNextMessageId();
                    MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
                            new MqttPublishVariableHeader(topic, messageId), ByteBuffer.wrap(messageBytes));
                    LOGGER.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}, messageId: {}", subscribeStore.getClientId(), topic, respQoS.value(), messageId);
                    MqttPacket mqttPacket = new MqttPacket();
                    mqttPacket.setMqttFixedHeader(publishMessage.fixedHeader());
                    byte[] bytes1 = Lang.toBytes(publishMessage.variableHeader());
                    byte[] bytes2 = ByteBufferUtils.readBytes(publishMessage.payload(), publishMessage.payload().remaining());
                    byte[] bytes = new byte[bytes1.length + bytes2.length];
                    System.arraycopy(bytes1, 0, bytes, 0, bytes1.length);
                    System.arraycopy(bytes2, 0, bytes, bytes1.length, bytes2.length);
                    mqttPacket.setBody(bytes);
                    Tio.send(sessionStoreService.get(subscribeStore.getClientId()).getChannel(), mqttPacket);
                }
            }
        });
    }

}
