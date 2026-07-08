package io.github.sinri.keel.core.maids.gatling;

import io.github.sinri.keel.tesuto.KeelJUnit5Test;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GatlingOptionsTest extends KeelJUnit5Test {
    @Test
    void rejectsNonPositiveBarrels() {
        GatlingOptions options = new GatlingOptions("test");

        assertThrows(IllegalArgumentException.class, () -> options.setBarrels(0));
        assertThrows(IllegalArgumentException.class, () -> options.setBarrels(-1));
    }

    @Test
    void acceptsPositiveBarrels() {
        GatlingOptions options = new GatlingOptions("test");

        options.setBarrels(1);
        assertEquals(1, options.getBarrels());

        options.setBarrels(2);
        assertEquals(2, options.getBarrels());
    }

    @Test
    void rejectsTooSmallAverageRestInterval() {
        GatlingOptions options = new GatlingOptions("test");

        assertThrows(IllegalArgumentException.class, () -> options.setAverageRestInterval(-1));
        assertThrows(IllegalArgumentException.class, () -> options.setAverageRestInterval(0));
        assertThrows(IllegalArgumentException.class, () -> options.setAverageRestInterval(1));
    }

    @Test
    void rejectsTooLargeAverageRestInterval() {
        GatlingOptions options = new GatlingOptions("test");

        assertThrows(
                IllegalArgumentException.class,
                () -> options.setAverageRestInterval(GatlingOptions.MAX_AVERAGE_REST_INTERVAL + 1)
        );
    }

    @Test
    void acceptsAverageRestIntervalBounds() {
        GatlingOptions options = new GatlingOptions("test");

        options.setAverageRestInterval(2);
        assertEquals(2, options.getAverageRestInterval());

        options.setAverageRestInterval(GatlingOptions.MAX_AVERAGE_REST_INTERVAL);
        assertEquals(GatlingOptions.MAX_AVERAGE_REST_INTERVAL, options.getAverageRestInterval());
    }
}
