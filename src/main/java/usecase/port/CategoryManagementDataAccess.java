package usecase.port;

/** Category edits preserve references held by transactions and budgets. */
public interface CategoryManagementDataAccess extends CategoryDataAccess {
    void addCategory(String name);

    void renameCategory(String currentName, String newName);

    void deleteCategory(String name);
}
