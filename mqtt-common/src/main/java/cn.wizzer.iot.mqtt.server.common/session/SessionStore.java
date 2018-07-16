/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.wizzer.iot.mqtt.server.common.session;


import cn.wizzer.iot.mqtt.tio.codec.MqttPublishMessage;

import java.io.Serializable;

/**
 * 会话存储
 */
public class SessionStore implements Serializable {
    private static final long serialVersionUID = -1L;

    private String clientId;

    private String channelId;

    private boolean cleanSession;

    private MqttPublishMessage willMessage;

    public SessionStore(String clientId, String channelId, boolean cleanSession, MqttPublishMessage willMessage) {
        this.clientId = clientId;
        this.channelId = channelId;
        this.cleanSession = cleanSession;
        this.willMessage = willMessage;
    }

    public String getClientId() {
        return clientId;
    }

    public SessionStore setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public String getChannelId() {
        return channelId;
    }

    public SessionStore setChannelId(String channelId) {
        this.channelId = channelId;
        return this;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public SessionStore setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
        return this;
    }

    public MqttPublishMessage getWillMessage() {
        return willMessage;
    }

    public SessionStore setWillMessage(MqttPublishMessage willMessage) {
        this.willMessage = willMessage;
        return this;
    }
}
