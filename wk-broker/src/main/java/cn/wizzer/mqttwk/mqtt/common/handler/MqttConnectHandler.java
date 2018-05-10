package cn.wizzer.mqttwk.mqtt.common.handler;

import cn.wizzer.mqttwk.mqtt.common.MqttPacket;
import cn.wizzer.mqttwk.mqtt.common.intf.AbsMqttBsHandler;
import cn.wizzer.mqttwk.mqtt.common.intf.MqttBsHandlerIntf;
import cn.wizzer.mqttwk.mqtt.common.message.MqttConnectMessage;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;

/**
 * Created by wizzer on 2018/5/9.
 */
@IocBean
public class MqttConnectHandler extends AbsMqttBsHandler<MqttConnectMessage> implements MqttBsHandlerIntf {
    private static Logger log = LoggerFactory.getLogger(MqttConnectHandler.class);
    /**
     *
     * @author tanyaowu
     */
    public MqttConnectHandler() {
    }

    /**
     * @return
     * @author tanyaowu
     */
    @Override
    public Class<MqttConnectMessage> bodyClass() {
        return MqttConnectMessage.class;
    }

    /**
     * @param packet
     * @param bsBody
     * @param channelContext
     * @return
     * @throws Exception
     * @author tanyaowu
     */
    @Override
    public Object handler(MqttPacket packet, MqttConnectMessage bsBody, ChannelContext channelContext) throws Exception {
        log.info("收到群聊请求消息:{}", Json.toJson(bsBody));

//        GroupMsgRespBody groupMsgRespBody = new GroupMsgRespBody();
//        groupMsgRespBody.setText(bsBody.getText());
//        groupMsgRespBody.setToGroup(bsBody.getToGroup());
//
//        ShowcasePacket respPacket = new ShowcasePacket();
//        respPacket.setType(Type.GROUP_MSG_RESP);
//        respPacket.setBody(Json.toJson(groupMsgRespBody).getBytes(ShowcasePacket.CHARSET));
//        Aio.sendToGroup(channelContext.getGroupContext(), bsBody.getToGroup(), respPacket);

        return null;
    }
}
