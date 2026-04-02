/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.wizzer.iot.mqtt.server.store.subscribe;

import cn.hutool.core.util.StrUtil;
import cn.wizzer.iot.mqtt.server.common.subscribe.ISubscribeStoreService;
import cn.wizzer.iot.mqtt.server.common.subscribe.SubscribeStore;
import cn.wizzer.iot.mqtt.server.store.cache.SubscribeNotWildcardCache;
import cn.wizzer.iot.mqtt.server.store.cache.SubscribeWildcardCache;
import org.nutz.aop.interceptor.async.Async;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 订阅存储服务
 */
@IocBean
public class SubscribeStoreService implements ISubscribeStoreService {

    @Inject
    private SubscribeNotWildcardCache subscribeNotWildcardCache;

    @Inject
    private SubscribeWildcardCache subscribeWildcardCache;

    @Override
    public void put(String topicFilter, SubscribeStore subscribeStore) {
        if (StrUtil.contains(topicFilter, '#') || StrUtil.contains(topicFilter, '+')) {
            subscribeWildcardCache.put(topicFilter, subscribeStore.getClientId(), subscribeStore);
        } else {
            subscribeNotWildcardCache.put(topicFilter, subscribeStore.getClientId(), subscribeStore);
        }
    }

    @Override
    public void remove(String topicFilter, String clientId) {
        if (StrUtil.contains(topicFilter, '#') || StrUtil.contains(topicFilter, '+')) {
            subscribeWildcardCache.remove(topicFilter, clientId);
        } else {
            subscribeNotWildcardCache.remove(topicFilter, clientId);
        }
    }

    @Override
    public void removeForClient(String clientId) {
        subscribeNotWildcardCache.removeForClient(clientId);
        subscribeWildcardCache.removeForClient(clientId);
    }

    @Override
    public List<SubscribeStore> search(String topic) {
        List<SubscribeStore> subscribeStores = new ArrayList<SubscribeStore>();
        List<SubscribeStore> list = subscribeNotWildcardCache.all(topic);
        if (list.size() > 0) {
            subscribeStores.addAll(list);
        }
        // 优化: 预先分割topic, 避免在循环中重复分割
        List<String> splitTopics = StrUtil.split(topic, '/');
        subscribeWildcardCache.all().forEach((topicFilter, map) -> {
            List<String> splitTopicFilters = StrUtil.split(topicFilter, '/');
            if (splitTopics.size() >= splitTopicFilters.size()) {
                // 优化: 使用StringBuilder替代String拼接, 减少GC压力
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < splitTopicFilters.size(); i++) {
                    String value = splitTopicFilters.get(i);
                    if (value.equals("+")) {
                        sb.append("+/");
                    } else if (value.equals("#")) {
                        sb.append("#/");
                        break;
                    } else {
                        sb.append(splitTopics.get(i)).append('/');
                    }
                }
                // 移除末尾的 /
                if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '/') {
                    sb.setLength(sb.length() - 1);
                }
                if (topicFilter.equals(sb.toString())) {
                    Collection<SubscribeStore> collection = map.values();
                    subscribeStores.addAll(collection);
                }
            }
        });
        return subscribeStores;
    }

}
