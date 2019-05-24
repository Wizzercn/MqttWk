/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.wizzer.iot.mqtt.server.broker.protocol;

import cn.wizzer.iot.mqtt.server.broker.config.BrokerProperties;
import cn.wizzer.iot.mqtt.server.common.session.ISessionStoreService;
import cn.wizzer.iot.mqtt.server.common.session.SessionStore;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * PINGREQ连接处理
 */
public class PingReq {

    private static final Logger LOGGER = LoggerFactory.getLogger(PingReq.class);
    private ISessionStoreService sessionStoreService;
    private BrokerProperties brokerProperties;
    private ChannelGroup channelGroup;
    private Map<String, ChannelId> channelIdMap;

    public PingReq(ISessionStoreService sessionStoreService, BrokerProperties brokerProperties, ChannelGroup channelGroup, Map<String, ChannelId> channelIdMap) {
        this.sessionStoreService = sessionStoreService;
        this.brokerProperties = brokerProperties;
        this.channelGroup = channelGroup;
        this.channelIdMap = channelIdMap;
    }

    public void processPingReq(Channel channel, MqttMessage msg) {
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        if (sessionStoreService.containsKey(clientId)) {
            SessionStore sessionStore = sessionStoreService.get(clientId);
            ChannelId channelId = channelIdMap.get(sessionStore.getBrokerId() + "_" + sessionStore.getChannelId());
            if (brokerProperties.getId().equals(sessionStore.getBrokerId()) && channelId != null) {
                sessionStoreService.expire(clientId, sessionStore.getExpire());
                MqttMessage pingRespMessage = MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PINGRESP, false, MqttQoS.AT_MOST_ONCE, false, 0), null, null);
                LOGGER.debug("PINGREQ - clientId: {}", clientId);
                channel.writeAndFlush(pingRespMessage);
            }
        }
    }

}
