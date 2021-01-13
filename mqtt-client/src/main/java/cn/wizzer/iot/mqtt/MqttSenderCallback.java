package cn.wizzer.iot.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.nutz.ioc.loader.annotation.IocBean;

/**
 * 发送者消息回调
 * @author wizzer@qq.com
 */
@IocBean
public class MqttSenderCallback implements MqttCallback {

    public void connectionLost(Throwable cause) {

    }

    public void messageArrived(String topic, MqttMessage message) throws Exception {
        System.out.println("Client 接收消息主题 : " + topic);
        System.out.println("Client 接收消息Qos : " + message.getQos());
        System.out.println("Client 接收消息内容 : " + new String(message.getPayload()));
    }

    public void deliveryComplete(IMqttDeliveryToken token) {

    }

}
