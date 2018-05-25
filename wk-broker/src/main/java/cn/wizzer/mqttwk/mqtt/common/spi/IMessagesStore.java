package cn.wizzer.mqttwk.mqtt.common.spi;

import cn.wizzer.mqttwk.mqtt.common.message.MqttQoS;
import cn.wizzer.mqttwk.mqtt.common.spi.impl.subscriptions.Topic;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Collection;

/**
 * Created by wizzer on 2018/5/13.
 */
public interface IMessagesStore {
    class StoredMessage implements Serializable {

        private static final long serialVersionUID = 1755296138639817304L;
        final MqttQoS m_qos;
        final byte[] m_payload;
        final String m_topic;
        private boolean m_retained;
        private String m_clientID;
        private MessageGUID m_guid;

        public StoredMessage(byte[] message, MqttQoS qos, String topic) {
            m_qos = qos;
            m_payload = message;
            m_topic = topic;
        }

        public MqttQoS getQos() {
            return m_qos;
        }

        public String getTopic() {
            return m_topic;
        }

        public void setGuid(MessageGUID guid) {
            this.m_guid = guid;
        }

        public MessageGUID getGuid() {
            return m_guid;
        }

        public String getClientID() {
            return m_clientID;
        }

        public void setClientID(String m_clientID) {
            this.m_clientID = m_clientID;
        }

        public ByteBuffer getPayload() {
            return ByteBuffer.wrap(m_payload);
        }

        public void setRetained(boolean retained) {
            this.m_retained = retained;
        }

        public boolean isRetained() {
            return m_retained;
        }

        @Override
        public String toString() {
            return "PublishEvent{clientID='" + m_clientID + '\'' + ", m_retain="
                    + m_retained + ", m_qos=" + m_qos + ", m_topic='" + m_topic + '\'' + '}';
        }
    }

    /**
     * Used to initialize all persistent store structures
     */
    void initStore();

    /**
     * Return a list of retained messages that satisfy the condition.
     *
     * @param condition the condition to match during the search.
     * @return the collection of matching messages.
     */
    Collection<StoredMessage> searchMatching(IMatchingCondition condition);

    void cleanRetained(Topic topic);

    void storeRetained(Topic topic, StoredMessage storedMessage);
}
