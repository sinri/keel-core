package io.github.sinri.keel.core.utils.runtime;

import io.github.sinri.keel.core.utils.RuntimeUtils;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;



/**
 * @since 5.0.0
 */
public class KeelRuntimeMonitor {
    private final AtomicReference<GCStatResult> _lastGCRef = new AtomicReference<>();
    private final AtomicReference<CPUTimeResult> _lastCPUTimeRef = new AtomicReference<>();
    private final Vertx vertx;

    public KeelRuntimeMonitor(Vertx vertx) {
        this.vertx = vertx;
    }

    public void startRuntimeMonitor(long interval, @NotNull Handler<MonitorSnapshot> handler) {
        // after [interval] waiting, actual snapshots would be taken.
        vertx.setPeriodic(interval, timer -> {
            MonitorSnapshot monitorSnapshot = new MonitorSnapshot();

            GCStatResult gcSnapshot = RuntimeUtils.getGCSnapshot();
            CPUTimeResult cpuTimeSnapshot = RuntimeUtils.getCPUTimeSnapshot();

            JVMMemoryResult jvmMemoryResultSnapshot = RuntimeUtils.makeJVMMemorySnapshot();

            GCStatResult lastGC = _lastGCRef.get();
            if (lastGC != null) {
                GCStatResult gcDiff = gcSnapshot.since(lastGC);
                monitorSnapshot.setGCStat(gcDiff);
            } else {
                monitorSnapshot.setGCStat(new GCStatResult());
            }
            _lastGCRef.set(gcSnapshot);

            CPUTimeResult lastCpuTime = this._lastCPUTimeRef.get();
            if (lastCpuTime == null) {
                _lastCPUTimeRef.set(cpuTimeSnapshot);
                monitorSnapshot.setCPUTime(new CPUTimeResult());
            } else {
                CPUTimeResult cpuTimeDiff = cpuTimeSnapshot.since(lastCpuTime);
                monitorSnapshot.setCPUTime(cpuTimeDiff);
            }

            monitorSnapshot.setJvmMemoryResult(jvmMemoryResultSnapshot);

            handler.handle(monitorSnapshot);
        });
    }
}
