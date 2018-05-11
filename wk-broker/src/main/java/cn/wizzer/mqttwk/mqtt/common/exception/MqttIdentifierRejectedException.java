package cn.wizzer.mqttwk.mqtt.common.exception;

/**
 * Created by wizzer on 2018/5/11.
 */
public class MqttIdentifierRejectedException extends DecoderException {

    private static final long serialVersionUID = -1323503322689614981L;

    /**
     * Creates a new instance
     */
    public MqttIdentifierRejectedException() {
    }

    /**
     * Creates a new instance
     */
    public MqttIdentifierRejectedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance
     */
    public MqttIdentifierRejectedException(String message) {
        super(message);
    }

    /**
     * Creates a new instance
     */
    public MqttIdentifierRejectedException(Throwable cause) {
        super(cause);
    }

}
