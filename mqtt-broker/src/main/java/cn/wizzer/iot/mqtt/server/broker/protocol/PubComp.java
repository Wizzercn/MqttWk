/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.wizzer.iot.mqtt.server.broker.protocol;

import cn.wizzer.iot.mqtt.server.common.message.IDupPubRelMessageStoreService;
import cn.wizzer.iot.mqtt.server.common.message.IMessageIdService;
import cn.wizzer.iot.mqtt.server.tio.codec.MqttMessageIdVariableHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;

/**
 * PUBCOMP连接处理
 */
public class PubComp {

    private static final Logger LOGGER = LoggerFactory.getLogger(PubComp.class);

    private IMessageIdService messageIdService;

    private IDupPubRelMessageStoreService dupPubRelMessageStoreService;

    public PubComp(IMessageIdService messageIdService, IDupPubRelMessageStoreService dupPubRelMessageStoreService) {
        this.messageIdService = messageIdService;
        this.dupPubRelMessageStoreService = dupPubRelMessageStoreService;
    }

    public void processPubComp(ChannelContext channel, MqttMessageIdVariableHeader variableHeader) {
        int messageId = variableHeader.messageId();
        LOGGER.debug("PUBCOMP - clientId: {}, messageId: {}", (String) channel.getAttribute("clientId"), messageId);
        dupPubRelMessageStoreService.remove((String) channel.getAttribute("clientId"), variableHeader.messageId());
    }
}
