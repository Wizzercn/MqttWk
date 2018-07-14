/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.wizzer.iot.mqtt.server.common.message;

import java.io.Serializable;

/**
 * PUBREL重发消息存储
 */
public class DupPubRelMessageStore implements Serializable {

	private static final long serialVersionUID = -4111642532532950980L;

	private String clientId;

	private int messageId;

	public String getClientId() {
		return clientId;
	}

	public DupPubRelMessageStore setClientId(String clientId) {
		this.clientId = clientId;
		return this;
	}

	public int getMessageId() {
		return messageId;
	}

	public DupPubRelMessageStore setMessageId(int messageId) {
		this.messageId = messageId;
		return this;
	}

}
