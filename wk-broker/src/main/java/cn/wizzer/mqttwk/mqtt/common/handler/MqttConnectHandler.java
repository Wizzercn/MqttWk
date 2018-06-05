package cn.wizzer.mqttwk.mqtt.common.handler;

import cn.wizzer.mqttwk.mqtt.ConnectionDescriptor;
import cn.wizzer.mqttwk.mqtt.ConnectionDescriptorStore;
import cn.wizzer.mqttwk.mqtt.common.MqttPacket;
import cn.wizzer.mqttwk.mqtt.common.connections.IConnectionsManager;
import cn.wizzer.mqttwk.mqtt.common.intf.AbsMqttBsHandler;
import cn.wizzer.mqttwk.mqtt.common.intf.MqttBsHandlerIntf;
import cn.wizzer.mqttwk.mqtt.common.message.*;
import cn.wizzer.mqttwk.mqtt.common.spi.impl.subscriptions.Subscription;
import cn.wizzer.mqttwk.mqtt.common.utils.ByteUtil;
import cn.wizzer.mqttwk.mqtt.common.utils.MqttUtils;
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
import org.tio.core.Tio;
import org.tio.core.ChannelContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
    // maps clientID to Will testament, if specified on CONNECT
    private ConcurrentMap<String, MqttWillMessage> m_willStore = new ConcurrentHashMap<>();

    public void init() {
        allowAnonymous = conf.getBoolean("mqttwk.allowAnonymous", true);
        allowZeroByteClientId = conf.getBoolean("mqttwk.allowZeroByteClientId", false);
        digestMethod = conf.get("mqttwk.user.digestMethod", "MD5");
        //加载用户名及密码
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
        log.info("Configuring message interceptors...");

    }

    @Override
    public Object handler(MqttPacket packet, MqttMessage message, ChannelContext channelContext) throws Exception {
        MqttConnectMessage msg = (MqttConnectMessage) message;
        MqttMessageType messageType = msg.fixedHeader().messageType();
        log.debug("收到消息:{}", Json.toJson(message));
        MqttConnectPayload payload = msg.payload();
        String clientId = payload.clientIdentifier();
        log.debug("Processing CONNECT message. CId={}, username={}", clientId, payload.userName());
        //协议版本判断
        if (msg.variableHeader().version() != MqttVersion.MQTT_3_1.protocolLevel()
                && msg.variableHeader().version() != MqttVersion.MQTT_3_1_1.protocolLevel()) {
            Tio.send(channelContext, connAckPacket(CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION, false));
            Tio.close(channelContext, "MQTT protocol version is not valid");
            log.error("MQTT protocol version is not valid. CId={}", clientId);
            return null;
        }
        //如果clientId为空,而服务端配置clientId不能为空,则拒绝连接
        final boolean cleanSession = msg.variableHeader().isCleanSession();
        if (clientId == null || clientId.length() == 0) {
            if (!cleanSession || !this.allowZeroByteClientId) {
                Tio.send(channelContext, connAckPacket(CONNECTION_REFUSED_IDENTIFIER_REJECTED, false));
                Tio.close(channelContext, "The MQTT client ID cannot be empty");
                log.error("The MQTT client ID cannot be empty. Username={}", payload.userName());
                return null;
            }
            // 生成clientId
            clientId = UUID.randomUUID().toString().replace("-", "");
            log.info("Client has connected with a server generated identifier. CId={}, username={}", clientId,
                    payload.userName());
        }
        //验证登录
        if (!login(channelContext, msg, clientId)) {
            return null;
        }
        //
        ConnectionDescriptor descriptor = new ConnectionDescriptor(clientId, channelContext, cleanSession);
        final ConnectionDescriptor existing = this.connectionDescriptors.addConnection(descriptor);
        if (existing != null) {
            log.info("Client ID is being used in an existing connection, force to be closed. CId={}", clientId);
            existing.abort();
            this.connectionDescriptors.removeConnection(existing);
            this.connectionDescriptors.addConnection(descriptor);
        }

        initializeKeepAliveTimeout(channelContext, msg, clientId);
        storeWillMessage(msg, clientId);
        if (!sendAck(descriptor, msg, clientId)) {
            Tio.close(channelContext, "");
            return null;
        }
//
        m_interceptor.notifyClientConnected(msg);
        //
        if (!descriptor.assignState(SENDACK, SESSION_CREATED)) {
            Tio.close(channelContext, "");
            return null;
        }
        final ClientSession clientSession = this.sessionsRepository.createOrLoadClientSession(clientId, cleanSession);

        if (!republish(descriptor, msg, clientSession)) {
            Tio.close(channelContext, "");
            return null;
        }

        int flushIntervalMs = 500/* (keepAlive * 1000) / 2 */;
        setupAutoFlusher(channel, flushIntervalMs);

        final boolean success = descriptor.assignState(MESSAGES_REPUBLISHED, ESTABLISHED);
        if (!success) {
            Tio.close(channelContext, "");
        }

        log.info("Connected client <{}> with login <{}>", clientId, payload.userName());
        return null;
    }


    private boolean sendAck(ConnectionDescriptor descriptor, MqttConnectMessage msg, String clientId) throws IOException {
        log.debug("Sending CONNACK. CId={}", clientId);
        final boolean success = descriptor.assignState(DISCONNECTED, SENDACK);
        if (!success) {
            return false;
        }

        MqttPacket okResp;
        ClientSession clientSession = this.sessionsRepository.sessionForClient(clientId);
        boolean isSessionAlreadyStored = clientSession != null;
        final boolean msgCleanSessionFlag = msg.variableHeader().isCleanSession();
        if (!msgCleanSessionFlag && isSessionAlreadyStored) {
            okResp = connAckPacket(CONNECTION_ACCEPTED,true);
        } else {
            okResp = connAckPacket(CONNECTION_ACCEPTED,false);
        }
        descriptor.writeAndFlush(mqttPacket);
        log.debug("CONNACK has been sent. CId={}", clientId);

        if (isSessionAlreadyStored && msgCleanSessionFlag) {
            for (Subscription existingSub : clientSession.getSubscriptions()) {
                this.subscriptions.removeSubscription(existingSub.getTopicFilter(), clientId);
            }
        }
        return true;
    }

    private void initializeKeepAliveTimeout(ChannelContext channelContext, MqttConnectMessage msg, String clientId) {
        int keepAlive = msg.variableHeader().keepAliveTimeSeconds();

        MqttUtils.keepAlive(channelContext, keepAlive);
        MqttUtils.cleanSession(channelContext, msg.variableHeader().isCleanSession());
        MqttUtils.clientID(channelContext, clientId);
        int idleTime = Math.round(keepAlive * 1.5f);
        channelContext.getGroupContext().setHeartbeatTimeout(idleTime);
        log.debug("Connection has been configured CId={}, keepAlive={}, removeTemporaryQoS2={}, idleTime={}",
                clientId, keepAlive, msg.variableHeader().isCleanSession(), idleTime);
    }

    private void storeWillMessage(MqttConnectMessage msg, String clientId) {
        // Handle will flag
        if (msg.variableHeader().isWillFlag()) {
            MqttQoS willQos = MqttQoS.valueOf(msg.variableHeader().willQos());
            log.debug("Configuring MQTT last will and testament CId={}, willQos={}, willTopic={}, willRetain={}",
                    clientId, willQos, msg.payload().willTopic(), msg.variableHeader().isWillRetain());
            byte[] willPayload = msg.payload().willMessageInBytes();
            ByteBuffer bb = (ByteBuffer) ByteBuffer.allocate(willPayload.length).put(willPayload).flip();
            // save the will testament in the clientID store
            MqttWillMessage will = new MqttWillMessage(msg.payload().willTopic(), bb, msg.variableHeader().isWillRetain(),
                    willQos);
            m_willStore.put(clientId, will);
            log.debug("MQTT last will and testament has been configured. CId={}", clientId);
        }
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
            channelContext.setAttribute("username",login);
            channelContext.setAttribute("clientId",clientId);
        } else if (!this.allowAnonymous) {
            log.error("Client didn't supply any credentials and MQTT anonymous mode is disabled. CId={}", clientId);
            failedCredentials(channelContext);
            return false;
        }
        return true;
    }

    private void failedCredentials(ChannelContext channelContext) throws IOException {
        Tio.send(channelContext, connAckPacket(CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD, false));
        Tio.close(channelContext, "Client {} failed to connect with bad username or password.");
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
        String authPwd = Strings.sNull(authMap.get(username));
        return Strings.isNotBlank(authPwd) && authPwd.equals(encodedPasswd);
    }

    private MqttPacket connAckPacket(MqttConnectReturnCode returnCode, boolean sessionPresent) throws IOException {
        MqttConnAckMessage mqttConnAckMessage = connAck(returnCode, sessionPresent);
        MqttPacket mqttPacket = new MqttPacket();
        mqttPacket.setMqttFixedHeader(mqttConnAckMessage.fixedHeader());
        mqttPacket.setBody(ByteUtil.getBytes(mqttConnAckMessage.variableHeader()));
        return mqttPacket;
    }

    private MqttConnAckMessage connAck(MqttConnectReturnCode returnCode, boolean sessionPresent) {
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE,
                false, 0);
        MqttConnAckVariableHeader mqttConnAckVariableHeader = new MqttConnAckVariableHeader(returnCode, sessionPresent);
        return new MqttConnAckMessage(mqttFixedHeader, mqttConnAckVariableHeader);
    }
}
