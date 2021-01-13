package cn.wizzer.iot.mqtt;

import cn.wizzer.iot.model.IotDev;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.nutz.aop.interceptor.async.Async;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;

/**
 * 接收者消息回调
 * @author wizzer@qq.com
 */
@IocBean
public class MqttRecieveCallback implements MqttCallback {
    private static final Log log = Logs.get();
    @Inject
    private Dao dao;

    public void connectionLost(Throwable cause) {
        log.error(cause);
    }

    public void messageArrived(String topic, MqttMessage message) throws Exception {
        System.out.println("Client 接收消息主题 : " + topic);
        System.out.println("Client 接收消息Qos : " + message.getQos());
        System.out.println("Client 接收消息内容 : " + new String(message.getPayload()));
        String devId = topic;
        if(topic.contains("/")){
            devId=topic.substring(topic.lastIndexOf("/")+1);
            System.out.println("设备ID : " + devId);
            IotDev iotDev=new IotDev();
            iotDev.setDevId(devId);
            iotDev.setDevData(new String(message.getPayload()));
            insert(iotDev);
        }

    }

    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    // 异步插入数据库
    @Async
    public void insert(IotDev iotDev){
       dao.fastInsert(iotDev);
    }

}
