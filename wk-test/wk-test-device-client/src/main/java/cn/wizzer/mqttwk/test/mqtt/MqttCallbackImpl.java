package cn.wizzer.mqttwk.test.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;

@IocBean(name="mqttCallback")
public class MqttCallbackImpl implements MqttCallback {
    
    private static final Log log = Logs.get();

    @Override
    public void connectionLost(Throwable cause) {
        log.info("!!!!FUCK!!!!", cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        log.debugf("topic=%s, msg id=%s, qos=%s, len=%s", topic, message.getId(), message.getQos(), message.getPayload().length);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        if (!log.isDebugEnabled())
            return;
        log.debugf("token topics=[%s]", Strings.join(",", token.getTopics()));
    }

}
