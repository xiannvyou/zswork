package life.zswork.util.dac;

public class DacException extends RuntimeException{
    public DacException() {
        super();
    }

    public DacException(String message) {
        super(message);
    }

    public DacException(String message, Throwable cause) {
        super(message, cause);
    }

    public DacException(Throwable cause) {
        super(cause);
    }

    protected DacException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
