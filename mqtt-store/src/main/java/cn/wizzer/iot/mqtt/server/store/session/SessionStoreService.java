/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.wizzer.iot.mqtt.server.store.session;

import cn.wizzer.iot.mqtt.server.common.session.ISessionStoreService;
import cn.wizzer.iot.mqtt.server.common.session.SessionStore;
import org.nutz.integration.jedis.JedisAgent;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import redis.clients.jedis.Jedis;

/**
 * 会话存储服务
 */
@IocBean
public class SessionStoreService implements ISessionStoreService {
    private final static String CACHE_PRE = "mqttwk:session:";
    @Inject
    private JedisAgent jedisAgent;
    @Inject
    private PropertiesProxy conf;

    @Override
    public void put(String clientId, SessionStore sessionStore) {
        try (Jedis jedis = jedisAgent.getResource()) {
            jedis.set((CACHE_PRE + clientId).getBytes(), Lang.toBytes(sessionStore));
        }
    }


    @Override
    public SessionStore get(String clientId) {
        try (Jedis jedis = jedisAgent.getResource()) {
            return Lang.fromBytes(jedis.get((CACHE_PRE + clientId).getBytes()), SessionStore.class);
        }
    }

    @Override
    public boolean containsKey(String clientId) {
        try (Jedis jedis = jedisAgent.getResource()) {
            return !jedis.keys((CACHE_PRE + clientId).getBytes()).isEmpty();
        }
    }

    @Override
    public void remove(String clientId) {
        try (Jedis jedis = jedisAgent.getResource()) {
            jedis.del((CACHE_PRE + clientId).getBytes());
        }
    }
}
