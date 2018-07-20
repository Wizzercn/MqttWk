package cn.wizzer.iot.mqtt.server.broker.service;

import org.nutz.aop.interceptor.async.Async;
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
    private ServerGroupContext serverGroupContext;
    private ServerGroupContext wsServerGroupContext;

    @Async
    public void send(String clientId, Packet packet) {
        try {
            if (serverGroupContext == null) {
                serverGroupContext = ioc.get(ServerGroupContext.class, "serverGroupContext");
            }
            if (wsServerGroupContext == null) {
                wsServerGroupContext = ioc.get(ServerGroupContext.class, "wsServerGroupContext");
            }
            Tio.sendToBsId(serverGroupContext, clientId, packet);
            Tio.sendToBsId(wsServerGroupContext, clientId, packet);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.warn("Tio send Packet to clientId {} is fail.", "[" + clientId + "]");
        }
    }

    @Async
    public ChannelContext getChannel(String clientId) {
        try {
            if (serverGroupContext == null) {
                serverGroupContext = ioc.get(ServerGroupContext.class, "serverGroupContext");
            }
            if (wsServerGroupContext == null) {
                wsServerGroupContext = ioc.get(ServerGroupContext.class, "wsServerGroupContext");
            }
            ChannelContext channel = Tio.getChannelContextByBsId(serverGroupContext, clientId);
            if (channel == null) {
                channel = Tio.getChannelContextByBsId(wsServerGroupContext, clientId);
            }
            return channel;
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.warn("Tio get ChannelContext by clientId {} is fail.", "[" + clientId + "]");
        }
        return null;
    }
}
