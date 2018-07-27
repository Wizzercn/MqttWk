/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.wizzer.iot.mqtt.server.store.session;

import cn.wizzer.iot.mqtt.server.common.session.ISessionStoreService;
import cn.wizzer.iot.mqtt.server.common.session.SessionStore;
import com.alibaba.fastjson.JSONObject;
import org.nutz.aop.interceptor.async.Async;
import org.nutz.integration.jedis.RedisService;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

/**
 * 会话存储服务
 */
@IocBean
public class SessionStoreService implements ISessionStoreService {
    private final static String CACHE_PRE = "mqttwk:session:";
    @Inject
    private RedisService redisService;

    @Override
    public void put(String clientId, SessionStore sessionStore) {
        redisService.set(CACHE_PRE + clientId, JSONObject.toJSONString(sessionStore));
    }


    @Override
    public SessionStore get(String clientId) {
        String obj = redisService.get(CACHE_PRE + clientId);
        if (obj != null)
            return JSONObject.parseObject(obj, SessionStore.class);
        return null;
    }

    @Override
    public boolean containsKey(String clientId) {
        return redisService.exists(CACHE_PRE + clientId);
    }

    @Override
    @Async
    public void remove(String clientId) {
        redisService.del(CACHE_PRE + clientId);
    }
}
