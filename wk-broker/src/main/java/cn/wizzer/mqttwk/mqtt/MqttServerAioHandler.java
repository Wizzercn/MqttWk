package cn.wizzer.mqttwk.mqtt;

import cn.wizzer.mqttwk.mqtt.common.MqttAbsAioHandler;
import cn.wizzer.mqttwk.mqtt.common.MqttPacket;
import cn.wizzer.mqttwk.mqtt.common.handler.MqttConnectHandler;
import cn.wizzer.mqttwk.mqtt.common.intf.AbsMqttBsHandler;
import cn.wizzer.mqttwk.mqtt.common.message.MqttDecoder;
import cn.wizzer.mqttwk.mqtt.common.message.MqttFixedHeader;
import cn.wizzer.mqttwk.mqtt.common.message.MqttMessageType;
import org.nutz.ioc.Ioc;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.intf.Packet;
import org.tio.server.intf.ServerAioHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wizzer on 2018/5/9.
 */
@IocBean(create = "init")
public class MqttServerAioHandler extends MqttAbsAioHandler implements ServerAioHandler {
    private static Logger log = LoggerFactory.getLogger(MqttServerAioHandler.class);
    private static Map<MqttMessageType, AbsMqttBsHandler<?>> handlerMap = new HashMap<>();
    @Inject("refer:$ioc")
    private Ioc ioc;
    public void init() {
        handlerMap.put(MqttMessageType.CONNECT, ioc.get(MqttConnectHandler.class));
    }

    /**
     * 处理消息
     */
    @Override
    public void handler(Packet packet, ChannelContext channelContext) throws Exception {
        MqttPacket mqttPacket = (MqttPacket) packet;
        MqttFixedHeader mqttFixedHeader = MqttDecoder.decodeFixedHeader(mqttPacket.getHeader(),mqttPacket.getBodyLength());
        AbsMqttBsHandler<?> mqttBsHandler = handlerMap.get(mqttFixedHeader.messageType());
        if (mqttBsHandler == null) {
            log.error("{}, 找不到处理类，type:{}", channelContext, mqttFixedHeader.messageType());
            return;
        }
        mqttBsHandler.handler(mqttPacket, channelContext);
        return;
    }
}
