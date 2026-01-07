package io.github.sinri.keel.core.utils.runtime;

import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.NullMarked;

/**
 * Represents JVM memory statistics at a specific point in time.
 * <p>
 * This record provides comprehensive memory information including physical memory,
 * runtime heap memory, and MXBean-based memory metrics.
 *
 * @param statTime                   the timestamp when this statistics was captured (milliseconds since epoch)
 * @param physicalMaxBytes           the maximum physical memory available to the system in bytes
 * @param physicalUsedBytes          the physical memory currently in use by the system in bytes
 * @param runtimeHeapMaxBytes        the maximum heap memory the JVM will attempt to use (e.g., -Xmx)
 * @param runtimeHeapAllocatedBytes  the total heap memory currently allocated by the JVM
 * @param runtimeHeapUsedBytes       the heap memory currently in use (allocated minus free)
 * @param mxHeapUsedBytes            the heap memory used as reported by MemoryMXBean
 * @param mxNonHeapUsedBytes         the non-heap memory used as reported by MemoryMXBean
 * @since 5.0.0
 */
@NullMarked
public record JVMMemoryResult(
        long statTime,
        long physicalMaxBytes,
        long physicalUsedBytes,
        long runtimeHeapMaxBytes,
        long runtimeHeapAllocatedBytes,
        long runtimeHeapUsedBytes,
        long mxHeapUsedBytes,
        long mxNonHeapUsedBytes
) implements RuntimeStatResult<JVMMemoryResult> {

    //    @Override
    //    public long statTime() {
    //        return statTime;
    //    }

    @Override
    public JVMMemoryResult since(JVMMemoryResult start) {
        throw new UnsupportedOperationException("Meaningless operation");
    }

    @Override
    public JsonObject toJsonObject() {
        return new JsonObject()
                .put("stat_time", statTime)
                .put("physical_max_bytes", physicalMaxBytes)
                .put("physical_used_bytes", physicalUsedBytes)
                .put("runtime_heap_max_bytes", runtimeHeapMaxBytes)
                .put("runtime_heap_allocated_bytes", runtimeHeapAllocatedBytes)
                .put("runtime_heap_used_bytes", runtimeHeapUsedBytes)
                .put("mx_heap_used_bytes", mxHeapUsedBytes)
                .put("mx_non_heap_used_bytes", mxNonHeapUsedBytes)
                ;
    }
}
