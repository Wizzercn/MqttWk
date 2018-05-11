package cn.wizzer.mqttwk.mqtt.common;

import cn.wizzer.mqttwk.mqtt.common.message.MqttDecoder;
import cn.wizzer.mqttwk.mqtt.common.message.MqttFixedHeader;
import org.nutz.ioc.loader.annotation.IocBean;
import org.tio.core.ChannelContext;
import org.tio.core.GroupContext;
import org.tio.core.exception.AioDecodeException;
import org.tio.core.intf.AioHandler;
import org.tio.core.intf.Packet;

import java.nio.ByteBuffer;

/**
 * Created by wizzer on 2018/5/9.
 */
@IocBean
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
        MqttFixedHeader mqttFixedHeader = MqttDecoder.decodeFixedHeader(buffer);
        int bodyLength = mqttFixedHeader.remainingLength();

        if (bodyLength < 0) {
            throw new AioDecodeException("bodyLength [" + bodyLength + "] is not right, remote:" + channelContext.getClientNode());
        }

        int neededLength = MqttPacket.HEADER_LENGHT + bodyLength;
        int test = readableLength - neededLength;
        if (test < 0) // 不够消息体长度(剩下的buffe组不了消息体)
        {
            return null;
        } else {
            MqttPacket mqttPacket = new MqttPacket();
            mqttPacket.setMqttFixedHeader(mqttFixedHeader);
            if (bodyLength > 0) {
                byte[] dst = new byte[bodyLength];
                buffer.get(dst);
                mqttPacket.setBody(dst);
            }
            return mqttPacket;
        }
    }

    /**
     * 编码：把业务消息包编码为可以发送的ByteBuffer
     * 消息头：MqttFixedHeader
     * 消息体：byte[]
     */
    @Override
    public ByteBuffer encode(Packet packet, GroupContext groupContext, ChannelContext channelContext) {
        MqttPacket showcasePacket = (MqttPacket) packet;
        byte[] body = showcasePacket.getBody();
        int bodyLen = 0;
        if (body != null) {
            bodyLen = body.length;
        }

        //总长度是消息头的长度+消息体的长度
        int allLen = MqttPacket.HEADER_LENGHT + bodyLen;

        ByteBuffer buffer = ByteBuffer.allocate(allLen);
        buffer.order(groupContext.getByteOrder());
//
//        //写入消息类型
//        buffer.put(showcasePacket.getMqttFixedHeader());
//        //写入消息体长度
//        buffer.putInt(bodyLen);

        //写入消息体
        if (body != null) {
            buffer.put(body);
        }
        return buffer;
    }
}
