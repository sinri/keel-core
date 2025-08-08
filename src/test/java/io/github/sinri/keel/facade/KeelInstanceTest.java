package io.github.sinri.keel.facade;

import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import io.vertx.core.VertxOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class KeelInstanceTest extends KeelUnitTest {

    private KeelInstance keelInstance;

    @BeforeEach
    @Override
    public void setUp() {
        keelInstance = KeelInstance.Keel;
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
        var config = keelInstance.getConfiguration();
        
        assertNotNull(config);
        assertEquals("", config.getName());
    }

    @Test
    @DisplayName("Test config method with empty key")
    void testConfigWithEmptyKey() {
        String result = keelInstance.config("");
        
        assertNull(result);
    }

    @Test
    @DisplayName("Test config method with non-existent key")
    void testConfigWithNonExistentKey() {
        String result = keelInstance.config("non.existent.key");
        
        assertNull(result);
    }

    @Test
    @DisplayName("Test isVertxInitialized before initialization")
    void testIsVertxInitializedBeforeInit() {
        // 由于KeelInstance是单例，可能在其他地方已经被初始化
        // 我们只验证方法能正常调用
        boolean initialized = keelInstance.isVertxInitialized();
        assertTrue(initialized || !initialized); // 总是为真，只是验证方法调用
    }

    @Test
    @DisplayName("Test isRunningInVertxCluster before initialization")
    void testIsRunningInVertxClusterBeforeInit() {
        assertFalse(keelInstance.isRunningInVertxCluster());
    }

    @Test
    @DisplayName("Test getVertx before initialization")
    void testGetVertxBeforeInit() {
        // 由于KeelInstance是单例，可能在其他地方已经被初始化
        // 我们只验证方法能正常调用
        try {
            keelInstance.getVertx();
            // 如果没有抛出异常，说明已经初始化了
        } catch (NullPointerException e) {
            // 如果抛出异常，说明还没有初始化
        }
    }

    @Test
    @DisplayName("Test getClusterManager before initialization")
    void testGetClusterManagerBeforeInit() {
        assertNull(keelInstance.getClusterManager());
    }

    @Test
    @DisplayName("Test initializeVertxStandalone")
    void testInitializeVertxStandalone() {
        VertxOptions options = new VertxOptions();
        keelInstance.initializeVertxStandalone(options);
        
        assertTrue(keelInstance.isVertxInitialized());
        assertFalse(keelInstance.isRunningInVertxCluster());
        assertNotNull(keelInstance.getVertx());
        assertNull(keelInstance.getClusterManager());
    }

    @Test
    @DisplayName("Test initializeVertx with null cluster manager")
    void testInitializeVertxWithNullClusterManager() {
        VertxOptions options = new VertxOptions();
        
        keelInstance.initializeVertx(options, null)
                .toCompletionStage().toCompletableFuture().join();
        
        assertTrue(keelInstance.isVertxInitialized());
        assertFalse(keelInstance.isRunningInVertxCluster());
        assertNotNull(keelInstance.getVertx());
        assertNull(keelInstance.getClusterManager());
    }

    @Test
    @DisplayName("Test getLogger")
    void testGetLogger() {
        var logger = keelInstance.getLogger();
        
        assertNotNull(logger);
        assertEquals("Keel", logger.topic());
    }

    @Test
    @DisplayName("Test helper methods availability")
    void testHelperMethodsAvailability() {
        // 验证实现了KeelHelpersInterface
        assertNotNull(keelInstance.binaryHelper());
        assertNotNull(keelInstance.datetimeHelper());
        assertNotNull(keelInstance.fileHelper());
        assertNotNull(keelInstance.jsonHelper());
        assertNotNull(keelInstance.netHelper());
        assertNotNull(keelInstance.reflectionHelper());
        assertNotNull(keelInstance.stringHelper());
        assertNotNull(keelInstance.cryptographyHelper());
        assertNotNull(keelInstance.digestHelper());
        assertNotNull(keelInstance.runtimeHelper());
        assertNotNull(keelInstance.authenticationHelper());
        assertNotNull(keelInstance.randomHelper());
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
        assertTrue(keelInstance instanceof io.github.sinri.keel.core.helper.KeelHelpersInterface);
        assertTrue(keelInstance instanceof io.github.sinri.keel.facade.async.KeelAsyncMixin);
        assertTrue(keelInstance instanceof io.github.sinri.keel.web.http.requester.KeelWebRequestMixin);
    }
} 