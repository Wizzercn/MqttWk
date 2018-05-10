package cn.wizzer.mqttwk;

import cn.wizzer.mqttwk.kafka.WkKafkaAdmin;
import cn.wizzer.mqttwk.kafka.WkKafkaProducer;
import org.nutz.boot.NbApp;
import org.nutz.ioc.Ioc;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.Modules;

/**
 * Created by wizzer on 2018/5/10.
 */
@IocBean(create = "init", depose = "close")
@Modules(packages = "cn.wizzer")
public class MainLauncher {
    private final static Log log = Logs.get();
    @Inject("refer:$ioc")
    private Ioc ioc;

    public static void main(String[] args) throws Exception {
        NbApp nb = new NbApp().setArgs(args).setPrintProcDoc(true);
        nb.getAppContext().setMainPackage("cn.wizzer");
        nb.run();
    }

    public void init() {
        //初始化kafka topic
        ioc.get(WkKafkaAdmin.class);
        //初始化kafka生产者
        ioc.get(WkKafkaProducer.class);
    }

    public void close() {

    }
}
