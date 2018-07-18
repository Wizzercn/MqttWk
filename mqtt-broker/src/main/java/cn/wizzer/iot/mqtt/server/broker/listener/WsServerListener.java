package cn.wizzer.iot.mqtt.server.broker.listener;

import cn.wizzer.iot.mqtt.server.broker.config.BrokerProperties;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Encoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.core.intf.Packet;
import org.tio.websocket.common.WsResponse;
import org.tio.websocket.common.WsSessionContext;
import org.tio.websocket.server.WsServerAioListener;

/**
 * Created by wizzer on 2018
 */
@IocBean
public class WsServerListener extends WsServerAioListener {
    private static Logger LOGGER = LoggerFactory.getLogger(WsServerListener.class);
    @Inject
    private BrokerProperties brokerProperties;

    @Override
    public void onAfterConnected(ChannelContext channelContext, boolean isConnected, boolean isReconnect) throws Exception {
        super.onAfterConnected(channelContext, isConnected, isReconnect);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("onAfterConnected\r\n{}", channelContext);
        }

    }

    @Override
    public void onAfterSent(ChannelContext channelContext, Packet packet, boolean isSentSuccess) throws Exception {
        super.onAfterSent(channelContext, packet, isSentSuccess);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("onAfterSent\r\n{}\r\n{}", packet.logstr(), channelContext);
        }
    }

    @Override
    public void onBeforeClose(ChannelContext channelContext, Throwable throwable, String remark, boolean isRemove) throws Exception {
        super.onBeforeClose(channelContext, throwable, remark, isRemove);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("onBeforeClose\r\n{}", channelContext);
        }

        WsSessionContext wsSessionContext = (WsSessionContext) channelContext.getAttribute();

        if (wsSessionContext.isHandshaked()) {

            int count = Tio.getAllChannelContexts(channelContext.groupContext).getObj().size();

            String msg = channelContext.getClientNode().toString() + " 离开了，现在共有【" + count + "】人在线";
            //用tio-websocket，服务器发送到客户端的Packet都是WsResponse
            WsResponse wsResponse = WsResponse.fromText(msg, Encoding.UTF8);
            //群发
            Tio.sendToGroup(channelContext.groupContext, brokerProperties.getId(), wsResponse);
        }
    }

    @Override
    public void onAfterDecoded(ChannelContext channelContext, Packet packet, int packetSize) throws Exception {
        super.onAfterDecoded(channelContext, packet, packetSize);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("onAfterDecoded\r\n{}\r\n{}", packet.logstr(), channelContext);
        }
    }

    @Override
    public void onAfterReceivedBytes(ChannelContext channelContext, int receivedBytes) throws Exception {
        super.onAfterReceivedBytes(channelContext, receivedBytes);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("onAfterReceivedBytes\r\n{}", channelContext);
        }
    }

    @Override
    public void onAfterHandled(ChannelContext channelContext, Packet packet, long cost) throws Exception {
        super.onAfterHandled(channelContext, packet, cost);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("onAfterHandled\r\n{}\r\n{}", packet.logstr(), channelContext);
        }
    }
}
