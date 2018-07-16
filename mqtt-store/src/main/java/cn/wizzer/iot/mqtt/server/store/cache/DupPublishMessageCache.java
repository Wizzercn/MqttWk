package cn.wizzer.iot.mqtt.server.store.cache;

import cn.wizzer.iot.mqtt.server.common.message.DupPublishMessageStore;
import org.nutz.integration.jedis.RedisService;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wizzer on 2018
 */
@IocBean
public class DupPublishMessageCache {
    @Inject
    private RedisService redisService;

    public ConcurrentHashMap<Integer, DupPublishMessageStore> put(String clientId, ConcurrentHashMap<Integer, DupPublishMessageStore> map) {
        redisService.set(Lang.toBytes("mqtt:publish:" + clientId), Lang.toBytes(map));
        return map;
    }

    public ConcurrentHashMap<Integer, DupPublishMessageStore> get(String clientId) {
        return Lang.fromBytes(redisService.get(Lang.toBytes("mqtt:publish:" + clientId)), ConcurrentHashMap.class);
    }

    public boolean containsKey(String clientId) {
        return !redisService.keys(Lang.toBytes("mqtt:publish:" + clientId)).isEmpty();
    }

    public boolean remove(String clientId) {
        return redisService.del(Lang.toBytes("mqtt:publish:" + clientId)) > 0;
    }
}
