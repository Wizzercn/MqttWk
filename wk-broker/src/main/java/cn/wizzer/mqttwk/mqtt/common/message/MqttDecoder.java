package cn.wizzer.mqttwk.mqtt.common.message;

import cn.wizzer.mqttwk.mqtt.common.exception.DecoderException;
import cn.wizzer.mqttwk.mqtt.common.exception.MqttIdentifierRejectedException;
import org.nutz.lang.Encoding;
import org.tio.core.utils.ByteBufferUtils;

import java.nio.ByteBuffer;

import static cn.wizzer.mqttwk.mqtt.common.message.MqttCodecUtil.resetUnusedFields;
import static cn.wizzer.mqttwk.mqtt.common.message.MqttCodecUtil.validateFixedHeader;

/**
 * 解码器
 * Created by wizzer on 2018/5/9.
 */
public class MqttDecoder {
    /**
     * 解码固定头部
     *
     * @param buffer
     * @return
     */
    public static MqttFixedHeader decodeFixedHeader(ByteBuffer buffer) {
        short b1 = (short) ByteBufferUtils.readUB1(buffer);
        MqttMessageType messageType = MqttMessageType.valueOf(b1 >> 4);
        boolean dupFlag = (b1 & 0x08) == 0x08;
        int qosLevel = (b1 & 0x06) >> 1;
        boolean retain = (b1 & 0x01) != 0;

        int remainingLength = 0;
        int multiplier = 1;
        short digit;
        int loops = 0;
        do {
            digit = (short) ByteBufferUtils.readUB1(buffer);
            remainingLength += (digit & 127) * multiplier;
            multiplier *= 128;
            loops++;
        } while ((digit & 128) != 0 && loops < 4);

        // MQTT protocol limits Remaining Length to 4 bytes
        if (loops == 4 && (digit & 128) != 0) {
            throw new DecoderException("remaining length exceeds 4 digits (" + messageType + ')');
        }
        MqttFixedHeader decodedFixedHeader =
                new MqttFixedHeader(messageType, dupFlag, MqttQoS.valueOf(qosLevel), retain, remainingLength);
        return validateFixedHeader(resetUnusedFields(decodedFixedHeader));
    }

    /**
     * 解码可变头部
     *
     * @param buffer
     * @param mqttFixedHeader
     * @return
     */
    public static Result<?> decodeVariableHeader(ByteBuffer buffer, MqttFixedHeader mqttFixedHeader) {
        switch (mqttFixedHeader.messageType()) {
            case CONNECT:
                return decodeConnectionVariableHeader(buffer);
//
//            case CONNACK:
//                return decodeConnAckVariableHeader(body);
//
//            case SUBSCRIBE:
//            case UNSUBSCRIBE:
//            case SUBACK:
//            case UNSUBACK:
//            case PUBACK:
//            case PUBREC:
//            case PUBCOMP:
//            case PUBREL:
//                return decodeMessageIdVariableHeader(body);
//
//            case PUBLISH:
//                return decodePublishVariableHeader(body, mqttFixedHeader);
//
//            case PINGREQ:
//            case PINGRESP:
//            case DISCONNECT:
            // Empty variable header
//                return new Result<Object>(null, 0);
        }
        return new Result<Object>(null, 0); //should never reach here
    }

    /**
     * 解码消息内容
     *
     * @param buffer
     * @param messageType
     * @param bytesRemainingInVariablePart
     * @param variableHeader
     * @return
     */
    public static Result<?> decodePayload(
            ByteBuffer buffer,
            MqttMessageType messageType,
            int bytesRemainingInVariablePart,
            Object variableHeader) {
        switch (messageType) {
            case CONNECT:
                return decodeConnectionPayload(buffer, (MqttConnectVariableHeader) variableHeader);
//
//            case SUBSCRIBE:
//                return decodeSubscribePayload(buffer, bytesRemainingInVariablePart);
//
//            case SUBACK:
//                return decodeSubackPayload(buffer, bytesRemainingInVariablePart);
//
//            case UNSUBSCRIBE:
//                return decodeUnsubscribePayload(buffer, bytesRemainingInVariablePart);
//
//            case PUBLISH:
//                return decodePublishPayload(buffer, bytesRemainingInVariablePart);

            default:
                // unknown payload , no byte consumed
                return new Result<Object>(null, 0);
        }
    }

    private static Result<MqttConnectPayload> decodeConnectionPayload(
            ByteBuffer buffer,
            MqttConnectVariableHeader mqttConnectVariableHeader) {
        final Result<String> decodedClientId = decodeString(buffer);
        final String decodedClientIdValue = decodedClientId.value;
        final MqttVersion mqttVersion = MqttVersion.fromProtocolNameAndLevel(mqttConnectVariableHeader.name(),
                (byte) mqttConnectVariableHeader.version());
        if (!MqttCodecUtil.isValidClientId(mqttVersion, decodedClientIdValue)) {
            throw new MqttIdentifierRejectedException("invalid clientIdentifier: " + decodedClientIdValue);
        }
        int numberOfBytesConsumed = decodedClientId.numberOfBytesConsumed;

        Result<String> decodedWillTopic = null;
        Result<byte[]> decodedWillMessage = null;
        if (mqttConnectVariableHeader.isWillFlag()) {
            decodedWillTopic = decodeString(buffer, 0, 32767);
            numberOfBytesConsumed += decodedWillTopic.numberOfBytesConsumed;
            decodedWillMessage = decodeByteArray(buffer);
            numberOfBytesConsumed += decodedWillMessage.numberOfBytesConsumed;
        }
        Result<String> decodedUserName = null;
        Result<byte[]> decodedPassword = null;
        if (mqttConnectVariableHeader.hasUserName()) {
            decodedUserName = decodeString(buffer);
            numberOfBytesConsumed += decodedUserName.numberOfBytesConsumed;
        }
        if (mqttConnectVariableHeader.hasPassword()) {
            decodedPassword = decodeByteArray(buffer);
            numberOfBytesConsumed += decodedPassword.numberOfBytesConsumed;
        }

        final MqttConnectPayload mqttConnectPayload =
                new MqttConnectPayload(
                        decodedClientId.value,
                        decodedWillTopic != null ? decodedWillTopic.value : null,
                        decodedWillMessage != null ? decodedWillMessage.value : null,
                        decodedUserName != null ? decodedUserName.value : null,
                        decodedPassword != null ? decodedPassword.value : null);
        return new Result<MqttConnectPayload>(mqttConnectPayload, numberOfBytesConsumed);
    }

    /**
     * MqttConnectVariableHeader
     *
     * @param buffer
     * @return
     */
    private static Result<MqttConnectVariableHeader> decodeConnectionVariableHeader(ByteBuffer buffer) {
        final Result<String> protoString = decodeString(buffer);//获取协议版本
        int numberOfBytesConsumed = protoString.numberOfBytesConsumed;

        final byte protocolLevel = buffer.get();
        numberOfBytesConsumed += 1;

        final MqttVersion mqttVersion = MqttVersion.fromProtocolNameAndLevel(protoString.value, protocolLevel);

        final int b1 = ByteBufferUtils.readUB1(buffer);
        numberOfBytesConsumed += 1;

        final Result<Integer> keepAlive = decodeMsbLsb(buffer);
        numberOfBytesConsumed += keepAlive.numberOfBytesConsumed;

        final boolean hasUserName = (b1 & 0x80) == 0x80;
        final boolean hasPassword = (b1 & 0x40) == 0x40;
        final boolean willRetain = (b1 & 0x20) == 0x20;
        final int willQos = (b1 & 0x18) >> 3;
        final boolean willFlag = (b1 & 0x04) == 0x04;
        final boolean cleanSession = (b1 & 0x02) == 0x02;
        if (mqttVersion == MqttVersion.MQTT_3_1_1) {
            final boolean zeroReservedFlag = (b1 & 0x01) == 0x0;
            if (!zeroReservedFlag) {
                // MQTT v3.1.1: The Server MUST validate that the reserved flag in the CONNECT Control Packet is
                // set to zero and disconnect the Client if it is not zero.
                // See http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc385349230
                throw new DecoderException("non-zero reserved flag");
            }
        }

        final MqttConnectVariableHeader mqttConnectVariableHeader = new MqttConnectVariableHeader(
                mqttVersion.protocolName(),
                mqttVersion.protocolLevel(),
                hasUserName,
                hasPassword,
                willRetain,
                willQos,
                willFlag,
                cleanSession,
                keepAlive.value);
        return new Result<MqttConnectVariableHeader>(mqttConnectVariableHeader, numberOfBytesConsumed);
    }

    private static Result<String> decodeString(ByteBuffer buffer) {
        return decodeString(buffer, 0, Integer.MAX_VALUE);
    }

    private static Result<String> decodeString(ByteBuffer buffer, int minBytes, int maxBytes) {
        final Result<Integer> decodedSize = decodeMsbLsb(buffer);
        int size = decodedSize.value;
        int numberOfBytesConsumed = decodedSize.numberOfBytesConsumed;
        if (size < minBytes || size > maxBytes) {
            buffer.position(buffer.position() + size);
            numberOfBytesConsumed += size;
            return new Result<String>(null, numberOfBytesConsumed);
        }
        String s = new String(ByteBufferUtils.readBytes(buffer, size), Encoding.CHARSET_UTF8);
        numberOfBytesConsumed += size;
        return new Result<String>(s, numberOfBytesConsumed);
    }

    private static Result<byte[]> decodeByteArray(ByteBuffer buffer) {
        final Result<Integer> decodedSize = decodeMsbLsb(buffer);
        int size = decodedSize.value;
        byte[] bytes = new byte[size];
        buffer.get(bytes);
        return new Result<byte[]>(bytes, decodedSize.numberOfBytesConsumed + size);
    }

    private static Result<Integer> decodeMsbLsb(ByteBuffer buffer) {
        return decodeMsbLsb(buffer, 0, 65535);
    }

    private static Result<Integer> decodeMsbLsb(ByteBuffer buffer, int min, int max) {
        short msbSize = (short) ByteBufferUtils.readUB1(buffer);//Length MSB
        short lsbSize = (short) ByteBufferUtils.readUB1(buffer);//Length LSB
        final int numberOfBytesConsumed = 2;
        int result = msbSize << 8 | lsbSize;
        if (result < min || result > max) {
            result = -1;
        }
        return new Result<Integer>(result, numberOfBytesConsumed);
    }

    public static final class Result<T> {

        public final T value;
        public final int numberOfBytesConsumed;

        Result(T value, int numberOfBytesConsumed) {
            this.value = value;
            this.numberOfBytesConsumed = numberOfBytesConsumed;
        }
    }
}
