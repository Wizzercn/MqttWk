/**
 * Created by wizzer on 2018
 */

package cn.wizzer.iot.mqtt.server.store.session;

import cn.wizzer.iot.mqtt.server.common.session.ISessionStoreService;
import cn.wizzer.iot.mqtt.server.common.session.SessionStore;
import cn.wizzer.iot.mqtt.server.store.util.StoreUtil;
import com.alibaba.fastjson.JSON;
import org.nutz.aop.interceptor.async.Async;
import org.nutz.integration.jedis.RedisService;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;

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
        //SessionStore对象不能正常转为JSON,使用工具类类解决
        NutMap nutMap = StoreUtil.transPublishToMapBeta(sessionStore);
        redisService.set(CACHE_PRE + clientId, JSON.toJSONString(nutMap));
    }


    @Override
    public SessionStore get(String clientId) {
        String jsonObj = redisService.get(CACHE_PRE + clientId);
        if (Strings.isNotBlank(jsonObj)) {
            NutMap nutMap = JSON.parseObject(jsonObj, NutMap.class);
            return StoreUtil.mapTransToPublishMsgBeta(nutMap);
        }
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
