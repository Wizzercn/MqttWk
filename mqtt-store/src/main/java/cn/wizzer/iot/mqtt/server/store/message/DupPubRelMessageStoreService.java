/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.wizzer.iot.mqtt.server.store.message;

import cn.wizzer.iot.mqtt.server.common.message.DupPubRelMessageStore;
import cn.wizzer.iot.mqtt.server.common.message.IDupPubRelMessageStoreService;
import cn.wizzer.iot.mqtt.server.common.message.IMessageIdService;
import org.apache.ignite.IgniteCache;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@IocBean
public class DupPubRelMessageStoreService implements IDupPubRelMessageStoreService {

	@Inject
	private IMessageIdService messageIdService;

	@Inject
	private IgniteCache<String, ConcurrentHashMap<Integer, DupPubRelMessageStore>> dupPubRelMessageCache;

	@Override
	public void put(String clientId, DupPubRelMessageStore dupPubRelMessageStore) {
		ConcurrentHashMap<Integer, DupPubRelMessageStore> map = dupPubRelMessageCache.containsKey(clientId) ? dupPubRelMessageCache.get(clientId) : new ConcurrentHashMap<Integer, DupPubRelMessageStore>();
		map.put(dupPubRelMessageStore.getMessageId(), dupPubRelMessageStore);
		dupPubRelMessageCache.put(clientId, map);
	}

	@Override
	public List<DupPubRelMessageStore> get(String clientId) {
		if (dupPubRelMessageCache.containsKey(clientId)) {
			ConcurrentHashMap<Integer, DupPubRelMessageStore> map = dupPubRelMessageCache.get(clientId);
			Collection<DupPubRelMessageStore> collection = map.values();
			return new ArrayList<DupPubRelMessageStore>(collection);
		}
		return new ArrayList<DupPubRelMessageStore>();
	}

	@Override
	public void remove(String clientId, int messageId) {
		if (dupPubRelMessageCache.containsKey(clientId)) {
			ConcurrentHashMap<Integer, DupPubRelMessageStore> map = dupPubRelMessageCache.get(clientId);
			if (map.containsKey(messageId)) {
				map.remove(messageId);
				if (map.size() > 0) {
					dupPubRelMessageCache.put(clientId, map);
				} else {
					dupPubRelMessageCache.remove(clientId);
				}
			}
		}
	}

	@Override
	public void removeByClient(String clientId) {
		if (dupPubRelMessageCache.containsKey(clientId)) {
			ConcurrentHashMap<Integer, DupPubRelMessageStore> map = dupPubRelMessageCache.get(clientId);
			map.forEach((messageId, dupPubRelMessageStore) -> {
				messageIdService.releaseMessageId(messageId);
			});
			map.clear();
			dupPubRelMessageCache.remove(clientId);
		}
	}
}
