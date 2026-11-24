package framework;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import entity.Budget;
import entity.Category;
import entity.ExchangeRate;
import entity.Money;
import entity.Transaction;
import entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** File schema uses ISO dates and exact decimal strings, independent of Java reflection on entities. */
final class LedgerJson {
    private static final int VERSION = 1;
    private static final Gson GSON = new GsonBuilder()
            .setStrictness(Strictness.STRICT).setPrettyPrinting().create();

    record Ledger(List<String> categories, List<Transaction> transactions, List<Budget> budgets) {
        Ledger {
            categories = List.copyOf(categories);
            transactions = List.copyOf(transactions);
            budgets = List.copyOf(budgets);
        }
    }

    static Ledger decode(String json) {
        FileData file = Objects.requireNonNull(GSON.fromJson(json, FileData.class));
        if (file.version() != VERSION) {
            throw new IllegalArgumentException("Unsupported ledger format version.");
        }
        return new Ledger(file.categories().stream().map(name -> new Category(name).getName()).toList(),
                file.transactions().stream().map(TransactionRow::toTransaction).toList(),
                file.budgets().stream().map(BudgetRow::toBudget).toList());
    }

    static String encode(Ledger ledger) {
        return GSON.toJson(new FileData(VERSION, ledger.categories(),
                ledger.transactions().stream().map(TransactionRow::from).toList(),
                ledger.budgets().stream().map(BudgetRow::from).toList())) + "\n";
    }

    private record FileData(int version, List<String> categories,
                            List<TransactionRow> transactions, List<BudgetRow> budgets) { }

    private record MoneyRow(String amount, String currency) {
        static MoneyRow from(Money money) {
            return new MoneyRow(money.getAmount().toPlainString(), money.getCurrency());
        }

        Money toMoney() {
            return new Money(new BigDecimal(amount), currency);
        }
    }

    private record QuoteRow(String currency, String cadPerUnit, String observedDate, String source) {
        static QuoteRow from(ExchangeRate rate) {
            return new QuoteRow(rate.currency(), rate.cadPerUnit().toPlainString(),
                    rate.observedDate().toString(), rate.source());
        }

        ExchangeRate toQuote() {
            return new ExchangeRate(currency, new BigDecimal(cadPerUnit), LocalDate.parse(observedDate), source);
        }
    }

    private record TransactionRow(String id, String date, TransactionType type, String category,
                                  MoneyRow original, QuoteRow quote, String note) {
        static TransactionRow from(Transaction transaction) {
            return new TransactionRow(transaction.id(), transaction.date().toString(), transaction.type(),
                    transaction.categoryName(), MoneyRow.from(transaction.originalMoney()),
                    transaction.exchangeRate().map(QuoteRow::from).orElse(null), transaction.note());
        }

        Transaction toTransaction() {
            return new Transaction(id, LocalDate.parse(date), type, category, original.toMoney(),
                    Optional.ofNullable(quote).map(QuoteRow::toQuote), note);
        }
    }

    private record BudgetRow(String category, String month, MoneyRow limit) {
        static BudgetRow from(Budget budget) {
            return new BudgetRow(budget.getCategory().getName(), budget.getMonth().toString(),
                    MoneyRow.from(budget.getLimit()));
        }

        Budget toBudget() {
            return new Budget(new Category(category), YearMonth.parse(month), limit.toMoney());
        }
    }
}
