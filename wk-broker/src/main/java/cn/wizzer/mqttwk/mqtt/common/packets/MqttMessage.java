package cn.wizzer.mqttwk.mqtt.common.packets;

import cn.wizzer.mqttwk.mqtt.common.utils.StringUtil;

/**
 * Created by wizzer on 2018/5/9.
 */
public class MqttMessage {
    private final MqttFixedHeader mqttFixedHeader;
    private final Object variableHeader;
    private final Object payload;
    private final boolean decoderResult;

    public MqttMessage(MqttFixedHeader mqttFixedHeader) {
        this(mqttFixedHeader, null, null);
    }

    public MqttMessage(MqttFixedHeader mqttFixedHeader, Object variableHeader) {
        this(mqttFixedHeader, variableHeader, null);
    }

    public MqttMessage(MqttFixedHeader mqttFixedHeader, Object variableHeader, Object payload) {
        this(mqttFixedHeader, variableHeader, payload, true);
    }

    public MqttMessage(
            MqttFixedHeader mqttFixedHeader,
            Object variableHeader,
            Object payload,
            boolean decoderResult) {
        this.mqttFixedHeader = mqttFixedHeader;
        this.variableHeader = variableHeader;
        this.payload = payload;
        this.decoderResult = decoderResult;
    }

    public MqttFixedHeader fixedHeader() {
        return mqttFixedHeader;
    }

    public Object variableHeader() {
        return variableHeader;
    }

    public Object payload() {
        return payload;
    }

    public boolean decoderResult() {
        return decoderResult;
    }

    @Override
    public String toString() {
        return new StringBuilder(StringUtil.simpleClassName(this))
                .append('[')
                .append("fixedHeader=").append(fixedHeader() != null ? fixedHeader().toString() : "")
                .append(", variableHeader=").append(variableHeader() != null ? variableHeader.toString() : "")
                .append(", payload=").append(payload() != null ? payload.toString() : "")
                .append(']')
                .toString();
    }
}
