package net.blueberrymc.common.bml;

public class ModDescriptionNotFoundException extends InvalidModDescriptionException {
    public ModDescriptionNotFoundException() {
        super();
    }

    public ModDescriptionNotFoundException(String message) {
        super(message);
    }

    public ModDescriptionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModDescriptionNotFoundException(Throwable cause) {
        super(cause);
    }
}
