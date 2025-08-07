package io.github.sinri.keel.core.helper;

import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class KeelHelpersInterfaceTest extends KeelUnitTest {

    private final KeelHelpersInterface helpers = new KeelHelpersInterface() {};

    @Test
    @DisplayName("Test binaryHelper returns singleton instance")
    void testBinaryHelper() {
        KeelBinaryHelper helper1 = helpers.binaryHelper();
        KeelBinaryHelper helper2 = helpers.binaryHelper();
        
        assertNotNull(helper1);
        assertSame(helper1, helper2);
        assertSame(helper1, KeelBinaryHelper.getInstance());
    }

    @Test
    @DisplayName("Test datetimeHelper returns singleton instance")
    void testDateTimeHelper() {
        KeelDateTimeHelper helper1 = helpers.datetimeHelper();
        KeelDateTimeHelper helper2 = helpers.datetimeHelper();
        
        assertNotNull(helper1);
        assertSame(helper1, helper2);
        assertSame(helper1, KeelDateTimeHelper.getInstance());
    }

    @Test
    @DisplayName("Test fileHelper returns singleton instance")
    void testFileHelper() {
        KeelFileHelper helper1 = helpers.fileHelper();
        KeelFileHelper helper2 = helpers.fileHelper();
        
        assertNotNull(helper1);
        assertSame(helper1, helper2);
        assertSame(helper1, KeelFileHelper.getInstance());
    }

    @Test
    @DisplayName("Test jsonHelper returns singleton instance")
    void testJsonHelper() {
        KeelJsonHelper helper1 = helpers.jsonHelper();
        KeelJsonHelper helper2 = helpers.jsonHelper();
        
        assertNotNull(helper1);
        assertSame(helper1, helper2);
        assertSame(helper1, KeelJsonHelper.getInstance());
    }

    @Test
    @DisplayName("Test netHelper returns singleton instance")
    void testNetHelper() {
        KeelNetHelper helper1 = helpers.netHelper();
        KeelNetHelper helper2 = helpers.netHelper();
        
        assertNotNull(helper1);
        assertSame(helper1, helper2);
        assertSame(helper1, KeelNetHelper.getInstance());
    }

    @Test
    @DisplayName("Test reflectionHelper returns singleton instance")
    void testReflectionHelper() {
        KeelReflectionHelper helper1 = helpers.reflectionHelper();
        KeelReflectionHelper helper2 = helpers.reflectionHelper();
        
        assertNotNull(helper1);
        assertSame(helper1, helper2);
        assertSame(helper1, KeelReflectionHelper.getInstance());
    }

    @Test
    @DisplayName("Test stringHelper returns singleton instance")
    void testStringHelper() {
        KeelStringHelper helper1 = helpers.stringHelper();
        KeelStringHelper helper2 = helpers.stringHelper();
        
        assertNotNull(helper1);
        assertSame(helper1, helper2);
        assertSame(helper1, KeelStringHelper.getInstance());
    }

    @Test
    @DisplayName("Test cryptographyHelper returns singleton instance")
    void testCryptographyHelper() {
        KeelCryptographyHelper helper1 = helpers.cryptographyHelper();
        KeelCryptographyHelper helper2 = helpers.cryptographyHelper();
        
        assertNotNull(helper1);
        assertSame(helper1, helper2);
        assertSame(helper1, KeelCryptographyHelper.getInstance());
    }

    @Test
    @DisplayName("Test digestHelper returns singleton instance")
    void testDigestHelper() {
        KeelDigestHelper helper1 = helpers.digestHelper();
        KeelDigestHelper helper2 = helpers.digestHelper();
        
        assertNotNull(helper1);
        assertSame(helper1, helper2);
        assertSame(helper1, KeelDigestHelper.getInstance());
    }

    @Test
    @DisplayName("Test runtimeHelper returns singleton instance")
    void testRuntimeHelper() {
        KeelRuntimeHelper helper1 = helpers.runtimeHelper();
        KeelRuntimeHelper helper2 = helpers.runtimeHelper();
        
        assertNotNull(helper1);
        assertSame(helper1, helper2);
        assertSame(helper1, KeelRuntimeHelper.getInstance());
    }

    @Test
    @DisplayName("Test authenticationHelper returns singleton instance")
    void testAuthenticationHelper() {
        KeelAuthenticationHelper helper1 = helpers.authenticationHelper();
        KeelAuthenticationHelper helper2 = helpers.authenticationHelper();
        
        assertNotNull(helper1);
        assertSame(helper1, helper2);
        assertSame(helper1, KeelAuthenticationHelper.getInstance());
    }

    @Test
    @DisplayName("Test randomHelper returns singleton instance")
    void testRandomHelper() {
        KeelRandomHelper helper1 = helpers.randomHelper();
        KeelRandomHelper helper2 = helpers.randomHelper();
        
        assertNotNull(helper1);
        assertSame(helper1, helper2);
        assertSame(helper1, KeelRandomHelper.getInstance());
    }

    @Test
    @DisplayName("Test all helpers return different instances")
    void testAllHelpersAreDifferent() {
        // 验证所有helper返回的是不同类型的实例
        assertNotSame(helpers.binaryHelper(), helpers.datetimeHelper());
        assertNotSame(helpers.fileHelper(), helpers.jsonHelper());
        assertNotSame(helpers.netHelper(), helpers.reflectionHelper());
        assertNotSame(helpers.stringHelper(), helpers.cryptographyHelper());
        assertNotSame(helpers.digestHelper(), helpers.runtimeHelper());
        assertNotSame(helpers.authenticationHelper(), helpers.randomHelper());
    }

    @Test
    @DisplayName("Test interface is functional")
    void testInterfaceIsFunctional() {
        // 验证接口可以正常实例化
        assertNotNull(helpers);
        assertTrue(helpers instanceof KeelHelpersInterface);
    }
} 