package cn.wizzer.iot.mqtt.server.store.cache;

import cn.wizzer.iot.mqtt.server.common.subscribe.SubscribeStore;
import com.alibaba.fastjson.JSONObject;
import org.nutz.aop.interceptor.async.Async;
import org.nutz.integration.jedis.JedisAgent;
import org.nutz.integration.jedis.RedisService;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wizzer on 2018
 */
@IocBean
public class SubscribeNotWildcardCache {
    private final static String CACHE_PRE = "mqttwk:subnotwildcard:";
    private final static String CACHE_CLIENT_PRE = "mqttwk:client:";
    private final static String CACHE_TOPIC = "mqttwk:subnotwildcard_topic";
    @Inject
    private RedisService redisService;
    @Inject
    private JedisAgent jedisAgent;

    public SubscribeStore put(String topic, String clientId, SubscribeStore subscribeStore) {
        redisService.hset(CACHE_PRE + topic, clientId, JSONObject.toJSONString(subscribeStore));
        redisService.sadd(CACHE_CLIENT_PRE + clientId, topic);
        redisService.sadd(CACHE_TOPIC, topic);
        return subscribeStore;
    }

    public SubscribeStore get(String topic, String clientId) {
        return JSONObject.parseObject(redisService.hget(CACHE_PRE + topic, clientId), SubscribeStore.class);
    }

    public boolean containsKey(String topic, String clientId) {
        return redisService.hexists(CACHE_PRE + topic, clientId);
    }

    @Async
    public void remove(String topic, String clientId) {
        redisService.srem(CACHE_CLIENT_PRE + clientId, topic);
        redisService.hdel(CACHE_PRE + topic, clientId);
        if (redisService.hlen(CACHE_PRE + topic) == 0) {
            redisService.srem(CACHE_TOPIC, topic);
        }
    }

    @Async
    public void removeForClient(String clientId) {
        for (String topic : redisService.smembers(CACHE_CLIENT_PRE + clientId)) {
            redisService.hdel(CACHE_PRE + topic, clientId);
            if (redisService.hlen(CACHE_PRE + topic) == 0) {
                redisService.srem(CACHE_TOPIC, topic);
            }
        }
        redisService.del(CACHE_CLIENT_PRE + clientId);
    }

    public Map<String, ConcurrentHashMap<String, SubscribeStore>> all() {
        Map<String, ConcurrentHashMap<String, SubscribeStore>> map = new HashMap<>();
        Set<String> topics = redisService.smembers(CACHE_TOPIC);
        for (String topic : topics) {
            ConcurrentHashMap<String, SubscribeStore> map1 = new ConcurrentHashMap<>();
            Map<String, String> map2 = redisService.hgetAll(CACHE_PRE + topic);
            if (map2 != null && !map2.isEmpty()) {
                map2.forEach((k, v) -> {
                    map1.put(k, JSONObject.parseObject(v, SubscribeStore.class));
                });
                map.put(topic, map1);
            }
        }
        return map;
    }

    public List<SubscribeStore> all(String topic) {
        List<SubscribeStore> list = new ArrayList<>();
        Map<String, String> map = redisService.hgetAll(CACHE_PRE + topic);
        if (map != null && !map.isEmpty()) {
            map.forEach((k, v) -> {
                list.add(JSONObject.parseObject(v, SubscribeStore.class));
            });
        }
        return list;
    }
}
