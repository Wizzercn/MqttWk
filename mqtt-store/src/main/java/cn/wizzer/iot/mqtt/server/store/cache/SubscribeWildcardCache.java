package cn.wizzer.iot.mqtt.server.store.cache;

import cn.wizzer.iot.mqtt.server.common.subscribe.SubscribeStore;
import org.nutz.integration.jedis.JedisAgent;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wizzer on 2018
 */
@IocBean
public class SubscribeWildcardCache {
    private final static String CACHE_PRE = "mqttwk:subwildcard:";
    @Inject
    private JedisAgent jedisAgent;
    @Inject
    private PropertiesProxy conf;

    public ConcurrentHashMap<String, SubscribeStore> put(String clientId, ConcurrentHashMap<String, SubscribeStore> map) {
        try (Jedis jedis = jedisAgent.getResource()) {
            jedis.set((CACHE_PRE + clientId).getBytes(), Lang.toBytes(map));
        }
        return map;
    }

    public ConcurrentHashMap<String, SubscribeStore> get(String clientId) {
        try (Jedis jedis = jedisAgent.getResource()) {
            return Lang.fromBytes(jedis.get((CACHE_PRE + clientId).getBytes()), ConcurrentHashMap.class);
        }
    }

    public boolean containsKey(String clientId) {
        try (Jedis jedis = jedisAgent.getResource()) {
            return !jedis.keys((CACHE_PRE + clientId).getBytes()).isEmpty();
        }
    }

    public boolean remove(String clientId) {
        try (Jedis jedis = jedisAgent.getResource()) {
            return jedis.del((CACHE_PRE + clientId).getBytes()) > 0;
        }
    }

    public Map<String, ConcurrentHashMap<String, SubscribeStore>> all() {
        try (Jedis jedis = jedisAgent.getResource()) {
            Map<String, ConcurrentHashMap<String, SubscribeStore>> map = new HashMap<>();
            jedis.keys((CACHE_PRE + "*").getBytes()).forEach(
                    entry -> {
                        map.put(new String(entry).substring(CACHE_PRE.length()), Lang.fromBytes(jedis.get(entry), ConcurrentHashMap.class));
                    }
            );
            return map;
        }
    }
}
