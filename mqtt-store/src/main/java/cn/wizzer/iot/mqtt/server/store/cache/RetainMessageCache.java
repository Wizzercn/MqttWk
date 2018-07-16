package cn.wizzer.iot.mqtt.server.store.cache;

import cn.wizzer.iot.mqtt.server.common.message.RetainMessageStore;
import org.nutz.integration.jedis.RedisService;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wizzer on 2018
 */
@IocBean
public class RetainMessageCache {
    private final static String CACHE_PRE = "mqttwk:retain:";
    @Inject
    private RedisService redisService;
    @Inject
    private PropertiesProxy conf;

    public RetainMessageStore put(String topic, RetainMessageStore obj) {
        redisService.set((CACHE_PRE + topic).getBytes(), Lang.toBytes(obj));
        redisService.expire((CACHE_PRE + topic).getBytes(), conf.getInt("mqttwk.broker.keep-alive", 60) + 1);
        return obj;
    }

    public RetainMessageStore get(String topic) {
        return Lang.fromBytes(redisService.get((CACHE_PRE + topic).getBytes()), RetainMessageStore.class);
    }

    public boolean containsKey(String topic) {
        return !redisService.keys((CACHE_PRE + topic).getBytes()).isEmpty();
    }

    public boolean remove(String topic) {
        return redisService.del((CACHE_PRE + topic).getBytes()) > 0;
    }

    public Map<String, RetainMessageStore> all() {
        Map<String, RetainMessageStore> map = new HashMap<>();
        redisService.keys((CACHE_PRE + "*").getBytes()).forEach(
                entry -> {
                    map.put(new String(entry), Lang.fromBytes(redisService.get(entry), RetainMessageStore.class));
                }
        );
        return map;
    }
}
