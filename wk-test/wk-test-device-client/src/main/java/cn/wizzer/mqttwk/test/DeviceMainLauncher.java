package cn.wizzer.mqttwk.test;

import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.*;
import org.nutz.boot.NbApp;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.lang.Tasks;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;

@IocBean(create="init")
public class DeviceMainLauncher {
    
    private static final Log log = Logs.get();

    @Inject
    MqttClient mqttClient;
    
    @Inject
    MqttAsyncClient mqttAsyncClient;
    
	public void init() throws MqttException {
        // 这里为了方便演示,订阅发布都是同一个topic
	    String topic = "data-topic";
	    // 启动一个订阅者
	    Tasks.getTaskScheduler().schedule(()->{
	        try {
                mqttClient.subscribe(topic, (_topic, msg)->{
                    log.debugf("revc topic=%s msg=%s", _topic, msg.toString());
                });
            }
            catch (MqttException e) {
                log.debug("FUCK", e);
            }
	    }, 1, TimeUnit.MILLISECONDS);
	    // 定时发布一个消息
	    Tasks.getTaskScheduler().scheduleAtFixedRate(()->{
	        try {
	            NutMap msg = new NutMap();
	            msg.setv("sender", mqttClient.getClientId());
	            msg.setv("time", System.currentTimeMillis());
                mqttClient.publish(topic, new MqttMessage(Json.toJson(msg).getBytes()));
            }
            catch (MqttException e) {
                log.debug("FUCK", e);
            }
	    }, 2, 5, TimeUnit.SECONDS);
	}

	// 请启动2次!!
	public static void main(String[] args) throws Exception {
		new NbApp().setPrintProcDoc(true).run();
	}

}
