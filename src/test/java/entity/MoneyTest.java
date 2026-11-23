package entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoneyTest {

    @Test
    void amountsAreKeptToTwoDecimalPlaces() {
        assertEquals("10.01", new Money(new BigDecimal("10.005"), "CAD").getAmount().toPlainString());
    }

    @Test
    void currencyDoesNotDependOnCase() {
        assertEquals(new Money(BigDecimal.ONE, "cad"), new Money(BigDecimal.ONE, "CAD"));
    }

    @Test
    void differentCurrenciesCannotBeAdded() {
        Money cad = new Money(BigDecimal.ONE, "CAD");
        Money usd = new Money(BigDecimal.ONE, "USD");

        assertThrows(IllegalArgumentException.class, () -> cad.add(usd));
        assertThrows(IllegalArgumentException.class, () -> cad.compareTo(usd));
    }

    @Test
    void decimalSumIsExactUnlikeDouble() {
        Money sum = new Money(new BigDecimal("0.10"), "CAD").add(new Money(new BigDecimal("0.20"), "CAD"));

        assertEquals(new Money(new BigDecimal("0.30"), "CAD"), sum);
    }

    @Test
    void zeroIsNotPositive() {
        assertTrue(Money.zero("CAD").getAmount().signum() == 0);
        assertTrue(!Money.zero("CAD").isPositive());
    }

    @Test
    void budgetRequiresPositiveLimit() {
        assertThrows(IllegalArgumentException.class,
                () -> new Budget(new Category("Food"), java.time.YearMonth.of(2026, 11), Money.zero("CAD")));
    }
}
