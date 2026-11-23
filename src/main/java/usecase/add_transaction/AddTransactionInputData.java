package usecase.add_transaction;

import entity.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AddTransactionInputData(LocalDate date, TransactionType type,
                                      String categoryName, BigDecimal amount,
                                      String currency, String note) {
}
