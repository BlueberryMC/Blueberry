package net.blueberrymc.common.bml;

public class InvalidModDescriptionException extends RuntimeException {
    public InvalidModDescriptionException() {
        super();
    }

    public InvalidModDescriptionException(String message) {
        super(message);
    }

    public InvalidModDescriptionException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidModDescriptionException(Throwable cause) {
        super(cause);
    }

    protected InvalidModDescriptionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
