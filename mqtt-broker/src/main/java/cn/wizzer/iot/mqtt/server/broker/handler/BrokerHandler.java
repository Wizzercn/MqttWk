package cn.wizzer.iot.mqtt.server.broker.handler;

import cn.wizzer.iot.mqtt.server.broker.packet.MqttPacket;
import cn.wizzer.iot.mqtt.server.broker.protocol.ProtocolProcess;
import cn.wizzer.iot.mqtt.tio.codec.*;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.tio.core.ChannelContext;
import org.tio.core.intf.Packet;
import org.tio.server.intf.ServerAioHandler;

/**
 * Created by wizzer on 2018
 */
@IocBean
public class BrokerHandler extends MqttAbsAioHandler implements ServerAioHandler {
    private final static Log log = Logs.get();
    @Inject
    private ProtocolProcess protocolProcess;

    @Override
    public void handler(Packet packet, ChannelContext channelContext) throws Exception {
        MqttPacket mqttPacket = (MqttPacket) packet;
        if ("UNFINISHED".equals(mqttPacket.getMqttMessage().decoderResult()))
            return;
        switch (mqttPacket.getMqttMessage().fixedHeader().messageType()) {
            case CONNECT:
                protocolProcess.connect().processConnect(channelContext, (MqttConnectMessage) mqttPacket.getMqttMessage());
                break;
            case CONNACK:
                break;
            case PUBLISH:
                protocolProcess.publish().processPublish(channelContext, (MqttPublishMessage) mqttPacket.getMqttMessage());
                break;
            case PUBACK:
                protocolProcess.pubAck().processPubAck(channelContext, (MqttMessageIdVariableHeader) mqttPacket.getMqttMessage().variableHeader());
                break;
            case PUBREC:
                protocolProcess.pubRec().processPubRec(channelContext, (MqttMessageIdVariableHeader) mqttPacket.getMqttMessage().variableHeader());
                break;
            case PUBREL:
                protocolProcess.pubRel().processPubRel(channelContext, (MqttMessageIdVariableHeader) mqttPacket.getMqttMessage().variableHeader());
                break;
            case PUBCOMP:
                protocolProcess.pubComp().processPubComp(channelContext, (MqttMessageIdVariableHeader) mqttPacket.getMqttMessage().variableHeader());
                break;
            case SUBSCRIBE:
                protocolProcess.subscribe().processSubscribe(channelContext, (MqttSubscribeMessage) mqttPacket.getMqttMessage());
                break;
            case SUBACK:
                break;
            case UNSUBSCRIBE:
                protocolProcess.unSubscribe().processUnSubscribe(channelContext, (MqttUnsubscribeMessage) mqttPacket.getMqttMessage());
                break;
            case UNSUBACK:
                break;
            case PINGREQ:
                protocolProcess.pingReq().processPingReq(channelContext, mqttPacket.getMqttMessage());
                break;
            case PINGRESP:
                break;
            case DISCONNECT:
                protocolProcess.disConnect().processDisConnect(channelContext, mqttPacket.getMqttMessage());
                break;
            default:
                break;
        }
    }
}
