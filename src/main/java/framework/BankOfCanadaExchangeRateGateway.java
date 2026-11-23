package framework;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import entity.ExchangeRate;
import usecase.port.ExchangeRateGateway;
import usecase.port.ExchangeRateUnavailableException;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

/** Historical daily reference quotes. The network and JSON stay in the outer layer. */
public class BankOfCanadaExchangeRateGateway implements ExchangeRateGateway {
    private static final Map<String, String> SERIES = Map.of("USD", "FXUSDCAD", "CNY", "FXCNYCAD");
    private final TextFetcher fetcher;
    private final Map<String, ExchangeRate> cache = new HashMap<>();

    @FunctionalInterface
    interface TextFetcher {
        String get(URI uri) throws Exception;
    }

    public BankOfCanadaExchangeRateGateway() {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        this.fetcher = uri -> {
            HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(8))
                    .header("Accept", "application/json").GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Exchange service returned HTTP " + response.statusCode());
            }
            return response.body();
        };
    }

    BankOfCanadaExchangeRateGateway(TextFetcher fetcher) {
        this.fetcher = fetcher;
    }

    @Override
    public synchronized Optional<ExchangeRate> findRate(String currency, LocalDate date) {
        String series = SERIES.get(currency);
        if (series == null) {
            throw new IllegalArgumentException("Unsupported foreign currency: " + currency);
        }
        String key = currency + "|" + date;
        if (cache.containsKey(key)) {
            return Optional.of(cache.get(key));
        }
        LocalDate start = date.minusDays(14);
        URI uri = URI.create("https://www.bankofcanada.ca/valet/observations/" + series
                + "/json?start_date=" + start + "&end_date=" + date);
        try {
            JsonObject root = JsonParser.parseString(fetcher.get(uri)).getAsJsonObject();
            List<ExchangeRate> quotes = StreamSupport.stream(root.getAsJsonArray("observations").spliterator(), false)
                    .map(JsonElement::getAsJsonObject)
                    .filter(row -> row.has(series) && row.get(series).isJsonObject())
                    .filter(row -> row.getAsJsonObject(series).has("v")
                            && !row.getAsJsonObject(series).get("v").isJsonNull())
                    .map(row -> new ExchangeRate(currency,
                            new BigDecimal(row.getAsJsonObject(series).get("v").getAsString()),
                            LocalDate.parse(row.get("d").getAsString()), "Bank of Canada"))
                    .filter(rate -> !rate.observedDate().isAfter(date) && !rate.observedDate().isBefore(start))
                    .toList();
            Optional<ExchangeRate> chosen = quotes.stream().max(Comparator.comparing(ExchangeRate::observedDate));
            chosen.ifPresent(rate -> cache.put(key, rate));
            return chosen;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ExchangeRateUnavailableException("Exchange request interrupted.", ex);
        } catch (Exception ex) {
            throw new ExchangeRateUnavailableException("Historical exchange rate unavailable; try again later.", ex);
        }
    }
}
