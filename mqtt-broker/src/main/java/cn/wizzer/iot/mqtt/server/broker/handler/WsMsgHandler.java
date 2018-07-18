package cn.wizzer.iot.mqtt.server.broker.handler;

import cn.wizzer.iot.mqtt.server.broker.config.BrokerProperties;
import cn.wizzer.iot.mqtt.server.broker.protocol.ProtocolProcess;
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
        LOGGER.info("收到来自{}的ws握手包\r\n{}", clientip, request.toString());
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
        //绑定到群组，后面会有群发
        LOGGER.debug("111");
//        Tio.bindGroup(channelContext, brokerProperties.getId());
//        int count = Tio.getAllChannelContexts(channelContext.groupContext).getObj().size();
//
//        String msg = channelContext.getClientNode().toString() + " 进来了，现在共有【" + count + "】人在线";
//        //用tio-websocket，服务器发送到客户端的Packet都是WsResponse
//        WsResponse wsResponse = WsResponse.fromText(msg, Encoding.UTF8);
//        //群发
//        Tio.sendToGroup(channelContext.groupContext, brokerProperties.getId(), wsResponse);
    }

    /**
     * 字节消息（binaryType = arraybuffer）过来后会走这个方法
     */
    @Override
    public Object onBytes(WsRequest wsRequest, byte[] bytes, ChannelContext channelContext) throws Exception {
        LOGGER.debug("bytes:::"+new String(bytes));
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

        LOGGER.debug("收到ws消息:{}", text);
//
//        if (Objects.equals("心跳内容", text)) {
//            return null;
//        }
//
//        String msg = channelContext.getClientNode().toString() + " 说：" + text;
//        //用tio-websocket，服务器发送到客户端的Packet都是WsResponse
//        WsResponse wsResponse = WsResponse.fromText(msg, Encoding.UTF8);
//        //群发
//        Tio.sendToGroup(channelContext.groupContext, brokerProperties.getId(), wsResponse);

        //返回值是要发送给客户端的内容，一般都是返回null
        return null;
    }

}
