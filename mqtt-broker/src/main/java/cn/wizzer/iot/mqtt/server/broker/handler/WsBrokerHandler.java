package cn.wizzer.iot.mqtt.server.broker.handler;

import cn.wizzer.iot.mqtt.server.broker.packet.MqttPacket;
import cn.wizzer.iot.mqtt.server.broker.protocol.ProtocolProcess;
import cn.wizzer.iot.mqtt.tio.codec.*;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.intf.Packet;
import org.tio.server.intf.ServerAioHandler;

/**
 * Created by wizzer on 2018
 */
@IocBean(create = "init")
public class WsBrokerHandler extends WsAbsAioHandler implements ServerAioHandler {
    private static Logger log = LoggerFactory.getLogger(WsBrokerHandler.class);
    @Inject
    private ProtocolProcess protocolProcess;
    @Inject
    private WsMsgHandler wsMsgHandler;

    public void init() {
        super.setWsMsgHandler(this.wsMsgHandler);
    }

    //    @Override
//    public void handler(Packet packet, ChannelContext channelContext) throws Exception {
//
//        WsRequest wsRequest = (WsRequest) packet;
//
//        if (wsRequest.isHandShake()) {
//            WsSessionContext wsSessionContext = (WsSessionContext) channelContext.getAttribute();
//            HttpRequest request = wsSessionContext.getHandshakeRequestPacket();
//            HttpResponse httpResponse = wsSessionContext.getHandshakeResponsePacket();
//            HttpResponse r = wsMsgHandler.handshake(request, httpResponse, channelContext);
//            if (r == null) {
//                Tio.remove(channelContext, "业务层不同意握手");
//                return;
//            }
//            wsSessionContext.setHandshakeResponsePacket(r);
//
//            WsResponse wsResponse = new WsResponse();
//            wsResponse.setHandShake(true);
//            Tio.send(channelContext, wsResponse);
//            wsSessionContext.setHandshaked(true);
//
//            wsMsgHandler.onAfterHandshaked(request, httpResponse, channelContext);
//            return;
//        }
//
//        WsResponse wsResponse = h(wsRequest, wsRequest.getBody(), wsRequest.getWsOpcode(), channelContext);
//
//        if (wsResponse != null) {
//            Tio.send(channelContext, wsResponse);
//        }
//
//        return;
//    }
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
