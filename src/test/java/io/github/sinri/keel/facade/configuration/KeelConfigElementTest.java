package io.github.sinri.keel.facade.configuration;

import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class KeelConfigElementTest extends KeelUnitTest {
    private KeelConfigElement root;

    @BeforeEach
    @Override
    public void setUp() {
        root = new KeelConfigElement("root");
        root.setValue("root_value");

        KeelConfigElement child1 = new KeelConfigElement("child1");
        child1.setValue("value1");
        root.addChild(child1);

        KeelConfigElement child2 = new KeelConfigElement("child2");
        child2.setValue("123");
        root.addChild(child2);

        KeelConfigElement grandChild = new KeelConfigElement("grandChild");
        grandChild.setValue("true");
        child1.addChild(grandChild);
    }

    @Test
    void testBasicOperations() {
        assertEquals("root", root.getName());
        assertEquals("root_value", root.getValueAsString());
        assertEquals("root_value", root.getValueAsStringElse("default"));
        
        KeelConfigElement child = root.getChild("child1");
        assertNotNull(child);
        assertEquals("value1", child.getValueAsString());
        
        // Test non-existent child
        assertNull(root.getChild("nonexistent"));
        
        // Test remove child
        root.removeChild("child1");
        assertNull(root.getChild("child1"));
    }

    @Test
    void testValueTypeConversions() {
        KeelConfigElement child2 = root.getChild("child2");
        assertNotNull(child2);
        
        // Integer conversions
        assertEquals(123, child2.getValueAsInteger());
        assertEquals(123, child2.getValueAsIntegerElse(0));
        
        // Test with invalid number
        KeelConfigElement invalidNumber = new KeelConfigElement("invalid");
        invalidNumber.setValue("not_a_number");
        assertEquals(0, invalidNumber.getValueAsIntegerElse(0));
        
        // Test null value
        KeelConfigElement nullValue = new KeelConfigElement("null");
        assertNull(nullValue.getValueAsInteger());
        assertEquals(42, nullValue.getValueAsIntegerElse(42));
    }

    @Test
    void testHierarchicalAccess() {
        // Test single level access
        assertEquals("value1", root.readString(List.of("child1")));
        
        // Test multi-level access
        assertEquals("true", root.readString(List.of("child1", "grandChild")));
        
        // Test non-existent path
        assertNull(root.readString(List.of("nonexistent")));
        assertEquals("default", root.readString(List.of("nonexistent"), "default"));
        
        // Test with varargs
        assertEquals("true", root.extract("child1", "grandChild").getValueAsString());
    }

    @Test
    void testJsonConversion() {
        JsonObject json = root.toJsonObject();
        
        assertEquals("root", json.getString("name"));
        assertEquals("root_value", json.getString("value"));
        assertTrue(json.getJsonArray("children").size() > 0);
        
        // Test reconstruction from JSON
        KeelConfigElement reconstructed = KeelConfigElement.fromJsonObject(json);
        assertEquals(root.getName(), reconstructed.getName());
        assertEquals(root.getValueAsString(), reconstructed.getValueAsString());
        assertEquals(root.getChildren().size(), reconstructed.getChildren().size());
    }

    @Test
    void testPropertiesLoading() throws IOException {
        // Create a temporary properties file
        File tempFile = File.createTempFile("test", ".properties");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("app.name=TestApp\n");
            writer.write("app.version=1.0.0\n");
            writer.write("app.settings.debug=true\n");
        }

        KeelConfigElement config = new KeelConfigElement("config");
        config.loadPropertiesFile(tempFile.getAbsolutePath(), StandardCharsets.UTF_8);

        assertEquals("TestApp", config.readString(List.of("app", "name")));
        assertEquals("1.0.0", config.readString(List.of("app", "version")));
        assertEquals("true", config.readString(List.of("app", "settings", "debug")));

        // Clean up
        tempFile.delete();
    }

    @Test
    void testCopyConstructor() {
        KeelConfigElement copy = new KeelConfigElement(root);
        
        assertEquals(root.getName(), copy.getName());
        assertEquals(root.getValueAsString(), copy.getValueAsString());
        assertEquals(root.getChildren().size(), copy.getChildren().size());
    }

    @Test
    void testBooleanOperations() {
        KeelConfigElement child = root.extract("child1", "grandChild");
        assertNotNull(child);
        
        assertTrue(child.getValueAsBoolean());
        assertTrue(child.getValueAsBooleanElse(false));
        
        // Test with invalid boolean
        KeelConfigElement invalidBool = new KeelConfigElement("invalid");
        invalidBool.setValue("not_a_boolean");
        assertFalse(invalidBool.getValueAsBooleanElse(false));
    }

    @Test
    void testLoadProperties() {
        Properties props = new Properties();
        props.setProperty("database.url", "jdbc:mysql://localhost:3306/test");
        props.setProperty("database.username", "user");
        props.setProperty("database.password", "pass");

        KeelConfigElement config = new KeelConfigElement("database");
        config.loadProperties(props);

        assertEquals("jdbc:mysql://localhost:3306/test", config.readString(List.of("database", "url")));
        assertEquals("user", config.readString(List.of("database", "username")));
        assertEquals("pass", config.readString(List.of("database", "password")));
    }
}