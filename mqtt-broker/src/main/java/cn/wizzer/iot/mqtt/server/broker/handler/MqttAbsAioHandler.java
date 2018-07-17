package cn.wizzer.iot.mqtt.server.broker.handler;

import cn.wizzer.iot.mqtt.server.broker.packet.MqttPacket;
import cn.wizzer.iot.mqtt.tio.codec.MqttDecoder;
import cn.wizzer.iot.mqtt.tio.codec.MqttEncoder;
import cn.wizzer.iot.mqtt.tio.codec.MqttMessage;
import org.nutz.json.Json;
import org.nutz.log.Log;
import org.nutz.log.Logs;
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
    private final static Log log = Logs.get();

    /**
     * 解码：把接收到的ByteBuffer，解码成应用可以识别的业务消息包
     */
    @Override
    public MqttPacket decode(ByteBuffer buffer, int limit, int position, int readableLength, ChannelContext channelContext) throws AioDecodeException {
        if (readableLength < MqttPacket.HEADER_LENGHT) {
            return null;
        }
        //解析固定头部内容
        try {
            MqttMessage mqttMessage = MqttDecoder.decode(buffer);
            log.debug("get mqttMessage::" + Json.toJson(mqttMessage));
            if (mqttMessage != null && "SUCCESS".equals(mqttMessage.decoderResult())) {
                MqttPacket mqttPacket = new MqttPacket();
                mqttPacket.setMqttMessage(mqttMessage);
                return mqttPacket;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
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
        log.debug("send mqttPacket::" + Json.toJson(mqttPacket));
        //写入消息体
        ByteBuffer buffer = MqttEncoder.doEncode(mqttPacket.getMqttMessage());
        buffer.flip();
        return buffer;
    }
}
