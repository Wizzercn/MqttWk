package cn.wizzer.iot.mqtt.server.store.cache;

import cn.wizzer.iot.mqtt.server.common.message.RetainMessageStore;
import org.nutz.integration.jedis.RedisService;
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
    @Inject
    private RedisService redisService;

    public RetainMessageStore put(String topic, RetainMessageStore obj) {
        redisService.set(Lang.toBytes("mqtt:retain:" + topic), Lang.toBytes(obj));
        return obj;
    }

    public RetainMessageStore get(String topic) {
        return Lang.fromBytes(redisService.get(Lang.toBytes("mqtt:retain:" + topic)), RetainMessageStore.class);
    }

    public boolean containsKey(String topic) {
        return !redisService.keys(Lang.toBytes("mqtt:retain:" + topic)).isEmpty();
    }

    public boolean remove(String topic) {
        return redisService.del(Lang.toBytes("mqtt:retain:" + topic)) > 0;
    }

    public Map<String, RetainMessageStore> all() {
        Map<String, RetainMessageStore> map = new HashMap<>();
        redisService.keys(Lang.toBytes("mqtt:retain:*")).forEach(
                entry -> {
                    map.put(new String(entry), Lang.fromBytes(redisService.get(entry), RetainMessageStore.class));
                }
        );
        return map;
    }
}
