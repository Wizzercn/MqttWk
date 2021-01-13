package cn.wizzer.iot.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.nutz.ioc.Ioc;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;

/**
 * @author wizzer@qq.com
 */
@IocBean(create = "init")
public class MqttReciever {
    private static final Log log = Logs.get();
    @Inject("refer:$ioc")
    private Ioc ioc;
    @Inject
    private PropertiesProxy conf;

    private static int QoS = 1;
    public static MqttClient mqttClient = null;
    private static MemoryPersistence memoryPersistence = null;
    private static MqttConnectOptions mqttConnectOptions = null;

    private static String mqtt_host;
    private static String mqtt_clientId;
    private static String mqtt_topic;
    private static String mqtt_username;
    private static String mqtt_password;

    public void init() {
        mqtt_host = conf.get("mqtt.host", "");
        mqtt_clientId = conf.get("mqtt.recieverClientId", "");
        mqtt_topic = conf.get("mqtt.topic", "");
        mqtt_username = conf.get("mqtt.username", "");
        mqtt_password = conf.get("mqtt.password", "");
        memoryPersistence = new MemoryPersistence();
        try {
            mqttClient = new MqttClient(mqtt_host, mqtt_clientId, memoryPersistence);
        } catch (MqttException e) {
            log.errorf("mqttClient %s", e.getMessage());
        }
        mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setAutomaticReconnect(true);
        //超时时间 60秒
        mqttConnectOptions.setConnectionTimeout(60);
        //心跳时间 超时断开连接
        mqttConnectOptions.setKeepAliveInterval(0);
        mqttConnectOptions.setUserName(mqtt_username);
        mqttConnectOptions.setPassword(mqtt_password.toCharArray());
        if (null != mqttClient && !mqttClient.isConnected()) {
            try {
                mqttClient.setCallback(ioc.get(MqttRecieveCallback.class));
                mqttClient.connect(mqttConnectOptions);
                mqttClient.subscribe(mqtt_topic, QoS);
            } catch (MqttException e) {
                log.errorf("mqttClient %s", e.getMessage());
            }
        }
    }
}
