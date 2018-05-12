package cn.wizzer.mqttwk.mqtt.common.message;


import cn.wizzer.mqttwk.mqtt.common.utils.StringUtil;

/**
 * Variable header of {@link MqttConnectMessage}
 */
public final class MqttConnAckVariableHeader {

    private final MqttConnectReturnCode connectReturnCode;

    private final boolean sessionPresent;

    public MqttConnAckVariableHeader(MqttConnectReturnCode connectReturnCode, boolean sessionPresent) {
        this.connectReturnCode = connectReturnCode;
        this.sessionPresent = sessionPresent;
    }

    public MqttConnectReturnCode connectReturnCode() {
        return connectReturnCode;
    }

    public boolean isSessionPresent() {
        return sessionPresent;
    }

    @Override
    public String toString() {
        return new StringBuilder(StringUtil.simpleClassName(this))
            .append('[')
            .append("connectReturnCode=").append(connectReturnCode)
            .append(", sessionPresent=").append(sessionPresent)
            .append(']')
            .toString();
    }
}
