package cn.wizzer.iot.mqtt.server.broker.webapi;

import cn.wizzer.iot.mqtt.server.broker.cluster.RedisCluster;
import cn.wizzer.iot.mqtt.server.broker.config.BrokerProperties;
import cn.wizzer.iot.mqtt.server.broker.internal.InternalMessage;
import cn.wizzer.iot.mqtt.server.broker.internal.InternalSendServer;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.nutz.http.Request;
import org.nutz.http.Response;
import org.nutz.http.Sender;
import org.nutz.integration.jedis.RedisService;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.AdaptBy;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wizzer on 2019/5/24
 */
@IocBean
@At("/open/api/mqttwk")
public class WebApiController {
    private static final Log log = Logs.get();
    private static final String CACHE_SESSION_PRE = "mqttwk:session:";
    private static final String CACHE_CLIENT_PRE = "mqttwk:client:";
    @Inject
    private RedisService redisService;
    @Inject
    private InternalSendServer internalSendServer;
    @Inject
    private BrokerProperties brokerProperties;
    @Inject
    private RedisCluster redisCluster;

    /**
     * 向设备发送数据,发送格式见 test_send 方法实例代码
     * @param data
     * @return
     */
    @At("/send")
    @Ok("json")
    @AdaptBy(type = JsonAdaptor.class)
    public Object send(NutMap data) {
        NutMap nutMap = NutMap.NEW();
        try {
            String processId = Lang.JdkTool.getProcessId("0");
            InternalMessage message = new InternalMessage();
            message.setBrokerId(brokerProperties.getId());
            message.setProcessId(processId);
            message.setClientId(R.UU32());
            message.setTopic(data.getString("topic", ""));
            message.setRetain(data.getBoolean("retain"));
            message.setDup(data.getBoolean("dup"));
            message.setMqttQoS(data.getInt("qos"));
            message.setMessageBytes(data.getString("message", "").getBytes());
            log.debug("send:::" + Json.toJson(message));
            //如果开启集群功能
            if (brokerProperties.getClusterEnabled()) {
                redisCluster.sendMessage(message);
            } else {
                internalSendServer.sendPublishMessage(message.getClientId(), message.getTopic(), MqttQoS.valueOf(message.getMqttQoS()), message.getMessageBytes(), message.isRetain(), message.isDup());
            }
            nutMap.put("code", 0);
            nutMap.put("msg", "success");
        } catch (Exception e) {
            log.error(e);
            nutMap.put("code", -1);
            nutMap.put("msg", e.getMessage());
        }
        return nutMap;
    }

    @At("/test_send")
    @Ok("json")
    public Object test_send() {
        NutMap nutMap = NutMap.NEW();
        try {
            Request req = Request.create("http://127.0.0.1:8922/open/api/mqttwk/send", Request.METHOD.POST);
            NutMap message = NutMap.NEW();
            message.addv("topic", "/topic/mqttwk");
            message.addv("retain", true);
            message.addv("dup", true);
            message.addv("qos", 1);
            message.addv("message", "wizzer");
            req.setData(Json.toJson(message));
            Response resp = Sender.create(req).send();
            if (resp.isOK()) {
                nutMap.put("code", 0);
            }
        } catch (Exception e) {
            log.error(e);
            nutMap.put("code", -1);
        }
        return nutMap;
    }

    /**
     * 获取在线设备数量、客户端名称、订阅主题
     * example: {"code":0,"msg":"","data":{"total":0,"list":[{"clientId":"pc-web","topics":["/topic_back"]}]}}
     */
    @At("/info")
    @Ok("json")
    public Object info() {
        NutMap nutMap = NutMap.NEW();
        try {
            NutMap data = NutMap.NEW();
            ScanParams scanParams = new ScanParams();
            scanParams.match(CACHE_SESSION_PRE + "*");
            scanParams.count(Integer.MAX_VALUE);
            List<String> list = new ArrayList<>();
            int total = 0;
            while (true) {
                ScanResult scanResult = redisService.scan("0", scanParams);
                List elements = scanResult.getResult();
                if (elements != null && elements.size() > 0) {
                    list.addAll(elements);
                    total += elements.size();
                }
                String cursor = scanResult.getStringCursor();
                if ("0".equals(cursor)) {
                    break;
                }
            }
            List<NutMap> dataList = new ArrayList<>();
            for (String k : list) {
                dataList.add(NutMap.NEW()
                        .addv("clientId", k.substring(k.lastIndexOf(":") + 1))
                        .addv("topics", redisService.smembers(CACHE_CLIENT_PRE + k.substring(k.lastIndexOf(":") + 1)))
                );
            }
            data.addv("total", total);
            data.addv("list", dataList);
            nutMap.put("code", 0);
            nutMap.put("msg", "");
            nutMap.put("data", data);
        } catch (Exception e) {
            log.error(e);
            nutMap.put("code", -1);
            nutMap.put("msg", e.getMessage());
        }
        return nutMap;
    }
}
