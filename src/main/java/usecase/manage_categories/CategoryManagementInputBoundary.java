package usecase.manage_categories;

public interface CategoryManagementInputBoundary {
    void list();

    void add(String name);

    void rename(String currentName, String newName);

    void delete(String name);
}
