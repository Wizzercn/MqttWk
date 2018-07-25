package cn.wizzer.iot.mqtt.server.store.cache;

import cn.wizzer.iot.mqtt.server.common.message.DupPublishMessageStore;
import com.alibaba.fastjson.JSONObject;
import org.nutz.aop.interceptor.async.Async;
import org.nutz.integration.jedis.RedisService;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

import java.util.Map;
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

    public DupPublishMessageStore put(String clientId, Integer messageId, DupPublishMessageStore dupPublishMessageStore) {
        redisService.hset(CACHE_PRE + clientId, String.valueOf(messageId), JSONObject.toJSONString(dupPublishMessageStore));
        return dupPublishMessageStore;
    }

    public ConcurrentHashMap<Integer, DupPublishMessageStore> get(String clientId) {
        ConcurrentHashMap<Integer, DupPublishMessageStore> map = new ConcurrentHashMap<>();
        Map<String,String> map1=redisService.hgetAll(CACHE_PRE + clientId);
        if(map1!=null&&!map1.isEmpty()) {
            map1.forEach((k, v) -> {
                map.put(Integer.valueOf(k), JSONObject.parseObject(v, DupPublishMessageStore.class));
            });
        }
        return map;
    }

    public boolean containsKey(String clientId) {
        return redisService.exists(CACHE_PRE + clientId);
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
