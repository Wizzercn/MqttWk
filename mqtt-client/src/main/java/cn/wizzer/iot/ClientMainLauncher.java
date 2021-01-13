package cn.wizzer.iot;

import cn.wizzer.iot.mqtt.MqttReciever;
import cn.wizzer.iot.mqtt.MqttSender;
import org.nutz.boot.NbApp;
import org.nutz.dao.Dao;
import org.nutz.dao.util.Daos;
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
public class ClientMainLauncher {
    private static final Log log = Logs.get();
    @Inject("refer:$ioc")
    private Ioc ioc;
    @Inject
    private PropertiesProxy conf;
    @Inject
    private Dao dao;


    public static void main(String[] args) throws Exception {
        NbApp nb = new NbApp().setArgs(args).setPrintProcDoc(true);
        nb.getAppContext().setMainPackage("cn.wizzer");
        nb.run();
    }

    public void init() {
        try {
            //通过POJO类创建表结构
            Daos.createTablesInPackage(dao, "cn.wizzer", false);
            //通过POJO类修改表结构
            Daos.migration(dao, "cn.wizzer", true, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 初始化MQTT接收者,并订阅topic
        ioc.get(MqttReciever.class);//MQTT订阅
        // 模拟MQTT发送消息
        this.test_send();
    }

    public void test_send() {
        try {
            MqttSender myMqttClient = ioc.get(MqttSender.class);
            int i=0;
            while (true) {
                //topic一般包含设备ID
                myMqttClient.publishMessage("/mqtt/dev/dev00001", "hello"+i, 1);
                Thread.sleep(2500);
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
