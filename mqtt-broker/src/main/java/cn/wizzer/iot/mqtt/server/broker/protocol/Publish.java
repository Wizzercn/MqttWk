/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.wizzer.iot.mqtt.server.broker.protocol;

import cn.wizzer.iot.mqtt.server.broker.config.BrokerProperties;
import cn.wizzer.iot.mqtt.server.broker.internal.InternalCommunication;
import cn.wizzer.iot.mqtt.server.broker.internal.InternalMessage;
import cn.wizzer.iot.mqtt.server.common.message.*;
import cn.wizzer.iot.mqtt.server.common.session.ISessionStoreService;
import cn.wizzer.iot.mqtt.server.common.session.SessionStore;
import cn.wizzer.iot.mqtt.server.common.subscribe.ISubscribeStoreService;
import cn.wizzer.iot.mqtt.server.common.subscribe.SubscribeStore;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

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

    private ChannelGroup channelGroup;

    private Map<String, ChannelId> channelIdMap;

    private BrokerProperties brokerProperties;

    public Publish(ISessionStoreService sessionStoreService, ISubscribeStoreService subscribeStoreService, IMessageIdService messageIdService, IRetainMessageStoreService retainMessageStoreService, IDupPublishMessageStoreService dupPublishMessageStoreService, InternalCommunication internalCommunication, ChannelGroup channelGroup, Map<String, ChannelId> channelIdMap, BrokerProperties brokerProperties) {
        this.sessionStoreService = sessionStoreService;
        this.subscribeStoreService = subscribeStoreService;
        this.messageIdService = messageIdService;
        this.retainMessageStoreService = retainMessageStoreService;
        this.dupPublishMessageStoreService = dupPublishMessageStoreService;
        this.internalCommunication = internalCommunication;
        this.channelGroup = channelGroup;
        this.channelIdMap = channelIdMap;
        this.brokerProperties = brokerProperties;
    }

    public void processPublish(Channel channel, MqttPublishMessage msg) {
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        // publish 延长session失效时间 - 优化: 直接get避免containsKey+get两次Redis调用
        SessionStore currentSession = sessionStoreService.get(clientId);
        if (currentSession != null) {
            ChannelId channelId = channelIdMap.get(currentSession.getBrokerId() + "_" + currentSession.getChannelId());
            if (brokerProperties.getId().equals(currentSession.getBrokerId()) && channelId != null) {
                sessionStoreService.expire(clientId, currentSession.getExpire());
            }
        }
        // 优化: payload只读取一次, 所有QoS分支和retain共用
        byte[] messageBytes = new byte[msg.payload().readableBytes()];
        msg.payload().getBytes(msg.payload().readerIndex(), messageBytes);
        String topic = msg.variableHeader().topicName();
        MqttQoS qosLevel = msg.fixedHeader().qosLevel();
        // 构建内部消息并转发
        InternalMessage internalMessage = new InternalMessage().setTopic(topic)
                .setMqttQoS(qosLevel.value()).setMessageBytes(messageBytes)
                .setDup(false).setRetain(false).setClientId(clientId);
        internalCommunication.internalSend(internalMessage);
        // 本地发布消息给订阅者
        this.sendPublishMessage(topic, qosLevel, messageBytes, false, false);
        // QoS=1 返回PUBACK
        if (qosLevel == MqttQoS.AT_LEAST_ONCE) {
            this.sendPubAckMessage(channel, msg.variableHeader().packetId());
        }
        // QoS=2 返回PUBREC
        if (qosLevel == MqttQoS.EXACTLY_ONCE) {
            this.sendPubRecMessage(channel, msg.variableHeader().packetId());
        }
        // retain=1, 保留消息
        if (msg.fixedHeader().isRetain()) {
            if (messageBytes.length == 0) {
                retainMessageStoreService.remove(topic);
            } else {
                RetainMessageStore retainMessageStore = new RetainMessageStore().setTopic(topic).setMqttQoS(qosLevel.value())
                        .setMessageBytes(messageBytes);
                retainMessageStoreService.put(topic, retainMessageStore);
            }
        }
    }

    private void sendPublishMessage(String topic, MqttQoS mqttQoS, byte[] messageBytes, boolean retain, boolean dup) {
        List<SubscribeStore> subscribeStores = subscribeStoreService.search(topic);
        for (SubscribeStore subscribeStore : subscribeStores) {
            // 优化: 直接get, 避免containsKey+get两次Redis调用
            SessionStore sessionStore = sessionStoreService.get(subscribeStore.getClientId());
            if (sessionStore == null) {
                continue;
            }
            // 订阅者收到MQTT消息的QoS级别, 最终取决于发布消息的QoS和主题订阅的QoS
            MqttQoS respQoS = mqttQoS.value() > subscribeStore.getMqttQoS() ? MqttQoS.valueOf(subscribeStore.getMqttQoS()) : mqttQoS;
            int messageId = 0;
            // QoS > 0 需要消息ID和重复消息存储
            if (respQoS != MqttQoS.AT_MOST_ONCE) {
                messageId = messageIdService.getNextMessageId();
                DupPublishMessageStore dupPublishMessageStore = new DupPublishMessageStore().setClientId(subscribeStore.getClientId())
                        .setTopic(topic).setMqttQoS(respQoS.value()).setMessageBytes(messageBytes).setMessageId(messageId);
                dupPublishMessageStoreService.put(subscribeStore.getClientId(), dupPublishMessageStore);
            }
            MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
                    new MqttPublishVariableHeader(topic, messageId), Unpooled.buffer().writeBytes(messageBytes));
            LOGGER.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}, messageId: {}", subscribeStore.getClientId(), topic, respQoS.value(), messageId);
            // 优化: sessionStore已经在前面获取, 不再重复查询Redis
            ChannelId channelId = channelIdMap.get(sessionStore.getBrokerId() + "_" + sessionStore.getChannelId());
            if (channelId != null) {
                Channel channel = channelGroup.find(channelId);
                if (channel != null) {
                    channel.writeAndFlush(publishMessage);
                }
            }
        }
    }

    private void sendPubAckMessage(Channel channel, int messageId) {
        MqttPubAckMessage pubAckMessage = (MqttPubAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(messageId), null);
        channel.writeAndFlush(pubAckMessage);
    }

    private void sendPubRecMessage(Channel channel, int messageId) {
        MqttMessage pubRecMessage = MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBREC, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(messageId), null);
        channel.writeAndFlush(pubRecMessage);
    }

}

