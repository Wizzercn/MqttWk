/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.wizzer.iot.mqtt.server.store.message;

import cn.hutool.core.util.StrUtil;
import cn.wizzer.iot.mqtt.server.common.message.IRetainMessageStoreService;
import cn.wizzer.iot.mqtt.server.common.message.RetainMessageStore;
import cn.wizzer.iot.mqtt.server.store.cache.RetainMessageCache;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

import java.util.ArrayList;
import java.util.List;

@IocBean
public class RetainMessageStoreService implements IRetainMessageStoreService {

    @Inject
    private RetainMessageCache retainMessageCache;

    @Override
    public void put(String topic, RetainMessageStore retainMessageStore) {
        retainMessageCache.put(topic, retainMessageStore);
    }

    @Override
    public RetainMessageStore get(String topic) {
        return retainMessageCache.get(topic);
    }

    @Override
    public void remove(String topic) {
        retainMessageCache.remove(topic);
    }

    @Override
    public boolean containsKey(String topic) {
        return retainMessageCache.containsKey(topic);
    }

    @Override
    public List<RetainMessageStore> search(String topicFilter) {
        List<RetainMessageStore> retainMessageStores = new ArrayList<RetainMessageStore>();
        if (!StrUtil.contains(topicFilter, '#') && !StrUtil.contains(topicFilter, '+')) {
            if (retainMessageCache.containsKey(topicFilter)) {
                retainMessageStores.add(retainMessageCache.get(topicFilter));
            }
        } else {
            // 优化: 预先分割topicFilter, 避免在循环中重复分割
            List<String> splitTopicFilters = StrUtil.split(topicFilter, '/');
            retainMessageCache.all().forEach((topic, val) -> {
                List<String> splitTopics = StrUtil.split(topic, '/');
                if (splitTopics.size() >= splitTopicFilters.size()) {
                    // 优化: 使用StringBuilder替代String拼接
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
                        retainMessageStores.add(val);
                    }
                }
            });
        }
        return retainMessageStores;
    }
}
