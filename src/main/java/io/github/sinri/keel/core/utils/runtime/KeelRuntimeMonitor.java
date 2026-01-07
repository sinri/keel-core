package io.github.sinri.keel.core.utils.runtime;

import io.github.sinri.keel.core.utils.RuntimeUtils;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;


/**
 * Runtime monitoring service for tracking GC, CPU, and memory metrics over time.
 * <p>
 * This monitor captures periodic snapshots and calculates deltas between consecutive measurements.
 *
 * @since 5.0.0
 */
@NullMarked
public class KeelRuntimeMonitor {
    private final AtomicReference<@Nullable GCStatResult> _lastGCRef = new AtomicReference<>();
    private final AtomicReference<@Nullable CPUTimeResult> _lastCPUTimeRef = new AtomicReference<>();
    private final Vertx vertx;

    public KeelRuntimeMonitor(Vertx vertx) {
        this.vertx = vertx;
    }

    /**
     * Starts the runtime monitoring with a periodic timer.
     * <p>
     * After the specified interval, actual snapshots will be taken and the handler will be invoked
     * with delta statistics calculated from the previous snapshot.
     *
     * @param interval the monitoring interval in milliseconds
     * @param handler  the handler to receive monitoring snapshots
     */
    public void startRuntimeMonitor(long interval, Handler<MonitorSnapshot> handler) {
        // after [interval] waiting, actual snapshots would be taken.
        vertx.setPeriodic(interval, timer -> {
            GCStatResult gcSnapshot = RuntimeUtils.getGCSnapshot();
            CPUTimeResult cpuTimeSnapshot = RuntimeUtils.getCPUTimeSnapshot();
            JVMMemoryResult jvmMemoryResultSnapshot = RuntimeUtils.makeJVMMemorySnapshot();

            GCStatResult gcDiff;
            GCStatResult lastGC = _lastGCRef.get();
            if (lastGC != null) {
                gcDiff = gcSnapshot.since(lastGC);
            } else {
                // Create an empty GCStatResult for the first measurement
                gcDiff = new GCStatResult(System.currentTimeMillis(), 0, 0, 0, 0, null, null);
            }
            _lastGCRef.set(gcSnapshot);

            CPUTimeResult cpuTimeDiff;
            CPUTimeResult lastCpuTime = this._lastCPUTimeRef.get();
            if (lastCpuTime == null) {
                _lastCPUTimeRef.set(cpuTimeSnapshot);
                // Create an empty CPUTimeResult for the first measurement
                cpuTimeDiff = new CPUTimeResult(System.currentTimeMillis(), 0, 0, 0, 0, 0, 0, 0, 0);
            } else {
                cpuTimeDiff = cpuTimeSnapshot.since(lastCpuTime);
                _lastCPUTimeRef.set(cpuTimeSnapshot);
            }

            MonitorSnapshot monitorSnapshot = new MonitorSnapshot(gcDiff, cpuTimeDiff, jvmMemoryResultSnapshot);

            handler.handle(monitorSnapshot);
        });
    }
}
