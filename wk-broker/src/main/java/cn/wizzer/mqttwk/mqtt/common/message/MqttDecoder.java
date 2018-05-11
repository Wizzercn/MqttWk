package cn.wizzer.mqttwk.mqtt.common.message;

import cn.wizzer.mqttwk.mqtt.common.exception.DecoderException;
import org.tio.core.utils.ByteBufferUtils;

import java.nio.ByteBuffer;

import static cn.wizzer.mqttwk.mqtt.common.message.MqttCodecUtil.resetUnusedFields;
import static cn.wizzer.mqttwk.mqtt.common.message.MqttCodecUtil.validateFixedHeader;

/**
 * Created by wizzer on 2018/5/9.
 */
public class MqttDecoder {
    /**
     * Decodes the fixed header. It's one byte for the flags and then variable bytes for the remaining length.
     *
     * @param buffer the buffer to decode from
     * @return the fixed header
     */
    public static MqttFixedHeader decodeFixedHeader(ByteBuffer buffer) {
        short b1 = (short)ByteBufferUtils.readUB1(buffer);
        MqttMessageType messageType = MqttMessageType.valueOf(b1 >> 4);
        boolean dupFlag = (b1 & 0x08) == 0x08;
        int qosLevel = (b1 & 0x06) >> 1;
        boolean retain = (b1 & 0x01) != 0;

        int remainingLength = ByteBufferUtils.readUB1(buffer);
        int multiplier = 1;
        short digit;
        int loops = 0;
        do {
            digit = buffer.get();
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

}
