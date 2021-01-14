package net.blueberrymc.common.bml;

public class InvalidModException extends RuntimeException {
    public InvalidModException() {
        super();
    }

    public InvalidModException(String message) {
        super(message);
    }

    public InvalidModException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidModException(Throwable cause) {
        super(cause);
    }

    protected InvalidModException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
