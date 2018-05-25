package cn.wizzer.mqttwk.mqtt.common.persistence;

import cn.wizzer.mqttwk.mqtt.Constants;
import cn.wizzer.mqttwk.mqtt.common.spi.IMessagesStore;
import cn.wizzer.mqttwk.mqtt.common.spi.ISessionsStore;
import cn.wizzer.mqttwk.mqtt.common.spi.ISubscriptionsStore;
import cn.wizzer.mqttwk.mqtt.common.spi.impl.subscriptions.Subscription;
import cn.wizzer.mqttwk.mqtt.common.spi.impl.subscriptions.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

public class MemorySessionStore implements ISessionsStore, ISubscriptionsStore {

    private static final Logger LOG = LoggerFactory.getLogger(MemorySessionStore.class);

    static class Session {
        final String clientID;
        final Map<Topic, Subscription> subscriptions = new ConcurrentHashMap<>();
        boolean cleanSession;
        final BlockingQueue<IMessagesStore.StoredMessage> queue = new ArrayBlockingQueue<>(Constants.MAX_MESSAGE_QUEUE);
        final Map<Integer, IMessagesStore.StoredMessage> secondPhaseStore = new ConcurrentHashMap<>();
        final Map<Integer, IMessagesStore.StoredMessage> outboundFlightMessages =
                Collections.synchronizedMap(new HashMap<Integer, IMessagesStore.StoredMessage>());
        final Map<Integer, IMessagesStore.StoredMessage> inboundFlightMessages = new ConcurrentHashMap<>();

        Session(String clientID, boolean cleanSession) {
            this.clientID = clientID;
            this.cleanSession = cleanSession;
        }
    }

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private ConcurrentNavigableMap<LocalDateTime, Set<String>> sessionsClosingTimes = new ConcurrentSkipListMap<>();

    MemorySessionStore() {
    }

    private Session getSession(String clientID) {
        Session session = sessions.get(clientID);
        if (session == null) {
            LOG.error("Can't find the session for client <{}>", clientID);
            throw new RuntimeException("Can't find the session for client <" + clientID + ">");
        }
        return session;
    }

    @Override
    public void removeSubscription(Topic topic, String clientID) {
        LOG.debug("removeSubscription topic filter: {} for clientID: {}", topic, clientID);
        getSession(clientID).subscriptions.remove(topic);
    }

    @Override
    public void initStore() {
    }

    @Override
    public ISubscriptionsStore subscriptionStore() {
        return this;
    }

    @Override
    public void addNewSubscription(Subscription newSubscription) {
        final String clientID = newSubscription.getClientId();
        Session session = sessions.get(clientID);
        if (session == null) {
            LOG.error("Can't find the session for client <{}>", clientID);
            return;
        }

        session.subscriptions.put(newSubscription.getTopicFilter(), newSubscription);
    }

    @Override
    public void wipeSubscriptions(String clientID) {
        if (!sessions.containsKey(clientID)) {
            LOG.error("Can't find the session for client <{}>", clientID);
            return;
        }

        sessions.get(clientID).subscriptions.clear();
    }

    @Override
    public boolean contains(String clientID) {
        return sessions.containsKey(clientID);
    }

    @Override
    public void createNewDurableSession(String clientID) {
        Session innerSession = new Session(clientID, false);
        sessions.put(clientID, innerSession);
    }

    @Override
    public void removeDurableSession(String clientId) {
        this.sessions.remove(clientId);
        this.wipeSubscriptions(clientId);
    }

    @Override
    public void updateCleanStatus(String clientId, boolean newCleanStatus) {
        sessions.get(clientId).cleanSession = newCleanStatus;
    }

    @Override
    public PersistentSession loadSessionByKey(String clientID) {
        return new PersistentSession(clientID, sessions.get(clientID).cleanSession);
    }

    @Override
    public Collection<PersistentSession> listAllSessions() {
        Collection<PersistentSession> result = new ArrayList<>();
        for (Session entry : sessions.values()) {
            result.add(new PersistentSession(entry.clientID, entry.cleanSession));
        }
        return result;
    }

    @Override
    public List<Subscription> listAllSubscriptions() {
        List<Subscription> allSubscriptions = new ArrayList<>();
        for (Session entry : sessions.values()) {
            allSubscriptions.addAll(entry.subscriptions.values());
        }
        return allSubscriptions;
    }

    @Override
    public Collection<Subscription> listClientSubscriptions(String clientID) {
        final Session session = sessions.get(clientID);
        if (session == null) {
            throw new IllegalStateException("Asking for subscriptions of not persisted client: " + clientID);
        }
        return session.subscriptions.values();
    }

    @Override
    public Subscription reload(Subscription subcription) {
        String clientID = subcription.getClientId();
        if (!sessions.containsKey(clientID)) {
            LOG.error("Can't find the session for client <{}>", clientID);
            return null;
        }

        Map<Topic, Subscription> subscriptions = sessions.get(clientID).subscriptions;
        if (subscriptions == null || subscriptions.isEmpty()) {
            return null;
        }
        return subscriptions.get(subcription.getTopicFilter());
    }

    @Override
    public IMessagesStore.StoredMessage inFlightAck(String clientID, int messageID) {
        return getSession(clientID).outboundFlightMessages.remove(messageID);
    }

    @Override
    public void inFlight(String clientID, int messageID, IMessagesStore.StoredMessage msg) {
        Session session = sessions.get(clientID);
        if (session == null) {
            LOG.error("Can't find the session for client <{}>", clientID);
            return;
        }

        session.outboundFlightMessages.put(messageID, msg);
    }

    /**
     * Return the next valid packetIdentifier for the given client session.
     */
    @Override
    public int nextPacketID(String clientID) {
        if (!sessions.containsKey(clientID)) {
            LOG.error("Can't find the session for client <{}>", clientID);
            return -1;
        }

        Map<Integer, IMessagesStore.StoredMessage> m = sessions.get(clientID).outboundFlightMessages;
        int maxId = m.keySet().isEmpty() ? 0 : Collections.max(m.keySet());
        int nextPacketId = (maxId + 1) % 0xFFFF;
        m.put(nextPacketId, null);
        return nextPacketId;
    }

    @Override
    public BlockingQueue<IMessagesStore.StoredMessage> queue(String clientID) {
        if (!sessions.containsKey(clientID)) {
            LOG.error("Can't find the session for client <{}>", clientID);
            return null;
        }

        return sessions.get(clientID).queue;
    }

    @Override
    public void dropQueue(String clientID) {
        final Session session = sessions.get(clientID);
        session.queue.clear();
        session.outboundFlightMessages.clear();
        session.inboundFlightMessages.clear();
    }

    @Override
    public void moveInFlightToSecondPhaseAckWaiting(String clientID, int messageID, IMessagesStore.StoredMessage msg) {
        LOG.info("Moving msg inflight second phase store, clientID <{}> messageID {}", clientID, messageID);
        Session session = sessions.get(clientID);
        if (session == null) {
            LOG.error("Can't find the session for client <{}>", clientID);
            return;
        }

        session.secondPhaseStore.put(messageID, msg);
        session.outboundFlightMessages.put(messageID, msg);
    }

    @Override
    public IMessagesStore.StoredMessage completeReleasedPublish(String clientID, int messageID) {
        LOG.info("Acknowledged message in second phase, clientID <{}> messageID {}", clientID, messageID);
        return getSession(clientID).secondPhaseStore.remove(messageID);
    }

    @Override
    public int getInflightMessagesNo(String clientID) {
        Session session = sessions.get(clientID);
        if (session == null) {
            LOG.error("Can't find the session for client <{}>", clientID);
            return 0;
        }

        return session.inboundFlightMessages.size() +
//            countPubReleaseWaitingPubComplete(clientID) +
            session.outboundFlightMessages.size();
    }

    @Override
    public int countPubReleaseWaitingPubComplete(String clientID) {
        if (!sessions.containsKey(clientID)) {
            LOG.error("Can't find the session for client <{}>", clientID);
            return 0;
        }

        return sessions.get(clientID).secondPhaseStore.size();
    }

    @Override
    public void removeTemporaryQoS2(String clientID) {
        LOG.debug("Session cleanup for client <{}>", clientID);

        Session session = sessions.get(clientID);
        if (session == null) {
            LOG.error("Can't find the session for client <{}>", clientID);
            return;
        }

        LOG.info("Removing stored messages with QoS 2. ClientId={}", clientID);
        session.secondPhaseStore.clear();

        // TODO this missing last step breaks the junit test
        //sessions.remove(clientID);
    }

    @Override
    public synchronized void trackSessionClose(LocalDateTime when, String clientID) {
        this.sessionsClosingTimes.putIfAbsent(when, new HashSet<>());
        this.sessionsClosingTimes.computeIfPresent(when, (key, oldSet) -> {
            oldSet.add(clientID);
            return oldSet;
        });
    }

    @Override
    public Set<String> sessionOlderThan(LocalDateTime queryPin) {
        final Set<String> results = new HashSet<>();
        LocalDateTime keyBefore = this.sessionsClosingTimes.lowerKey(queryPin);
        while (keyBefore != null) {
            results.addAll(this.sessionsClosingTimes.get(keyBefore));
            keyBefore = this.sessionsClosingTimes.lowerKey(keyBefore);
        }
        return results;
    }
}
