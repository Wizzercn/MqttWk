package cn.wizzer.iot.mqtt.server.store.cache;

import cn.wizzer.iot.mqtt.server.common.message.DupPubRelMessageStore;
import com.alibaba.fastjson.JSONObject;
import org.nutz.aop.interceptor.async.Async;
import org.nutz.integration.jedis.RedisService;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wizzer on 2018
 */
@IocBean
public class DupPubRelMessageCache {
    private final static String CACHE_PRE = "mqttwk:pubrel:";
    @Inject
    private RedisService redisService;
    @Inject
    private PropertiesProxy conf;

    public DupPubRelMessageStore put(String clientId, Integer messageId, DupPubRelMessageStore dupPubRelMessageStore) {
        redisService.hset(CACHE_PRE + clientId, String.valueOf(messageId), JSONObject.toJSONString(dupPubRelMessageStore));
        return dupPubRelMessageStore;
    }

    public ConcurrentHashMap<Integer, DupPubRelMessageStore> get(String clientId) {
        ConcurrentHashMap<Integer, DupPubRelMessageStore> map = new ConcurrentHashMap<>();
        redisService.hgetAll(CACHE_PRE + clientId).forEach((k, v) -> {
            map.put(Integer.valueOf(k), JSONObject.parseObject(v, DupPubRelMessageStore.class));
        });
        return map;
    }

    public boolean containsKey(String clientId) {
        return !redisService.keys(CACHE_PRE + clientId).isEmpty();
    }

    @Async
    public boolean remove(String clientId, Integer messageId) {
        return redisService.hdel(CACHE_PRE + clientId, String.valueOf(messageId)) > 0;
    }

    @Async
    public boolean remove(String clientId) {
        return redisService.del(CACHE_PRE + clientId) > 0;
    }
}
