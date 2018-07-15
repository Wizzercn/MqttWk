/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.wizzer.iot.mqtt.server.broker.protocol;

import cn.wizzer.iot.mqtt.server.broker.internal.InternalCommunication;
import cn.wizzer.iot.mqtt.server.broker.internal.InternalMessage;
import cn.wizzer.iot.mqtt.server.broker.packet.MqttPacket;
import cn.wizzer.iot.mqtt.server.common.message.*;
import cn.wizzer.iot.mqtt.server.common.session.ISessionStoreService;
import cn.wizzer.iot.mqtt.server.common.subscribe.ISubscribeStoreService;
import cn.wizzer.iot.mqtt.server.common.subscribe.SubscribeStore;
import cn.wizzer.iot.mqtt.tio.codec.*;
import org.nutz.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.core.utils.ByteBufferUtils;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * PUBLISH连接处理
 */
public class Publish {

    private static final Logger LOGGER = LoggerFactory.getLogger(Publish.class);

    private ISessionStoreService sessionStoreService;

    private ISubscribeStoreService subscribeStoreService;

    private IMessageIdService messageIdService;

    private IRetainMessageStoreService retainMessageStoreService;

    private IDupPublishMessageStoreService dupPublishMessageStoreService;

    private InternalCommunication internalCommunication;

    public Publish(ISessionStoreService sessionStoreService, ISubscribeStoreService subscribeStoreService, IMessageIdService messageIdService, IRetainMessageStoreService retainMessageStoreService, IDupPublishMessageStoreService dupPublishMessageStoreService, InternalCommunication internalCommunication) {
        this.sessionStoreService = sessionStoreService;
        this.subscribeStoreService = subscribeStoreService;
        this.messageIdService = messageIdService;
        this.retainMessageStoreService = retainMessageStoreService;
        this.dupPublishMessageStoreService = dupPublishMessageStoreService;
        this.internalCommunication = internalCommunication;
    }

    public void processPublish(ChannelContext channel, MqttPublishMessage msg) {
        // QoS=0
        if (msg.fixedHeader().qosLevel() == MqttQoS.AT_MOST_ONCE) {
            byte[] messageBytes = new byte[msg.payload().remaining()];
            msg.payload().get(messageBytes, 0, msg.payload().remaining());
            InternalMessage internalMessage = new InternalMessage().setTopic(msg.variableHeader().topicName())
                    .setMqttQoS(msg.fixedHeader().qosLevel().value()).setMessageBytes(messageBytes)
                    .setDup(false).setRetain(false);
            internalCommunication.internalSend(internalMessage);
            this.sendPublishMessage(msg.variableHeader().topicName(), msg.fixedHeader().qosLevel(), messageBytes, false, false);
        }
        // QoS=1
        if (msg.fixedHeader().qosLevel() == MqttQoS.AT_LEAST_ONCE) {
            byte[] messageBytes = new byte[msg.payload().remaining()];
            msg.payload().get(messageBytes, 0, msg.payload().remaining());
            InternalMessage internalMessage = new InternalMessage().setTopic(msg.variableHeader().topicName())
                    .setMqttQoS(msg.fixedHeader().qosLevel().value()).setMessageBytes(messageBytes)
                    .setDup(false).setRetain(false);
            internalCommunication.internalSend(internalMessage);
            this.sendPublishMessage(msg.variableHeader().topicName(), msg.fixedHeader().qosLevel(), messageBytes, false, false);
            this.sendPubAckMessage(channel, msg.variableHeader().packetId());
        }
        // QoS=2
        if (msg.fixedHeader().qosLevel() == MqttQoS.EXACTLY_ONCE) {
            byte[] messageBytes = new byte[msg.payload().remaining()];
            msg.payload().get(messageBytes, 0, msg.payload().remaining());
            InternalMessage internalMessage = new InternalMessage().setTopic(msg.variableHeader().topicName())
                    .setMqttQoS(msg.fixedHeader().qosLevel().value()).setMessageBytes(messageBytes)
                    .setDup(false).setRetain(false);
            internalCommunication.internalSend(internalMessage);
            this.sendPublishMessage(msg.variableHeader().topicName(), msg.fixedHeader().qosLevel(), messageBytes, false, false);
            this.sendPubRecMessage(channel, msg.variableHeader().packetId());
        }
        // retain=1, 保留消息
        if (msg.fixedHeader().isRetain()) {
            byte[] messageBytes = new byte[msg.payload().remaining()];
            msg.payload().get(messageBytes, 0, msg.payload().remaining());
            if (messageBytes.length == 0) {
                retainMessageStoreService.remove(msg.variableHeader().topicName());
            } else {
                RetainMessageStore retainMessageStore = new RetainMessageStore().setTopic(msg.variableHeader().topicName()).setMqttQoS(msg.fixedHeader().qosLevel().value())
                        .setMessageBytes(messageBytes);
                retainMessageStoreService.put(msg.variableHeader().topicName(), retainMessageStore);
            }
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
                    DupPublishMessageStore dupPublishMessageStore = new DupPublishMessageStore().setClientId(subscribeStore.getClientId())
                            .setTopic(topic).setMqttQoS(respQoS.value()).setMessageBytes(messageBytes);
                    dupPublishMessageStoreService.put(subscribeStore.getClientId(), dupPublishMessageStore);
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
                    DupPublishMessageStore dupPublishMessageStore = new DupPublishMessageStore().setClientId(subscribeStore.getClientId())
                            .setTopic(topic).setMqttQoS(respQoS.value()).setMessageBytes(messageBytes);
                    dupPublishMessageStoreService.put(subscribeStore.getClientId(), dupPublishMessageStore);
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

    private void sendPubAckMessage(ChannelContext channel, int messageId) {
        MqttPubAckMessage pubAckMessage = (MqttPubAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(messageId), null);
        MqttPacket mqttPacket = new MqttPacket();
        mqttPacket.setMqttFixedHeader(pubAckMessage.fixedHeader());
        mqttPacket.setBody(Lang.toBytes(pubAckMessage.variableHeader()));
        Tio.send(channel, mqttPacket);
    }

    private void sendPubRecMessage(ChannelContext channel, int messageId) {
        MqttMessage pubRecMessage = MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBREC, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(messageId), null);
        MqttPacket mqttPacket = new MqttPacket();
        mqttPacket.setMqttFixedHeader(pubRecMessage.fixedHeader());
        mqttPacket.setBody(Lang.toBytes(pubRecMessage.variableHeader()));
        Tio.send(channel, mqttPacket);
    }

}
