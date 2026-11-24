package framework;

import entity.Budget;
import entity.Category;
import entity.ExchangeRate;
import entity.Money;
import entity.Transaction;
import entity.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import usecase.port.LedgerAccessException;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonLedgerStoreTest {
    private static final LocalDate DATE = LocalDate.of(2026, 11, 10);
    private static final YearMonth MONTH = YearMonth.from(DATE);

    @TempDir
    Path temp;

    @Test
    void reopeningRestoresTransactionsBudgetsCategoriesAndExactQuotes() {
        Path file = temp.resolve("ledger.json");
        JsonLedgerStore store = new JsonLedgerStore(file);
        store.addCategory("出行");
        ExchangeRate rate = new ExchangeRate("USD", new BigDecimal("1.3819"), DATE.minusDays(1), "Test quote");
        Transaction converted = transaction("one", "出行", Optional.of(rate));
        Transaction pending = transaction("two", "Food", Optional.empty());

        store.save(converted);
        store.save(pending);
        store.save(new Budget(new Category("出行"), MONTH, money("25", "CAD")));
        store.save(new Budget(new Category("出行"), MONTH, money("30.45", "CAD")));

        JsonLedgerStore reopened = new JsonLedgerStore(file);
        assertEquals(converted, reopened.findAll().get(0));
        assertEquals(pending, reopened.findAll().get(1));
        assertEquals(money("13.82", "CAD"), reopened.findAll().get(0).convertedMoney().orElseThrow());
        assertTrue(reopened.findAll().get(1).convertedMoney().isEmpty());
        assertTrue(reopened.exists("出行"));
        assertEquals(money("30.45", "CAD"), reopened.find("出行", MONTH).orElseThrow().getLimit());
        assertEquals(1, reopened.findByMonth(MONTH).size());
    }

    @Test
    void renamePreservesHistoryAndUsageBlocksDeletion() {
        Path file = temp.resolve("ledger.json");
        JsonLedgerStore store = new JsonLedgerStore(file);
        store.save(transaction("one", "food", Optional.empty()));
        store.save(new Budget(new Category("FOOD"), MONTH, money("30", "CAD")));
        store.save(new Budget(new Category("Rent"), MONTH.plusMonths(1), money("50", "CAD")));

        assertThrows(IllegalArgumentException.class, () -> store.addCategory(" Food "));
        assertThrows(IllegalArgumentException.class, () -> store.renameCategory("Food", "rEnT"));
        store.renameCategory("food", "Dining");
        assertThrows(IllegalArgumentException.class, () -> store.deleteCategory("Dining"));
        assertThrows(IllegalArgumentException.class, () -> store.deleteCategory("Rent"));
        store.deleteCategory("Other");

        JsonLedgerStore reopened = new JsonLedgerStore(file);
        assertFalse(reopened.exists("Other"));
        assertFalse(reopened.exists("Food"));
        assertEquals("Dining", reopened.findAll().get(0).categoryName());
        assertTrue(reopened.find("dining", MONTH).isPresent());
        assertTrue(reopened.find("Food", MONTH).isEmpty());
    }

    @Test
    void corruptAndStaleFilesAreNotOverwritten() throws Exception {
        Path file = temp.resolve("ledger.json");
        JsonLedgerStore first = new JsonLedgerStore(file);
        JsonLedgerStore stale = new JsonLedgerStore(file);
        first.addCategory("Books");

        assertThrows(LedgerAccessException.class, () -> stale.addCategory("Travel"));
        assertFalse(stale.exists("Travel"));
        assertTrue(new JsonLedgerStore(file).exists("Books"));

        Files.writeString(file, "{broken");
        assertThrows(LedgerAccessException.class, () -> new JsonLedgerStore(file));
        assertThrows(LedgerAccessException.class, () -> first.addCategory("Travel"));
        assertEquals("{broken", Files.readString(file));
    }

    private static Transaction transaction(String id, String category, Optional<ExchangeRate> rate) {
        return new Transaction(id, DATE, TransactionType.SPENDING, category, money("10", "USD"), rate, "Test");
    }

    private static Money money(String amount, String currency) {
        return new Money(new BigDecimal(amount), currency);
    }
}
