package cn.wizzer.iot.mqtt.server.store.cache;

import cn.wizzer.iot.mqtt.server.common.subscribe.SubscribeStore;
import com.alibaba.fastjson.JSONObject;
import org.nutz.aop.interceptor.async.Async;
import org.nutz.integration.jedis.RedisService;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wizzer on 2018
 */
@IocBean
public class SubscribeNotWildcardCache {
    private final static String CACHE_PRE = "mqttwk:subnotwildcard:";
    @Inject
    private RedisService redisService;

    public SubscribeStore put(String topic, String clientId, SubscribeStore subscribeStore) {
        redisService.hset(CACHE_PRE + topic, clientId, JSONObject.toJSONString(subscribeStore));
        return subscribeStore;
    }

    public SubscribeStore get(String topic, String clientId) {
        return JSONObject.parseObject(redisService.hget(CACHE_PRE + topic, clientId), SubscribeStore.class);
    }

    public boolean containsKey(String topic, String clientId) {
        return redisService.hexists(CACHE_PRE + topic, clientId);
    }

    @Async
    public boolean remove(String topic, String clientId) {
        return redisService.hdel(CACHE_PRE + topic, clientId) > 0;
    }

    public Map<String, ConcurrentHashMap<String, SubscribeStore>> all() {
        Map<String, ConcurrentHashMap<String, SubscribeStore>> map = new HashMap<>();
        redisService.keys(CACHE_PRE + "*").forEach(
                entry -> {
                    ConcurrentHashMap<String, SubscribeStore> map1 = new ConcurrentHashMap<>();
                    redisService.hgetAll(entry).forEach((k, v) -> {
                        map1.put(k, JSONObject.parseObject(redisService.hget(entry, k), SubscribeStore.class));
                    });
                    map.put(entry.substring(CACHE_PRE.length()), map1);
                }
        );
        return map;
    }
}
