package cn.wizzer.mqttwk.mqtt.common.message;

import cn.wizzer.mqttwk.mqtt.common.utils.StringUtil;

/**
 * Variable Header of the {@link MqttPublishMessage}
 */
public final class MqttPublishVariableHeader {

    private final String topicName;
    private final int packetId;

    public MqttPublishVariableHeader(String topicName, int packetId) {
        this.topicName = topicName;
        this.packetId = packetId;
    }

    public String topicName() {
        return topicName;
    }

    /**
     * @deprecated Use {@link #packetId()} instead.
     */
    @Deprecated
    public int messageId() {
        return packetId;
    }

    public int packetId() {
        return packetId;
    }

    @Override
    public String toString() {
        return new StringBuilder(StringUtil.simpleClassName(this))
                .append('[')
                .append("topicName=").append(topicName)
                .append(", packetId=").append(packetId)
                .append(']')
                .toString();
    }
}
