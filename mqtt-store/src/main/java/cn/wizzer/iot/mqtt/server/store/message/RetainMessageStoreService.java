/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.wizzer.iot.mqtt.server.store.message;

import cn.hutool.core.util.StrUtil;
import cn.wizzer.iot.mqtt.server.common.message.IRetainMessageStoreService;
import cn.wizzer.iot.mqtt.server.common.message.RetainMessageStore;
import org.apache.ignite.IgniteCache;
import org.nutz.ioc.loader.annotation.IocBean;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@IocBean
public class RetainMessageStoreService implements IRetainMessageStoreService {

	@Resource
	private IgniteCache<String, RetainMessageStore> retainMessageCache;

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
			retainMessageCache.forEach(entry -> {
				String topic = entry.getKey();
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
						RetainMessageStore retainMessageStore = entry.getValue();
						retainMessageStores.add(retainMessageStore);
					}
				}
			});
		}
		return retainMessageStores;
	}
}
