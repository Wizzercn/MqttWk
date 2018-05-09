package cn.wizzer.mqttwk.mqtt.common.intf;

import cn.wizzer.mqttwk.mqtt.common.MqttPacket;
import org.tio.core.ChannelContext;

/**
 * 业务处理接口
 * Created by wizzer on 2018/5/9.
 */
public interface MqttBsHandlerIntf {
    /**
     *
     * @param packet
     * @param channelContext
     * @return
     * @throws Exception
     * @author tanyaowu
     */
    public Object handler(MqttPacket packet, ChannelContext channelContext) throws Exception;
}
