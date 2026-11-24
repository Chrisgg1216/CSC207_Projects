package entity;

/** A spending category such as "Food" or "Rent". */
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
        int hash = 0;
        for (int codePoint : name.codePoints().toArray()) {
            hash = 31 * hash + Character.toLowerCase(Character.toUpperCase(codePoint));
        }
        return hash;
    }

    @Override
    public String toString() {
        return name;
    }
}
