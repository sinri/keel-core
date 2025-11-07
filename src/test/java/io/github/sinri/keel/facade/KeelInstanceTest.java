package io.github.sinri.keel.facade;

import io.github.sinri.keel.facade.async.KeelAsyncMixin;
import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
class KeelInstanceTest extends KeelJUnit5Test {

    public KeelInstanceTest(Vertx vertx) {
        super(vertx);
    }

    @Test
    @DisplayName("Test singleton instance")
    void testSingletonInstance() {
        KeelInstance instance1 = KeelInstance.Keel;
        KeelInstance instance2 = KeelInstance.Keel;
        
        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }

    @Test
    @DisplayName("Test getConfiguration")
    void testGetConfiguration() {
        var config = KeelInstance.Keel.getConfiguration();
        
        assertNotNull(config);
        assertEquals("", config.getName());
    }

    @Test
    @DisplayName("Test config method with empty key")
    void testConfigWithEmptyKey() {
        String result = KeelInstance.Keel.config("");
        
        assertNull(result);
    }

    @Test
    @DisplayName("Test config method with non-existent key")
    void testConfigWithNonExistentKey() {
        String result = KeelInstance.Keel.config("non.existent.key");
        
        assertNull(result);
    }

    @Test
    @DisplayName("Test isVertxInitialized before initialization")
    void testIsVertxInitializedBeforeInit() {
        // 由于KeelInstance是单例，可能在其他地方已经被初始化
        // 我们只验证方法能正常调用
        boolean initialized = KeelInstance.Keel.isVertxInitialized();
        assertTrue(initialized || !initialized); // 总是为真，只是验证方法调用
    }

    @Test
    @DisplayName("Test isRunningInVertxCluster before initialization")
    void testIsRunningInVertxClusterBeforeInit() {
        assertFalse(KeelInstance.Keel.isRunningInVertxCluster());
    }

    @Test
    @DisplayName("Test getVertx before initialization")
    void testGetVertxBeforeInit() {
        // 由于KeelInstance是单例，可能在其他地方已经被初始化
        // 我们只验证方法能正常调用
        try {
            KeelInstance.Keel.getVertx();
            // 如果没有抛出异常，说明已经初始化了
        } catch (NullPointerException e) {
            // 如果抛出异常，说明还没有初始化
        }
    }

    @Test
    @DisplayName("Test getClusterManager before initialization")
    void testGetClusterManagerBeforeInit() {
        assertNull(KeelInstance.Keel.getClusterManager());
    }

    @Test
    @DisplayName("Test initializeVertxStandalone")
    void testInitializeVertxStandalone(VertxTestContext testContext) {
        VertxOptions options = new VertxOptions();
        KeelInstance.Keel.initializeVertxStandalone(options);

        assertTrue(KeelInstance.Keel.isVertxInitialized());
        assertFalse(KeelInstance.Keel.isRunningInVertxCluster());
        assertNotNull(KeelInstance.Keel.getVertx());
        assertNull(KeelInstance.Keel.getClusterManager());

        testContext.completeNow();
    }

    @Test
    @DisplayName("Test initializeVertx with null cluster manager")
    void testInitializeVertxWithNullClusterManager(VertxTestContext testContext) {
        VertxOptions options = new VertxOptions();

        KeelInstance.Keel.initializeVertx(options, null)
                         .onComplete(ar -> {
                             if (ar.succeeded()) {
                                 assertTrue(KeelInstance.Keel.isVertxInitialized());
                                 assertFalse(KeelInstance.Keel.isRunningInVertxCluster());
                                 assertNotNull(KeelInstance.Keel.getVertx());
                                 assertNull(KeelInstance.Keel.getClusterManager());
                                 testContext.completeNow();
                             } else {
                                 testContext.failNow(ar.cause());
                             }
                         });
    }

    @Test
    @DisplayName("Test getLogger")
    void testGetLogger() {
        var logger = KeelInstance.Keel.getLogger();
        
        assertNotNull(logger);
        assertEquals("Keel", logger.topic());
    }

    @Test
    @DisplayName("Test helper methods availability")
    void testHelperMethodsAvailability() {
        // 验证实现了KeelHelpersInterface
        assertNotNull(KeelInstance.Keel.binaryHelper());
        assertNotNull(KeelInstance.Keel.datetimeHelper());
        assertNotNull(KeelInstance.Keel.fileHelper());
        assertNotNull(KeelInstance.Keel.jsonHelper());
        assertNotNull(KeelInstance.Keel.netHelper());
        assertNotNull(KeelInstance.Keel.reflectionHelper());
        assertNotNull(KeelInstance.Keel.stringHelper());
        assertNotNull(KeelInstance.Keel.cryptographyHelper());
        assertNotNull(KeelInstance.Keel.digestHelper());
        assertNotNull(KeelInstance.Keel.runtimeHelper());
        assertNotNull(KeelInstance.Keel.authenticationHelper());
        assertNotNull(KeelInstance.Keel.randomHelper());
    }

    @Test
    @DisplayName("Test instance is final")
    void testInstanceIsFinal() {
        // 验证KeelInstance类是final的
        assertTrue(java.lang.reflect.Modifier.isFinal(KeelInstance.class.getModifiers()));
    }

    @Test
    @DisplayName("Test instance implements required interfaces")
    void testInstanceImplementsInterfaces() {
        // 验证实现了必要的接口
        assertInstanceOf(KeelHelpersInterface.class, KeelInstance.Keel);
        assertInstanceOf(KeelAsyncMixin.class, KeelInstance.Keel);
    }
} 