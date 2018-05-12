package cn.wizzer.mqttwk.mqtt.common.handler;

import cn.wizzer.mqttwk.mqtt.common.MqttPacket;
import cn.wizzer.mqttwk.mqtt.common.intf.AbsMqttBsHandler;
import cn.wizzer.mqttwk.mqtt.common.intf.MqttBsHandlerIntf;
import cn.wizzer.mqttwk.mqtt.common.message.*;
import cn.wizzer.mqttwk.mqtt.common.utils.ByteUtil;
import org.apache.commons.codec.binary.Hex;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.lang.Encoding;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.Aio;
import org.tio.core.ChannelContext;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static cn.wizzer.mqttwk.mqtt.common.message.MqttConnectReturnCode.*;

/**
 * Created by wizzer on 2018/5/9.
 */
@IocBean(create = "init")
public class MqttConnectHandler extends AbsMqttBsHandler<MqttConnectMessage> implements MqttBsHandlerIntf {
    private static Logger log = LoggerFactory.getLogger(MqttConnectHandler.class);
    @Inject
    private PropertiesProxy conf;
    private boolean allowAnonymous;
    private boolean allowZeroByteClientId;
    private String digestMethod;
    private NutMap authMap = NutMap.NEW();
    private MessageDigest messageDigest;

    public void init() {
        allowAnonymous = conf.getBoolean("mqttwk.allowAnonymous", true);
        allowZeroByteClientId = conf.getBoolean("mqttwk.allowZeroByteClientId", false);
        digestMethod = conf.get("mqttwk.user.digestMethod", "MD5");
        Map<String, Object> tempMap = Lang.filter(new LinkedHashMap<>(conf), "mqttwk.user.", null, null, null);
        for (int i = 0; i < tempMap.size(); i++) {
            if (tempMap.get("" + i + ".username") != null)
                authMap.put(Strings.sNull(tempMap.get("" + i + ".username")), Strings.sNull(tempMap.get("" + i + ".password")));
        }
        try {
            this.messageDigest = MessageDigest.getInstance(digestMethod);
        } catch (NoSuchAlgorithmException nsaex) {
            log.error(String.format("Can't find %s for password encoding", digestMethod), nsaex);
            throw new RuntimeException(nsaex);
        }
    }

    @Override
    public Object handler(MqttPacket packet, MqttMessage message, ChannelContext channelContext) throws Exception {
        MqttConnectMessage msg = (MqttConnectMessage) message;
        MqttMessageType messageType = msg.fixedHeader().messageType();
        log.debug("收到消息:{}", Json.toJson(message));
        MqttConnectPayload payload = msg.payload();
        String clientId = payload.clientIdentifier();
        log.debug("Processing CONNECT message. CId={}, username={}", clientId, payload.userName());

        if (msg.variableHeader().version() != MqttVersion.MQTT_3_1.protocolLevel()
                && msg.variableHeader().version() != MqttVersion.MQTT_3_1_1.protocolLevel()) {
            MqttConnAckMessage badProto = connAck(CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION);
            MqttPacket mqttPacket = new MqttPacket();
            mqttPacket.setMqttFixedHeader(badProto.fixedHeader());
            mqttPacket.setBody(ByteUtil.getBytes(badProto.variableHeader()));
            log.error("MQTT protocol version is not valid. CId={}", clientId);
            Aio.send(channelContext, mqttPacket);
            Aio.close(channelContext, "");
            return null;
        }

        final boolean cleanSession = msg.variableHeader().isCleanSession();
        if (clientId == null || clientId.length() == 0) {
            if (!cleanSession || !this.allowZeroByteClientId) {
                MqttConnAckMessage badId = connAck(CONNECTION_REFUSED_IDENTIFIER_REJECTED);
                MqttPacket mqttPacket = new MqttPacket();
                mqttPacket.setMqttFixedHeader(badId.fixedHeader());
                mqttPacket.setBody(ByteUtil.getBytes(badId.variableHeader()));
                log.error("MQTT protocol version is not valid. CId={}", clientId);
                Aio.send(channelContext, mqttPacket);
                Aio.close(channelContext, "");
                log.error("The MQTT client ID cannot be empty. Username={}", payload.userName());
                return null;
            }

            // Generating client id.
            clientId = UUID.randomUUID().toString().replace("-", "");
            log.info("Client has connected with a server generated identifier. CId={}, username={}", clientId,
                    payload.userName());
        }

        if (!login(channelContext, msg, clientId)) {
            Aio.close(channelContext, "");
            return null;
        }
//
//        ConnectionDescriptor descriptor = new ConnectionDescriptor(clientId, channel, cleanSession);
//        final ConnectionDescriptor existing = this.connectionDescriptors.addConnection(descriptor);
//        if (existing != null) {
//            LOG.info("Client ID is being used in an existing connection, force to be closed. CId={}", clientId);
//            existing.abort();
//            //return;
//            this.connectionDescriptors.removeConnection(existing);
//            this.connectionDescriptors.addConnection(descriptor);
//        }
//
//        initializeKeepAliveTimeout(channel, msg, clientId);
//        storeWillMessage(msg, clientId);
//        if (!sendAck(descriptor, msg, clientId)) {
//            channel.close().addListener(CLOSE_ON_FAILURE);
//            return null;
//        }
//
//        m_interceptor.notifyClientConnected(msg);
//
//        if (!descriptor.assignState(SENDACK, SESSION_CREATED)) {
//            channel.close().addListener(CLOSE_ON_FAILURE);
//            return null;
//        }
//        final ClientSession clientSession = this.sessionsRepository.createOrLoadClientSession(clientId, cleanSession);
//
//        if (!republish(descriptor, msg, clientSession)) {
//            channel.close().addListener(CLOSE_ON_FAILURE);
//            return null;
//        }
//
//        int flushIntervalMs = 500/* (keepAlive * 1000) / 2 */;
//        setupAutoFlusher(channel, flushIntervalMs);
//
//        final boolean success = descriptor.assignState(MESSAGES_REPUBLISHED, ESTABLISHED);
//        if (!success) {
//            channel.close().addListener(CLOSE_ON_FAILURE);
//        }

        log.info("Connected client <{}> with login <{}>", clientId, payload.userName());
        return null;
    }

    private boolean login(ChannelContext channelContext, MqttConnectMessage msg, final String clientId) throws IOException {
        // handle user authentication
        if (msg.variableHeader().hasUserName()) {
            byte[] pwd = null;
            if (msg.variableHeader().hasPassword()) {
                pwd = msg.payload().passwordInBytes();
            } else if (!allowAnonymous) {
                log.error("Client didn't supply any password and MQTT anonymous mode is disabled CId={}", clientId);
                failedCredentials(channelContext);
                return false;
            }
            final String login = msg.payload().userName();
            if (!checkValid(clientId, login, pwd)) {
                log.error("Authenticator has rejected the MQTT credentials CId={}, username={}", clientId, login);
                failedCredentials(channelContext);
                return false;
            }
            //将用户名绑定到频道
            Aio.bindUser(channelContext, login);
        } else if (!this.allowAnonymous) {
            log.error("Client didn't supply any credentials and MQTT anonymous mode is disabled. CId={}", clientId);
            failedCredentials(channelContext);
            return false;
        }
        return true;
    }

    private void failedCredentials(ChannelContext channelContext) throws IOException {
        MqttConnAckMessage badId = connAck(CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD);
        MqttPacket mqttPacket = new MqttPacket();
        mqttPacket.setMqttFixedHeader(badId.fixedHeader());
        mqttPacket.setBody(ByteUtil.getBytes(badId.variableHeader()));
        Aio.send(channelContext, mqttPacket);
        Aio.close(channelContext, "");
        log.info("Client {} failed to connect with bad username or password.", channelContext);
    }

    private boolean checkValid(String clientId, String username, byte[] password) {
        if (username == null || password == null) {
            log.info("username or password was null");
            return false;
        }
        messageDigest.update(password);
        byte[] digest = messageDigest.digest();
        String encodedPasswd = new String(Hex.encodeHex(digest));
        return Strings.sNull(authMap.get(username)).equals(encodedPasswd);
    }

    private MqttConnAckMessage connAck(MqttConnectReturnCode returnCode) {
        return connAck(returnCode, false);
    }

    private MqttConnAckMessage connAckWithSessionPresent(MqttConnectReturnCode returnCode) {
        return connAck(returnCode, true);
    }

    private MqttConnAckMessage connAck(MqttConnectReturnCode returnCode, boolean sessionPresent) {
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE,
                false, 0);
        MqttConnAckVariableHeader mqttConnAckVariableHeader = new MqttConnAckVariableHeader(returnCode, sessionPresent);
        return new MqttConnAckMessage(mqttFixedHeader, mqttConnAckVariableHeader);
    }
}
