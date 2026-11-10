package entity;

/**
 * How close a category is to its monthly limit.
 *
 * OK      - below the warning threshold (default 80%).
 * WARNING - at or above the threshold, up to and including 100% of the limit.
 * OVER    - spending is strictly above the limit.
 */
public enum BudgetStatus {
    OK,
    WARNING,
    OVER
}
