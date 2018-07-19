package cn.wizzer.iot.mqtt.server.broker.handler;

import cn.wizzer.iot.mqtt.server.broker.protocol.ProtocolProcess;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Encoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.core.intf.Packet;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.server.intf.ServerAioHandler;
import org.tio.websocket.common.Opcode;
import org.tio.websocket.common.WsRequest;
import org.tio.websocket.common.WsResponse;
import org.tio.websocket.common.WsSessionContext;

import java.nio.ByteBuffer;

/**
 * Created by wizzer on 2018
 */
@IocBean
public class WsBrokerHandler extends WsAbsAioHandler implements ServerAioHandler {
    private static Logger log = LoggerFactory.getLogger(WsBrokerHandler.class);
    @Inject
    private ProtocolProcess protocolProcess;
    @Inject
    private WsMsgHandler wsMsgHandler;


    @Override
    public void handler(Packet packet, ChannelContext channelContext) throws Exception {

        WsRequest wsRequest = (WsRequest) packet;

        if (wsRequest.isHandShake()) {
            WsSessionContext wsSessionContext = (WsSessionContext) channelContext.getAttribute();
            HttpRequest request = wsSessionContext.getHandshakeRequestPacket();
            HttpResponse httpResponse = wsSessionContext.getHandshakeResponsePacket();
            HttpResponse r = wsMsgHandler.handshake(request, httpResponse, channelContext);
            if (r == null) {
                Tio.remove(channelContext, "业务层不同意握手");
                return;
            }
            wsSessionContext.setHandshakeResponsePacket(r);

            WsResponse wsResponse = new WsResponse();
            wsResponse.setHandShake(true);
            Tio.send(channelContext, wsResponse);
            wsSessionContext.setHandshaked(true);

            wsMsgHandler.onAfterHandshaked(request, httpResponse, channelContext);
            return;
        }

        WsResponse wsResponse = h(wsRequest, wsRequest.getBody(), wsRequest.getWsOpcode(), channelContext);

        if (wsResponse != null) {
            Tio.send(channelContext, wsResponse);
        }

        return;
    }


    private WsResponse h(WsRequest websocketPacket, byte[] bytes, Opcode opcode, ChannelContext channelContext) throws Exception {
        WsResponse wsResponse = null;
        if (opcode == Opcode.TEXT) {
            if (bytes == null || bytes.length == 0) {
                Tio.remove(channelContext, "错误的websocket包，body为空");
                return null;
            }
            String text = new String(bytes, Encoding.CHARSET_UTF8);
            Object retObj = wsMsgHandler.onText(websocketPacket, text, channelContext);
            String methodName = "onText";
            wsResponse = processRetObj(retObj, methodName, channelContext);
            return wsResponse;
        } else if (opcode == Opcode.BINARY) {
            if (bytes == null || bytes.length == 0) {
                Tio.remove(channelContext, "错误的websocket包，body为空");
                return null;
            }
            Object retObj = wsMsgHandler.onBytes(websocketPacket, bytes, channelContext);
            String methodName = "onBytes";
            wsResponse = processRetObj(retObj, methodName, channelContext);
            return wsResponse;
        } else if (opcode == Opcode.PING || opcode == Opcode.PONG) {
            log.info("收到" + opcode);
            return null;
        } else if (opcode == Opcode.CLOSE) {
            Object retObj = wsMsgHandler.onClose(websocketPacket, bytes, channelContext);
            String methodName = "onClose";
            wsResponse = processRetObj(retObj, methodName, channelContext);
            return wsResponse;
        } else {
            Tio.remove(channelContext, "错误的websocket包，错误的Opcode");
            return null;
        }
    }

    private WsResponse processRetObj(Object obj, String methodName, ChannelContext channelContext) throws Exception {
        WsResponse wsResponse = null;
        if (obj == null) {
            return null;
        } else {
            if (obj instanceof String) {
                String str = (String) obj;
                wsResponse = WsResponse.fromText(str, Encoding.UTF8);
                return wsResponse;
            } else if (obj instanceof byte[]) {
                wsResponse = WsResponse.fromBytes((byte[]) obj);
                return wsResponse;
            } else if (obj instanceof WsResponse) {
                return (WsResponse) obj;
            } else if (obj instanceof ByteBuffer) {
                byte[] bs = ((ByteBuffer) obj).array();
                wsResponse = WsResponse.fromBytes(bs);
                return wsResponse;
            } else {
                log.error("{} {}.{}()方法，只允许返回byte[]、ByteBuffer、WsResponse或null，但是程序返回了{}", channelContext, this.getClass().getName(), methodName, obj.getClass().getName());
                return null;
            }
        }
    }

}
