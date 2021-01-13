package cn.wizzer.iot.mqtt;

import org.eclipse.paho.client.mqttv3.*;
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
public class MqttSender {
    private static final Log log = Logs.get();
    @Inject("refer:$ioc")
    private Ioc ioc;
    @Inject
    private PropertiesProxy conf;

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
        mqtt_clientId = conf.get("mqtt.senderClientId", "");
        mqtt_topic = conf.get("mqtt.topic", "");
        mqtt_username = conf.get("mqtt.username", "");
        mqtt_password = conf.get("mqtt.password", "");
        //初始化连接设置对象
        mqttConnectOptions = new MqttConnectOptions();
        //初始化MqttClient

        //true可以安全地使用内存持久性作为客户端断开连接时清除的所有状态
        mqttConnectOptions.setCleanSession(true);
        //设置连接超时
        mqttConnectOptions.setConnectionTimeout(30);
        mqttConnectOptions.setUserName(mqtt_username);
        mqttConnectOptions.setPassword(mqtt_password.toCharArray());
        //设置持久化方式
        memoryPersistence = new MemoryPersistence();
        try {
            mqttClient = new MqttClient(mqtt_host, mqtt_clientId, memoryPersistence);
        } catch (MqttException e) {
            e.printStackTrace();
        }
        //设置连接和回调
        if (null != mqttClient) {
            if (!mqttClient.isConnected()) {
                //客户端添加回调函数
                mqttClient.setCallback(ioc.get(MqttSenderCallback.class));
                //创建连接
                try {
                    log.info("创建连接");
                    mqttClient.connect(mqttConnectOptions);
                } catch (MqttException e) {
                    log.errorf("创建连接失败 %s", e.getMessage());
                }

            }
        } else {
            log.error("mqttClient 为空");
        }
    }

    public void closeConnect() {
        //关闭存储方式
        if (null != memoryPersistence) {
            try {
                memoryPersistence.close();
            } catch (MqttPersistenceException e) {
                e.printStackTrace();
            }
        }

        //关闭连接
        if (null != mqttClient) {
            if (mqttClient.isConnected()) {
                try {
                    mqttClient.disconnect();
                    mqttClient.close();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void publishMessage(String pubTopic, String message, int qos) {
        if (null != mqttClient && mqttClient.isConnected()) {
            MqttMessage mqttMessage = new MqttMessage();
            mqttMessage.setQos(qos);
            mqttMessage.setPayload(message.getBytes());
            MqttTopic topic = mqttClient.getTopic(pubTopic);
            if (null != topic) {
                try {
                    MqttDeliveryToken publish = topic.publish(mqttMessage);
                    if (!publish.isComplete()) {
                        log.infof("消息发布成功::%s - %s", pubTopic, message);
                    }
                } catch (MqttException e) {
                    log.errorf("消息发布失败::%s", e.getMessage());
                }
            }

        } else {
            reConnect();
            publishMessage(pubTopic, message, qos);
        }

    }

    //重新连接
    public void reConnect() {
        if (null != mqttClient) {
            if (!mqttClient.isConnected()) {
                if (null != mqttConnectOptions) {
                    try {
                        mqttClient.connect(mqttConnectOptions);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            init();
        }

    }
}
