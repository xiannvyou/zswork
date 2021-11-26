package life.zswork.reptlie;

public class ReptileException extends RuntimeException {
    public ReptileException() {
        super();
    }

    public ReptileException(String message) {
        super(message);
    }

    public ReptileException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReptileException(Throwable cause) {
        super(cause);
    }

    protected ReptileException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
