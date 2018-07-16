package cn.wizzer.iot.mqtt.server.store.cache;

import cn.wizzer.iot.mqtt.server.common.message.DupPubRelMessageStore;
import org.nutz.integration.jedis.RedisService;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wizzer on 2018
 */
@IocBean
public class DupPubRelMessageCache {
    @Inject
    private RedisService redisService;

    public ConcurrentHashMap<Integer, DupPubRelMessageStore> put(String clientId, ConcurrentHashMap<Integer, DupPubRelMessageStore> map) {
        redisService.set(Lang.toBytes("mqtt:pubrel:" + clientId), Lang.toBytes(map));
        return map;
    }

    public ConcurrentHashMap<Integer, DupPubRelMessageStore> get(String clientId) {
        return Lang.fromBytes(redisService.get(Lang.toBytes("mqtt:pubrel:" + clientId)), ConcurrentHashMap.class);
    }

    public boolean containsKey(String clientId) {
        return !redisService.keys(Lang.toBytes("mqtt:pubrel:" + clientId)).isEmpty();
    }

    public boolean remove(String clientId) {
        return redisService.del(Lang.toBytes("mqtt:pubrel:" + clientId)) > 0;
    }
}
