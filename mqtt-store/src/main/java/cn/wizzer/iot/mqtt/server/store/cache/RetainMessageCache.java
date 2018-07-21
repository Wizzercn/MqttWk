package cn.wizzer.iot.mqtt.server.store.cache;

import cn.wizzer.iot.mqtt.server.common.message.RetainMessageStore;
import org.nutz.integration.jedis.JedisAgent;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wizzer on 2018
 */
@IocBean
public class RetainMessageCache {
    private final static String CACHE_PRE = "mqttwk:retain:";
    @Inject
    private JedisAgent jedisAgent;
    @Inject
    private PropertiesProxy conf;

    public RetainMessageStore put(String topic, RetainMessageStore obj) {
        try (Jedis jedis = jedisAgent.getResource()) {
            jedis.set((CACHE_PRE + topic).getBytes(), Lang.toBytes(obj));
            return obj;
        }
    }

    public RetainMessageStore get(String topic) {
        try (Jedis jedis = jedisAgent.getResource()) {
            return Lang.fromBytes(jedis.get((CACHE_PRE + topic).getBytes()), RetainMessageStore.class);
        }
    }

    public boolean containsKey(String topic) {
        try (Jedis jedis = jedisAgent.getResource()) {
            return !jedis.keys((CACHE_PRE + topic).getBytes()).isEmpty();
        }
    }

    public boolean remove(String topic) {
        try (Jedis jedis = jedisAgent.getResource()) {
            return jedis.del((CACHE_PRE + topic).getBytes()) > 0;
        }
    }

    public Map<String, RetainMessageStore> all() {
        try (Jedis jedis = jedisAgent.getResource()) {
            Map<String, RetainMessageStore> map = new HashMap<>();
            jedis.keys((CACHE_PRE + "*").getBytes()).forEach(
                    entry -> {
                        map.put(new String(entry).substring(CACHE_PRE.length()), Lang.fromBytes(jedis.get(entry), RetainMessageStore.class));
                    }
            );
            return map;
        }
    }
}
