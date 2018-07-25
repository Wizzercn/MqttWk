package cn.wizzer.iot.mqtt.server.broker.handler;

import cn.wizzer.iot.mqtt.server.broker.config.BrokerProperties;
import cn.wizzer.iot.mqtt.server.broker.protocol.ProtocolProcess;
import cn.wizzer.iot.mqtt.server.tio.codec.*;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.websocket.common.WsRequest;
import org.tio.websocket.common.WsSessionContext;
import org.tio.websocket.server.handler.IWsMsgHandler;

import java.nio.ByteBuffer;

/**
 * Created by wizzer on 2018
 */
@IocBean
public class WsMsgHandler implements IWsMsgHandler {
    private static Logger LOGGER = LoggerFactory.getLogger(WsMsgHandler.class);
    @Inject
    private BrokerProperties brokerProperties;
    @Inject
    private ProtocolProcess protocolProcess;

    /**
     * 握手时走这个方法，业务可以在这里获取cookie，request参数等
     */
    @Override
    public HttpResponse handshake(HttpRequest request, HttpResponse httpResponse, ChannelContext channelContext) throws Exception {
        String clientip = request.getClientIp();
        LOGGER.debug("收到来自{}的ws握手包\r\n{}", clientip, request.toString());
        return httpResponse;
    }

    /**
     * @param httpRequest
     * @param httpResponse
     * @param channelContext
     * @throws Exception
     * @author tanyaowu
     */
    @Override
    public void onAfterHandshaked(HttpRequest httpRequest, HttpResponse httpResponse, ChannelContext channelContext) throws Exception {

    }

    /**
     * 字节消息（binaryType = arraybuffer）过来后会走这个方法
     */
    @Override
    public Object onBytes(WsRequest wsRequest, byte[] bytes, ChannelContext channelContext) throws Exception {
        MqttMessage mqttMessage = MqttDecoder.decode(ByteBuffer.wrap(bytes));
        //不使用ws或wss协议连这个端口会null
        if (mqttMessage == null || "UNFINISHED".equals(mqttMessage.decoderResult()))
            return null;
        switch (mqttMessage.fixedHeader().messageType()) {
            case CONNECT:
                protocolProcess.connect().processConnect(channelContext, (MqttConnectMessage) mqttMessage);
                break;
            case CONNACK:
                break;
            case PUBLISH:
                protocolProcess.publish().processPublish(channelContext, (MqttPublishMessage) mqttMessage);
                break;
            case PUBACK:
                protocolProcess.pubAck().processPubAck(channelContext, (MqttMessageIdVariableHeader) mqttMessage.variableHeader());
                break;
            case PUBREC:
                protocolProcess.pubRec().processPubRec(channelContext, (MqttMessageIdVariableHeader) mqttMessage.variableHeader());
                break;
            case PUBREL:
                protocolProcess.pubRel().processPubRel(channelContext, (MqttMessageIdVariableHeader) mqttMessage.variableHeader());
                break;
            case PUBCOMP:
                protocolProcess.pubComp().processPubComp(channelContext, (MqttMessageIdVariableHeader) mqttMessage.variableHeader());
                break;
            case SUBSCRIBE:
                protocolProcess.subscribe().processSubscribe(channelContext, (MqttSubscribeMessage) mqttMessage);
                break;
            case SUBACK:
                break;
            case UNSUBSCRIBE:
                protocolProcess.unSubscribe().processUnSubscribe(channelContext, (MqttUnsubscribeMessage) mqttMessage);
                break;
            case UNSUBACK:
                break;
            case PINGREQ:
                protocolProcess.pingReq().processPingReq(channelContext, mqttMessage);
                break;
            case PINGRESP:
                break;
            case DISCONNECT:
                protocolProcess.disConnect().processDisConnect(channelContext, mqttMessage);
                break;
            default:
                break;
        }
        return null;
    }

    /**
     * 当客户端发close flag时，会走这个方法
     */
    @Override
    public Object onClose(WsRequest wsRequest, byte[] bytes, ChannelContext channelContext) throws Exception {
        Tio.remove(channelContext, "receive close flag");
        return null;
    }

    /*
     * 字符消息（binaryType = blob）过来后会走这个方法
     */
    @Override
    public Object onText(WsRequest wsRequest, String text, ChannelContext channelContext) throws Exception {
        WsSessionContext wsSessionContext = (WsSessionContext) channelContext.getAttribute();
        HttpRequest httpRequest = wsSessionContext.getHandshakeRequestPacket();//获取websocket握手包
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("握手包:{}", httpRequest);
        }
        return null;
    }

}
