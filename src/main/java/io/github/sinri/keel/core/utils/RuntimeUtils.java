package io.github.sinri.keel.core.utils;

import io.github.sinri.keel.core.utils.runtime.CPUTimeResult;
import io.github.sinri.keel.core.utils.runtime.GCStatResult;
import io.github.sinri.keel.core.utils.runtime.JVMMemoryResult;
import org.jetbrains.annotations.NotNull;
import org.openjdk.jol.info.ClassLayout;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Field;
import java.util.IdentityHashMap;

/**
 * @since 2.9.3
 * @since 3.1.3 Add more GarbageCollectorMXBean.
 */
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

    @NotNull
    public static GCStatResult getGCSnapshot() {
        return GCStatResult.parseGarbageCollectorMXBeans(ManagementFactory.getGarbageCollectorMXBeans());
    }

    /**
     * @since 2.9.4
     */
    @NotNull
    public static CPUTimeResult getCPUTimeSnapshot() {
        CentralProcessor processor = systemInfo.getHardware().getProcessor();
        long[] systemCpuLoadTicks = processor.getSystemCpuLoadTicks();

        return new CPUTimeResult()
                .setSpentInUserState(systemCpuLoadTicks[CentralProcessor.TickType.USER.getIndex()])
                .setSpentInNiceState(systemCpuLoadTicks[CentralProcessor.TickType.NICE.getIndex()])
                .setSpentInSystemState(systemCpuLoadTicks[CentralProcessor.TickType.SYSTEM.getIndex()])
                .setSpentInIdleState(systemCpuLoadTicks[CentralProcessor.TickType.IDLE.getIndex()])
                .setSpentInIOWaitState(systemCpuLoadTicks[CentralProcessor.TickType.IOWAIT.getIndex()])
                .setSpentInIRQState(systemCpuLoadTicks[CentralProcessor.TickType.IRQ.getIndex()])
                .setSpentInSoftIRQState(systemCpuLoadTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()])
                .setSpentInStealState(systemCpuLoadTicks[CentralProcessor.TickType.STEAL.getIndex()]);
    }

    public static JVMMemoryResult makeJVMMemorySnapshot() {
        Runtime runtime = runtime();
        long maxMemory = runtime.maxMemory();// JVM 会试图使用的内存总量（如通过Xmx设置或自动设置）
        long totalMemory = runtime.totalMemory(); // JVM总内存量（当前实际申请下来的）
        long freeMemory = runtime.freeMemory(); // JVM空闲内存量（当前实际申请下来的内存但没使用或已释放的）

        GlobalMemory memory = systemInfo.getHardware().getMemory();

        // freeMemory + maxMemory - totalMemory
        return new JVMMemoryResult()
                .setPhysicalMaxBytes(memory.getTotal())
                .setPhysicalUsedBytes(memory.getTotal() - memory.getAvailable())
                .setRuntimeHeapMaxBytes(maxMemory)
                .setRuntimeHeapAllocatedBytes(totalMemory)
                .setRuntimeHeapUsedBytes(totalMemory - freeMemory)
                .setMxHeapUsedBytes(getHeapMemoryUsage().getUsed())
                .setMxNonHeapUsedBytes(getNonHeapMemoryUsage().getUsed()) // 独立的
                ;
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
     * @since 3.1.4
     */
    public static double getSystemLoadAverage() {
        return osMX().getSystemLoadAverage();
    }

    /**
     * @since 3.1.4
     */
    public static MemoryUsage getHeapMemoryUsage() {
        return memoryMX().getHeapMemoryUsage();
    }

    /**
     * @since 3.1.4
     */
    public static MemoryUsage getNonHeapMemoryUsage() {
        return memoryMX().getNonHeapMemoryUsage();
    }

    /**
     * @since 3.1.4
     */
    public static int getObjectPendingFinalizationCount() {
        return memoryMX().getObjectPendingFinalizationCount();
    }

    /**
     * @param object the object to calculate its size
     * @return the size of the object, in bytes.
     * @since 4.0.0
     */
    public static long measureObjectSizeWithJOL(Object object) {
        ClassLayout classLayout = ClassLayout.parseInstance(object);
        return classLayout.instanceSize();
    }

    /**
     * To calculate the size of an object and its referenced objects.
     * It is not accurate.
     *
     * @param obj the object to calculate its deep size
     * @return the deep size of the provided object, in bytes.
     * @since 4.0.0
     */
    public static long calculateObjectDeepSizeWithJOL(Object obj) {
        IdentityHashMap<Object, Object> visited = new IdentityHashMap<>();
        return calculateObjectDeepSizeWithJOL(obj, visited);
    }

    /**
     * @since 4.0.0
     */
    private static long calculateObjectDeepSizeWithJOL(Object obj, IdentityHashMap<Object, Object> visited) {
        if (obj == null || visited.containsKey(obj)) {
            return 0;
        }
        visited.put(obj, null);

        long size = ClassLayout.parseInstance(obj).instanceSize();
        Class<?> clazz = obj.getClass();

        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                // 跳过 JDK 内部类的字段
                if (field.getDeclaringClass().getName().startsWith("java.") ||
                        field.getDeclaringClass().getName().startsWith("javax.")) {
                    continue;
                }
                if (!field.getType().isPrimitive()) {
                    field.setAccessible(true);
                    try {
                        Object fieldValue = field.get(obj);
                        if (fieldValue != null) {
                            size += calculateObjectDeepSizeWithJOL(fieldValue, visited);
                        }
                    } catch (IllegalAccessException e) {
                        //e.printStackTrace();
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return size;
    }
}
