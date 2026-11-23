package entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {
    private final LocalDate date = LocalDate.of(2026, 11, 19);

    @Test
    void rejectsCurrencyMismatchAndFutureQuote() {
        assertThrows(IllegalArgumentException.class, () -> transaction(new ExchangeRate("CNY", BigDecimal.ONE, date, "Test")));
        assertThrows(IllegalArgumentException.class, () -> transaction(new ExchangeRate("USD", BigDecimal.ONE, date.plusDays(1), "Test")));
    }

    private Transaction transaction(ExchangeRate rate) {
        return new Transaction("1", date, TransactionType.SPENDING, "Food",
                new Money(BigDecimal.ONE, "USD"), Optional.of(rate), "");
    }

    @Test
    void pendingHasNoConvertedAmount() {
        var transaction = new Transaction("1", date, TransactionType.SPENDING, "Food",
                new Money(BigDecimal.ONE, "USD"), Optional.empty(), null);
        assertTrue(transaction.convertedMoney().isEmpty());
        assertEquals("", transaction.note());
    }
}
