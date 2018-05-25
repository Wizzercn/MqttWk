package cn.wizzer.mqttwk.mqtt.common.connections;

import cn.wizzer.mqttwk.mqtt.common.spi.impl.subscriptions.Subscription;
import cn.wizzer.mqttwk.mqtt.common.spi.impl.subscriptions.Topic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MqttSession {
    private boolean cleanSession;
    private String clientId;
    private String username;
    private Map<Topic, Subscription> subscriptions = new ConcurrentHashMap<>();

    public boolean isCleanSession() {
        return cleanSession;
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Map<Topic, Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Map<Topic, Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }
}
