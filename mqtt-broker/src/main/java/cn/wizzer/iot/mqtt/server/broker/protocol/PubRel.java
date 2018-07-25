/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.wizzer.iot.mqtt.server.broker.protocol;

import cn.wizzer.iot.mqtt.server.broker.packet.MqttPacket;
import cn.wizzer.iot.mqtt.server.tio.codec.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;

/**
 * PUBREL连接处理
 */
public class PubRel {

    private static final Logger LOGGER = LoggerFactory.getLogger(PubRel.class);

    public void processPubRel(ChannelContext channel, MqttMessageIdVariableHeader variableHeader) {
        MqttMessage pubCompMessage = MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBCOMP, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(variableHeader.messageId()), null);
        LOGGER.debug("PUBREL - clientId: {}, messageId: {}", (String) channel.getAttribute("clientId"), variableHeader.messageId());
        MqttPacket mqttPacket = new MqttPacket();
        mqttPacket.setMqttMessage(pubCompMessage);
        Tio.send(channel, mqttPacket);
    }

}
