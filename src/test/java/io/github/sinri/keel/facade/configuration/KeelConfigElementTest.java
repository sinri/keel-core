package io.github.sinri.keel.facade.configuration;

import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
class KeelConfigElementTest extends KeelJUnit5Test {
    private KeelConfigElement root;

    public KeelConfigElementTest(Vertx vertx) {
        super(vertx);
    }

    @BeforeEach
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

        KeelConfigElement config = new KeelConfigElement("config");
        config.loadProperties(props);

        assertEquals("jdbc:mysql://localhost:3306/test", config.readString(List.of("database", "url")));
        assertEquals("user", config.readString(List.of("database", "username")));
        assertEquals("pass", config.readString(List.of("database", "password")));
    }

    @Test
    void testLoadPropertiesEdgeCases() {
        Properties props = new Properties();
        // Test single level key
        props.setProperty("simple", "value");
        // Test multi-level key
        props.setProperty("nested.deep.value", "nested_value");
        // Test key with dots in value
        props.setProperty("key.with.dots", "value.with.dots");

        KeelConfigElement config = new KeelConfigElement("root");
        config.loadProperties(props);

        // Test single level
        assertEquals("value", config.readString(List.of("simple")));
        
        // Test multi-level
        assertEquals("nested_value", config.readString(List.of("nested", "deep", "value")));
        
        // Test key with dots in value
        assertEquals("value.with.dots", config.readString(List.of("key", "with", "dots")));
        
        // Verify the structure is correct
        assertNotNull(config.getChild("simple"));
        assertNotNull(config.getChild("nested"));
        assertNotNull(config.getChild("key"));
    }

    @Test
    void transformChildrenToPropertyList() {
        // 测试基本功能：将子节点转换为属性列表
        List<KeelConfigProperty> properties = root.transformChildrenToPropertyList();

        getUnitTestLogger().info("root as json", root.toJsonObject());
        getUnitTestLogger().info("root as properties", new JsonObject()
                .put("array", new JsonArray(
                        properties.stream().map(kcp -> {
                            return kcp.toString();
                        }).collect(Collectors.toList()))
                )
        );

        // 验证返回的属性数量（应该有3个属性：child1, child1.grandChild, child2）
        assertEquals(3, properties.size());

        // 验证属性按深度优先搜索的顺序输出
        // 实际顺序：child1, child1.grandChild, child2
        assertEquals("child1", properties.get(0).toString().split("=")[0]);
        assertEquals("child1.grandChild", properties.get(1).toString().split("=")[0]);
        assertEquals("child2", properties.get(2).toString().split("=")[0]);

        // 验证属性值
        assertEquals("value1", properties.get(0).toString().split("=")[1]);
        assertEquals("true", properties.get(1).toString().split("=")[1]);
        assertEquals("123", properties.get(2).toString().split("=")[1]);

        // 测试空子节点的情况
        KeelConfigElement emptyRoot = new KeelConfigElement("empty");
        List<KeelConfigProperty> emptyProperties = emptyRoot.transformChildrenToPropertyList();
        assertEquals(0, emptyProperties.size());

        // 测试只有值没有子节点的情况
        KeelConfigElement leafNode = new KeelConfigElement("leaf");
        leafNode.setValue("leaf_value");
        List<KeelConfigProperty> leafProperties = leafNode.transformChildrenToPropertyList();
        assertEquals(0, leafProperties.size()); // 没有子节点，所以不会输出属性

        // 测试复杂嵌套结构
        KeelConfigElement complexRoot = new KeelConfigElement("complex");
        complexRoot.setValue("root_value");

        KeelConfigElement level1a = new KeelConfigElement("a");
        level1a.setValue("a_value");
        complexRoot.addChild(level1a);

        KeelConfigElement level1b = new KeelConfigElement("b");
        level1b.setValue("b_value");
        complexRoot.addChild(level1b);

        KeelConfigElement level2a = new KeelConfigElement("sub");
        level2a.setValue("sub_value");
        level1a.addChild(level2a);

        KeelConfigElement level2b = new KeelConfigElement("deep");
        level2b.setValue("deep_value");
        level1b.addChild(level2b);

        List<KeelConfigProperty> complexProperties = complexRoot.transformChildrenToPropertyList();

        // 应该有4个属性：a, a.sub, b, b.deep（按深度优先搜索顺序）
        assertEquals(4, complexProperties.size());

        // 验证按深度优先搜索顺序
        assertEquals("a", complexProperties.get(0).toString().split("=")[0]);
        assertEquals("a.sub", complexProperties.get(1).toString().split("=")[0]);
        assertEquals("b", complexProperties.get(2).toString().split("=")[0]);
        assertEquals("b.deep", complexProperties.get(3).toString().split("=")[0]);

        // 验证属性值
        assertEquals("a_value", complexProperties.get(0).toString().split("=")[1]);
        assertEquals("sub_value", complexProperties.get(1).toString().split("=")[1]);
        assertEquals("b_value", complexProperties.get(2).toString().split("=")[1]);
        assertEquals("deep_value", complexProperties.get(3).toString().split("=")[1]);

        // 测试字典序排序在同一层级内的效果
        KeelConfigElement sortTestRoot = new KeelConfigElement("sortTest");

        KeelConfigElement zChild = new KeelConfigElement("z");
        zChild.setValue("z_value");
        sortTestRoot.addChild(zChild);

        KeelConfigElement aChild = new KeelConfigElement("a");
        aChild.setValue("a_value");
        sortTestRoot.addChild(aChild);

        KeelConfigElement mChild = new KeelConfigElement("m");
        mChild.setValue("m_value");
        sortTestRoot.addChild(mChild);

        List<KeelConfigProperty> sortTestProperties = sortTestRoot.transformChildrenToPropertyList();

        // 验证同一层级内按字典序排序
        assertEquals("a", sortTestProperties.get(0).toString().split("=")[0]);
        assertEquals("m", sortTestProperties.get(1).toString().split("=")[0]);
        assertEquals("z", sortTestProperties.get(2).toString().split("=")[0]);
    }
}