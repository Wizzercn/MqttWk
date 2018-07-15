/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.wizzer.iot.mqtt.server.broker.protocol;

import cn.wizzer.iot.mqtt.server.broker.packet.MqttPacket;
import cn.wizzer.iot.mqtt.tio.codec.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;

/**
 * PINGREQ连接处理
 */
public class PingReq {

    private static final Logger LOGGER = LoggerFactory.getLogger(PingReq.class);

    public void processPingReq(ChannelContext channel, MqttMessage msg) {
        MqttMessage pingRespMessage = MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PINGRESP, false, MqttQoS.AT_MOST_ONCE, false, 0), null, null);
        LOGGER.debug("PINGREQ - clientId: {}", (String) channel.getAttribute("clientId"));
        MqttPacket mqttPacket = new MqttPacket();
        mqttPacket.setMqttFixedHeader(pingRespMessage.fixedHeader());
        Tio.send(channel, mqttPacket);
    }

}
