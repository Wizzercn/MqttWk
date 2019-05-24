package cn.wizzer.iot.mqtt.server.broker.webapi;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;

/**
 * Created by wizzer on 2019/5/24
 */
@IocBean
@At("/open/api/mqttwk")
public class WebApiController {
    private static final Log log = Logs.get();

}
