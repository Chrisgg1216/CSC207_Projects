package usecase;

import entity.Money;
import usecase.check_budget.CheckBudgetOutputBoundary;
import usecase.check_budget.CheckBudgetOutputData;
import usecase.port.CategoryDataAccess;
import usecase.port.SpendingDataAccess;
import usecase.set_budget.SetBudgetOutputBoundary;
import usecase.set_budget.SetBudgetOutputData;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Fakes used by the use case tests, so no test needs real storage or the internet. */
final class TestSupport {

    private TestSupport() {
    }

    static final YearMonth NOV = YearMonth.of(2026, 11);
    static final YearMonth DEC = YearMonth.of(2026, 12);

    static class FakeCategories implements CategoryDataAccess {
        private final List<String> names;

        FakeCategories(String... names) {
            this.names = List.of(names);
        }

        @Override
        public boolean exists(String categoryName) {
            return names.stream().anyMatch(n -> n.equalsIgnoreCase(categoryName));
        }

        @Override
        public List<String> findAllNames() {
            return names;
        }
    }

    static class FakeSpending implements SpendingDataAccess {
        private final Map<String, Money> spent = new HashMap<>();
        int pending;

        void spend(String category, YearMonth month, String amount) {
            spent.put(category.toLowerCase() + "|" + month, new Money(new BigDecimal(amount), "CAD"));
        }

        @Override
        public Money totalSpent(String categoryName, YearMonth month, String baseCurrency) {
            return spent.getOrDefault(categoryName.toLowerCase() + "|" + month, Money.zero(baseCurrency));
        }

        @Override
        public int countPendingConversion(YearMonth month) {
            return pending;
        }
    }

    static class SetCapture implements SetBudgetOutputBoundary {
        SetBudgetOutputData success;
        String error;

        @Override
        public void prepareSuccessView(SetBudgetOutputData outputData) {
            success = outputData;
        }

        @Override
        public void prepareFailView(String errorMessage) {
            error = errorMessage;
        }
    }

    static class CheckCapture implements CheckBudgetOutputBoundary {
        CheckBudgetOutputData success;
        String error;

        @Override
        public void prepareSuccessView(CheckBudgetOutputData outputData) {
            success = outputData;
        }

        @Override
        public void prepareFailView(String errorMessage) {
            error = errorMessage;
        }
    }
}
