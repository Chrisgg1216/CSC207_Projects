package app;

import framework.BankOfCanadaExchangeRateGateway;
import java.time.LocalDate;

/** Explicit live API check, deliberately excluded from deterministic unit tests. */
public final class ExchangeRateSmokeCheck {
    private ExchangeRateSmokeCheck() { }

    public static void main(String[] args) {
        LocalDate date = args.length == 0 ? LocalDate.now() : LocalDate.parse(args[0]);
        BankOfCanadaExchangeRateGateway gateway = new BankOfCanadaExchangeRateGateway();
        for (String currency : new String[]{"USD", "CNY"}) {
            var quote = gateway.findRate(currency, date)
                    .orElseThrow(() -> new IllegalStateException("No recent quote for " + currency));
            System.out.println(currency + " → CAD | requested " + date + " | observed "
                    + quote.observedDate() + " | rate " + quote.cadPerUnit() + " | " + quote.source());
        }
    }
}
