/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package cn.wizzer.iot.mqtt.tio.codec;


import cn.wizzer.iot.mqtt.tio.exception.DecoderException;
import org.nutz.lang.Encoding;

import java.nio.ByteBuffer;

import static cn.wizzer.iot.mqtt.tio.codec.MqttCodecUtil.isValidClientId;


/**
 * Encodes Mqtt messages into bytes following the protocol specification v3.1
 * as described here <a href="http://public.dhe.ibm.com/software/dw/webservices/ws-mqtt/mqtt-v3r1.html">MQTTV3.1</a>
 */
public final class MqttEncoder {

    public static final MqttEncoder INSTANCE = new MqttEncoder();
    public static final int[] EMPTY_INTS = {};
    public static final byte[] EMPTY_BYTES = {};
    public static final char[] EMPTY_CHARS = {};

    private MqttEncoder() {
    }

    /**
     * This is the main encoding method.
     * It's only visible for testing.
     *
     * @param message MQTT message to encode
     * @return ByteBuf with encoded bytes
     */
    public static ByteBuffer doEncode(MqttMessage message) {

        switch (message.fixedHeader().messageType()) {
            case CONNECT:
                return encodeConnectMessage((MqttConnectMessage) message);

            case CONNACK:
                return encodeConnAckMessage((MqttConnAckMessage) message);

            case PUBLISH:
                return encodePublishMessage((MqttPublishMessage) message);

            case SUBSCRIBE:
                return encodeSubscribeMessage((MqttSubscribeMessage) message);

            case UNSUBSCRIBE:
                return encodeUnsubscribeMessage((MqttUnsubscribeMessage) message);

            case SUBACK:
                return encodeSubAckMessage((MqttSubAckMessage) message);

            case UNSUBACK:
            case PUBACK:
            case PUBREC:
            case PUBREL:
            case PUBCOMP:
                return encodeMessageWithOnlySingleByteFixedHeaderAndMessageId(message);

            case PINGREQ:
            case PINGRESP:
            case DISCONNECT:
                return encodeMessageWithOnlySingleByteFixedHeader(message);

            default:
                throw new IllegalArgumentException(
                        "Unknown message type: " + message.fixedHeader().messageType().value());
        }
    }

    private static ByteBuffer encodeConnectMessage(
            MqttConnectMessage message) {
        int payloadBufferSize = 0;

        MqttFixedHeader mqttFixedHeader = message.fixedHeader();
        MqttConnectVariableHeader variableHeader = message.variableHeader();
        MqttConnectPayload payload = message.payload();
        MqttVersion mqttVersion = MqttVersion.fromProtocolNameAndLevel(variableHeader.name(),
                (byte) variableHeader.version());

        // as MQTT 3.1 & 3.1.1 spec, If the User Name Flag is set to 0, the Password Flag MUST be set to 0
        if (!variableHeader.hasUserName() && variableHeader.hasPassword()) {
            throw new DecoderException("Without a username, the password MUST be not set");
        }

        // Client id
        String clientIdentifier = payload.clientIdentifier();
        if (!isValidClientId(mqttVersion, clientIdentifier)) {
            throw new MqttIdentifierRejectedException("invalid clientIdentifier: " + clientIdentifier);
        }
        byte[] clientIdentifierBytes = encodeStringUtf8(clientIdentifier);
        payloadBufferSize += 2 + clientIdentifierBytes.length;

        // Will topic and message
        String willTopic = payload.willTopic();
        byte[] willTopicBytes = willTopic != null ? encodeStringUtf8(willTopic) : EMPTY_BYTES;
        byte[] willMessage = payload.willMessageInBytes();
        byte[] willMessageBytes = willMessage != null ? willMessage : EMPTY_BYTES;
        if (variableHeader.isWillFlag()) {
            payloadBufferSize += 2 + willTopicBytes.length;
            payloadBufferSize += 2 + willMessageBytes.length;
        }

        String userName = payload.userName();
        byte[] userNameBytes = userName != null ? encodeStringUtf8(userName) : EMPTY_BYTES;
        if (variableHeader.hasUserName()) {
            payloadBufferSize += 2 + userNameBytes.length;
        }

        byte[] password = payload.passwordInBytes();
        byte[] passwordBytes = password != null ? password : EMPTY_BYTES;
        if (variableHeader.hasPassword()) {
            payloadBufferSize += 2 + passwordBytes.length;
        }

        // Fixed header
        byte[] protocolNameBytes = mqttVersion.protocolNameBytes();
        int variableHeaderBufferSize = 2 + protocolNameBytes.length + 4;
        int variablePartSize = variableHeaderBufferSize + payloadBufferSize;
        int fixedHeaderBufferSize = 1 + getVariableLengthInt(variablePartSize);
        ByteBuffer buf = ByteBuffer.allocate(fixedHeaderBufferSize + variablePartSize);
        buf.putInt(getFixedHeaderByte1(mqttFixedHeader));
        writeVariableLengthInt(buf, variablePartSize);

        buf.putShort((short) protocolNameBytes.length);
        buf.put(protocolNameBytes);

        buf.putInt(variableHeader.version());
        buf.putInt(getConnVariableHeaderFlag(variableHeader));
        buf.putShort((short) variableHeader.keepAliveTimeSeconds());

        // Payload
        buf.putShort((short) clientIdentifierBytes.length);
        buf.put(clientIdentifierBytes, 0, clientIdentifierBytes.length);
        if (variableHeader.isWillFlag()) {
            buf.putShort((short) willTopicBytes.length);
            buf.put(willTopicBytes, 0, willTopicBytes.length);
            buf.putShort((short) willMessageBytes.length);
            buf.put(willMessageBytes, 0, willMessageBytes.length);
        }
        if (variableHeader.hasUserName()) {
            buf.putShort((short) userNameBytes.length);
            buf.put(userNameBytes, 0, userNameBytes.length);
        }
        if (variableHeader.hasPassword()) {
            buf.putShort((short) passwordBytes.length);
            buf.put(passwordBytes, 0, passwordBytes.length);
        }
        return buf;
    }

    private static int getConnVariableHeaderFlag(MqttConnectVariableHeader variableHeader) {
        int flagByte = 0;
        if (variableHeader.hasUserName()) {
            flagByte |= 0x80;
        }
        if (variableHeader.hasPassword()) {
            flagByte |= 0x40;
        }
        if (variableHeader.isWillRetain()) {
            flagByte |= 0x20;
        }
        flagByte |= (variableHeader.willQos() & 0x03) << 3;
        if (variableHeader.isWillFlag()) {
            flagByte |= 0x04;
        }
        if (variableHeader.isCleanSession()) {
            flagByte |= 0x02;
        }
        return flagByte;
    }

    private static ByteBuffer encodeConnAckMessage(
            MqttConnAckMessage message) {
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.put((byte) (getFixedHeaderByte1(message.fixedHeader())));
        buf.put((byte) 2);
        buf.put((byte) (message.variableHeader().isSessionPresent() ? 0x01 : 0x00));
        buf.put((byte) (message.variableHeader().connectReturnCode().byteValue()));
        return buf;
    }

    private static ByteBuffer encodeSubscribeMessage(
            MqttSubscribeMessage message) {
        int variableHeaderBufferSize = 2;
        int payloadBufferSize = 0;

        MqttFixedHeader mqttFixedHeader = message.fixedHeader();
        MqttMessageIdVariableHeader variableHeader = message.variableHeader();
        MqttSubscribePayload payload = message.payload();

        for (MqttTopicSubscription topic : payload.topicSubscriptions()) {
            String topicName = topic.topicName();
            byte[] topicNameBytes = encodeStringUtf8(topicName);
            payloadBufferSize += 2 + topicNameBytes.length;
            payloadBufferSize += 1;
        }

        int variablePartSize = variableHeaderBufferSize + payloadBufferSize;
        int fixedHeaderBufferSize = 1 + getVariableLengthInt(variablePartSize);
        ByteBuffer buf = ByteBuffer.allocate(fixedHeaderBufferSize + variablePartSize);
        buf.putInt(getFixedHeaderByte1(mqttFixedHeader));
        writeVariableLengthInt(buf, variablePartSize);

        // Variable Header
        int messageId = variableHeader.messageId();
        buf.putShort((short) messageId);

        // Payload
        for (MqttTopicSubscription topic : payload.topicSubscriptions()) {
            String topicName = topic.topicName();
            byte[] topicNameBytes = encodeStringUtf8(topicName);
            buf.putShort((short) topicNameBytes.length);
            buf.put(topicNameBytes, 0, topicNameBytes.length);
            buf.putInt(topic.qualityOfService().value());
        }

        return buf;
    }

    private static ByteBuffer encodeUnsubscribeMessage(
            MqttUnsubscribeMessage message) {
        int variableHeaderBufferSize = 2;
        int payloadBufferSize = 0;

        MqttFixedHeader mqttFixedHeader = message.fixedHeader();
        MqttMessageIdVariableHeader variableHeader = message.variableHeader();
        MqttUnsubscribePayload payload = message.payload();

        for (String topicName : payload.topics()) {
            byte[] topicNameBytes = encodeStringUtf8(topicName);
            payloadBufferSize += 2 + topicNameBytes.length;
        }

        int variablePartSize = variableHeaderBufferSize + payloadBufferSize;
        int fixedHeaderBufferSize = 1 + getVariableLengthInt(variablePartSize);
        ByteBuffer buf = ByteBuffer.allocate(fixedHeaderBufferSize + variablePartSize);
        buf.putInt(getFixedHeaderByte1(mqttFixedHeader));
        writeVariableLengthInt(buf, variablePartSize);

        // Variable Header
        int messageId = variableHeader.messageId();
        buf.putShort((short) messageId);

        // Payload
        for (String topicName : payload.topics()) {
            byte[] topicNameBytes = encodeStringUtf8(topicName);
            buf.putShort((short) topicNameBytes.length);
            buf.put(topicNameBytes, 0, topicNameBytes.length);
        }

        return buf;
    }

    private static ByteBuffer encodeSubAckMessage(
            MqttSubAckMessage message) {
        int variableHeaderBufferSize = 2;
        int payloadBufferSize = message.payload().grantedQoSLevels().size();
        int variablePartSize = variableHeaderBufferSize + payloadBufferSize;
        int fixedHeaderBufferSize = 1 + getVariableLengthInt(variablePartSize);
        ByteBuffer buf = ByteBuffer.allocate(fixedHeaderBufferSize + variablePartSize);
        buf.putInt(getFixedHeaderByte1(message.fixedHeader()));
        writeVariableLengthInt(buf, variablePartSize);
        buf.putShort((short) message.variableHeader().messageId());
        for (int qos : message.payload().grantedQoSLevels()) {
            buf.putInt(qos);
        }

        return buf;
    }

    private static ByteBuffer encodePublishMessage(
            MqttPublishMessage message) {
        MqttFixedHeader mqttFixedHeader = message.fixedHeader();
        MqttPublishVariableHeader variableHeader = message.variableHeader();
        ByteBuffer payload = message.payload().duplicate();

        String topicName = variableHeader.topicName();
        byte[] topicNameBytes = encodeStringUtf8(topicName);

        int variableHeaderBufferSize = 2 + topicNameBytes.length +
                (mqttFixedHeader.qosLevel().value() > 0 ? 2 : 0);
        int payloadBufferSize = payload.getInt();
        int variablePartSize = variableHeaderBufferSize + payloadBufferSize;
        int fixedHeaderBufferSize = 1 + getVariableLengthInt(variablePartSize);
        ByteBuffer buf = ByteBuffer.allocate(fixedHeaderBufferSize + variablePartSize);
        buf.putInt(getFixedHeaderByte1(mqttFixedHeader));
        writeVariableLengthInt(buf, variablePartSize);
        buf.putShort((short) topicNameBytes.length);
        buf.put(topicNameBytes);
        if (mqttFixedHeader.qosLevel().value() > 0) {
            buf.putShort((short) variableHeader.packetId());
        }
        buf.put(payload);

        return buf;
    }

    private static ByteBuffer encodeMessageWithOnlySingleByteFixedHeaderAndMessageId(
            MqttMessage message) {
        MqttFixedHeader mqttFixedHeader = message.fixedHeader();
        MqttMessageIdVariableHeader variableHeader = (MqttMessageIdVariableHeader) message.variableHeader();
        int msgId = variableHeader.messageId();

        int variableHeaderBufferSize = 2; // variable part only has a message id
        int fixedHeaderBufferSize = 1 + getVariableLengthInt(variableHeaderBufferSize);
        ByteBuffer buf = ByteBuffer.allocate(fixedHeaderBufferSize + variableHeaderBufferSize);
        buf.putInt(getFixedHeaderByte1(mqttFixedHeader));
        writeVariableLengthInt(buf, variableHeaderBufferSize);
        buf.putShort((short) msgId);

        return buf;
    }

    private static ByteBuffer encodeMessageWithOnlySingleByteFixedHeader(
            MqttMessage message) {
        MqttFixedHeader mqttFixedHeader = message.fixedHeader();
        ByteBuffer buf = ByteBuffer.allocate(2);
        buf.putInt(getFixedHeaderByte1(mqttFixedHeader));
        buf.putInt(0);

        return buf;
    }

    private static int getFixedHeaderByte1(MqttFixedHeader header) {
        int ret = 0;
        ret |= header.messageType().value() << 4;
        if (header.isDup()) {
            ret |= 0x08;
        }
        ret |= header.qosLevel().value() << 1;
        if (header.isRetain()) {
            ret |= 0x01;
        }
        return ret;
    }

    private static void writeVariableLengthInt(ByteBuffer buf, int num) {
        do {
            int digit = num % 128;
            num /= 128;
            if (num > 0) {
                digit |= 0x80;
            }
            buf.putInt(digit);
        } while (num > 0);
    }

    private static int getVariableLengthInt(int num) {
        int count = 0;
        do {
            num /= 128;
            count++;
        } while (num > 0);
        return count;
    }

    private static byte[] encodeStringUtf8(String s) {
        return s.getBytes(Encoding.CHARSET_UTF8);
    }
}
