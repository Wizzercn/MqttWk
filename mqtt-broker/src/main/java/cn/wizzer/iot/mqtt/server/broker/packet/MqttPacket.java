package cn.wizzer.iot.mqtt.server.broker.packet;

import cn.wizzer.iot.mqtt.tio.codec.MqttFixedHeader;
import org.tio.core.intf.Packet;

/**
 * Created by wizzer on 2018/5/9.
 */
public class MqttPacket extends Packet {
    private static final long serialVersionUID = -5481926483435771100L;
    public static final int HEADER_LENGHT = 2;//消息头的长度 2
    private MqttFixedHeader mqttFixedHeader;
    private byte[] body;

    public MqttPacket() {
        super();
    }

    public MqttPacket(MqttFixedHeader mqttFixedHeader, byte[] body) {
        super();
        this.mqttFixedHeader = mqttFixedHeader;
        this.body = body;
    }

    public MqttFixedHeader getMqttFixedHeader() {
        return mqttFixedHeader;
    }

    public void setMqttFixedHeader(MqttFixedHeader mqttFixedHeader) {
        this.mqttFixedHeader = mqttFixedHeader;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
