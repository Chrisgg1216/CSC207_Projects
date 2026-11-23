package usecase.set_budget;

public interface SetBudgetOutputBoundary {

    void prepareSuccessView(SetBudgetOutputData outputData);

    void prepareFailView(String errorMessage);
}
