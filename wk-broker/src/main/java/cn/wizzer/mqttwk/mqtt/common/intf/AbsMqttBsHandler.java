package cn.wizzer.mqttwk.mqtt.common.intf;

import cn.wizzer.mqttwk.mqtt.common.MqttPacket;
import cn.wizzer.mqttwk.mqtt.common.exception.DecoderException;
import cn.wizzer.mqttwk.mqtt.common.message.MqttDecoder;
import cn.wizzer.mqttwk.mqtt.common.message.MqttFixedHeader;
import cn.wizzer.mqttwk.mqtt.common.message.MqttMessage;
import cn.wizzer.mqttwk.mqtt.common.message.MqttMessageFactory;
import org.nutz.ioc.loader.annotation.IocBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;

import java.nio.ByteBuffer;

/**
 * 解析消息内容 [消息体=动态头+内容]
 * Created by wizzer on 2018/5/9.
 */
@IocBean
public abstract class AbsMqttBsHandler<T extends MqttMessage> implements MqttBsHandlerIntf {
    private static Logger log = LoggerFactory.getLogger(AbsMqttBsHandler.class);
    private MqttFixedHeader mqttFixedHeader;
    private Object variableHeader;
    private int bytesRemainingInVariablePart;

    @Override
    public Object handler(MqttPacket packet, ChannelContext channelContext) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(packet.getBody());
        mqttFixedHeader = packet.getMqttFixedHeader();
        bytesRemainingInVariablePart = mqttFixedHeader.remainingLength();
        try {
            final MqttDecoder.Result<?> decodedVariableHeader = MqttDecoder.decodeVariableHeader(buffer, mqttFixedHeader);
            variableHeader = decodedVariableHeader.value;
            bytesRemainingInVariablePart -= decodedVariableHeader.numberOfBytesConsumed;
            final MqttDecoder.Result<?> decodedPayload =
                    MqttDecoder.decodePayload(
                            buffer,
                            mqttFixedHeader.messageType(),
                            bytesRemainingInVariablePart,
                            variableHeader);
            bytesRemainingInVariablePart -= decodedPayload.numberOfBytesConsumed;
            if (bytesRemainingInVariablePart != 0) {
                throw new DecoderException(
                        "non-zero remaining payload bytes: " +
                                bytesRemainingInVariablePart + " (" + mqttFixedHeader.messageType() + ')');
            }
            MqttMessage message = MqttMessageFactory.newMessage(
                    mqttFixedHeader, variableHeader, decodedPayload.value);
            mqttFixedHeader = null;
            variableHeader = null;
            return handler(packet, message, channelContext);
        } catch (Exception cause) {
            log.error(cause.getMessage(),cause);
            return MqttMessageFactory.newInvalidMessage(cause);
        }
    }

    public abstract Object handler(MqttPacket packet, MqttMessage mqttMessage, ChannelContext channelContext) throws Exception;

}
