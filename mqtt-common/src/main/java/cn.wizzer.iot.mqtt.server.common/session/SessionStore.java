/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.wizzer.iot.mqtt.server.common.session;


import cn.wizzer.iot.mqtt.tio.codec.MqttPublishMessage;
import org.tio.core.ChannelContext;

import java.io.Serializable;

/**
 * 会话存储
 */
public class SessionStore implements Serializable {

	private static final long serialVersionUID = 5209539791996944490L;

	private String clientId;

	private ChannelContext channel;

	private boolean cleanSession;

	private MqttPublishMessage willMessage;

	public SessionStore(String clientId, ChannelContext channel, boolean cleanSession, MqttPublishMessage willMessage) {
		this.clientId = clientId;
		this.channel = channel;
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

	public ChannelContext getChannel() {
		return channel;
	}

	public SessionStore setChannel(ChannelContext channel) {
		this.channel = channel;
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
