package entity;

import java.util.Objects;

/**
 * A spending category such as "Food" or "Rent".
 *
 * PLACEHOLDER: category management belongs to Person C. Replace with the shared
 * Category entity when it exists.
 */
public final class Category {

    private final String name;

    public Category(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name is required.");
        }
        this.name = name.trim();
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Category && name.equalsIgnoreCase(((Category) o).name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase());
    }

    @Override
    public String toString() {
        return name;
    }
}
