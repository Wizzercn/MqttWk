package cn.wizzer.mqttwk.mqtt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wizzer on 2018/5/14.
 */
public class MqttSessionContext {
    private static Logger log = LoggerFactory.getLogger(MqttSessionContext.class);

    private String clientId;
    private String username;
    private String keepAlive;

}
