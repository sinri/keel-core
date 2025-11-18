package io.github.sinri.keel.core.utils.runtime;

/**
 * @since 5.0.0
 */
public class MonitorSnapshot {
    private GCStatResult GCStat;
    private CPUTimeResult CPUTime;
    private JVMMemoryResult jvmMemoryResult;

    public CPUTimeResult getCPUTime() {
        return CPUTime;
    }

    public MonitorSnapshot setCPUTime(CPUTimeResult CPUTime) {
        this.CPUTime = CPUTime;
        return this;
    }

    public GCStatResult getGCStat() {
        return GCStat;
    }

    public MonitorSnapshot setGCStat(GCStatResult GCStat) {
        this.GCStat = GCStat;
        return this;
    }


    public JVMMemoryResult getJvmMemoryResult() {
        return jvmMemoryResult;
    }

    public MonitorSnapshot setJvmMemoryResult(JVMMemoryResult jvmMemoryResult) {
        this.jvmMemoryResult = jvmMemoryResult;
        return this;
    }
}
