package usecase.manage_categories;

import usecase.port.CategoryManagementDataAccess;
import usecase.port.LedgerAccessException;

public final class CategoryManagementInteractor implements CategoryManagementInputBoundary {
    private final CategoryManagementDataAccess categories;
    private final CategoryManagementOutputBoundary presenter;

    public CategoryManagementInteractor(CategoryManagementDataAccess categories,
                                        CategoryManagementOutputBoundary presenter) {
        this.categories = categories;
        this.presenter = presenter;
    }

    @Override
    public void list() {
        presenter.present(categories.findAllNames(), "", false);
    }

    @Override
    public void add(String name) {
        change(() -> categories.addCategory(name), "Category added.");
    }

    @Override
    public void rename(String currentName, String newName) {
        change(() -> categories.renameCategory(currentName, newName), "Category renamed.");
    }

    @Override
    public void delete(String name) {
        change(() -> categories.deleteCategory(name), "Category deleted.");
    }

    private void change(Runnable operation, String success) {
        try {
            operation.run();
        } catch (IllegalArgumentException | LedgerAccessException ex) {
            presenter.present(categories.findAllNames(), ex.getMessage(), true);
            return;
        }
        presenter.present(categories.findAllNames(), success, false);
    }
}
