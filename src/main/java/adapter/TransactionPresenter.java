package adapter;

import entity.Transaction;
import usecase.add_transaction.AddTransactionOutputBoundary;
import usecase.add_transaction.AddTransactionOutputData;

public class TransactionPresenter implements AddTransactionOutputBoundary {
    private final TransactionViewModel viewModel;

    public TransactionPresenter(TransactionViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(AddTransactionOutputData output) {
        viewModel.update(output.transactions().stream().map(this::format).toList(), output.message(), false);
    }

    private String format(Transaction t) {
        String conversion = t.convertedMoney().map(m -> m + " (reference; rate "
                + t.exchangeRate().orElseThrow().cadPerUnit().toPlainString() + ", "
                + t.exchangeRate().orElseThrow().observedDate() + ")")
                .orElse("[PENDING CONVERSION]");
        return t.date() + " | " + t.type() + " | " + t.categoryName() + " | "
                + t.originalMoney() + " → " + conversion + (t.note().isEmpty() ? "" : " | " + t.note());
    }

    @Override
    public void prepareFailView(String message) {
        viewModel.update(viewModel.getLines(), message, true);
    }
}
