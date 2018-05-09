package cn.wizzer.mqttwk.mqtt;

import cn.wizzer.mqttwk.base.Globals;
import cn.wizzer.mqttwk.mqtt.common.MqttAbsAioHandler;
import cn.wizzer.mqttwk.mqtt.common.MqttPacket;
import cn.wizzer.mqttwk.mqtt.common.handler.MqttConnectHandler;
import cn.wizzer.mqttwk.mqtt.common.intf.AbsMqttBsHandler;
import cn.wizzer.mqttwk.mqtt.common.intf.MqttBsHandlerIntf;
import cn.wizzer.mqttwk.mqtt.common.packets.MqttConnectVariableHeader;
import cn.wizzer.mqttwk.mqtt.common.packets.MqttDecoder;
import cn.wizzer.mqttwk.mqtt.common.packets.MqttFixedHeader;
import cn.wizzer.mqttwk.mqtt.common.packets.MqttMessageType;
import org.nutz.ioc.Ioc;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.intf.AioHandler;
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
    private MqttFixedHeader mqttFixedHeader;
    private static Map<MqttMessageType, AbsMqttBsHandler<?>> handlerMap = new HashMap<>();

    public void init() {
        handlerMap.put(MqttMessageType.CONNECT, Globals.ioc.get(MqttConnectHandler.class));
    }

    /**
     * 处理消息
     */
    @Override
    public void handler(Packet packet, ChannelContext channelContext) throws Exception {
        MqttPacket mqttPacket = (MqttPacket) packet;
        mqttFixedHeader = MqttDecoder.decodeFixedHeader(mqttPacket.getHeader(),mqttPacket.getBodyLength());
        AbsMqttBsHandler<?> mqttBsHandler = handlerMap.get(mqttFixedHeader.messageType());
        if (mqttBsHandler == null) {
            log.error("{}, 找不到处理类，type:{}", channelContext, mqttFixedHeader.messageType());
            return;
        }
        mqttBsHandler.handler(mqttPacket, channelContext);
        return;
    }
}
