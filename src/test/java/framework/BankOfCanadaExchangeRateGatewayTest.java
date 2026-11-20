package framework;

import org.junit.jupiter.api.Test;
import usecase.port.ExchangeRateUnavailableException;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class BankOfCanadaExchangeRateGatewayTest {
    @Test
    void weekendUsesLatestEarlierQuoteAndPreservesPrecisionAndCaches() {
        AtomicInteger requests = new AtomicInteger();
        var gateway = new BankOfCanadaExchangeRateGateway(uri -> {
            requests.incrementAndGet();
            assertTrue(uri.toString().contains("FXUSDCAD/json"));
            assertTrue(uri.toString().contains("end_date=2026-11-15"));
            return """
                    {"observations":[
                      {"d":"2026-11-16","FXUSDCAD":{"v":"9"}},
                      {"d":"2026-11-13","FXUSDCAD":{"v":"1.4257"}},
                      {"d":"2026-11-12","FXUSDCAD":{"v":"1.4255"}},
                      {"d":"2026-11-14","FXUSDCAD":{"v":null}}
                    ]}
                    """;
        });
        var date = LocalDate.of(2026, 11, 15);
        var quote = gateway.findRate("USD", date).orElseThrow();
        assertEquals(LocalDate.of(2026, 11, 13), quote.observedDate());
        assertEquals(new BigDecimal("1.4257"), quote.cadPerUnit());
        assertEquals(quote, gateway.findRate("USD", date).orElseThrow());
        assertEquals(1, requests.get());
    }

    @Test
    void cnyUsesCorrectSeriesAndIgnoresMissingValues() {
        var gateway = new BankOfCanadaExchangeRateGateway(uri -> {
            assertTrue(uri.toString().contains("FXCNYCAD"));
            return """
                    {"observations":[{"d":"2026-11-18"},
                     {"d":"2026-11-19","FXCNYCAD":{"v":"0.20"}}]}
                    """;
        });
        assertEquals(new BigDecimal("0.20"), gateway.findRate("CNY", LocalDate.of(2026, 11, 19))
                .orElseThrow().cadPerUnit());
    }

    @Test
    void noQuoteIsNotCachedSoLaterRetryCanSucceed() {
        AtomicInteger requests = new AtomicInteger();
        var gateway = new BankOfCanadaExchangeRateGateway(uri -> {
            if (requests.getAndIncrement() == 0) { return "{\"observations\":[]}"; }
            return "{\"observations\":[{\"d\":\"2026-11-19\",\"FXUSDCAD\":{\"v\":\"1.4\"}}]}";
        });
        assertTrue(gateway.findRate("USD", LocalDate.of(2026, 11, 19)).isEmpty());
        assertTrue(gateway.findRate("USD", LocalDate.of(2026, 11, 19)).isPresent());
    }

    @Test
    void malformedJsonAndNetworkFailureBecomeRecoverableErrors() {
        var malformed = new BankOfCanadaExchangeRateGateway(uri -> "not JSON");
        var offline = new BankOfCanadaExchangeRateGateway(uri -> { throw new IOException("offline"); });
        assertThrows(ExchangeRateUnavailableException.class,
                () -> malformed.findRate("USD", LocalDate.of(2026, 11, 19)));
        assertThrows(ExchangeRateUnavailableException.class,
                () -> offline.findRate("USD", LocalDate.of(2026, 11, 19)));
    }

    @Test
    void ignoresQuotesOutsideLookbackAndRejectsNonpositiveRates() {
        var old = new BankOfCanadaExchangeRateGateway(uri ->
                "{\"observations\":[{\"d\":\"2025-01-01\",\"FXUSDCAD\":{\"v\":\"1.4\"}}]}");
        assertTrue(old.findRate("USD", LocalDate.of(2026, 11, 19)).isEmpty());
        var negative = new BankOfCanadaExchangeRateGateway(uri ->
                "{\"observations\":[{\"d\":\"2026-11-19\",\"FXUSDCAD\":{\"v\":\"-1\"}}]}");
        assertThrows(ExchangeRateUnavailableException.class,
                () -> negative.findRate("USD", LocalDate.of(2026, 11, 19)));
    }
}
