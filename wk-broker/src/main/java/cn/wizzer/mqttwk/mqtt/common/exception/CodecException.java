package cn.wizzer.mqttwk.mqtt.common.exception;

/**
 * An {@link Exception} which is thrown by a codec.
 */
public class CodecException extends RuntimeException {

    private static final long serialVersionUID = -1464830400709348473L;

    /**
     * Creates a new instance.
     */
    public CodecException() {
    }

    /**
     * Creates a new instance.
     */
    public CodecException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance.
     */
    public CodecException(String message) {
        super(message);
    }

    /**
     * Creates a new instance.
     */
    public CodecException(Throwable cause) {
        super(cause);
    }
}
