package io.github.sinri.keel.core.utils.runtime;

import org.jspecify.annotations.NullMarked;

/**
 * A snapshot of runtime monitoring data containing GC statistics, CPU time information,
 * and JVM memory metrics.
 * <p>
 * This record provides an immutable view of the runtime state at a specific point in time.
 *
 * @param gcStat          the garbage collection statistics, or null if not available
 * @param cpuTime         the CPU time statistics, or null if not available
 * @param jvmMemoryResult the JVM memory statistics, or null if not available
 * @since 5.0.0
 */
@NullMarked
public record MonitorSnapshot(
        GCStatResult gcStat,
        CPUTimeResult cpuTime,
        JVMMemoryResult jvmMemoryResult
) {
}
