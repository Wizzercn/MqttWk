/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.wizzer.iot.mqtt.server.store.session;

import cn.wizzer.iot.mqtt.server.common.session.ISessionStoreService;
import cn.wizzer.iot.mqtt.server.common.session.SessionStore;
import org.nutz.aop.interceptor.async.Async;
import org.nutz.integration.jedis.RedisService;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;

/**
 * 会话存储服务
 */
@IocBean
public class SessionStoreService implements ISessionStoreService {
    private final static String CACHE_PRE = "mqttwk:session:";
    @Inject
    private RedisService redisService;
    @Inject
    private PropertiesProxy conf;

    @Override
    public void put(String clientId, SessionStore sessionStore) {
        redisService.set((CACHE_PRE + clientId).getBytes(), Lang.toBytes(sessionStore));
    }


    @Override
    public SessionStore get(String clientId) {
        return Lang.fromBytes(redisService.get((CACHE_PRE + clientId).getBytes()), SessionStore.class);
    }

    @Override
    public boolean containsKey(String clientId) {
        return !redisService.keys((CACHE_PRE + clientId).getBytes()).isEmpty();
    }

    @Override
    @Async
    public void remove(String clientId) {
        redisService.del((CACHE_PRE + clientId).getBytes());
    }
}
