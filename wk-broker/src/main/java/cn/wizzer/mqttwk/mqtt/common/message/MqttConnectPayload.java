package cn.wizzer.mqttwk.mqtt.common.message;

import cn.wizzer.mqttwk.mqtt.common.utils.StringUtil;
import org.nutz.lang.Encoding;

/**
 * Payload of {@link MqttConnectMessage}
 */
public final class MqttConnectPayload {

    private final String clientIdentifier;
    private final String willTopic;
    private final byte[] willMessage;
    private final String userName;
    private final byte[] password;

    public MqttConnectPayload(
            String clientIdentifier,
            String willTopic,
            byte[] willMessage,
            String userName,
            byte[] password) {
        this.clientIdentifier = clientIdentifier;
        this.willTopic = willTopic;
        this.willMessage = willMessage;
        this.userName = userName;
        this.password = password;
    }

    public String clientIdentifier() {
        return clientIdentifier;
    }

    public String willTopic() {
        return willTopic;
    }

    public byte[] willMessageInBytes() {
        return willMessage;
    }

    public String userName() {
        return userName;
    }

    public byte[] passwordInBytes() {
        return password;
    }

    @Override
    public String toString() {
        return new StringBuilder(StringUtil.simpleClassName(this))
            .append('[')
            .append("clientIdentifier=").append(clientIdentifier)
            .append(", willTopic=").append(willTopic)
            .append(", willMessage=").append(willMessage)
            .append(", userName=").append(userName)
            .append(", password=").append(password)
            .append(']')
            .toString();
    }
}
