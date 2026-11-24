package usecase.port;

/** A ledger read or write that could not be completed. */
public final class LedgerAccessException extends RuntimeException {
    public LedgerAccessException(String message) {
        super(message);
    }

    public LedgerAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
