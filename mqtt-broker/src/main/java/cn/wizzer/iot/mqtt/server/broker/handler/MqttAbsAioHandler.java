package cn.wizzer.iot.mqtt.server.broker.handler;

import cn.wizzer.iot.mqtt.server.broker.packet.MqttPacket;
import cn.wizzer.iot.mqtt.tio.codec.MqttDecoder;
import cn.wizzer.iot.mqtt.tio.codec.MqttEncoder;
import cn.wizzer.iot.mqtt.tio.codec.MqttMessage;
import org.nutz.lang.Lang;
import org.tio.core.ChannelContext;
import org.tio.core.GroupContext;
import org.tio.core.exception.AioDecodeException;
import org.tio.core.intf.AioHandler;
import org.tio.core.intf.Packet;

import java.nio.ByteBuffer;

/**
 * MQTT消息处理
 * Created by wizzer on 2018
 */
public abstract class MqttAbsAioHandler implements AioHandler {
    /**
     * 解码：把接收到的ByteBuffer，解码成应用可以识别的业务消息包
     * 消息头：MqttFixedHeader
     * 消息体：byte[]
     */
    @Override
    public MqttPacket decode(ByteBuffer buffer, int limit, int position, int readableLength, ChannelContext channelContext) throws AioDecodeException {
        if (readableLength < MqttPacket.HEADER_LENGHT) {
            return null;
        }
        //解析固定头部内容
        try {
            MqttMessage mqttMessage = MqttDecoder.decode(buffer, channelContext);
            if (mqttMessage != null) {
                MqttPacket mqttPacket = new MqttPacket();
                mqttPacket.setMqttMessage(mqttMessage);
                return mqttPacket;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 编码：把业务消息包编码为可以发送的ByteBuffer
     * 消息头：MqttFixedHeader
     * 消息体：byte[]
     */
    @Override
    public ByteBuffer encode(Packet packet, GroupContext groupContext, ChannelContext channelContext) {
        MqttPacket mqttPacket = (MqttPacket) packet;
        //总长度是消息头的长度+消息体的长度
//        int allLen = MqttPacket.HEADER_LENGHT + mqttPacket.getMqttMessage().fixedHeader().remainingLength();
//
//        ByteBuffer buffer = ByteBuffer.allocate(allLen);
//        buffer.order(groupContext.getByteOrder());
        //写入消息体
        ByteBuffer buffer=MqttEncoder.doEncode(mqttPacket.getMqttMessage());
        return buffer;
    }
}
