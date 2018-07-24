/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.wizzer.iot.mqtt.server.store.message;

import cn.wizzer.iot.mqtt.server.common.message.DupPubRelMessageStore;
import cn.wizzer.iot.mqtt.server.common.message.IDupPubRelMessageStoreService;
import cn.wizzer.iot.mqtt.server.common.message.IMessageIdService;
import cn.wizzer.iot.mqtt.server.store.cache.DupPubRelMessageCache;
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
    private DupPubRelMessageCache dupPubRelMessageCache;

    @Override
    public void put(String clientId, DupPubRelMessageStore dupPubRelMessageStore) {
        dupPubRelMessageCache.put(clientId, dupPubRelMessageStore.getMessageId(), dupPubRelMessageStore);
    }

    @Override
    public List<DupPubRelMessageStore> get(String clientId) {
        if (dupPubRelMessageCache.containsKey(clientId)) {
            ConcurrentHashMap<Integer, DupPubRelMessageStore> map = dupPubRelMessageCache.get(clientId);
            Collection<DupPubRelMessageStore> collection = map.values();
            return new ArrayList<>(collection);
        }
        return new ArrayList<>();
    }

    @Override
    public void remove(String clientId, int messageId) {
        dupPubRelMessageCache.remove(clientId, messageId);
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
