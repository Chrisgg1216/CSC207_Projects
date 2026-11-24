package adapter;

import usecase.manage_categories.CategoryManagementInputBoundary;

public final class CategoryController {
    private final CategoryManagementInputBoundary useCase;

    public CategoryController(CategoryManagementInputBoundary useCase) {
        this.useCase = useCase;
    }

    public void refresh() {
        useCase.list();
    }

    public void add(String name) {
        useCase.add(name);
    }

    public void rename(String currentName, String newName) {
        useCase.rename(currentName, newName);
    }

    public void delete(String name) {
        useCase.delete(name);
    }
}
