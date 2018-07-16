/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.wizzer.iot.mqtt.server.broker.protocol;

import cn.hutool.core.util.StrUtil;
import cn.wizzer.iot.mqtt.server.broker.packet.MqttPacket;
import cn.wizzer.iot.mqtt.server.common.message.IMessageIdService;
import cn.wizzer.iot.mqtt.server.common.message.IRetainMessageStoreService;
import cn.wizzer.iot.mqtt.server.common.message.RetainMessageStore;
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
import java.util.ArrayList;
import java.util.List;

/**
 * SUBSCRIBE连接处理
 */
public class Subscribe {

    private static final Logger LOGGER = LoggerFactory.getLogger(Subscribe.class);

    private ISubscribeStoreService subscribeStoreService;

    private IMessageIdService messageIdService;

    private IRetainMessageStoreService retainMessageStoreService;

    public Subscribe(ISubscribeStoreService subscribeStoreService, IMessageIdService messageIdService, IRetainMessageStoreService retainMessageStoreService) {
        this.subscribeStoreService = subscribeStoreService;
        this.messageIdService = messageIdService;
        this.retainMessageStoreService = retainMessageStoreService;
    }

    public void processSubscribe(ChannelContext channel, MqttSubscribeMessage msg) {
        List<MqttTopicSubscription> topicSubscriptions = msg.payload().topicSubscriptions();
        if (this.validTopicFilter(topicSubscriptions)) {
            String clientId = (String) channel.getAttribute("clientId");
            List<Integer> mqttQoSList = new ArrayList<Integer>();
            topicSubscriptions.forEach(topicSubscription -> {
                String topicFilter = topicSubscription.topicName();
                MqttQoS mqttQoS = topicSubscription.qualityOfService();
                SubscribeStore subscribeStore = new SubscribeStore(clientId, topicFilter, mqttQoS.value());
                subscribeStoreService.put(topicFilter, subscribeStore);
                mqttQoSList.add(mqttQoS.value());
                LOGGER.debug("SUBSCRIBE - clientId: {}, topFilter: {}, QoS: {}", clientId, topicFilter, mqttQoS.value());
            });
            MqttSubAckMessage subAckMessage = (MqttSubAckMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.SUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                    MqttMessageIdVariableHeader.from(msg.variableHeader().messageId()),
                    new MqttSubAckPayload(mqttQoSList));
            MqttPacket mqttPacket = new MqttPacket();
            mqttPacket.setMqttMessage(subAckMessage);
            Tio.send(channel, mqttPacket);
            // 发布保留消息
            topicSubscriptions.forEach(topicSubscription -> {
                String topicFilter = topicSubscription.topicName();
                MqttQoS mqttQoS = topicSubscription.qualityOfService();
                this.sendRetainMessage(channel, topicFilter, mqttQoS);
            });
        } else {
            Tio.close(channel, "");
        }
    }

    private boolean validTopicFilter(List<MqttTopicSubscription> topicSubscriptions) {
        for (MqttTopicSubscription topicSubscription : topicSubscriptions) {
            String topicFilter = topicSubscription.topicName();
            // 以#或+符号开头的、以/符号结尾的及不存在/符号的订阅按非法订阅处理, 这里没有参考标准协议
            if (StrUtil.startWith(topicFilter, '#') || StrUtil.startWith(topicFilter, '+') || StrUtil.endWith(topicFilter, '/') || !StrUtil.contains(topicFilter, '/'))
                return false;
            if (StrUtil.contains(topicFilter, '#')) {
                // 不是以/#字符串结尾的订阅按非法订阅处理
                if (!StrUtil.endWith(topicFilter, "/#")) return false;
                // 如果出现多个#符号的订阅按非法订阅处理
                if (StrUtil.count(topicFilter, '#') > 1) return false;
            }
            if (StrUtil.contains(topicFilter, '+')) {
                //如果+符号和/+字符串出现的次数不等的情况按非法订阅处理
                if (StrUtil.count(topicFilter, '+') != StrUtil.count(topicFilter, "/+")) return false;
            }
        }
        return true;
    }

    private void sendRetainMessage(ChannelContext channel, String topicFilter, MqttQoS mqttQoS) {
        List<RetainMessageStore> retainMessageStores = retainMessageStoreService.search(topicFilter);
        retainMessageStores.forEach(retainMessageStore -> {
            MqttQoS respQoS = retainMessageStore.getMqttQoS() > mqttQoS.value() ? mqttQoS : MqttQoS.valueOf(retainMessageStore.getMqttQoS());
            if (respQoS == MqttQoS.AT_MOST_ONCE) {
                MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBLISH, false, respQoS, false, 0),
                        new MqttPublishVariableHeader(retainMessageStore.getTopic(), 0), ByteBuffer.wrap(retainMessageStore.getMessageBytes()));
                LOGGER.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}", (String) channel.getAttribute("clientId"), retainMessageStore.getTopic(), respQoS.value());
                MqttPacket mqttPacket = new MqttPacket();
                mqttPacket.setMqttMessage(publishMessage);
                Tio.send(channel, mqttPacket);
            }
            if (respQoS == MqttQoS.AT_LEAST_ONCE) {
                int messageId = messageIdService.getNextMessageId();
                MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBLISH, false, respQoS, false, 0),
                        new MqttPublishVariableHeader(retainMessageStore.getTopic(), messageId), ByteBuffer.wrap(retainMessageStore.getMessageBytes()));
                LOGGER.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}, messageId: {}", (String) channel.getAttribute("clientId"), retainMessageStore.getTopic(), respQoS.value(), messageId);
                MqttPacket mqttPacket = new MqttPacket();
                mqttPacket.setMqttMessage(publishMessage);
                Tio.send(channel, mqttPacket);
            }
            if (respQoS == MqttQoS.EXACTLY_ONCE) {
                int messageId = messageIdService.getNextMessageId();
                MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBLISH, false, respQoS, false, 0),
                        new MqttPublishVariableHeader(retainMessageStore.getTopic(), messageId), ByteBuffer.wrap(retainMessageStore.getMessageBytes()));
                LOGGER.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}, messageId: {}", (String) channel.getAttribute("clientId"), retainMessageStore.getTopic(), respQoS.value(), messageId);
                MqttPacket mqttPacket = new MqttPacket();
                mqttPacket.setMqttMessage(publishMessage);
                Tio.send(channel, mqttPacket);
            }
        });
    }

}
