package io.github.sinri.keel.facade.configuration;

import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static io.github.sinri.keel.facade.KeelInstance.Keel;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeelConfigPropertiesBuilderTest extends KeelUnitTest {

    @Test
    void testBasicOperations() {
        KeelConfigPropertiesBuilder builder = new KeelConfigPropertiesBuilder();

        // Test single key-value pair
        builder.add("key1", "value1");
        assertEquals("key1=value1", builder.writeToString());

        // Test multiple key-value pairs
        builder.add("key2", "value2");
        String result = builder.writeToString();
        assertTrue(result.contains("key1=value1"));
        assertTrue(result.contains("key2=value2"));
    }

    @Test
    void testPrefixOperations() {
        KeelConfigPropertiesBuilder builder = new KeelConfigPropertiesBuilder();

        // Test prefix with string varargs
        builder.setPrefix("app", "config")
               .add("database", "mysql");
        assertEquals("app.config.database=mysql", builder.writeToString());

        // Test prefix with List
        builder = new KeelConfigPropertiesBuilder();
        builder.setPrefix(List.of("app", "config"))
               .add(List.of("database"), "mysql");
        assertEquals("app.config.database=mysql", builder.writeToString());

        // Test multiple properties with prefix
        builder.add("host", "localhost");
        String result = builder.writeToString();
        assertTrue(result.contains("app.config.database=mysql"));
        assertTrue(result.contains("app.config.host=localhost"));
    }

    @Test
    void testFileOperations(@TempDir Path tempDir) throws Exception {
        // 使用 JUnit 的 @TempDir 来管理临时目录
        Path tempFile = tempDir.resolve("test.properties");
        getUnitTestLogger().info("tempFile: " + tempFile.toString());

        try {
            KeelConfigPropertiesBuilder builder = new KeelConfigPropertiesBuilder();
            builder.setPrefix("test")
                   .add("key1", "value1")
                   .add("key2", "value2");

            // Test write to file
            var f = builder.writeToFile(tempFile.toString());
            Keel.blockAwait(f);

            // Verify file exists and is readable
            assertTrue(Files.exists(tempFile), "Temp file should exist");
            assertTrue(Files.isReadable(tempFile), "Temp file should be readable");

            // Verify file contents
            List<String> lines = Files.readAllLines(tempFile, StandardCharsets.US_ASCII);
            String fileContent = String.join("\n", lines);
            assertTrue(fileContent.contains("test.key1=value1"), "File should contain first property");
            assertTrue(fileContent.contains("test.key2=value2"), "File should contain second property");

            // Test append to file
            KeelConfigPropertiesBuilder appendBuilder = new KeelConfigPropertiesBuilder();
            appendBuilder.add("key3", "value3");
            f = appendBuilder.appendToFile(tempFile.toString());
            Keel.blockAwait(f);

            // Verify appended content
            lines = Files.readAllLines(tempFile, StandardCharsets.US_ASCII);
            fileContent = String.join("\n", lines);
            assertTrue(fileContent.contains("test.key1=value1"), "File should retain first property");
            assertTrue(fileContent.contains("test.key2=value2"), "File should retain second property");
            assertTrue(fileContent.contains("key3=value3"), "File should contain appended property");

        } finally {
            // 清理文件（虽然 @TempDir 会自动清理，这里为了保险起见）
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testEmptyBuilder() {
        KeelConfigPropertiesBuilder builder = new KeelConfigPropertiesBuilder();
        assertEquals("", builder.writeToString());
    }

    @Test
    void testComplexKeychain() {
        KeelConfigPropertiesBuilder builder = new KeelConfigPropertiesBuilder();

        // Test complex keychain with List
        builder.add(List.of("database", "master", "host"), "localhost");
        assertEquals("database.master.host=localhost", builder.writeToString());

        // Test complex keychain with prefix
        builder = new KeelConfigPropertiesBuilder();
        builder.setPrefix("app", "config")
               .add(List.of("database", "master", "host"), "localhost")
               .add(List.of("database", "slave", "host"), "127.0.0.1");

        String result = builder.writeToString();
        assertTrue(result.contains("app.config.database.master.host=localhost"));
        assertTrue(result.contains("app.config.database.slave.host=127.0.0.1"));
    }

    @Test
    void testSetConfigPropertyList() {
        KeelConfigPropertiesBuilder builder = new KeelConfigPropertiesBuilder();

        // Create a new property list
        KeelConfigProperty property1 = new KeelConfigProperty()
                .setKeychain(List.of("test", "key1"))
                .setValue("value1");
        KeelConfigProperty property2 = new KeelConfigProperty()
                .setKeychain(List.of("test", "key2"))
                .setValue("value2");

        builder.setConfigPropertyList(List.of(property1, property2));

        String result = builder.writeToString();
        assertTrue(result.contains("test.key1=value1"));
        assertTrue(result.contains("test.key2=value2"));
    }
}