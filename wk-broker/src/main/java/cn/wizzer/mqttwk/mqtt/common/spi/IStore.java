package cn.wizzer.mqttwk.mqtt.common.spi;

public interface IStore {

    void initStore();

    void close();

    /**
     * Factory method to instantiate the messages store.
     *
     * @return the create instance of IMessagesStore.
     * */
    IMessagesStore messagesStore();

    /**
     * Factory method to instantiate the session store.
     *
     * @return the create instance of ISessionsStore.
     * */
    ISessionsStore sessionsStore();
}
