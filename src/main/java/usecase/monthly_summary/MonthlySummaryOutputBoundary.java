package usecase.monthly_summary;

public interface MonthlySummaryOutputBoundary {
    void prepareSuccessView(MonthlySummaryOutputData summary);

    void prepareFailView(String message);
}
