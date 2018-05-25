package cn.wizzer.mqttwk.mqtt.common.utils;

import org.tio.core.ChannelContext;

/**
 * Created by wizzer on 2018/5/13.
 */
public class MqttUtils {

    public static final String ATTR_KEY_USERNAME = "username";
    public static final String ATTR_SESSION_STOLEN = "sessionStolen";
    public static final String ATTR_CHANNEL_STATUS = "channelStatus";
    public static final String ATTR_KEY_KEEPALIVE = "keepAlive";
    public static final String ATTR_KEY_CLEANSESSION = "removeTemporaryQoS2";
    public static final String ATTR_KEY_CLIENTID = "ClientID";


    public static Object getAttribute(ChannelContext channelContext, String key) {
        return channelContext.getAttribute(key);
    }

    public static void keepAlive(ChannelContext channelContext, int keepAlive) {
        channelContext.setAttribute(ATTR_KEY_KEEPALIVE, keepAlive);
    }

    public static void cleanSession(ChannelContext channelContext, boolean cleanSession) {
        channelContext.setAttribute(ATTR_KEY_KEEPALIVE, cleanSession);
    }

    public static boolean cleanSession(ChannelContext channelContext) {
        return (Boolean) channelContext.getAttribute(MqttUtils.ATTR_KEY_CLEANSESSION);
    }

    public static void clientID(ChannelContext channelContext, String clientID) {
        channelContext.setAttribute(ATTR_KEY_CLIENTID, clientID);
    }

    public static String clientID(ChannelContext channelContext) {
        return (String) channelContext.getAttribute(ATTR_KEY_CLIENTID);
    }

    public static void userName(ChannelContext channelContext, String username) {
        channelContext.setAttribute(ATTR_KEY_USERNAME, username);
    }

    public static String userName(ChannelContext channelContext) {
        return (String) channelContext.getAttribute(MqttUtils.ATTR_KEY_USERNAME);
    }

}
