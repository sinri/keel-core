package io.github.sinri.keel.core.utils.runtime;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.management.GarbageCollectorMXBean;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.github.sinri.keel.base.KeelInstance.Keel;

/**
 * @since 2.9.4
 */
public class GCStatResult implements RuntimeStatResult<GCStatResult> {
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

    private final long statTime;
    private long minorGCCount = 0;
    private long minorGCTime = 0;
    private long majorGCCount = 0;
    private long majorGCTime = 0;
    @Nullable
    private String majorGCType;
    @Nullable
    private String minorGCType;

    public GCStatResult() {
        this.statTime = System.currentTimeMillis();
    }

    private GCStatResult(long statTime) {
        this.statTime = statTime;
    }

    /**
     * @since 4.0.2
     */
    public static void handleMajorGCNames(@NotNull Handler<Set<String>> handler) {
        handler.handle(majorGCNames);
    }

    /**
     * @since 4.0.2
     */
    public static void handleMinorGCNames(@NotNull Handler<Set<String>> handler) {
        handler.handle(minorGCNames);
    }

    /**
     * @since 4.0.2
     */
    public static void handleIgnoreGCNames(@NotNull Handler<Set<String>> handler) {
        handler.handle(ignoreGCNames);
    }

    /**
     * @since 4.0.2
     */
    public static GCStatResult parseGarbageCollectorMXBeans(@NotNull List<GarbageCollectorMXBean> gcList) {
        GCStatResult gcStatResult = new GCStatResult();
        for (GarbageCollectorMXBean gc : gcList) {
            if (gc == null) {
                continue;
            }
            gcStatResult.refreshWithGC(gc);
        }
        return gcStatResult;
    }

    public long getMinorGCCount() {
        return minorGCCount;
    }

    public long getMinorGCTime() {
        return minorGCTime;
    }

    public long getMajorGCCount() {
        return majorGCCount;
    }

    public long getMajorGCTime() {
        return majorGCTime;
    }

    /**
     * @since 4.0.2
     */
    @Nullable
    public String getMajorGCType() {
        return majorGCType;
    }

    /**
     * @since 4.0.2
     */
    @Nullable
    public String getMinorGCType() {
        return minorGCType;
    }

    public long getStatTime() {
        return statTime;
    }

    @Override
    public GCStatResult since(GCStatResult start) {
        GCStatResult x = new GCStatResult(getStatTime());
        x.majorGCCount = getMajorGCCount() - start.getMajorGCCount();
        x.minorGCCount = getMinorGCCount() - start.getMinorGCCount();
        x.majorGCTime = getMajorGCTime() - start.getMajorGCTime();
        x.minorGCTime = getMinorGCTime() - start.getMinorGCTime();
        x.majorGCType = this.majorGCType;
        x.minorGCType = this.minorGCType;
        return x;
    }

    @Override
    public JsonObject toJsonObject() {
        return new JsonObject()
                .put("stat_time", getStatTime())
                .put("major", new JsonObject()
                        .put("count", getMajorGCCount())
                        .put("time", getMajorGCTime())
                        .put("type", getMajorGCType())
                )
                .put("minor", new JsonObject()
                        .put("count", getMinorGCCount())
                        .put("time", getMinorGCTime())
                        .put("type", getMinorGCType())
                );
    }

    /**
     * @since 3.1.4
     * @since 4.0.2 refine
     */
    private void refreshWithGC(@NotNull GarbageCollectorMXBean gc) {
        if (minorGCNames.contains(gc.getName())) {
            this.minorGCCount = gc.getCollectionCount();
            if (gc.getCollectionTime() >= 0) {
                this.minorGCTime = gc.getCollectionTime();
            }
            this.minorGCType = gc.getName();
        } else if (majorGCNames.contains(gc.getName())) {
            this.majorGCCount = gc.getCollectionCount();
            if (gc.getCollectionTime() >= 0) {
                this.majorGCTime = gc.getCollectionTime();
            }
            this.majorGCType = gc.getName();
        } else if (!ignoreGCNames.contains(gc.getName())) {
            Keel.getRecorderFactory().createEventRecorder(getClass().getName()).error(log -> log
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
}
