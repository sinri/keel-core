package io.github.sinri.keel.core.utils.runtime;

import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.text.DecimalFormat;

/**
 * Represents System-wide CPU Load tick counters.
 * <p>
 * Contains eight items to represent milliseconds spent in
 * User (0),
 * Nice (1),
 * System (2),
 * Idle (3),
 * IOwait (4),
 * Hardware interrupts (IRQ) (5),
 * Software interrupts/DPC (SoftIRQ) (6),
 * or Steal (7) states.
 * <p>
 * By measuring the difference between ticks across a time interval,
 * CPU load over that interval may be calculated.
 * <p>
 * On some operating systems with variable numbers of logical processors,
 * the size of this array could change and may not align with other per-processor methods.
 * <p>
 * Note that while tick counters are in units of milliseconds,
 * they may advance in larger increments along with (platform dependent) clock ticks.
 * For example, by default Windows clock ticks are 1/64 of a second (about 15 or 16 milliseconds)
 * and Linux ticks are distribution and configuration dependent but usually 1/100 of a second (10 milliseconds).
 * Nice and IOWait information is not available on Windows,
 * and IOwait and IRQ information is not available on macOS, so these ticks will always be zero.
 * To calculate overall Idle time using this method,
 * include both Idle and IOWait ticks.
 * Similarly, IRQ, SoftIRQ, and Steal ticks should be added to the System value to get the total.
 * System ticks also include time executing other virtual hosts (steal).
 *
 * @param statTime           the timestamp when this statistics was captured (milliseconds since epoch)
 * @param spentInUserState   time spent in User state (milliseconds)
 * @param spentInNiceState   time spent in Nice state (milliseconds)
 * @param spentInSystemState time spent in System state (milliseconds)
 * @param spentInIdleState   time spent in Idle state (milliseconds)
 * @param spentInIOWaitState time spent in IOWait state (milliseconds)
 * @param spentInIRQState    time spent in Hardware interrupts (IRQ) state (milliseconds)
 * @param spentInSoftIRQState time spent in Software interrupts/DPC (SoftIRQ) state (milliseconds)
 * @param spentInStealState  time spent in Steal state (milliseconds)
 * @since 5.0.0
 */
@NullMarked
public record CPUTimeResult(
        long statTime,
        long spentInUserState,
        long spentInNiceState,
        long spentInSystemState,
        long spentInIdleState,
        long spentInIOWaitState,
        long spentInIRQState,
        long spentInSoftIRQState,
        long spentInStealState
) implements RuntimeStatResult<CPUTimeResult> {

    //    @Override
    //    public long statTime() {
    //        return statTime;
    //    }

    @Override
    public CPUTimeResult since(CPUTimeResult start) {
        return new CPUTimeResult(
                statTime(),
                spentInUserState() - start.spentInUserState(),
                spentInNiceState() - start.spentInNiceState(),
                spentInSystemState() - start.spentInSystemState(),
                spentInIdleState() - start.spentInIdleState(),
                spentInIOWaitState() - start.spentInIOWaitState(),
                spentInIRQState() - start.spentInIRQState(),
                spentInSoftIRQState() - start.spentInSoftIRQState(),
                spentInStealState() - start.spentInStealState()
        );
    }

    /**
     * Calculates the CPU usage ratio based on idle time versus total time.
     *
     * @return the CPU usage as a value between 0.0 and 1.0
     */
    public double cpuUsage() {
        long total = this.spentInUserState
                + this.spentInNiceState
                + this.spentInSystemState
                + this.spentInIdleState
                + this.spentInIOWaitState
                + this.spentInIRQState
                + this.spentInSoftIRQState
                + this.spentInStealState;
        if (total == 0) return 0;
        return 1.0 - 1.0 * spentInIdleState / total;
    }

    /**
     * Gets the CPU usage as a formatted percentage string.
     *
     * @return the CPU usage percentage (e.g., "75.32"), or null if the value is invalid (infinite or NaN)
     */
    @Nullable
    public String cpuUsagePercent() {
        double cpuUsage = cpuUsage();
        if (Double.isInfinite(cpuUsage) || Double.isNaN(cpuUsage)) {
            return null;
        }
        return new DecimalFormat("#.##").format(cpuUsage * 100);
    }

    @Override
    public JsonObject toJsonObject() {
        return new JsonObject()
                .put("stat_time", statTime())
                .put("User", this.spentInUserState)
                .put("Nice", this.spentInNiceState)
                .put("Idle", this.spentInIdleState)
                .put("IOWait", this.spentInIOWaitState)
                .put("IRQ", this.spentInIRQState)
                .put("SoftIRQ", this.spentInSoftIRQState)
                .put("Steal", this.spentInStealState)
                .put("usage", this.cpuUsagePercent());
    }
}
