package cn.wizzer.mqttwk.mqtt.common.spi;

import cn.wizzer.mqttwk.mqtt.common.persistence.PersistentSession;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Queue;
import java.util.Set;

/**
 * Store used to handle the persistence of the subscriptions tree.
 */
public interface ISessionsStore {

    void initStore();

    ISubscriptionsStore subscriptionStore();

    /**
     * @param clientID
     *            the session client ID.
     * @return true iff there is a session stored for the client
     */
    boolean contains(String clientID);

    void createNewDurableSession(String clientID);

    void removeDurableSession(String clientId);

    void updateCleanStatus(String clientID, boolean cleanSession);

    PersistentSession loadSessionByKey(String clientID);

    /**
     * Returns all the sessions
     *
     * @return the collection of all stored client sessions.
     */
    Collection<PersistentSession> listAllSessions();

    IMessagesStore.StoredMessage inFlightAck(String clientID, int messageID);

    /**
     * Save the message msg with  messageID, clientID as in flight
     *
     * @param clientID
     *            the client ID
     * @param messageID
     *            the message ID
     * @param msg
     *            the message to put in flight zone
     */
    void inFlight(String clientID, int messageID, IMessagesStore.StoredMessage msg);

    /**
     * Return the next valid packetIdentifier for the given client session.
     *
     * @param clientID
     *            the clientID requesting next packet id.
     * @return the next valid id.
     */
    int nextPacketID(String clientID);

    /**
     * List the published retained messages for the session
     *
     * @param clientID
     *            the client ID owning the queue.
     * @return the queue of messages.
     */
    Queue<IMessagesStore.StoredMessage> queue(String clientID);

    void dropQueue(String clientID);

    void moveInFlightToSecondPhaseAckWaiting(String clientID, int messageID, IMessagesStore.StoredMessage msg);

    /**
     * @param clientID
     *            the client ID accessing the second phase.
     * @param messageID
     *            the message ID that reached the second phase.
     * @return the guid of message just acked.
     */
    IMessagesStore.StoredMessage completeReleasedPublish(String clientID, int messageID);

    /**
     * Returns the number of inflight messages for the given client ID
     *
     * @param clientID target client.
     * @return count of pending in flight publish messages.
     */
    int getInflightMessagesNo(String clientID);

    /**
     * Returns the number of second-phase ACK pending messages for the given client ID
     *
     * @param clientID target client.
     * @return count of pending in flight publish messages.
     */
    int countPubReleaseWaitingPubComplete(String clientID);

    void removeTemporaryQoS2(String clientID);

    /**
     * Tracks the closing time of a persistent session
     * */
    void trackSessionClose(LocalDateTime when, String clientID);

    /**
     * List the sessions ids closed before the pin date.
     * */
    Set<String> sessionOlderThan(LocalDateTime queryPin);
}
