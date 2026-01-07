package io.github.sinri.keel.core.utils;

import io.github.sinri.keel.core.utils.runtime.CPUTimeResult;
import io.github.sinri.keel.core.utils.runtime.GCStatResult;
import io.github.sinri.keel.core.utils.runtime.JVMMemoryResult;
import org.jspecify.annotations.NullMarked;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;

/**
 * 运行时工具。
 * <p>
 * 目前主要用于监控。
 *
 * @since 5.0.0
 */
@NullMarked
public class RuntimeUtils {

    private static final SystemInfo systemInfo = new SystemInfo();

    private RuntimeUtils() {
    }


    private static OperatingSystemMXBean osMX() {
        return ManagementFactory.getOperatingSystemMXBean();
    }

    private static MemoryMXBean memoryMX() {
        return ManagementFactory.getMemoryMXBean();
    }

    private static Runtime runtime() {
        return Runtime.getRuntime();
    }


    public static GCStatResult getGCSnapshot() {
        return GCStatResult.parseGarbageCollectorMXBeans(ManagementFactory.getGarbageCollectorMXBeans());
    }


    public static CPUTimeResult getCPUTimeSnapshot() {
        CentralProcessor processor = systemInfo.getHardware().getProcessor();
        long[] systemCpuLoadTicks = processor.getSystemCpuLoadTicks();

        return new CPUTimeResult(
                System.currentTimeMillis(),
                systemCpuLoadTicks[CentralProcessor.TickType.USER.getIndex()],
                systemCpuLoadTicks[CentralProcessor.TickType.NICE.getIndex()],
                systemCpuLoadTicks[CentralProcessor.TickType.SYSTEM.getIndex()],
                systemCpuLoadTicks[CentralProcessor.TickType.IDLE.getIndex()],
                systemCpuLoadTicks[CentralProcessor.TickType.IOWAIT.getIndex()],
                systemCpuLoadTicks[CentralProcessor.TickType.IRQ.getIndex()],
                systemCpuLoadTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()],
                systemCpuLoadTicks[CentralProcessor.TickType.STEAL.getIndex()]
        );
    }

    public static JVMMemoryResult makeJVMMemorySnapshot() {
        Runtime runtime = runtime();
        long maxMemory = runtime.maxMemory();// JVM 会试图使用的内存总量（如通过Xmx设置或自动设置）
        long totalMemory = runtime.totalMemory(); // JVM总内存量（当前实际申请下来的）
        long freeMemory = runtime.freeMemory(); // JVM空闲内存量（当前实际申请下来的内存但没使用或已释放的）

        GlobalMemory memory = systemInfo.getHardware().getMemory();

        // freeMemory + maxMemory - totalMemory
        return new JVMMemoryResult(
                System.currentTimeMillis(),
                memory.getTotal(),
                memory.getTotal() - memory.getAvailable(),
                maxMemory,
                totalMemory,
                totalMemory - freeMemory,
                getHeapMemoryUsage().getUsed(),
                getNonHeapMemoryUsage().getUsed() // 独立的
        );
    }

    /**
     * Returns the system load average for the last minute. The system load average is the sum of the number of runnable
     * entities queued to the available processors and the number of runnable entities running on the available
     * processors averaged over a period of time. The way in which the load average is calculated is operating system
     * specific but is typically a damped time-dependent average.
     * If the load average is not available, a negative value is returned.
     * This method is designed to provide a hint about the system load and may be queried frequently. The load average
     * may be unavailable on some platform where it is expensive to implement this method.
     *
     * @return the system load average; or a negative value if not available.
     */
    public static double getSystemLoadAverage() {
        return osMX().getSystemLoadAverage();
    }


    public static MemoryUsage getHeapMemoryUsage() {
        return memoryMX().getHeapMemoryUsage();
    }


    public static MemoryUsage getNonHeapMemoryUsage() {
        return memoryMX().getNonHeapMemoryUsage();
    }


    public static int getObjectPendingFinalizationCount() {
        return memoryMX().getObjectPendingFinalizationCount();
    }
}
