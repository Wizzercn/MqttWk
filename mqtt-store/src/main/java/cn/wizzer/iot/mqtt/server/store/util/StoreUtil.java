package cn.wizzer.iot.mqtt.server.store.util;

import cn.wizzer.iot.mqtt.server.common.session.SessionStore;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.*;
import org.nutz.lang.util.NutMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;

/**
 * Created by cs on 2018
 */
public class StoreUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreUtil.class);

    public static NutMap transPublishToMapBeta(SessionStore store) {
        try {
            NutMap sessionStore = new NutMap();
            sessionStore.put("clientId", store.getClientId());
            sessionStore.put("channelId", store.getChannelId());
            sessionStore.put("cleanSession", store.isCleanSession());
            sessionStore.put("brokerId", store.getBrokerId());
            sessionStore.put("expire", store.getExpire());
            MqttPublishMessage msg = store.getWillMessage();
            if (null != msg) {
                sessionStore.addv("payload", Base64.getEncoder().encodeToString(msg.payload().array()));
                sessionStore.addv("messageType", msg.fixedHeader().messageType().value());
                sessionStore.addv("isDup", msg.fixedHeader().isDup());
                sessionStore.addv("qosLevel", msg.fixedHeader().qosLevel().value());
                sessionStore.addv("isRetain", msg.fixedHeader().isRetain());
                sessionStore.addv("remainingLength", msg.fixedHeader().remainingLength());
                sessionStore.addv("topicName", msg.variableHeader().topicName());
                sessionStore.addv("packetId", msg.variableHeader().packetId());
                sessionStore.addv("hasWillMessage", true);
            }

            return sessionStore;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }


    public static SessionStore mapTransToPublishMsgBeta(NutMap store) {
        SessionStore sessionStore = new SessionStore();
        if (store.getBoolean("hasWillMessage", false)) {
            byte[] payloads = Base64.getDecoder().decode(store.getString("payload"));
            ByteBuf buf = null;
            try {
                buf = Unpooled.wrappedBuffer(payloads);
                MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(
                        MqttMessageType.valueOf(store.getInt("messageType")),
                        store.getBoolean("isDup"),
                        MqttQoS.valueOf(store.getInt("qosLevel")),
                        store.getBoolean("isRetain"),
                        store.getInt("remainingLength"));

                MqttPublishVariableHeader mqttPublishVariableHeader = new MqttPublishVariableHeader(store.getString("topicName"),
                        store.getInt("packetId"));
                MqttPublishMessage mqttPublishMessage = new MqttPublishMessage(mqttFixedHeader, mqttPublishVariableHeader, buf);
                sessionStore.setWillMessage(mqttPublishMessage);
            } finally {
                if (buf != null) {
                    buf.release();
                }
            }
        }
        sessionStore.setChannelId(store.getString("channelId"));
        sessionStore.setClientId(store.getString("clientId"));
        sessionStore.setCleanSession(store.getBoolean("cleanSession"));
        sessionStore.setBrokerId(store.getString("brokerId"));
        sessionStore.setExpire(store.getInt("expire"));
        return sessionStore;
    }
}
