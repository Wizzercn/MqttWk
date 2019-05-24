package cn.wizzer.iot.mqtt.server.broker.cluster;

import cn.wizzer.iot.mqtt.server.broker.config.BrokerProperties;
import cn.wizzer.iot.mqtt.server.broker.internal.InternalMessage;
import cn.wizzer.iot.mqtt.server.broker.internal.InternalSendServer;
import com.alibaba.fastjson.JSONObject;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.nutz.aop.interceptor.async.Async;
import org.nutz.integration.jedis.pubsub.PubSub;
import org.nutz.integration.jedis.pubsub.PubSubService;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wizzer on 2018
 */
@IocBean(create = "init")
public class RedisCluster implements PubSub {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCluster.class);
    private static final String CLUSTER_TOPIC = "mqttwk:cluster";
    @Inject
    private PubSubService pubSubService;
    @Inject
    private BrokerProperties brokerProperties;
    @Inject
    private InternalSendServer internalSendServer;

    public void init() {
        pubSubService.reg(CLUSTER_TOPIC, this);
    }

    @Override
    public void onMessage(String channel, String message) {
        InternalMessage internalMessage = JSONObject.parseObject(message, InternalMessage.class);
        //判断进程ID是否是自身实例,若相同则不发送,否则集群模式下重复发消息
        if (!brokerProperties.getId().equals(internalMessage.getBrokerId()) && !Lang.JdkTool.getProcessId("0").equals(internalMessage.getProcessId()))
            internalSendServer.sendPublishMessage(internalMessage.getClientId(), internalMessage.getTopic(), MqttQoS.valueOf(internalMessage.getMqttQoS()), internalMessage.getMessageBytes(), internalMessage.isRetain(), internalMessage.isDup());
    }

    @Async
    public void sendMessage(InternalMessage internalMessage) {
        pubSubService.fire(CLUSTER_TOPIC, JSONObject.toJSONString(internalMessage));
    }

}
