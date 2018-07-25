/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.wizzer.iot.mqtt.server.broker.protocol;

import cn.wizzer.iot.mqtt.server.common.message.IDupPublishMessageStoreService;
import cn.wizzer.iot.mqtt.server.common.message.IMessageIdService;
import cn.wizzer.iot.mqtt.server.tio.codec.MqttMessageIdVariableHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;

/**
 * PUBACK连接处理
 */
public class PubAck {

    private static final Logger LOGGER = LoggerFactory.getLogger(PubAck.class);

    private IMessageIdService messageIdService;

    private IDupPublishMessageStoreService dupPublishMessageStoreService;

    public PubAck(IMessageIdService messageIdService, IDupPublishMessageStoreService dupPublishMessageStoreService) {
        this.messageIdService = messageIdService;
        this.dupPublishMessageStoreService = dupPublishMessageStoreService;
    }

    public void processPubAck(ChannelContext channel, MqttMessageIdVariableHeader variableHeader) {
        int messageId = variableHeader.messageId();
        LOGGER.debug("PUBACK - clientId: {}, messageId: {}", (String) channel.getAttribute("clientId"), messageId);
        dupPublishMessageStoreService.remove((String) channel.getAttribute("clientId"), messageId);
        messageIdService.releaseMessageId(messageId);
    }

}
