package framework;

import entity.Budget;
import entity.Category;
import entity.Transaction;
import usecase.port.BudgetDataAccess;
import usecase.port.CategoryManagementDataAccess;
import usecase.port.LedgerAccessException;
import usecase.port.TransactionDataAccess;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;

/** One shared, versioned ledger for transactions, budgets and category references. */
public final class JsonLedgerStore implements TransactionDataAccess, BudgetDataAccess, CategoryManagementDataAccess {
    private static final List<String> DEFAULT_CATEGORIES = List.of("Food", "Rent", "Transport", "Entertainment", "Other");
    private final Path file;
    private LedgerJson.Ledger ledger;
    private String loadedJson;

    public JsonLedgerStore(Path file) {
        this.file = file.toAbsolutePath().normalize();
        try {
            loadedJson = readFile();
            ledger = loadedJson == null
                    ? new LedgerJson.Ledger(DEFAULT_CATEGORIES, List.of(), List.of())
                    : LedgerJson.decode(loadedJson);
            validateReferences();
        } catch (IOException | RuntimeException ex) {
            throw new LedgerAccessException("Cannot open the ledger at " + this.file
                    + ". Check the file and permissions before restarting.", ex);
        }
    }

    @Override
    public synchronized List<Transaction> findAll() {
        return ledger.transactions();
    }

    @Override
    public synchronized void save(Transaction transaction) {
        String name = currentName(transaction.categoryName())
                .orElseThrow(() -> new LedgerAccessException("The category changed. Choose a category and try again."));
        List<Transaction> updated = new ArrayList<>(ledger.transactions());
        updated.removeIf(existing -> existing.id().equals(transaction.id()));
        updated.add(withCategory(transaction, name));
        commit(new LedgerJson.Ledger(ledger.categories(), updated, ledger.budgets()));
    }

    @Override
    public synchronized Optional<Budget> find(String categoryName, YearMonth month) {
        return ledger.budgets().stream().filter(budget -> matchesBudget(budget, categoryName, month)).findFirst();
    }

    @Override
    public synchronized List<Budget> findByMonth(YearMonth month) {
        return ledger.budgets().stream().filter(budget -> budget.getMonth().equals(month)).toList();
    }

    @Override
    public synchronized void save(Budget budget) {
        String name = currentName(budget.getCategory().getName())
                .orElseThrow(() -> new LedgerAccessException("The category changed. Choose a category and try again."));
        List<Budget> updated = new ArrayList<>(ledger.budgets());
        updated.removeIf(existing -> matchesBudget(existing, name, budget.getMonth()));
        updated.add(withCategory(budget, name));
        commit(new LedgerJson.Ledger(ledger.categories(), ledger.transactions(), updated));
    }

    @Override
    public synchronized boolean exists(String categoryName) {
        return categoryName != null && currentName(categoryName).isPresent();
    }

    @Override
    public synchronized List<String> findAllNames() {
        return ledger.categories();
    }

    @Override
    public synchronized void addCategory(String name) {
        String validName = new Category(name).getName();
        requireAvailable(validName);
        List<String> updated = new ArrayList<>(ledger.categories());
        updated.add(validName);
        commit(new LedgerJson.Ledger(updated, ledger.transactions(), ledger.budgets()));
    }

    @Override
    public synchronized void renameCategory(String currentName, String newName) {
        String oldName = requireCategory(currentName);
        String updatedName = new Category(newName).getName();
        if (oldName.equals(updatedName)) {
            return;
        }
        if (!oldName.equalsIgnoreCase(updatedName)) {
            requireAvailable(updatedName);
        }
        List<String> categories = ledger.categories().stream()
                .map(name -> name.equalsIgnoreCase(oldName) ? updatedName : name).toList();
        List<Transaction> transactions = ledger.transactions().stream()
                .map(item -> item.categoryName().equalsIgnoreCase(oldName) ? withCategory(item, updatedName) : item)
                .toList();
        List<Budget> budgets = ledger.budgets().stream()
                .map(item -> item.getCategory().getName().equalsIgnoreCase(oldName) ? withCategory(item, updatedName) : item)
                .toList();
        commit(new LedgerJson.Ledger(categories, transactions, budgets));
    }

    @Override
    public synchronized void deleteCategory(String name) {
        String existing = requireCategory(name);
        if (ledger.transactions().stream().anyMatch(item -> item.categoryName().equalsIgnoreCase(existing))
                || ledger.budgets().stream().anyMatch(item -> item.getCategory().getName().equalsIgnoreCase(existing))) {
            throw new IllegalArgumentException("This category is used by a transaction or budget and cannot be deleted.");
        }
        List<String> categories = ledger.categories().stream().filter(item -> !item.equals(existing)).toList();
        commit(new LedgerJson.Ledger(categories, ledger.transactions(), ledger.budgets()));
    }

    private void commit(LedgerJson.Ledger updated) {
        try {
            Files.createDirectories(file.getParent());
            Path lockFile = file.resolveSibling(file.getFileName() + ".lock");
            try (FileChannel channel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                 FileLock ignored = channel.lock()) {
                if (!Objects.equals(loadedJson, readFile())) {
                    throw new LedgerAccessException("The ledger changed in another session. Restart before saving.");
                }
                String json = LedgerJson.encode(updated);
                AtomicTextFile.write(file, json);
                loadedJson = json;
                ledger = updated;
            }
        } catch (IOException | OverlappingFileLockException ex) {
            throw new LedgerAccessException("Could not save the ledger at " + file + ".", ex);
        }
    }

    private String readFile() throws IOException {
        try {
            return Files.readString(file);
        } catch (NoSuchFileException ex) {
            return null;
        }
    }

    private void validateReferences() {
        var names = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        for (String name : ledger.categories()) {
            if (!names.add(name)) {
                throw new IllegalArgumentException("Duplicate category in ledger.");
            }
        }
        var ids = new HashSet<String>();
        for (Transaction transaction : ledger.transactions()) {
            if (!ids.add(transaction.id()) || !names.contains(transaction.categoryName())) {
                throw new IllegalArgumentException("Invalid transaction references in ledger.");
            }
        }
        var budgetNamesByMonth = new HashMap<YearMonth, TreeSet<String>>();
        for (Budget budget : ledger.budgets()) {
            String name = budget.getCategory().getName();
            if (!names.contains(name) || !budgetNamesByMonth.computeIfAbsent(budget.getMonth(),
                    month -> new TreeSet<>(String.CASE_INSENSITIVE_ORDER)).add(name)) {
                throw new IllegalArgumentException("Invalid budget references in ledger.");
            }
        }
    }

    private Optional<String> currentName(String name) {
        return ledger.categories().stream().filter(item -> item.equalsIgnoreCase(name.trim())).findFirst();
    }

    private String requireCategory(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Choose a category.");
        }
        return currentName(name).orElseThrow(() -> new IllegalArgumentException("Category no longer exists."));
    }

    private void requireAvailable(String name) {
        if (currentName(name).isPresent()) {
            throw new IllegalArgumentException("A category with that name already exists.");
        }
    }

    private static boolean matchesBudget(Budget budget, String name, YearMonth month) {
        return budget.getCategory().getName().equalsIgnoreCase(name.trim()) && budget.getMonth().equals(month);
    }

    private static Transaction withCategory(Transaction item, String name) {
        return new Transaction(item.id(), item.date(), item.type(), name, item.originalMoney(),
                item.exchangeRate(), item.note());
    }

    private static Budget withCategory(Budget item, String name) {
        return new Budget(new Category(name), item.getMonth(), item.getLimit());
    }
}
