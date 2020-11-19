package cn.wizzer.iot.mqtt.server.store.cache;

import cn.wizzer.iot.mqtt.server.common.message.RetainMessageStore;
import com.alibaba.fastjson.JSONObject;
import org.nutz.aop.interceptor.async.Async;
import org.nutz.integration.jedis.JedisAgent;
import org.nutz.integration.jedis.RedisService;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Streams;
import redis.clients.jedis.*;

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
    @Inject
    private JedisAgent jedisAgent;

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
    public void remove(String topic) {
        redisService.del(CACHE_PRE + topic);
    }

    public Map<String, RetainMessageStore> all() {
        Map<String, RetainMessageStore> map = new HashMap<>();
        ScanParams match = new ScanParams().match(CACHE_PRE + "*");
        if (jedisAgent.isClusterMode()) {
            JedisCluster jedisCluster = jedisAgent.getJedisClusterWrapper().getJedisCluster();
            for (JedisPool pool : jedisCluster.getClusterNodes().values()) {
                try (Jedis jedis = pool.getResource()) {
                    ScanResult<String> scan = null;
                    do {
                        scan = jedis.scan(scan == null ? ScanParams.SCAN_POINTER_START : scan.getStringCursor(), match);
                        for (String key : scan.getResult()) {
                            map.put(key.substring(CACHE_PRE.length()), JSONObject.parseObject(redisService.get(key), RetainMessageStore.class));
                        }
                    } while (!scan.isCompleteIteration());
                }
            }
        } else {
            Jedis jedis = null;
            try {
                jedis = jedisAgent.jedis();
                ScanResult<String> scan = null;
                do {
                    scan = jedis.scan(scan == null ? ScanParams.SCAN_POINTER_START : scan.getStringCursor(), match);
                    for (String key : scan.getResult()) {
                        map.put(key.substring(CACHE_PRE.length()), JSONObject.parseObject(redisService.get(key), RetainMessageStore.class));
                    }
                } while (!scan.isCompleteIteration());
            } finally {
                Streams.safeClose(jedis);
            }
        }
        return map;
    }
}
