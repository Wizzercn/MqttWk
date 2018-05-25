package cn.wizzer.mqttwk.mqtt.common.message;

import java.nio.ByteBuffer;

/**
 * Created by wizzer on 2018/5/14.
 */
public class MqttWillMessage {
    private final String topic;
    private final ByteBuffer payload;
    private final boolean retained;
    private final MqttQoS qos;

    public MqttWillMessage(String topic, ByteBuffer payload, boolean retained, MqttQoS qos) {
        this.topic = topic;
        this.payload = payload;
        this.retained = retained;
        this.qos = qos;
    }

    public String getTopic() {
        return topic;
    }

    public ByteBuffer getPayload() {
        return payload;
    }

    public boolean isRetained() {
        return retained;
    }

    public MqttQoS getQos() {
        return qos;
    }
}
