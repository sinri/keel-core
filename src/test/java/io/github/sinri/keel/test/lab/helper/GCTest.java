package io.github.sinri.keel.test.lab.helper;

import io.github.sinri.keel.facade.tesuto.instant.InstantRunUnit;
import io.github.sinri.keel.facade.tesuto.instant.KeelInstantRunner;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Arrays;

public class GCTest extends KeelInstantRunner {
    @InstantRunUnit
    public Future<Void> test1() {
        showGC();
        Buffer buffer = Buffer.buffer();
        Buffer buffer2 = Buffer.buffer();
        for (int x = 0; x < 100; x++) {
            for (int i = 0; i < 1024; i++) {
                buffer.appendBytes(new byte[102400]);
                buffer2 = Buffer.buffer("Buffer " + i);
            }
            getIssueRecorder().debug("x=" + x + " > Buffer2 is " + buffer2);
            showGC();
        }
        return Future.succeededFuture();
    }

    private void showGC() {
        StringBuilder sb = new StringBuilder();
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            if (gc == null) {
                continue;
            }
            String gcName = gc.getName();
            String gcClassName = gc.getClass().getName();
            String objectName = gc.getObjectName().toString();
            String[] memoryPoolNames = gc.getMemoryPoolNames();
            long collectionTime = gc.getCollectionTime();
            long collectionCount = gc.getCollectionCount();
            sb.append("GC! gcName=").append(gcName).append(" | gcClassName=").append(gcClassName).append(" | objectName=").append(objectName)
                    .append(" | memoryPoolNames=").append(Arrays.toString(memoryPoolNames))
                    .append(" | collectionTime=").append(collectionTime).append(" | collectionCount=").append(collectionCount)
                    .append("\n");
        }
        getIssueRecorder().info(sb.toString());
    }
}
/*
使用 `GarbageCollectorMXBean` 监控 ZGC（Z Garbage Collector）时，所获得的暂停时间和周期信息具体指的是以下几种 GC 场景：

### ZGC Pauses

在 ZGC 中，"Pauses" 主要指两类非常短暂的全局停顿，它们是：

1. **Initial Mark (IM) 暂停**：
   - **场景**：这是 ZGC 的一次非常短暂的全局暂停，用于标记根对象（如线程栈、类加载器等）。这个阶段确保所有可达的对象都被初步标记。
   - **特点**：通常持续时间极短（微秒级别），因为它只涉及对根集的扫描。

2. **Final Remark (FR) 暂停**：
   - **场景**：发生在并发标记和重定位阶段之后，确保所有可达对象都被正确识别。它会处理任何在并发阶段中发生变化的对象引用。
   - **特点**：同样非常短暂，但可能比 Initial Mark 稍长一些，因为它需要重新检查某些特定的对象引用。

### ZGC Cycles

"ZGC Cycles" 则代表整个 ZGC 回收周期，包括了从开始到结束的所有阶段。一个完整的 ZGC 周期通常包含以下几个阶段：

1. **Initial Mark (IM)**：
   - **描述**：如前所述，这是一个非常短暂的暂停，用于标记根对象。

2. **Concurrent Mark**：
   - **描述**：在这个阶段，ZGC 并发地遍历堆中的对象图，以确定哪些对象是可到达的。应用程序线程在此期间继续运行。

3. **Concurrent Preclean**：
   - **描述**：此阶段进一步清理和准备即将被回收的对象，减少 Final Remark 阶段的工作量。

4. **Final Remark (FR)**：
   - **描述**：再次进行短暂的全局暂停，确保所有可达对象都被正确识别。

5. **Concurrent Relocate**：
   - **描述**：在这个阶段，ZGC 会将不可达的对象移动到新的位置，并更新所有指向这些对象的引用。这也是一个并发操作，应用程序线程可以继续执行。

6. **Concurrent Reset**：
   - **描述**：为下一个 GC 周期做准备，重置内部数据结构。

### 如何区分 Pauses 和 Cycles

- **Pauses** 是指那些非常短暂的全局停顿（Initial Mark 和 Final Remark），它们会在日志或监控工具中显示为具体的暂停事件。
- **Cycles** 是指整个 GC 过程的一次完整循环，包括所有的并发和暂停阶段。每个周期可能会包含多个暂停事件，但总的暂停时间应该非常短。

### 示例代码解释

当你使用 `GarbageCollectorMXBean` 获取 ZGC 的暂停次数和时间时：

```java
long collectionCount = zgcBean.getCollectionCount(); // 总的 GC 次数，对应于 ZGC Cycles
long collectionTime = zgcBean.getCollectionTime();   // 总的 GC 暂停时间，主要由 Initial Mark 和 Final Remark 构成
```

- `getCollectionCount()` 返回的是 ZGC 完整周期的数量，即 ZGC Cycles 的总数。
- `getCollectionTime()` 返回的是所有暂停事件（主要是 Initial Mark 和 Final Remark）累积的时间。

### 总结

通过 `GarbageCollectorMXBean` 获得的 ZGC Pauses 和 ZGC Cycles 分别反映了 ZGC 在垃圾回收过程中不同类型的活动：
- **Pauses** 指的是非常短暂的全局暂停（Initial Mark 和 Final Remark）。
- **Cycles** 指的是整个 ZGC 回收周期，包括所有并发和暂停阶段。

如果你有更多关于 ZGC 或者如何更深入地监控它的疑问，请随时提问！
 */
