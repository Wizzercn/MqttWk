package cn.wizzer.iot.mqtt.server.broker.service;

import org.nutz.ioc.Ioc;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.core.intf.Packet;
import org.tio.server.ServerGroupContext;

/**
 * Created by wizzer on 2018
 */
@IocBean
public class TioService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TioService.class);
    @Inject("refer:$ioc")
    private Ioc ioc;

    public void send(String channelId, Packet packet) {
        try {
            ChannelContext channel = Tio.getChannelContextById(ioc.get(ServerGroupContext.class), channelId);
            Tio.send(channel, packet);
        } catch (Exception e) {
            LOGGER.warn("Tio send Packet to channelId {} is fail.", "[" + channelId + "]");
        }
    }

    public ChannelContext getChannel(String channelId) {
        try {
            return Tio.getChannelContextById(ioc.get(ServerGroupContext.class), channelId);
        } catch (Exception e) {
            LOGGER.warn("Tio get ChannelContext by channelId {} is fail.", "[" + channelId + "]");
        }
        return null;
    }
}
