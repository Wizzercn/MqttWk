package cn.wizzer.iot.mqtt.server.store.cache;

import cn.wizzer.iot.mqtt.server.common.message.RetainMessageStore;
import com.alibaba.fastjson.JSONObject;
import org.nutz.aop.interceptor.async.Async;
import org.nutz.integration.jedis.RedisService;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by wizzer on 2018
 */
@IocBean
public class RetainMessageCache {
    private final static String CACHE_PRE = "mqttwk:retain:";
    private final static String CACHE_TOPIC = "mqttwk:retain_topic";
    @Inject
    private RedisService redisService;
    @Inject
    private PropertiesProxy conf;

    public RetainMessageStore put(String topic, RetainMessageStore obj) {
        redisService.set(CACHE_PRE + topic, JSONObject.toJSONString(obj));
        redisService.sadd(CACHE_TOPIC, topic);
        return obj;
    }

    public RetainMessageStore get(String topic) {
        return JSONObject.parseObject(redisService.get(CACHE_PRE + topic), RetainMessageStore.class);
    }

    public boolean containsKey(String topic) {
        return redisService.exists(CACHE_PRE + topic);
    }

    @Async
    public void remove(String topic) {
        redisService.del(CACHE_PRE + topic);
        redisService.srem(CACHE_TOPIC, topic);
    }

    public Map<String, RetainMessageStore> all() {
        Map<String, RetainMessageStore> map = new HashMap<>();
        Set<String> topics = redisService.smembers(CACHE_TOPIC);
        for (String topic : topics) {
            map.put(topic, JSONObject.parseObject(redisService.get(CACHE_PRE + topic), RetainMessageStore.class));
        }
        return map;
    }
}
