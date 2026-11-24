package adapter;

import usecase.manage_categories.CategoryManagementOutputBoundary;

import java.util.List;

public final class CategoryPresenter implements CategoryManagementOutputBoundary {
    private final CategoryViewModel viewModel;

    public CategoryPresenter(CategoryViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void present(List<String> categories, String message, boolean error) {
        viewModel.setState(new CategoryViewModel.State(categories, message, error));
    }
}
