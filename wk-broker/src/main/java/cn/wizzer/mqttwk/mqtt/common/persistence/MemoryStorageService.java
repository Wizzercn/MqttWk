package cn.wizzer.mqttwk.mqtt.common.persistence;

import cn.wizzer.mqttwk.mqtt.common.spi.IMessagesStore;
import cn.wizzer.mqttwk.mqtt.common.spi.ISessionsStore;
import cn.wizzer.mqttwk.mqtt.common.spi.IStore;

import java.util.concurrent.ScheduledExecutorService;

public class MemoryStorageService implements IStore {

    private MemorySessionStore m_sessionsStore;
    private MemoryMessagesStore m_messagesStore;

    // NB these params must be here "by contract" used in introspection instantiation used in
    // ProtocolProcessorBootstrapper.instantiateConfiguredStore
    public MemoryStorageService(ScheduledExecutorService scheduler) {
        m_messagesStore = new MemoryMessagesStore();
        m_sessionsStore = new MemorySessionStore();
        m_messagesStore.initStore();
        m_sessionsStore.initStore();
    }

    @Override
    public IMessagesStore messagesStore() {
        return m_messagesStore;
    }

    @Override
    public ISessionsStore sessionsStore() {
        return m_sessionsStore;
    }

    @Override
    public void initStore() {
    }

    @Override
    public void close() {
    }
}
