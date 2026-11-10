package framework;

import usecase.port.CategoryDataAccess;

import java.util.ArrayList;
import java.util.List;

/**
 * PLACEHOLDER for Person C's category storage. Holds the default categories in
 * memory so the Budget tab works before category management exists.
 */
public class InMemoryCategoryDataAccess implements CategoryDataAccess {

    private final List<String> names = new ArrayList<>(
            List.of("Food", "Rent", "Transport", "Entertainment", "Other"));

    @Override
    public boolean exists(String categoryName) {
        return names.stream().anyMatch(n -> n.equalsIgnoreCase(categoryName));
    }

    @Override
    public List<String> findAllNames() {
        return List.copyOf(names);
    }
}
