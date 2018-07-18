package cn.wizzer.iot.mqtt.server.store.cache;

import cn.wizzer.iot.mqtt.server.common.subscribe.SubscribeStore;
import org.nutz.integration.jedis.RedisService;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;

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
    private RedisService redisService;
    @Inject
    private PropertiesProxy conf;

    public ConcurrentHashMap<String, SubscribeStore> put(String clientId, ConcurrentHashMap<String, SubscribeStore> map) {
        redisService.set((CACHE_PRE + clientId).getBytes(), Lang.toBytes(map));
        return map;
    }

    public ConcurrentHashMap<String, SubscribeStore> get(String clientId) {
        return Lang.fromBytes(redisService.get((CACHE_PRE + clientId).getBytes()), ConcurrentHashMap.class);
    }

    public boolean containsKey(String clientId) {
        return !redisService.keys((CACHE_PRE + clientId).getBytes()).isEmpty();
    }

    public boolean remove(String clientId) {
        return redisService.del((CACHE_PRE + clientId).getBytes()) > 0;
    }

    public Map<String, ConcurrentHashMap<String, SubscribeStore>> all() {
        Map<String, ConcurrentHashMap<String, SubscribeStore>> map = new HashMap<>();
        redisService.keys((CACHE_PRE + "*").getBytes()).forEach(
                entry -> {
                    map.put(new String(entry).substring(CACHE_PRE.length()), Lang.fromBytes(redisService.get(entry), ConcurrentHashMap.class));
                }
        );
        return map;
    }
}
