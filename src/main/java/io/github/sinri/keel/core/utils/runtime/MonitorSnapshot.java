package io.github.sinri.keel.core.utils.runtime;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * @since 5.0.0
 */
@NullMarked
public class MonitorSnapshot {
    private @Nullable GCStatResult GCStat;
    private @Nullable CPUTimeResult CPUTime;
    private @Nullable JVMMemoryResult jvmMemoryResult;

    public @Nullable CPUTimeResult getCPUTime() {
        return CPUTime;
    }

    public MonitorSnapshot setCPUTime(CPUTimeResult CPUTime) {
        this.CPUTime = CPUTime;
        return this;
    }

    public @Nullable GCStatResult getGCStat() {
        return GCStat;
    }

    public MonitorSnapshot setGCStat(GCStatResult GCStat) {
        this.GCStat = GCStat;
        return this;
    }


    public @Nullable JVMMemoryResult getJvmMemoryResult() {
        return jvmMemoryResult;
    }

    public MonitorSnapshot setJvmMemoryResult(JVMMemoryResult jvmMemoryResult) {
        this.jvmMemoryResult = jvmMemoryResult;
        return this;
    }
}
