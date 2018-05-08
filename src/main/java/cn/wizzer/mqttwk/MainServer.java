package cn.wizzer.mqttwk;

import cn.wizzer.mqttwk.kafka.WkKafkaAdmin;
import cn.wizzer.mqttwk.kafka.WkKafkaProducer;
import org.nutz.ioc.impl.NutIoc;
import org.nutz.ioc.loader.combo.ComboIocLoader;
import org.nutz.log.Log;
import org.nutz.log.Logs;

/**
 * Created by wizzer on 2018/5/8.
 */
public class MainServer {
    private final static Log log = Logs.get();
    public static void main(String[] args) {
        try {
            ComboIocLoader loader = new ComboIocLoader(
                    new String[]{"*json", "config/ioc/", "*anno", "cn.wizzer"}
            );
            NutIoc ioc = new NutIoc(loader);
            //初始化kafka topic
            ioc.get(WkKafkaAdmin.class);
            //初始化kafka生产者
            ioc.get(WkKafkaProducer.class);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
