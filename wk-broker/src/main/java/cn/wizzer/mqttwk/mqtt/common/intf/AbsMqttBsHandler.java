package cn.wizzer.mqttwk.mqtt.common.intf;

import cn.wizzer.mqttwk.mqtt.common.MqttPacket;
import cn.wizzer.mqttwk.mqtt.common.message.MqttDecoder;
import cn.wizzer.mqttwk.mqtt.common.message.MqttMessage;
import cn.wizzer.mqttwk.mqtt.common.message.MqttMessageFactory;
import org.nutz.ioc.loader.annotation.IocBean;
import org.tio.core.ChannelContext;

import java.nio.ByteBuffer;

/**
 * 解析消息内容 [消息体=动态头+内容]
 * Created by wizzer on 2018/5/9.
 */
@IocBean
public abstract class AbsMqttBsHandler<T extends MqttMessage> implements MqttBsHandlerIntf {

    @Override
    public Object handler(MqttPacket packet, ChannelContext channelContext) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(packet.getBody());
        MqttMessage message= MqttMessageFactory.newMessage(packet.getMqttFixedHeader(), MqttDecoder.decodeVariableHeader(buffer, packet.getMqttFixedHeader())
                ,"todo..");
        return handler(packet, message, channelContext);
    }

    public abstract Object handler(MqttPacket packet, MqttMessage mqttMessage, ChannelContext channelContext) throws Exception;

}
