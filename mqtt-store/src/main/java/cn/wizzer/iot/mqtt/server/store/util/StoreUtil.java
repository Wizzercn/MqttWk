package cn.wizzer.iot.mqtt.server.store.util;

import cn.wizzer.iot.mqtt.server.common.session.SessionStore;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.mqtt.*;
import org.nutz.lang.util.NutMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by cs on 2018
 */
public class StoreUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreUtil.class);

    public static NutMap transPublishToMapBeta(SessionStore store) {
        try {
            NutMap sessionStore = new NutMap();
            sessionStore.addv("clientId", store.getClientId());
            sessionStore.addv("channelId", store.getChannelId());
            sessionStore.addv("cleanSession", store.isCleanSession());
            MqttPublishMessage msg = store.getWillMessage();
            if (null != msg) {
                sessionStore.addv("payload", new String(msg.payload().array(), "UTF-8"));
                sessionStore.addv("messageType", msg.fixedHeader().messageType().value());
                sessionStore.addv("isDup", msg.fixedHeader().isDup());
                sessionStore.addv("qosLevel", msg.fixedHeader().qosLevel().value());
                sessionStore.addv("isRetain", msg.fixedHeader().isRetain());
                sessionStore.addv("remainingLength", msg.fixedHeader().remainingLength());

                sessionStore.addv("topicName", msg.variableHeader().topicName());
                sessionStore.addv("packetId", msg.variableHeader().packetId());
                sessionStore.addv("msg", true);
            }

            return sessionStore;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    public static SessionStore mapTransToPublishMsgBeta(NutMap store) {
        SessionStore sessionStore = new SessionStore();
        String payload = store.getString("payload");
        ByteBuf buf = ByteBufUtil.writeUtf8(ByteBufAllocator.DEFAULT, payload);
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(
                MqttMessageType.valueOf(store.getInt("messageType")),
                store.getBoolean("isDup"),
                MqttQoS.valueOf(store.getInt("qosLevel")),
                store.getBoolean("isRetain"),
                store.getInt("remainingLength"));
        MqttPublishVariableHeader mqttPublishVariableHeader = new MqttPublishVariableHeader(store.getString("topicName"),
                store.getInt("packetId"));
        MqttPublishMessage mqttPublishMessage = new MqttPublishMessage(mqttFixedHeader, mqttPublishVariableHeader, buf);
        sessionStore.setChannelId(store.getString("channelId"));
        sessionStore.setClientId(store.getString("clientId"));
        sessionStore.setCleanSession(store.getBoolean("cleanSession"));
        sessionStore.setWillMessage(mqttPublishMessage);
        return sessionStore;
    }

}
