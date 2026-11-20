package usecase.port;

/** Recoverable external service failure: a transaction may remain pending. */
public class ExchangeRateUnavailableException extends RuntimeException {
    public ExchangeRateUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
