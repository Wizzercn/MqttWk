/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.wizzer.iot.mqtt.server.store.subscribe;

import cn.hutool.core.util.StrUtil;
import cn.wizzer.iot.mqtt.server.common.subscribe.ISubscribeStoreService;
import cn.wizzer.iot.mqtt.server.common.subscribe.SubscribeStore;
import cn.wizzer.iot.mqtt.server.store.cache.SubscribeNotWildcardCache;
import cn.wizzer.iot.mqtt.server.store.cache.SubscribeWildcardCache;
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
        subscribeNotWildcardCache.all().forEach((entry, map) -> {
            if (map.containsKey(clientId)) {
                subscribeNotWildcardCache.remove(entry, clientId);
            }
        });
        subscribeWildcardCache.all().forEach((entry, map) -> {
            if (map.containsKey(clientId)) {
                subscribeWildcardCache.remove(entry, clientId);
            }
        });
    }

    @Override
    public List<SubscribeStore> search(String topic) {
        List<SubscribeStore> subscribeStores = new ArrayList<SubscribeStore>();
        subscribeNotWildcardCache.all().forEach((topicFilter, map) -> {
            if (topic.equals(topicFilter)) {
                Collection<SubscribeStore> collection = map.values();
                List<SubscribeStore> list = new ArrayList<SubscribeStore>(collection);
                subscribeStores.addAll(list);
            }
        });
        subscribeWildcardCache.all().forEach((topicFilter, map) -> {
            if (StrUtil.split(topic, '/').size() >= StrUtil.split(topicFilter, '/').size()) {
                List<String> splitTopics = StrUtil.split(topic, '/');
                List<String> spliteTopicFilters = StrUtil.split(topicFilter, '/');
                String newTopicFilter = "";
                for (int i = 0; i < spliteTopicFilters.size(); i++) {
                    String value = spliteTopicFilters.get(i);
                    if (value.equals("+")) {
                        newTopicFilter = newTopicFilter + "+/";
                    } else if (value.equals("#")) {
                        newTopicFilter = newTopicFilter + "#/";
                        break;
                    } else {
                        newTopicFilter = newTopicFilter + splitTopics.get(i) + "/";
                    }
                }
                newTopicFilter = StrUtil.removeSuffix(newTopicFilter, "/");
                if (topicFilter.equals(newTopicFilter)) {
                    Collection<SubscribeStore> collection = map.values();
                    List<SubscribeStore> list = new ArrayList<SubscribeStore>(collection);
                    subscribeStores.addAll(list);
                }
            }
        });
        return subscribeStores;
    }

}
