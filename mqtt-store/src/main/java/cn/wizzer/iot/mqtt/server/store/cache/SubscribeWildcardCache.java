package cn.wizzer.iot.mqtt.server.store.cache;

import cn.wizzer.iot.mqtt.server.common.subscribe.SubscribeStore;
import org.nutz.integration.jedis.RedisService;
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
    @Inject
    private RedisService redisService;

    public ConcurrentHashMap<String, SubscribeStore> put(String clientId, ConcurrentHashMap<String, SubscribeStore> map) {
        redisService.set(Lang.toBytes("mqtt:subwildcard:" + clientId), Lang.toBytes(map));
        return map;
    }

    public ConcurrentHashMap<String, SubscribeStore> get(String clientId) {
        return Lang.fromBytes(redisService.get(Lang.toBytes("mqtt:subwildcard:" + clientId)), ConcurrentHashMap.class);
    }

    public boolean containsKey(String clientId) {
        return !redisService.keys(Lang.toBytes("mqtt:subwildcard:" + clientId)).isEmpty();
    }

    public boolean remove(String clientId) {
        return redisService.del(Lang.toBytes("mqtt:subwildcard:" + clientId)) > 0;
    }

    public Map<String, ConcurrentHashMap<String, SubscribeStore>> all() {
        Map<String, ConcurrentHashMap<String, SubscribeStore>> map = new HashMap<>();
        redisService.keys(Lang.toBytes("mqtt:subwildcard:*")).forEach(
                entry -> {
                    map.put(new String(entry), Lang.fromBytes(redisService.get(entry), ConcurrentHashMap.class));
                }
        );
        return map;
    }
}
