package cn.wizzer.iot.mqtt.server.store.message;

import cn.wizzer.iot.mqtt.server.common.message.IMessageIdService;
import org.nutz.integration.jedis.RedisService;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wizzer on 2018
 */
@IocBean(create = "init")
public class MessageIdService implements IMessageIdService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageIdService.class);

    @Inject
    private RedisService redisService;

    @Override
    public int getNextMessageId() {
        try {
            while (true) {
                int nextMsgId = (int) (redisService.incr("mqttwk:messageid:num") % 65536);
                if (nextMsgId > 0) {
                    return nextMsgId;
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return 0;
    }

    /**
     * 每次重启的时候初始化
     */
    public void init() {
        redisService.del("mqttwk:messageid:num");
    }
}
