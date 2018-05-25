package cn.wizzer.mqttwk.mqtt.common.message;

import cn.wizzer.mqttwk.mqtt.common.utils.ByteUtil;

import java.nio.ByteBuffer;

/**
 * See <a href="http://public.dhe.ibm.com/software/dw/webservices/ws-mqtt/mqtt-v3r1.html#publish">MQTTV3.1/publish</a>
 */
public class MqttPublishMessage extends MqttMessage {

    public MqttPublishMessage(
            MqttFixedHeader mqttFixedHeader,
            MqttPublishVariableHeader variableHeader,
            ByteBuffer payload) {
        super(mqttFixedHeader, variableHeader, payload);
    }

    @Override
    public MqttPublishVariableHeader variableHeader() {
        return (MqttPublishVariableHeader) super.variableHeader();
    }

    @Override
    public ByteBuffer payload() {
        return content();
    }

    public ByteBuffer content() {
        ByteBuffer data=null;
        try {
            data = ByteBuffer.wrap(ByteUtil.getBytes(super.payload()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
//
//    @Override
//    public MqttPublishMessage copy() {
//        return replace(content().copy());
//    }
//
//    @Override
//    public MqttPublishMessage duplicate() {
//        return replace(content().duplicate());
//    }
//
//    @Override
//    public MqttPublishMessage retainedDuplicate() {
//        return replace(content().retainedDuplicate());
//    }
//
//    @Override
//    public MqttPublishMessage replace(ByteBuf content) {
//        return new MqttPublishMessage(fixedHeader(), variableHeader(), content);
//    }
//
//    @Override
//    public int refCnt() {
//        return content().refCnt();
//    }
//
//    @Override
//    public MqttPublishMessage retain() {
//        content().retain();
//        return this;
//    }
//
//    @Override
//    public MqttPublishMessage retain(int increment) {
//        content().retain(increment);
//        return this;
//    }
//
//    @Override
//    public MqttPublishMessage touch() {
//        content().touch();
//        return this;
//    }
//
//    @Override
//    public MqttPublishMessage touch(Object hint) {
//        content().touch(hint);
//        return this;
//    }
//
//    @Override
//    public boolean release() {
//        return content().release();
//    }
//
//    @Override
//    public boolean release(int decrement) {
//        return content().release(decrement);
//    }

}
