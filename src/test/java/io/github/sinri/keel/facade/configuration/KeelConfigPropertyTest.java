package io.github.sinri.keel.facade.configuration;

import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
class KeelConfigPropertyTest extends KeelJUnit5Test {

    public KeelConfigPropertyTest(Vertx vertx) {
        super(vertx);
    }
    
    @Test
    void testConstructor() {
        KeelConfigProperty property = new KeelConfigProperty();
        assertEquals("=", property.toString(), "New instance should have empty keychain and empty value");
    }

    @Test
    void testSetKeychain() {
        KeelConfigProperty property = new KeelConfigProperty();
        property.setKeychain(Arrays.asList("a", "b", "c"));
        assertEquals("a.b.c=", property.toString(), "Keychain should be set and joined with dots");

        // Test with empty list
        property = new KeelConfigProperty();
        property.setKeychain(Collections.emptyList());
        assertEquals("=", property.toString(), "Empty keychain should result in empty string before equals");
    }

    @Test
    void testAddToKeychain() {
        KeelConfigProperty property = new KeelConfigProperty();
        property.addToKeychain("x")
                .addToKeychain("y")
                .addToKeychain("z");
        assertEquals("x.y.z=", property.toString(), "Keys should be added to chain and joined with dots");
    }

    @Test
    void testSetValue() {
        KeelConfigProperty property = new KeelConfigProperty();
        
        // Test with normal string
        property.setValue("test");
        assertEquals("=test", property.toString(), "Value should be set after equals sign");

        // Test with null value
        property.setValue(null);
        assertEquals("=", property.toString(), "Null value should be converted to empty string");

        // Test with empty string
        property.setValue("");
        assertEquals("=", property.toString(), "Empty string value should remain empty");
    }

    @Test
    void testCompleteUsage() {
        KeelConfigProperty property = new KeelConfigProperty();
        property.setKeychain(Arrays.asList("database", "mysql"))
                .addToKeychain("host")
                .setValue("localhost");
        assertEquals("database.mysql.host=localhost", property.toString(), 
            "Complete property should format correctly with keychain and value");
    }
}