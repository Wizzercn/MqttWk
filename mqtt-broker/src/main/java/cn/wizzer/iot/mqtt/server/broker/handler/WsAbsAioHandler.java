package cn.wizzer.iot.mqtt.server.broker.handler;

import cn.wizzer.iot.mqtt.server.broker.packet.MqttPacket;
import cn.wizzer.iot.mqtt.tio.codec.MqttEncoder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.GroupContext;
import org.tio.core.exception.AioDecodeException;
import org.tio.core.intf.AioHandler;
import org.tio.core.intf.Packet;
import org.tio.core.utils.ByteBufferUtils;
import org.tio.http.common.*;
import org.tio.websocket.common.*;
import org.tio.websocket.common.util.BASE64Util;
import org.tio.websocket.common.util.SHA1Util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wizzer on 2018
 */
public abstract class WsAbsAioHandler implements AioHandler {
    private static Logger log = LoggerFactory.getLogger(WsAbsAioHandler.class);

    @Override
    public WsRequest decode(ByteBuffer buffer, int limit, int position, int readableLength, ChannelContext channelContext) throws AioDecodeException {
        WsSessionContext wsSessionContext = (WsSessionContext) channelContext.getAttribute();
        if (!wsSessionContext.isHandshaked()) {
            HttpRequest request = HttpRequestDecoder.decode(buffer, limit, position, readableLength, channelContext, null);
            if (request == null) {
                return null;
            }
            HttpResponse httpResponse = updateWebSocketProtocol(request, channelContext);
            if (httpResponse == null) {
                throw new AioDecodeException("http协议升级到websocket协议失败");
            }
            wsSessionContext.setHandshakeRequestPacket(request);
            wsSessionContext.setHandshakeResponsePacket(httpResponse);
            WsRequest wsRequestPacket = new WsRequest();
            wsRequestPacket.setHandShake(true);
            return wsRequestPacket;
        }

        WsRequest websocketPacket = WsServerDecoder.decode(buffer, channelContext);
        return websocketPacket;
    }

    @Override
    public ByteBuffer encode(Packet packet, GroupContext groupContext, ChannelContext channelContext) {
        WsResponse wsResponse;
        if (packet.getClass().isAssignableFrom(MqttPacket.class)) {
            MqttPacket mqttPacket = (MqttPacket) packet;
            wsResponse = new WsResponse();
            wsResponse.setHandShake(false);
            ByteBuffer buffer = MqttEncoder.doEncode(mqttPacket.getMqttMessage());
            buffer.flip();
            wsResponse.setBody(ByteBufferUtils.readBytes(buffer, buffer.remaining()));
        } else {
            wsResponse = (WsResponse) packet;
        }
        //握手包
        if (wsResponse.isHandShake()) {
            WsSessionContext imSessionContext = (WsSessionContext) channelContext.getAttribute();
            HttpResponse handshakeResponsePacket = imSessionContext.getHandshakeResponsePacket();
            try {
                return HttpResponseEncoder.encode(handshakeResponsePacket, groupContext, channelContext);
            } catch (UnsupportedEncodingException e) {
                log.error(e.toString(), e);
                return null;
            }
        }

        ByteBuffer byteBuffer = WsServerEncoder.encode(wsResponse, groupContext, channelContext);
        return byteBuffer;
    }

    /**
     * 本方法改编自baseio: https://gitee.com/generallycloud/baseio<br>
     * 感谢开源作者的付出
     *
     * @param request
     * @param channelContext
     * @return
     * @author tanyaowu
     */
    public static HttpResponse updateWebSocketProtocol(HttpRequest request, ChannelContext channelContext) {
        Map<String, String> headers = request.getHeaders();
        String Sec_WebSocket_Key = headers.get(HttpConst.RequestHeaderKey.Sec_WebSocket_Key);

        if (StringUtils.isNotBlank(Sec_WebSocket_Key)) {
            String Sec_WebSocket_Key_Magic = Sec_WebSocket_Key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
            byte[] key_array = SHA1Util.SHA1(Sec_WebSocket_Key_Magic);
            String acceptKey = BASE64Util.byteArrayToBase64(key_array);
            HttpResponse httpResponse = new HttpResponse(request);

            httpResponse.setStatus(HttpResponseStatus.C101);

            Map<HeaderName, HeaderValue> respHeaders = new HashMap<>();
            respHeaders.put(HeaderName.Connection, HeaderValue.Connection.Upgrade);
            respHeaders.put(HeaderName.Upgrade, HeaderValue.Upgrade.WebSocket);
            respHeaders.put(HeaderName.Sec_WebSocket_Accept, HeaderValue.from(acceptKey));
            //以下是不规范的写法,看以后怎么改
            respHeaders.put(HeaderName.from("Sec-WebSocket-Protocol"), HeaderValue.from(headers.get("sec-websocket-protocol")));
            respHeaders.put(HeaderName.from("Sec-WebSocket-Version"), HeaderValue.from(headers.get("sec-websocket-version")));
            respHeaders.put(HeaderName.from("Sec-WebSocket-Key"), HeaderValue.from(headers.get("sec-websocket-key")));
            respHeaders.put(HeaderName.from("X-Powered-By"), HeaderValue.from("MqttWk <wizzer.cn>"));
            httpResponse.addHeaders(respHeaders);
            return httpResponse;
        }
        return null;
    }

}
