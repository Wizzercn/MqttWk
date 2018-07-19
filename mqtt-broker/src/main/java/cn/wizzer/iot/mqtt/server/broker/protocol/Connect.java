/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.wizzer.iot.mqtt.server.broker.protocol;

import cn.hutool.core.util.StrUtil;
import cn.wizzer.iot.mqtt.server.broker.config.BrokerProperties;
import cn.wizzer.iot.mqtt.server.broker.packet.MqttPacket;
import cn.wizzer.iot.mqtt.server.broker.service.TioService;
import cn.wizzer.iot.mqtt.server.common.auth.IAuthService;
import cn.wizzer.iot.mqtt.server.common.message.DupPubRelMessageStore;
import cn.wizzer.iot.mqtt.server.common.message.DupPublishMessageStore;
import cn.wizzer.iot.mqtt.server.common.message.IDupPubRelMessageStoreService;
import cn.wizzer.iot.mqtt.server.common.message.IDupPublishMessageStoreService;
import cn.wizzer.iot.mqtt.server.common.session.ISessionStoreService;
import cn.wizzer.iot.mqtt.server.common.session.SessionStore;
import cn.wizzer.iot.mqtt.server.common.subscribe.ISubscribeStoreService;
import cn.wizzer.iot.mqtt.tio.codec.*;
import org.nutz.lang.Encoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * CONNECT连接处理
 */
public class Connect {

    private static final Logger LOGGER = LoggerFactory.getLogger(Connect.class);

    private ISessionStoreService sessionStoreService;

    private ISubscribeStoreService subscribeStoreService;

    private IDupPublishMessageStoreService dupPublishMessageStoreService;

    private IDupPubRelMessageStoreService dupPubRelMessageStoreService;

    private IAuthService authService;

    private TioService tioService;

    private BrokerProperties brokerProperties;

    public Connect(ISessionStoreService sessionStoreService, ISubscribeStoreService subscribeStoreService, IDupPublishMessageStoreService dupPublishMessageStoreService, IDupPubRelMessageStoreService dupPubRelMessageStoreService, IAuthService authService, TioService tioService, BrokerProperties brokerProperties) {
        this.sessionStoreService = sessionStoreService;
        this.subscribeStoreService = subscribeStoreService;
        this.dupPublishMessageStoreService = dupPublishMessageStoreService;
        this.dupPubRelMessageStoreService = dupPubRelMessageStoreService;
        this.authService = authService;
        this.tioService = tioService;
        this.brokerProperties = brokerProperties;
    }

    public void processConnect(ChannelContext channel, MqttConnectMessage msg) {
        // 消息解码器出现异常
        if ("UNFINISHED".equals(msg.decoderResult())) {
            Throwable cause = msg.getCause();
            if (cause instanceof MqttUnacceptableProtocolVersionException) {
                // 不支持的协议版本
                MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                        new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION, false), null);
                MqttPacket mqttPacket = new MqttPacket();
                mqttPacket.setMqttMessage(connAckMessage);
                Tio.send(channel, mqttPacket);
                Tio.close(channel, "");
                return;
            } else if (cause instanceof MqttIdentifierRejectedException) {
                // 不合格的clientId
                MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                        new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED, false), null);
                MqttPacket mqttPacket = new MqttPacket();
                mqttPacket.setMqttMessage(connAckMessage);
                Tio.send(channel, mqttPacket);
                Tio.close(channel, "");
                return;
            }
            Tio.close(channel, "");
            return;
        }
        // clientId为空或null的情况, 这里要求客户端必须提供clientId, 不管cleanSession是否为1, 此处没有参考标准协议实现
        if (StrUtil.isBlank(msg.payload().clientIdentifier())) {
            MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                    new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED, false), null);
            MqttPacket mqttPacket = new MqttPacket();
            mqttPacket.setMqttMessage(connAckMessage);
            Tio.send(channel, mqttPacket);
            Tio.close(channel, "");
            return;
        }
        //如果服务端强制要求进行用户名及密码验证
        if (brokerProperties.getMqttPasswordMust()) {
            String username = msg.payload().userName();
            String password = msg.payload().passwordInBytes() == null ? null : new String(msg.payload().passwordInBytes(), Encoding.CHARSET_UTF8);
            if (!authService.checkValid(username, password)) {
                MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                        new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD, false), null);
                MqttPacket mqttPacket = new MqttPacket();
                mqttPacket.setMqttMessage(connAckMessage);
                Tio.send(channel, mqttPacket);
                Tio.close(channel, "");
                return;
            }
        }
        // 如果会话中已存储这个新连接的clientId, 就关闭之前该clientId的连接
        if (sessionStoreService.containsKey(msg.payload().clientIdentifier())) {
            SessionStore sessionStore = sessionStoreService.get(msg.payload().clientIdentifier());
            ChannelContext previous = tioService.getChannel(sessionStore.getClientId());
            Boolean cleanSession = sessionStore.isCleanSession();
            if (cleanSession) {
                sessionStoreService.remove(msg.payload().clientIdentifier());
                subscribeStoreService.removeForClient(msg.payload().clientIdentifier());
                dupPublishMessageStoreService.removeByClient(msg.payload().clientIdentifier());
                dupPubRelMessageStoreService.removeByClient(msg.payload().clientIdentifier());
            }
            if (previous != null)
                Tio.close(previous, "");
        }
        // 处理遗嘱信息
        SessionStore sessionStore = new SessionStore(msg.payload().clientIdentifier(), msg.variableHeader().isCleanSession(), null);
        if (msg.variableHeader().isWillFlag()) {
            MqttPublishMessage willMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.PUBLISH, false, MqttQoS.valueOf(msg.variableHeader().willQos()), msg.variableHeader().isWillRetain(), 0),
                    new MqttPublishVariableHeader(msg.payload().willTopic(), 0), msg.payload().willMessageInBytes());
            sessionStore.setWillMessage(willMessage);
        }
        // 处理连接心跳包
        if (msg.variableHeader().keepAliveTimeSeconds() > 0) {
            int idleTime = Math.round(msg.variableHeader().keepAliveTimeSeconds() * 1.5f);
            channel.getGroupContext().setHeartbeatTimeout(idleTime * 1000);
        }
        // 至此存储会话信息及返回接受客户端连接
        sessionStoreService.put(msg.payload().clientIdentifier(), sessionStore);
        // 绑定业务号
        Tio.bindBsId(channel, msg.payload().clientIdentifier());
        channel.setAttribute("clientId", msg.payload().clientIdentifier());
        Boolean sessionPresent = sessionStoreService.containsKey(msg.payload().clientIdentifier()) && !msg.variableHeader().isCleanSession();
        MqttConnAckMessage okResp = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_ACCEPTED, sessionPresent), null);
        MqttPacket okRespPacket = new MqttPacket();
        okRespPacket.setMqttMessage(okResp);
        Tio.send(channel, okRespPacket);
        LOGGER.debug("CONNECT - clientId: {}, cleanSession: {}", msg.payload().clientIdentifier(), msg.variableHeader().isCleanSession());
        // 如果cleanSession为0, 需要重发同一clientId存储的未完成的QoS1和QoS2的DUP消息
        if (!msg.variableHeader().isCleanSession()) {
            List<DupPublishMessageStore> dupPublishMessageStoreList = dupPublishMessageStoreService.get(msg.payload().clientIdentifier());
            List<DupPubRelMessageStore> dupPubRelMessageStoreList = dupPubRelMessageStoreService.get(msg.payload().clientIdentifier());
            dupPublishMessageStoreList.forEach(dupPublishMessageStore -> {
                MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBLISH, true, MqttQoS.valueOf(dupPublishMessageStore.getMqttQoS()), false, 0),
                        new MqttPublishVariableHeader(dupPublishMessageStore.getTopic(), dupPublishMessageStore.getMessageId()), ByteBuffer.wrap(dupPublishMessageStore.getMessageBytes()));
                MqttPacket publishPacket = new MqttPacket();
                publishPacket.setMqttMessage(publishMessage);
                Tio.send(channel, publishPacket);
            });
            dupPubRelMessageStoreList.forEach(dupPubRelMessageStore -> {
                MqttMessage pubRelMessage = MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBREL, true, MqttQoS.AT_MOST_ONCE, false, 0),
                        MqttMessageIdVariableHeader.from(dupPubRelMessageStore.getMessageId()), null);
                MqttPacket pubRelPacket = new MqttPacket();
                pubRelPacket.setMqttMessage(pubRelMessage);
                Tio.send(channel, pubRelPacket);
            });
        }
    }

}
