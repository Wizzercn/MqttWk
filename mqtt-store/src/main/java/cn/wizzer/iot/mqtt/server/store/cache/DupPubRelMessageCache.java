package cn.wizzer.iot.mqtt.server.store.cache;

import cn.wizzer.iot.mqtt.server.common.message.DupPubRelMessageStore;
import org.nutz.integration.jedis.JedisAgent;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import redis.clients.jedis.Jedis;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wizzer on 2018
 */
@IocBean
public class DupPubRelMessageCache {
    private final static String CACHE_PRE = "mqttwk:pubrel:";
    @Inject
    private JedisAgent jedisAgent;
    @Inject
    private PropertiesProxy conf;

    public ConcurrentHashMap<Integer, DupPubRelMessageStore> put(String clientId, ConcurrentHashMap<Integer, DupPubRelMessageStore> map) {
        try (Jedis jedis = jedisAgent.getResource()) {
            jedis.set((CACHE_PRE + clientId).getBytes(), Lang.toBytes(map));
            return map;
        }
    }

    public ConcurrentHashMap<Integer, DupPubRelMessageStore> get(String clientId) {
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
}
