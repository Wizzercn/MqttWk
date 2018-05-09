package cn.wizzer.mqttwk.mqtt;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.tio.server.AioServer;
import org.tio.server.ServerGroupContext;
import org.tio.server.intf.ServerAioListener;

/**
 * Created by wizzer on 2018/5/8.
 */
@IocBean(create = "init",depose = "close")
public class MqttServerStarter {
    private static final Log log = Logs.get();

    //事件监听器，可以为null，但建议自己实现该接口，可以参考showcase了解些接口
    private ServerAioListener aioListener;
    //一组连接共用的上下文对象
    public static ServerGroupContext serverGroupContext;
    //aioServer对象
    private AioServer aioServer;
    @Inject
    private MqttServerAioHandler mqttServerAioHandler;
    @Inject
    private PropertiesProxy conf;

    public void init() throws Exception {
        String serverIp = conf.get("mqttwk.host", "127.0.0.1");
        int port = conf.getInt("mqttwk.port", 9999);
        log.debug("mqtt server port::" + port);
        serverGroupContext = new ServerGroupContext("mqtt", mqttServerAioHandler, aioListener);
        serverGroupContext.setHeartbeatTimeout(30000);
        aioServer = new AioServer(serverGroupContext);
        aioServer.start(serverIp, port);
    }

    public void close(){
        if(aioServer!=null){
            aioServer.stop();
        }
    }
}
