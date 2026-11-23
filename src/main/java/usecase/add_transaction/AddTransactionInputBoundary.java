package usecase.add_transaction;

public interface AddTransactionInputBoundary {
    void execute(AddTransactionInputData input);

    /** Load the transaction list when the screen opens or Refresh is selected. */
    void refresh();
}
