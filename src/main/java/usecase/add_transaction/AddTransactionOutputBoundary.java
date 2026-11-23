package usecase.add_transaction;

public interface AddTransactionOutputBoundary {
    void prepareSuccessView(AddTransactionOutputData output);

    void prepareFailView(String message);
}
