package cn.wizzer.iot.mqtt.server.broker.packet;

import cn.wizzer.iot.mqtt.tio.codec.MqttMessage;
import org.tio.core.intf.Packet;

/**
 * Created by wizzer on 2018/5/9.
 */
public class MqttPacket extends Packet {
    private static final long serialVersionUID = -5481926483435771100L;
    public static final int HEADER_LENGHT = 2;//消息头的长度 2
    private MqttMessage mqttMessage;
    public MqttPacket() {
        super();
    }

    public MqttPacket(MqttMessage mqttMessage) {
        super();
        this.mqttMessage = mqttMessage;
    }

    public MqttMessage getMqttMessage() {
        return mqttMessage;
    }

    public void setMqttMessage(MqttMessage mqttMessage) {
        this.mqttMessage = mqttMessage;
    }

}
