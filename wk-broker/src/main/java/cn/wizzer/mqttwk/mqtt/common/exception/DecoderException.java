package cn.wizzer.mqttwk.mqtt.common.exception;

/**
 * An {@link CodecException} which is thrown by a decoder.
 */
public class DecoderException extends CodecException {

    private static final long serialVersionUID = 6926716840699621852L;

    /**
     * Creates a new instance.
     */
    public DecoderException() {
    }

    /**
     * Creates a new instance.
     */
    public DecoderException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance.
     */
    public DecoderException(String message) {
        super(message);
    }

    /**
     * Creates a new instance.
     */
    public DecoderException(Throwable cause) {
        super(cause);
    }
}
