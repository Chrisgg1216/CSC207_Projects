package adapter;

import usecase.check_budget.BudgetLine;
import usecase.check_budget.CheckBudgetOutputBoundary;
import usecase.check_budget.CheckBudgetOutputData;
import usecase.set_budget.SetBudgetOutputBoundary;
import usecase.set_budget.SetBudgetOutputData;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns use case output into text for the view.
 *
 * Accessibility: status is always written as a word and a symbol
 * ("[WARNING]", "[OVER]"), never only as a color, so users who are
 * color-blind or use a screen reader get the same information.
 */
public class BudgetPresenter implements SetBudgetOutputBoundary, CheckBudgetOutputBoundary {

    private final BudgetViewModel viewModel;

    public BudgetPresenter(BudgetViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(SetBudgetOutputData output) {
        String verb = output.isReplacedExisting() ? "updated" : "set";
        viewModel.setMessage("Budget " + verb + ": " + output.getCategoryName() + " "
                + output.getMonth() + " limit " + output.getLimit() + ".", false);
    }

    @Override
    public void prepareSuccessView(CheckBudgetOutputData output) {
        List<String> text = new ArrayList<>();
        for (BudgetLine line : output.getLines()) {
            text.add(formatLine(line));
        }
        viewModel.setLines(text);

        StringBuilder message = new StringBuilder();
        if (output.getLines().isEmpty()) {
            message.append("No budgets are set for ").append(output.getMonth()).append(".");
        }
        if (output.getPendingConversionCount() > 0) {
            if (message.length() > 0) {
                message.append(" ");
            }
            message.append(output.getPendingConversionCount())
                    .append(" transaction(s) are waiting for currency conversion and are not included.");
        }
        viewModel.setMessage(message.toString(), false);
    }

    @Override
    public void prepareFailView(String errorMessage) {
        viewModel.setMessage(errorMessage, true);
    }

    static String formatLine(BudgetLine line) {
        String head = line.getCategoryName() + ": " + line.getSpent().getAmount().toPlainString()
                + " of " + line.getLimit().getAmount().toPlainString() + " "
                + line.getLimit().getCurrency() + " used (" + line.getPercentUsed().toPlainString() + "%)";
        switch (line.getStatus()) {
            case OVER:
                return "✖ [OVER] " + head + ", over by "
                        + line.getRemaining().getAmount().negate().toPlainString() + ".";
            case WARNING:
                return "⚠ [WARNING] " + head + ", "
                        + line.getRemaining().getAmount().toPlainString() + " left. Close to the limit.";
            default:
                return "✔ [OK] " + head + ", "
                        + line.getRemaining().getAmount().toPlainString() + " left.";
        }
    }
}
