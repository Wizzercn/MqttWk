/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.wizzer.iot.mqtt.server.broker.protocol;

import cn.wizzer.iot.mqtt.server.broker.packet.MqttPacket;
import cn.wizzer.iot.mqtt.server.common.message.DupPubRelMessageStore;
import cn.wizzer.iot.mqtt.server.common.message.IDupPubRelMessageStoreService;
import cn.wizzer.iot.mqtt.server.common.message.IDupPublishMessageStoreService;
import cn.wizzer.iot.mqtt.tio.codec.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;

/**
 * PUBREC连接处理
 */
public class PubRec {

    private static final Logger LOGGER = LoggerFactory.getLogger(PubRel.class);

    private IDupPublishMessageStoreService dupPublishMessageStoreService;

    private IDupPubRelMessageStoreService dupPubRelMessageStoreService;

    public PubRec(IDupPublishMessageStoreService dupPublishMessageStoreService, IDupPubRelMessageStoreService dupPubRelMessageStoreService) {
        this.dupPublishMessageStoreService = dupPublishMessageStoreService;
        this.dupPubRelMessageStoreService = dupPubRelMessageStoreService;
    }

    public void processPubRec(ChannelContext channel, MqttMessageIdVariableHeader variableHeader) {
        MqttMessage pubRelMessage = MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBREL, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(variableHeader.messageId()), null);
        LOGGER.debug("PUBREC - clientId: {}, messageId: {}", (String) channel.getAttribute("clientId"), variableHeader.messageId());
        dupPublishMessageStoreService.remove((String) channel.getAttribute("clientId"), variableHeader.messageId());
        DupPubRelMessageStore dupPubRelMessageStore = new DupPubRelMessageStore().setClientId((String) channel.getAttribute("clientId"))
                .setMessageId(variableHeader.messageId());
        dupPubRelMessageStoreService.put((String) channel.getAttribute("clientId"), dupPubRelMessageStore);
        MqttPacket mqttPacket = new MqttPacket();
        mqttPacket.setMqttMessage(pubRelMessage);
        Tio.send(channel, mqttPacket);
    }

}
