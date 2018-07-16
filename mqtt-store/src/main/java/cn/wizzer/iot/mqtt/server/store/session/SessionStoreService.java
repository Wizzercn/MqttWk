/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.wizzer.iot.mqtt.server.store.session;

import cn.wizzer.iot.mqtt.server.common.session.ISessionStoreService;
import cn.wizzer.iot.mqtt.server.common.session.SessionStore;
import org.nutz.integration.jedis.RedisService;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;

/**
 * 会话存储服务
 */
@IocBean
public class SessionStoreService implements ISessionStoreService {

    @Inject
    private RedisService redisService;

    @Override
    public void put(String clientId, SessionStore sessionStore) {
        System.out.println("redisService:::"+redisService);
        System.out.println("sessionStore:::"+sessionStore);
        redisService.set(Lang.toBytes("mqttwk:session:" + clientId), Lang.toBytes(sessionStore));
    }

    @Override
    public SessionStore get(String clientId) {
        return Lang.fromBytes(redisService.get(Lang.toBytes("mqttwk:session:" + clientId)), SessionStore.class);
    }

    @Override
    public boolean containsKey(String clientId) {
        return redisService.get(Lang.toBytes("mqttwk:session:" + clientId)) != null;
    }

    @Override
    public void remove(String clientId) {
        redisService.del(Lang.toBytes("mqttwk:session:" + clientId));
    }
}
