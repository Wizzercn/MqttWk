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

    public void send(String clientId, Packet packet) {
        try {
            Tio.sendToBsId(ioc.get(ServerGroupContext.class, "serverGroupContext"), clientId, packet);
            Tio.sendToBsId(ioc.get(ServerGroupContext.class, "wsServerGroupContext"), clientId, packet);
        } catch (Exception e) {
            LOGGER.warn("Tio send Packet to clientId {} is fail.", "[" + clientId + "]");
        }
    }

    public ChannelContext getChannel(String clientId) {
        try {
            ChannelContext channel = Tio.getChannelContextByBsId(ioc.get(ServerGroupContext.class, "serverGroupContext"), clientId);
            if (channel == null) {
                channel = Tio.getChannelContextByBsId(ioc.get(ServerGroupContext.class, "wsServerGroupContext"), clientId);
            }
            return channel;
        } catch (Exception e) {
            LOGGER.warn("Tio get ChannelContext by clientId {} is fail.", "[" + clientId + "]");
        }
        return null;
    }
}
