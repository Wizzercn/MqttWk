package cn.wizzer.mqttwk.mqtt.common;

import org.tio.core.intf.Packet;

/**
 * Created by wizzer on 2018/5/9.
 */
public class MqttPacket extends Packet {
    private static final long serialVersionUID = -5481926483435771100L;
    public static final int HEADER_LENGHT = 2;//消息头的长度 2
    private byte header;
    private int bodyLength;
    private byte[] body;

    public MqttPacket() {
        super();
    }

    public MqttPacket(byte header, int bodyLength, byte[] body) {
        super();
        this.header = header;
        this.bodyLength = bodyLength;
        this.body = body;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public int getBodyLength() {
        return bodyLength;
    }

    public void setBodyLength(int bodyLength) {
        this.bodyLength = bodyLength;
    }

    public byte getHeader() {
        return header;
    }

    public void setHeader(byte header) {
        this.header = header;
    }

    @Override
    public String logstr() {
        return "" + header;
    }
}
