package framework;

import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SpendingEventBusTest {

    private static final YearMonth NOV = YearMonth.of(2026, 11);

    @Test
    void tellsEveryListenerTheMonth() {
        SpendingEventBus bus = new SpendingEventBus();
        List<YearMonth> first = new ArrayList<>();
        List<YearMonth> second = new ArrayList<>();
        bus.addListener(first::add);
        bus.addListener(second::add);

        bus.spendingChanged(NOV);

        assertEquals(List.of(NOV), first);
        assertEquals(List.of(NOV), second);
    }

    @Test
    void noListenersIsFine() {
        assertDoesNotThrow(() -> new SpendingEventBus().spendingChanged(NOV));
    }

    @Test
    void aFailingListenerDoesNotStopTheOthers() {
        SpendingEventBus bus = new SpendingEventBus();
        List<YearMonth> reached = new ArrayList<>();
        bus.addListener(month -> {
            throw new IllegalStateException("broken screen");
        });
        bus.addListener(reached::add);

        assertDoesNotThrow(() -> bus.spendingChanged(NOV));

        assertEquals(List.of(NOV), reached);
    }

    @Test
    void listenersAreToldEachTimeInOrderOfEvents() {
        SpendingEventBus bus = new SpendingEventBus();
        List<YearMonth> seen = new ArrayList<>();
        bus.addListener(seen::add);

        bus.spendingChanged(NOV);
        bus.spendingChanged(YearMonth.of(2026, 12));

        assertEquals(List.of(NOV, YearMonth.of(2026, 12)), seen);
    }
}
