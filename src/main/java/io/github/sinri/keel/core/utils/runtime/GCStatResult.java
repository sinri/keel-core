package io.github.sinri.keel.core.utils.runtime;

import io.github.sinri.keel.base.logger.factory.StdoutLoggerFactory;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.lang.management.GarbageCollectorMXBean;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Represents garbage collection statistics at a specific point in time.
 * <p>
 * This record provides information about major and minor GC counts, times, and types.
 * It maintains static registries of known GC implementations for various collectors
 * (Serial, Parallel, CMS, G1, ZGC, Shenandoah).
 *
 * @param statTime      the timestamp when this statistics was captured (milliseconds since epoch)
 * @param minorGCCount  the number of minor (young generation) garbage collections
 * @param minorGCTime   the time spent in minor garbage collections (milliseconds)
 * @param majorGCCount  the number of major (old generation) garbage collections
 * @param majorGCTime   the time spent in major garbage collections (milliseconds)
 * @param minorGCType   the name of the minor GC collector being used, or null if not determined
 * @param majorGCType   the name of the major GC collector being used, or null if not determined
 * @since 5.0.0
 */
@NullMarked
public record GCStatResult(
        long statTime,
        long minorGCCount,
        long minorGCTime,
        long majorGCCount,
        long majorGCTime,
        @Nullable String minorGCType,
        @Nullable String majorGCType
) implements RuntimeStatResult<GCStatResult> {

    private static final Set<String> minorGCNames;
    private static final Set<String> majorGCNames;
    private static final Set<String> ignoreGCNames;

    static {
        minorGCNames = new HashSet<>();
        majorGCNames = new HashSet<>();
        ignoreGCNames = new HashSet<>();

        // Serial Collector： "Copy"（年轻代），"MarkSweepCompact"（老年代）
        minorGCNames.add("Copy");
        majorGCNames.add("MarkSweepCompact");
        //Parallel Collector： "PS Scavenge"（年轻代），"PS MarkSweep"（老年代）
        minorGCNames.add("PS Scavenge");
        majorGCNames.add("PS MarkSweep");
        // CMS (Concurrent Mark Sweep) Collector： "ParNew"（年轻代），"ConcurrentMarkSweep"（老年代）
        minorGCNames.add("ParNew");
        majorGCNames.add("ConcurrentMarkSweep");
        // G1 (Garbage-First) Collector： "G1 Young Generation"（年轻代），"G1 Old Generation"（老年代）
        minorGCNames.add("G1 Young Generation");
        majorGCNames.add("G1 Old Generation");
        ignoreGCNames.add("G1 Concurrent GC"); // for JDK21
        majorGCNames.add("G1 Mixed Generation");// for JDK21
        // ZGC (Z Garbage Collector)： "ZGC"
        // @see  https://armsword.com/2023/08/10/es-jdk17-and-zgc/
        //minorGCNames.add("ZGC");
        majorGCNames.add("ZGC Pauses"); // since 4.0.0 majar since 3.2.5
        // 统计的是ZGC在GC过程中暂停的次数及暂停时间，这是JDK17新增的指标bean，无法统计Allocation Stall导致的线程挂起时间
        minorGCNames.add("ZGC Cycles"); // since 3.2.5 统计的是ZGC发生的次数以及总耗时
        // Shenandoah： "Shenandoah Pauses"
        minorGCNames.add("Shenandoah Pauses");
    }

    public static void handleMajorGCNames(Handler<Set<String>> handler) {
        handler.handle(majorGCNames);
    }

    public static void handleMinorGCNames(Handler<Set<String>> handler) {
        handler.handle(minorGCNames);
    }

    public static void handleIgnoreGCNames(Handler<Set<String>> handler) {
        handler.handle(ignoreGCNames);
    }

    /**
     * Parses garbage collector MXBeans and aggregates their statistics.
     * <p>
     * This factory method examines the list of GarbageCollectorMXBeans and
     * classifies them into minor and major GC categories based on their names.
     *
     * @param gcList the list of GarbageCollectorMXBean instances to parse
     * @return a GCStatResult containing aggregated statistics
     */
    public static GCStatResult parseGarbageCollectorMXBeans(List<@Nullable GarbageCollectorMXBean> gcList) {
        long statTime = System.currentTimeMillis();
        long minorGCCount = 0;
        long minorGCTime = 0;
        long majorGCCount = 0;
        long majorGCTime = 0;
        String minorGCType = null;
        String majorGCType = null;

        for (GarbageCollectorMXBean gc : gcList) {
            if (gc == null) {
                continue;
            }

            if (minorGCNames.contains(gc.getName())) {
                minorGCCount = gc.getCollectionCount();
                if (gc.getCollectionTime() >= 0) {
                    minorGCTime = gc.getCollectionTime();
                }
                minorGCType = gc.getName();
            } else if (majorGCNames.contains(gc.getName())) {
                majorGCCount = gc.getCollectionCount();
                if (gc.getCollectionTime() >= 0) {
                    majorGCTime = gc.getCollectionTime();
                }
                majorGCType = gc.getName();
            } else if (!ignoreGCNames.contains(gc.getName())) {
                StdoutLoggerFactory.getInstance().createLogger(GCStatResult.class.getName()).error(log -> log
                        .message("Found Unknown GarbageCollectorMXBean Name")
                        .context(ctx -> ctx
                                .put("class", gc.getClass().getName())
                                .put("name", gc.getName())
                                .put("memoryPoolNames", String.join(",", gc.getMemoryPoolNames()))
                                .put("objectName", gc.getObjectName())
                                .put("collectionCount", gc.getCollectionCount())
                                .put("collectionTime", gc.getCollectionTime())
                        )
                );
            }
        }

        return new GCStatResult(statTime, minorGCCount, minorGCTime, majorGCCount, majorGCTime, minorGCType, majorGCType);
    }

    //    @Override
    //    public long statTime() {
    //        return statTime;
    //    }

    @Override
    public GCStatResult since(GCStatResult start) {
        return new GCStatResult(
                statTime(),
                minorGCCount() - start.minorGCCount(),
                minorGCTime() - start.minorGCTime(),
                majorGCCount() - start.majorGCCount(),
                majorGCTime() - start.majorGCTime(),
                minorGCType(),
                majorGCType()
        );
    }

    @Override
    public JsonObject toJsonObject() {
        return new JsonObject()
                .put("stat_time", statTime())
                .put("major", new JsonObject()
                        .put("count", majorGCCount())
                        .put("time", majorGCTime())
                        .put("type", majorGCType())
                )
                .put("minor", new JsonObject()
                        .put("count", minorGCCount())
                        .put("time", minorGCTime())
                        .put("type", minorGCType())
                );
    }
}
