package de.sebastianroeder.ribbit;

public class RibbitStorageStateException extends Exception {
    public RibbitStorageStateException() {
        super();
    }

    public RibbitStorageStateException(String detailMessage) {
        super(detailMessage);
    }

    public RibbitStorageStateException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public RibbitStorageStateException(Throwable throwable) {
        super(throwable);
    }
}
