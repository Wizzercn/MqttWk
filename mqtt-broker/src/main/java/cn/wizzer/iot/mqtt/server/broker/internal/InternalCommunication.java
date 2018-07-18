/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.wizzer.iot.mqtt.server.broker.internal;

import cn.wizzer.iot.mqtt.server.broker.config.BrokerProperties;
import cn.wizzer.iot.mqtt.server.broker.packet.MqttPacket;
import cn.wizzer.iot.mqtt.server.broker.service.KafkaService;
import cn.wizzer.iot.mqtt.server.broker.service.TioService;
import cn.wizzer.iot.mqtt.server.common.message.IMessageIdService;
import cn.wizzer.iot.mqtt.server.common.session.ISessionStoreService;
import cn.wizzer.iot.mqtt.server.common.subscribe.ISubscribeStoreService;
import cn.wizzer.iot.mqtt.server.common.subscribe.SubscribeStore;
import cn.wizzer.iot.mqtt.tio.codec.*;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.nutz.aop.interceptor.async.Async;
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
    private BrokerProperties brokerProperties;

    @Inject
    private KafkaService kafkaService;

    @Inject
    private KafkaConsumer kafkaConsumer;

    @Inject
    private ISessionStoreService sessionStoreService;

    @Inject
    private ISubscribeStoreService subscribeStoreService;

    @Inject
    private IMessageIdService messageIdService;

    @Inject
    private TioService tioService;

    @Async
    public void internalSend(InternalMessage internalMessage) {
        try {
            this.sendPublishMessage(internalMessage.getTopic(), MqttQoS.valueOf(internalMessage.getMqttQoS()), internalMessage.getMessageBytes(), internalMessage.isRetain(), internalMessage.isDup());
            kafkaService.send(internalMessage);
        }catch (Exception e){
            e.printStackTrace();
        }
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
