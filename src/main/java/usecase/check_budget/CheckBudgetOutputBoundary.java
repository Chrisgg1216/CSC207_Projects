package usecase.check_budget;

public interface CheckBudgetOutputBoundary {

    void prepareSuccessView(CheckBudgetOutputData outputData);

    void prepareFailView(String errorMessage);
}
