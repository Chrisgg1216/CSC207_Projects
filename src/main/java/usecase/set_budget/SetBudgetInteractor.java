package usecase.set_budget;

import entity.Budget;
import entity.Category;
import entity.Money;
import usecase.port.BudgetDataAccess;
import usecase.port.CategoryDataAccess;

/**
 * Use case: set (or replace) the monthly limit for one category.
 */
public class SetBudgetInteractor implements SetBudgetInputBoundary {

    private final BudgetDataAccess budgets;
    private final CategoryDataAccess categories;
    private final SetBudgetOutputBoundary presenter;

    public SetBudgetInteractor(BudgetDataAccess budgets,
                               CategoryDataAccess categories,
                               SetBudgetOutputBoundary presenter) {
        this.budgets = budgets;
        this.categories = categories;
        this.presenter = presenter;
    }

    @Override
    public void execute(SetBudgetInputData input) {
        String categoryName = input.getCategoryName();
        if (categoryName == null || categoryName.isBlank()) {
            presenter.prepareFailView("Please choose a category.");
            return;
        }
        if (!categories.exists(categoryName.trim())) {
            presenter.prepareFailView("Category \"" + categoryName.trim() + "\" does not exist.");
            return;
        }
        if (input.getMonth() == null) {
            presenter.prepareFailView("Please choose a month.");
            return;
        }
        if (input.getLimit() == null || input.getLimit().signum() <= 0) {
            presenter.prepareFailView("The limit must be a number greater than zero.");
            return;
        }
        if (input.getCurrency() == null || input.getCurrency().isBlank()) {
            presenter.prepareFailView("No base currency is set.");
            return;
        }

        Money limit = new Money(input.getLimit(), input.getCurrency());
        if (!limit.isPositive()) {
            // e.g. 0.001 rounds to 0.00
            presenter.prepareFailView("The limit must be at least 0.01.");
            return;
        }

        boolean replaced = budgets.find(categoryName.trim(), input.getMonth()).isPresent();
        budgets.save(new Budget(new Category(categoryName), input.getMonth(), limit));
        presenter.prepareSuccessView(new SetBudgetOutputData(
                categoryName.trim(), input.getMonth(), limit, replaced));
    }
}
