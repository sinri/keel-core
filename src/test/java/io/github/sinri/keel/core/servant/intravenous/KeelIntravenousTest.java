package io.github.sinri.keel.core.servant.intravenous;

import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.sinri.keel.facade.KeelInstance.Keel;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
class KeelIntravenousTest extends KeelJUnit5Test {

    public KeelIntravenousTest(Vertx vertx) {
        super(vertx);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testSingleDropProcessor(VertxTestContext testContext) {
        Checkpoint cp1 = testContext.checkpoint();
        Checkpoint cp2 = testContext.checkpoint();
        Checkpoint cp3 = testContext.checkpoint();

        List<String> processedItems = new ArrayList<>();
        AtomicInteger processCount = new AtomicInteger(0);

        // 创建单滴处理器
        KeelIntravenous<String> intravenous = KeelIntravenous.instant(drop -> {
            getUnitTestLogger().info("Processing single drop: " + drop);
            processedItems.add(drop);
            processCount.incrementAndGet();
            return Future.succeededFuture();
        });

        // 部署静脉注射器
        intravenous.deployMe(new DeploymentOptions())
                   .compose(deploymentId -> {
                       getUnitTestLogger().info("Intravenous deployed with ID: " + deploymentId);
                       cp1.flag();

                       // 添加多个项目
                       intravenous.add("item1");
                       intravenous.add("item2");
                       intravenous.add("item3");

                       // 等待处理完成
                       return Keel.asyncSleep(2000L);
                   })
                   .compose(v -> {
                       // 验证处理结果
                       assertEquals(3, processedItems.size());
                       assertTrue(processedItems.contains("item1"));
                       assertTrue(processedItems.contains("item2"));
                       assertTrue(processedItems.contains("item3"));
                       assertEquals(3, processCount.get());

                       getUnitTestLogger().info("Single drop processor test completed successfully");
                       cp2.flag();

                       // 关闭静脉注射器
                       intravenous.shutdown();
                       return intravenous.undeployMe();
                   })
                   .onComplete(ar -> {
                       if (ar.succeeded()) {
                           cp3.flag();
                       } else {
                           testContext.failNow(ar.cause());
                       }
                   });
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testBatchProcessor(VertxTestContext testContext) {
        Checkpoint cp1 = testContext.checkpoint();
        Checkpoint cp2 = testContext.checkpoint();
        Checkpoint cp3 = testContext.checkpoint();

        List<List<String>> processedBatches = new ArrayList<>();
        AtomicInteger batchCount = new AtomicInteger(0);

        // 创建批量处理器
        KeelIntravenous<String> intravenous = KeelIntravenous.instantBatch(drops -> {
            getUnitTestLogger().info("Processing batch of " + drops.size() + " drops: " + drops);
            processedBatches.add(new ArrayList<>(drops));
            batchCount.incrementAndGet();
            return Future.succeededFuture();
        });

        // 部署静脉注射器
        intravenous.deployMe(new DeploymentOptions())
                   .compose(deploymentId -> {
                       getUnitTestLogger().info("Batch intravenous deployed with ID: " + deploymentId);
                       cp1.flag();

                       // 添加多个项目
                       intravenous.add("batch1");
                       intravenous.add("batch2");
                       intravenous.add("batch3");
                       intravenous.add("batch4");

                       // 等待处理完成
                       return Keel.asyncSleep(2000L);
                   })
                   .compose(v -> {
                       // 验证处理结果
                       assertTrue(batchCount.get() > 0);
                       assertTrue(processedBatches.size() > 0);

                       // 验证所有项目都被处理
                       List<String> allProcessed = new ArrayList<>();
                       for (List<String> batch : processedBatches) {
                           allProcessed.addAll(batch);
                       }

                       assertTrue(allProcessed.contains("batch1"));
                       assertTrue(allProcessed.contains("batch2"));
                       assertTrue(allProcessed.contains("batch3"));
                       assertTrue(allProcessed.contains("batch4"));

                       getUnitTestLogger().info("Batch processor test completed successfully");
                       cp2.flag();

                       // 关闭静脉注射器
                       intravenous.shutdown();
                       return intravenous.undeployMe();
                   })
                   .onComplete(ar -> {
                       if (ar.succeeded()) {
                           cp3.flag();
                       } else {
                           testContext.failNow(ar.cause());
                       }
                   });
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testStateManagement(VertxTestContext testContext) {
        Checkpoint cp1 = testContext.checkpoint();
        Checkpoint cp2 = testContext.checkpoint();
        Checkpoint cp3 = testContext.checkpoint();
        Checkpoint cp4 = testContext.checkpoint();

        KeelIntravenous<String> intravenous = KeelIntravenous.instant(drop -> {
            getUnitTestLogger().info("Processing: " + drop);
            return Future.succeededFuture();
        });

        // 初始状态检查
        assertFalse(intravenous.isStopped());
        assertFalse(intravenous.isUndeployed());
        assertTrue(intravenous.isNoDropsLeft());
        cp1.flag();

        intravenous.deployMe(new DeploymentOptions())
                   .compose(deploymentId -> {
                       // 部署后状态检查
                       assertFalse(intravenous.isStopped());
                       assertFalse(intravenous.isUndeployed());
                       assertTrue(intravenous.isNoDropsLeft());

                       // 添加项目
                       intravenous.add("test");

                       // 添加后状态检查
                       assertFalse(intravenous.isNoDropsLeft());
                       cp2.flag();

                       // 关闭静脉注射器
                       intravenous.shutdown();
                       assertTrue(intravenous.isStopped());

                       return Keel.asyncSleep(1000L);
                   })
                   .compose(v -> {
                       // 等待处理完成后状态检查
                       assertTrue(intravenous.isNoDropsLeft());
                       cp3.flag();

                       // 安全地卸载，避免 "Unknown deployment" 错误
                       return intravenous.undeployMe()
                                         .recover(throwable -> {
                                             if (throwable instanceof IllegalStateException &&
                                                     throwable.getMessage().contains("Unknown deployment")) {
                                                 getUnitTestLogger().info("Verticle already undeployed");
                                                 return Future.succeededFuture();
                                             }
                                             return Future.failedFuture(throwable);
                                         });
                   })
                   .onComplete(ar -> {
                       if (ar.succeeded()) {
                        // 卸载后状态检查
                        assertTrue(intravenous.isUndeployed());
                        
                        getUnitTestLogger().info("State management test completed successfully");
                           cp4.flag();
                       } else {
                           testContext.failNow(ar.cause());
                       }
                   });
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testExceptionHandling(VertxTestContext testContext) {
        Checkpoint cp1 = testContext.checkpoint();
        Checkpoint cp2 = testContext.checkpoint();

        AtomicInteger processCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        // 创建会抛出异常的单滴处理器
        KeelIntravenous<String> intravenous = KeelIntravenous.instant(drop -> {
            processCount.incrementAndGet();
            getUnitTestLogger().info("Processing with exception: " + drop);

            if ("error".equals(drop)) {
                errorCount.incrementAndGet();
                return Future.failedFuture(new RuntimeException("Test exception"));
            }
            return Future.succeededFuture();
        });

        intravenous.deployMe(new DeploymentOptions())
                   .compose(deploymentId -> {
                       // 添加正常项目和异常项目
                       intravenous.add("normal1");
                       intravenous.add("error");
                       intravenous.add("normal2");

                       return Keel.asyncSleep(2000L);
                   })
                   .compose(v -> {
                       // 验证异常被正确处理（默认情况下异常被忽略，但处理计数应该正确）
                       assertEquals(3, processCount.get());
                       assertEquals(1, errorCount.get());

                       getUnitTestLogger().info("Exception handling test completed successfully");
                       cp1.flag();

                       intravenous.shutdown();
                       return intravenous.undeployMe();
                   })
                   .onComplete(ar -> {
                       if (ar.succeeded()) {
                           cp2.flag();
                       } else {
                           testContext.failNow(ar.cause());
                       }
                   });
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testAddAfterShutdown(VertxTestContext testContext) {
        Checkpoint cp1 = testContext.checkpoint();
        Checkpoint cp2 = testContext.checkpoint();

        KeelIntravenous<String> intravenous = KeelIntravenous.instant(drop -> {
            getUnitTestLogger().info("Processing: " + drop);
            return Future.succeededFuture();
        });

        intravenous.deployMe(new DeploymentOptions())
                   .compose(deploymentId -> {
                       // 正常添加
                       intravenous.add("normal");

                       // 关闭静脉注射器
                       intravenous.shutdown();

                       // 尝试在关闭后添加项目，应该抛出异常
                       assertThrows(IllegalStateException.class, () -> {
                           intravenous.add("after_shutdown");
                       });

                       getUnitTestLogger().info("Add after shutdown test completed successfully");
                       cp1.flag();

                       return intravenous.undeployMe();
                   })
                   .onComplete(ar -> {
                       if (ar.succeeded()) {
                           cp2.flag();
                       } else {
                           testContext.failNow(ar.cause());
                       }
                   });
    }

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void testConcurrentProcessing(VertxTestContext testContext) {
        Checkpoint cp1 = testContext.checkpoint();
        Checkpoint cp2 = testContext.checkpoint();

        List<String> processedItems = new ArrayList<>();
        AtomicInteger processCount = new AtomicInteger(0);

        KeelIntravenous<String> intravenous = KeelIntravenous.instant(drop -> {
            getUnitTestLogger().info("Processing concurrent drop: " + drop);
            synchronized (processedItems) {
                processedItems.add(drop);
            }
            processCount.incrementAndGet();
            return Future.succeededFuture();
        });

        intravenous.deployMe(new DeploymentOptions())
                   .compose(deploymentId -> {
                       // 并发添加多个项目
                       for (int i = 0; i < 10; i++) {
                           intravenous.add("concurrent_" + i);
                       }

                       return Keel.asyncSleep(3000L);
                   })
                   .compose(v -> {
                       // 验证所有项目都被处理
                       assertEquals(10, processedItems.size());
                       assertEquals(10, processCount.get());

                       for (int i = 0; i < 10; i++) {
                           assertTrue(processedItems.contains("concurrent_" + i));
                       }

                       getUnitTestLogger().info("Concurrent processing test completed successfully");
                       cp1.flag();

                       intravenous.shutdown();
                       return intravenous.undeployMe();
                   })
                   .onComplete(ar -> {
                       if (ar.succeeded()) {
                           cp2.flag();
                       } else {
                           testContext.failNow(ar.cause());
                       }
                   });
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testBatchProcessorWithLargeBatch(VertxTestContext testContext) {
        Checkpoint cp1 = testContext.checkpoint();
        Checkpoint cp2 = testContext.checkpoint();

        List<List<String>> processedBatches = new ArrayList<>();
        AtomicInteger totalProcessed = new AtomicInteger(0);

        KeelIntravenous<String> intravenous = KeelIntravenous.instantBatch(drops -> {
            getUnitTestLogger().info("Processing large batch of " + drops.size() + " drops");
            processedBatches.add(new ArrayList<>(drops));
            totalProcessed.addAndGet(drops.size());
            return Future.succeededFuture();
        });

        intravenous.deployMe(new DeploymentOptions())
                   .compose(deploymentId -> {
                       // 添加大量项目
                       for (int i = 0; i < 50; i++) {
                           intravenous.add("large_batch_" + i);
                       }

                       return Keel.asyncSleep(3000L);
                   })
                   .compose(v -> {
                       // 验证所有项目都被处理
                       assertEquals(50, totalProcessed.get());
                       assertTrue(processedBatches.size() > 0);

                       // 验证所有项目都在批次中
                       List<String> allProcessed = new ArrayList<>();
                       for (List<String> batch : processedBatches) {
                           allProcessed.addAll(batch);
                       }

                       for (int i = 0; i < 50; i++) {
                           assertTrue(allProcessed.contains("large_batch_" + i));
                       }

                       getUnitTestLogger().info("Large batch processor test completed successfully");
                       cp1.flag();

                       intravenous.shutdown();
                       return intravenous.undeployMe();
                   })
                   .onComplete(ar -> {
                       if (ar.succeeded()) {
                           cp2.flag();
                       } else {
                           testContext.failNow(ar.cause());
                       }
                   });
    }
}