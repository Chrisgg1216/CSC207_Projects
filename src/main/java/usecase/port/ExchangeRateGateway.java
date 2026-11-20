package usecase.port;

import entity.ExchangeRate;
import java.time.LocalDate;
import java.util.Optional;

/** Returns a CAD quote on or before the requested date, or empty if unavailable. */
public interface ExchangeRateGateway {
    Optional<ExchangeRate> findRate(String currency, LocalDate date);
}
