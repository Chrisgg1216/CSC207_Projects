package entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/** A dated reference quote: CAD received for one unit of the original currency. */
public record ExchangeRate(String currency, BigDecimal cadPerUnit,
                           LocalDate observedDate, String source) {
    public ExchangeRate {
        currency = Objects.requireNonNull(currency).trim().toUpperCase(java.util.Locale.ROOT);
        Objects.requireNonNull(cadPerUnit);
        Objects.requireNonNull(observedDate);
        Objects.requireNonNull(source);
        if (cadPerUnit.signum() <= 0) {
            throw new IllegalArgumentException("Exchange rate must be positive.");
        }
    }
}
