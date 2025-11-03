package app.exceptions;

public class DatabaseException extends ApiException {

    public DatabaseException(String message) {
        super(500, message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(500, message);
        initCause(cause);
    }
}
