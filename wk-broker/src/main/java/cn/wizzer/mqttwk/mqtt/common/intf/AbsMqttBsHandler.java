package cn.wizzer.mqttwk.mqtt.common.intf;

import cn.wizzer.mqttwk.mqtt.common.MqttPacket;
import cn.wizzer.mqttwk.mqtt.common.message.MqttMessage;
import org.nutz.lang.Encoding;
import org.tio.core.ChannelContext;
import org.tio.utils.json.Json;

/**
 * Created by wizzer on 2018/5/9.
 */
public abstract class AbsMqttBsHandler<T extends MqttMessage> implements MqttBsHandlerIntf {

    public abstract Class<T> bodyClass();

    @Override
    public Object handler(MqttPacket packet, ChannelContext channelContext) throws Exception {
        String jsonStr = null;
        T bsBody = null;
        if (packet.getBody() != null) {
            jsonStr = new String(packet.getBody(), Encoding.CHARSET_UTF8);
            bsBody = Json.toBean(jsonStr, bodyClass());
        }
        //解析动态头部内容
        return handler(packet, bsBody, channelContext);
    }

    public abstract Object handler(MqttPacket packet, T bsBody, ChannelContext channelContext) throws Exception;

}
