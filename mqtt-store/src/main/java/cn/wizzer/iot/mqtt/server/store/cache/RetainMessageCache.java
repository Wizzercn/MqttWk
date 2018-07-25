package cn.wizzer.iot.mqtt.server.store.cache;

import cn.wizzer.iot.mqtt.server.common.message.RetainMessageStore;
import com.alibaba.fastjson.JSONObject;
import org.nutz.aop.interceptor.async.Async;
import org.nutz.integration.jedis.RedisService;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
        redisService.set(CACHE_PRE + topic, JSONObject.toJSONString(obj));
        return obj;
    }

    public RetainMessageStore get(String topic) {
        return JSONObject.parseObject(redisService.get(CACHE_PRE + topic), RetainMessageStore.class);
    }

    public boolean containsKey(String topic) {
        return redisService.exists(CACHE_PRE + topic);
    }

    @Async
    public boolean remove(String topic) {
        return redisService.del(CACHE_PRE + topic) > 0;
    }

    public Map<String, RetainMessageStore> all() {
        Map<String, RetainMessageStore> map = new HashMap<>();
        Set<String> set=redisService.keys(CACHE_PRE + "*");
        if(set!=null&&!set.isEmpty()) {
            set.forEach(
                    entry -> {
                        map.put(entry.substring(CACHE_PRE.length()), JSONObject.parseObject(redisService.get(entry), RetainMessageStore.class));
                    }
            );
        }
        return map;
    }
}
