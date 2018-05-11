package cn.wizzer.mqttwk.mqtt.common.exception;

/**
 * Created by wizzer on 2018/5/11.
 */
public class MqttUnacceptableProtocolVersionException extends DecoderException {

    private static final long serialVersionUID = 4914652213232455749L;

    /**
     * Creates a new instance
     */
    public MqttUnacceptableProtocolVersionException() {
    }

    /**
     * Creates a new instance
     */
    public MqttUnacceptableProtocolVersionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance
     */
    public MqttUnacceptableProtocolVersionException(String message) {
        super(message);
    }

    /**
     * Creates a new instance
     */
    public MqttUnacceptableProtocolVersionException(Throwable cause) {
        super(cause);
    }
}
