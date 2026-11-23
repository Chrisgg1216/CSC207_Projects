package framework;

import entity.Money;
import usecase.port.SpendingDataAccess;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

/**
 * PLACEHOLDER for Person A's transactions. Holds fixed demo spending so the Budget
 * tab can show warnings. Replace it with an adapter over Person A's
 * TransactionDataAccess (converted base-currency amounts, spending only,
 * excluding transactions pending conversion).
 */
public class InMemorySpendingDataAccess implements SpendingDataAccess {

    private final Map<String, BigDecimal> spent = new HashMap<>();
    private int pendingConversion;

    public void setSpent(String categoryName, YearMonth month, String amount) {
        spent.put(key(categoryName, month), new BigDecimal(amount));
    }

    public void setPendingConversion(int count) {
        this.pendingConversion = count;
    }

    @Override
    public Money totalSpent(String categoryName, YearMonth month, String baseCurrency) {
        return new Money(spent.getOrDefault(key(categoryName, month), BigDecimal.ZERO), baseCurrency);
    }

    @Override
    public int countPendingConversion(YearMonth month) {
        return pendingConversion;
    }

    private static String key(String categoryName, YearMonth month) {
        return categoryName.trim().toLowerCase() + "|" + month;
    }
}
