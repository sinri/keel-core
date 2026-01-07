package io.github.sinri.keel.core.utils.runtime;

import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.NullMarked;

import java.text.DecimalFormat;

/**
 * Represents memory statistics at a specific point in time.
 * <p>
 * This record provides information about total and available memory in bytes,
 * along with calculated usage metrics.
 *
 * @param statTime      the timestamp when this statistics was captured (milliseconds since epoch)
 * @param totalByte     the total memory in bytes
 * @param availableByte the available memory in bytes
 * @since 5.0.0
 */
@NullMarked
public record MemoryResult(
        long statTime,
        long totalByte,
        long availableByte
) implements RuntimeStatResult<MemoryResult> {

    //    @Override
    //    public long statTime() {
    //        return statTime;
    //    }

    @Override
    public MemoryResult since(MemoryResult start) {
        throw new UnsupportedOperationException("Meaningless operation");
    }

    /**
     * Calculates the memory usage ratio.
     *
     * @return the memory usage as a value between 0.0 and 1.0
     */
    public double memoryUsage() {
        return 1.0 - 1.0 * availableByte() / totalByte();
    }

    /**
     * Gets the memory usage as a formatted percentage string.
     *
     * @return the memory usage percentage (e.g., "75.32")
     */
    public String memoryUsagePercent() {
        return new DecimalFormat("#.##").format(memoryUsage() * 100);
    }

    @Override
    public JsonObject toJsonObject() {
        return new JsonObject()
                .put("stat_time", statTime())
                .put("total_bytes", totalByte())
                .put("available_bytes", availableByte())
                .put("usage", memoryUsagePercent());
    }
}
