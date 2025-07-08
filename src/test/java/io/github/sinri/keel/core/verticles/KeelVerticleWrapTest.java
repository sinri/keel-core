package io.github.sinri.keel.core.verticles;

import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.github.sinri.keel.facade.KeelInstance.Keel;
import static org.junit.jupiter.api.Assertions.*;

class KeelVerticleWrapTest extends KeelUnitTest {

    private AtomicBoolean started;
    private AtomicBoolean stopped;
    private AtomicInteger callCount;
    private AtomicReference<Exception> lastException;

    @BeforeEach
    @Override
    public void setUp() {
        started = new AtomicBoolean(false);
        stopped = new AtomicBoolean(false);
        callCount = new AtomicInteger(0);
        lastException = new AtomicReference<>();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testConstructorWithSupplier() {
        // 测试使用 Supplier 构造函数
        Supplier<Future<Void>> supplier = () -> {
            started.set(true);
            callCount.incrementAndGet();
            return Future.succeededFuture();
        };

        KeelVerticleWrap verticle = new KeelVerticleWrap(supplier);
        
        assertNotNull(verticle);
        assertFalse(started.get());
        assertEquals(0, callCount.get());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testConstructorWithFunction() {
        // 测试使用 Function 构造函数
        Function<Promise<Void>, Future<Void>> function = stopPromise -> {
            started.set(true);
            callCount.incrementAndGet();
            return Future.succeededFuture();
        };

        KeelVerticleWrap verticle = new KeelVerticleWrap(function);
        
        assertNotNull(verticle);
        assertFalse(started.get());
        assertEquals(0, callCount.get());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testSuccessfulDeploymentWithSupplier() {
        Supplier<Future<Void>> supplier = () -> {
            started.set(true);
            callCount.incrementAndGet();
            return Future.succeededFuture();
        };

        KeelVerticleWrap verticle = new KeelVerticleWrap(supplier);
        
        Future<Void> testFuture = verticle.deployMe(new DeploymentOptions())
                .compose(deploymentId -> {
                    assertNotNull(deploymentId);
                    assertFalse(deploymentId.isEmpty());
                    assertTrue(started.get());
                    assertEquals(1, callCount.get());
                    assertEquals(deploymentId, verticle.deploymentID());
                    
                    return verticle.undeployMe();
                });
        
        async(testFuture);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testSuccessfulDeploymentWithFunction() {
        Function<Promise<Void>, Future<Void>> function = stopPromise -> {
            started.set(true);
            callCount.incrementAndGet();
            return Future.succeededFuture();
        };

        KeelVerticleWrap verticle = new KeelVerticleWrap(function);
        
        Future<Void> testFuture = verticle.deployMe(new DeploymentOptions())
                .compose(deploymentId -> {
                    assertNotNull(deploymentId);
                    assertFalse(deploymentId.isEmpty());
                    assertTrue(started.get());
                    assertEquals(1, callCount.get());
                    assertEquals(deploymentId, verticle.deploymentID());
                    
                    return verticle.undeployMe();
                });
        
        async(testFuture);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testFailedDeploymentWithSupplier() {
        RuntimeException testException = new RuntimeException("Test failure");
        Supplier<Future<Void>> supplier = () -> {
            started.set(true);
            callCount.incrementAndGet();
            return Future.failedFuture(testException);
        };

        KeelVerticleWrap verticle = new KeelVerticleWrap(supplier);
        
        Future<Void> testFuture = verticle.deployMe(new DeploymentOptions())
                .compose(deploymentId -> {
                    fail("Deployment should have failed");
                    return Future.succeededFuture();
                })
                .recover(throwable -> {
                    assertTrue(started.get());
                    assertEquals(1, callCount.get());
                    assertEquals(testException, throwable);
                    return Future.succeededFuture();
                })
                .compose(v->{
                    return Future.succeededFuture();
                });
        
        async(testFuture);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testVerticleConfig() {
        JsonObject config = new JsonObject()
                .put("test.property", "test.value")
                .put("number", 42);

        Supplier<Future<Void>> supplier = () -> {
            started.set(true);
            return Future.succeededFuture();
        };

        KeelVerticleWrap verticle = new KeelVerticleWrap(supplier);
        
        Future<Void> testFuture = verticle.deployMe(new DeploymentOptions().setConfig(config))
                .compose(deploymentId -> {
                    JsonObject retrievedConfig = verticle.config();
                    assertNotNull(retrievedConfig);
                    assertEquals("test.value", retrievedConfig.getString("test.property"));
                    assertEquals(42, retrievedConfig.getInteger("number"));
                    
                    return verticle.undeployMe();
                });
        
        async(testFuture);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testGetVerticleInfo() {
        JsonObject config = new JsonObject().put("test", "value");
        
        Supplier<Future<Void>> supplier = () -> {
            started.set(true);
            return Future.succeededFuture();
        };

        KeelVerticleWrap verticle = new KeelVerticleWrap(supplier);
        
        Future<Void> testFuture = verticle.deployMe(new DeploymentOptions().setConfig(config))
                .compose(deploymentId -> {
                    JsonObject info = verticle.getVerticleInfo();
                    assertNotNull(info);
                    assertEquals(KeelVerticleWrap.class.getName(), info.getString("class"));
                    assertEquals(config, info.getJsonObject("config"));
                    assertEquals(deploymentId, info.getString("deployment_id"));
                    
                    return verticle.undeployMe();
                });
        
        async(testFuture);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testStopperPromiseWithFunction() {
        AtomicReference<Promise<Void>> stopperPromiseRef = new AtomicReference<>();
        
        Function<Promise<Void>, Future<Void>> function = stopPromise -> {
            started.set(true);
            callCount.incrementAndGet();
            stopperPromiseRef.set(stopPromise);
            return Future.succeededFuture();
        };

        KeelVerticleWrap verticle = new KeelVerticleWrap(function);
        
        Future<Void> testFuture = verticle.deployMe(new DeploymentOptions())
                .compose(deploymentId -> {
                    assertNotNull(deploymentId);
                    assertTrue(started.get());
                    assertEquals(1, callCount.get());
                    assertNotNull(stopperPromiseRef.get());
                    
                    // 触发停止
                    stopperPromiseRef.get().complete();
                    
                    // 给一些时间让异步操作完成
                    return Keel.asyncSleep(100L);
                });
        
        async(testFuture);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testAsyncStartOperation() {
        Supplier<Future<Void>> supplier = () -> {
            // 模拟异步启动操作
            return Keel.asyncSleep(100L)
                    .compose(v -> {
                        started.set(true);
                        return Future.succeededFuture();
                    });
        };

        KeelVerticleWrap verticle = new KeelVerticleWrap(supplier);
        
        Future<Void> testFuture = verticle.deployMe(new DeploymentOptions())
                .compose(deploymentId -> {
                    assertTrue(started.get());
                    return verticle.undeployMe();
                });
        
        async(testFuture);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testMultipleDeployments() {
        AtomicInteger deploymentCount = new AtomicInteger(0);
        
        Supplier<Future<Void>> supplier = () -> {
            deploymentCount.incrementAndGet();
            return Future.succeededFuture();
        };

        KeelVerticleWrap verticle1 = new KeelVerticleWrap(supplier);
        KeelVerticleWrap verticle2 = new KeelVerticleWrap(supplier);
        
        Future<Void> testFuture = verticle1.deployMe(new DeploymentOptions())
                .compose(deploymentId1 -> {
                    return verticle2.deployMe(new DeploymentOptions())
                            .compose(deploymentId2 -> {
                                assertNotEquals(deploymentId1, deploymentId2);
                                assertEquals(2, deploymentCount.get());
                                
                                return verticle1.undeployMe()
                                        .compose(v -> verticle2.undeployMe());
                            });
                });
        
        async(testFuture);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testExceptionDuringStop() {
        AtomicReference<Promise<Void>> stopperPromiseRef = new AtomicReference<>();
        
        Function<Promise<Void>, Future<Void>> function = stopPromise -> {
            started.set(true);
            stopperPromiseRef.set(stopPromise);
            return Future.succeededFuture();
        };

        KeelVerticleWrap verticle = new KeelVerticleWrap(function);
        
        Future<Void> testFuture = verticle.deployMe(new DeploymentOptions())
                .compose(deploymentId -> {
                    assertTrue(started.get());
                    
                    // 触发失败的停止操作
                    stopperPromiseRef.get().fail(new RuntimeException("Stop failed"));
                    
                    // 给一些时间让异步操作完成并记录错误
                    return Keel.asyncSleep(200L);
                })
                .compose(v -> {
                    // 即使停止失败，也应该能够正常继续
                    return Future.succeededFuture();
                });
        
        async(testFuture);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testNullConfigHandling() {
        Supplier<Future<Void>> supplier = () -> {
            started.set(true);
            return Future.succeededFuture();
        };

        KeelVerticleWrap verticle = new KeelVerticleWrap(supplier);
        
        Future<Void> testFuture = verticle.deployMe(new DeploymentOptions())
                .compose(deploymentId -> {
                    JsonObject config = verticle.config();
                    assertNotNull(config);
                    assertTrue(config.isEmpty());
                    
                    return verticle.undeployMe();
                });
        
        async(testFuture);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testStaticInstantMethods() {
        // 测试静态工厂方法
        AtomicBoolean supplierCalled = new AtomicBoolean(false);
        AtomicBoolean functionCalled = new AtomicBoolean(false);
        
        Supplier<Future<Void>> supplier = () -> {
            supplierCalled.set(true);
            return Future.succeededFuture();
        };
        
        Function<Promise<Void>, Future<Void>> function = stopPromise -> {
            functionCalled.set(true);
            return Future.succeededFuture();
        };
        
        KeelVerticle verticle1 = KeelVerticle.instant(supplier);
        KeelVerticle verticle2 = KeelVerticle.instant(function);
        
        assertNotNull(verticle1);
        assertNotNull(verticle2);
        assertTrue(verticle1 instanceof KeelVerticleWrap);
        assertTrue(verticle2 instanceof KeelVerticleWrap);
        
        Future<Void> testFuture = verticle1.deployMe(new DeploymentOptions())
                .compose(deploymentId1 -> {
                    assertTrue(supplierCalled.get());
                    return verticle2.deployMe(new DeploymentOptions())
                            .compose(deploymentId2 -> {
                                assertTrue(functionCalled.get());
                                return verticle1.undeployMe()
                                        .compose(v -> verticle2.undeployMe());
                            });
                });
        
        async(testFuture);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testLongRunningStartOperation() {
        Supplier<Future<Void>> supplier = () -> {
            // 模拟长时间运行的启动操作
            return Keel.asyncSleep(500L)
                    .compose(v -> {
                        started.set(true);
                        callCount.incrementAndGet();
                        return Future.succeededFuture();
                    });
        };

        KeelVerticleWrap verticle = new KeelVerticleWrap(supplier);
        
        long startTime = System.currentTimeMillis();
        Future<Void> testFuture = verticle.deployMe(new DeploymentOptions())
                .compose(deploymentId -> {
                    long endTime = System.currentTimeMillis();
                    assertTrue(endTime - startTime >= 500L);
                    assertTrue(started.get());
                    assertEquals(1, callCount.get());
                    
                    return verticle.undeployMe();
                });
        
        async(testFuture);
    }
}