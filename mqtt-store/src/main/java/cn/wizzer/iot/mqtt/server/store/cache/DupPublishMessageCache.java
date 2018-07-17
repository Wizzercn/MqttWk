package cn.wizzer.iot.mqtt.server.store.cache;

import cn.wizzer.iot.mqtt.server.common.message.DupPublishMessageStore;
import org.nutz.integration.jedis.RedisService;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wizzer on 2018
 */
@IocBean
public class DupPublishMessageCache {
    private final static String CACHE_PRE = "mqttwk:publish:";
    @Inject
    private RedisService redisService;
    @Inject
    private PropertiesProxy conf;

    public ConcurrentHashMap<Integer, DupPublishMessageStore> put(String clientId, ConcurrentHashMap<Integer, DupPublishMessageStore> map) {
        redisService.set((CACHE_PRE + clientId).getBytes(), Lang.toBytes(map));
        redisService.expire((CACHE_PRE + clientId).getBytes(), conf.getInt("mqttwk.broker.cache-timeout", 500));
        return map;
    }

    public ConcurrentHashMap<Integer, DupPublishMessageStore> get(String clientId) {
        return Lang.fromBytes(redisService.get((CACHE_PRE + clientId).getBytes()), ConcurrentHashMap.class);
    }

    public boolean containsKey(String clientId) {
        return !redisService.keys((CACHE_PRE + clientId).getBytes()).isEmpty();
    }

    public boolean remove(String clientId) {
        return redisService.del((CACHE_PRE + clientId).getBytes()) > 0;
    }
}
