package io.github.sinri.keel.core.helper;

import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class KeelFileHelperTest extends KeelUnitTest {

    private KeelFileHelper fileHelper;
    private String testDir;
    private String testFile;

    @BeforeEach
    @Override
    public void setUp() {
        fileHelper = KeelFileHelper.getInstance();
        testDir = "test_temp_dir";
        testFile = testDir + "/test_file.txt";
    }

    @AfterEach
    public void tearDown() {
        // 清理测试文件
        try {
            if (new File(testFile).exists()) {
                fileHelper.delete(testFile).toCompletionStage().toCompletableFuture().join();
            }
            if (new File(testDir).exists()) {
                fileHelper.deleteRecursive(testDir).toCompletionStage().toCompletableFuture().join();
            }
        } catch (Exception e) {
            // 忽略清理错误
        }
    }

    @Test
    @DisplayName("Test singleton instance")
    void testSingletonInstance() {
        KeelFileHelper instance1 = KeelFileHelper.getInstance();
        KeelFileHelper instance2 = KeelFileHelper.getInstance();
        
        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }

    @Test
    @DisplayName("Test exists method")
    void testExists() {
        // 测试不存在的文件
        var result = fileHelper.exists("non_existent_file.txt")
                .toCompletionStage().toCompletableFuture().join();
        assertFalse(result);
        
        // 测试存在的文件（当前目录）
        result = fileHelper.exists(".")
                .toCompletionStage().toCompletableFuture().join();
        assertTrue(result);
    }

    @Test
    @DisplayName("Test mkdirs method")
    void testMkdirs() {
        var result = fileHelper.mkdirs(testDir)
                .toCompletionStage().toCompletableFuture().join();
        
        // 验证目录创建成功
        assertTrue(new File(testDir).exists());
        assertTrue(new File(testDir).isDirectory());
    }

    @Test
    @DisplayName("Test writeFile and readFileAsString methods")
    void testWriteAndReadFile() {
        String content = "Hello, World!";
        String charset = StandardCharsets.UTF_8.name();
        
        // 创建目录
        fileHelper.mkdirs(testDir).toCompletionStage().toCompletableFuture().join();
        
        // 写入文件
        fileHelper.writeFile(testFile, content, charset)
                .toCompletionStage().toCompletableFuture().join();
        
        // 验证文件存在
        assertTrue(fileHelper.exists(testFile).toCompletionStage().toCompletableFuture().join());
        
        // 读取文件
        String readContent = fileHelper.readFileAsString(testFile, charset)
                .toCompletionStage().toCompletableFuture().join();
        
        assertEquals(content, readContent);
    }

    @Test
    @DisplayName("Test appendFile method")
    void testAppendFile() {
        String initialContent = "Initial content\n";
        String appendContent = "Appended content";
        String charset = StandardCharsets.UTF_8.name();
        
        // 创建目录
        fileHelper.mkdirs(testDir).toCompletionStage().toCompletableFuture().join();
        
        // 写入初始内容
        fileHelper.writeFile(testFile, initialContent, charset)
                .toCompletionStage().toCompletableFuture().join();
        
        // 追加内容
        fileHelper.appendFile(testFile, appendContent, charset)
                .toCompletionStage().toCompletableFuture().join();
        
        // 读取文件
        String readContent = fileHelper.readFileAsString(testFile, charset)
                .toCompletionStage().toCompletableFuture().join();
        
        assertEquals(initialContent + appendContent, readContent);
    }

    @Test
    @DisplayName("Test delete method")
    void testDelete() {
        // 创建目录和文件
        fileHelper.mkdirs(testDir).toCompletionStage().toCompletableFuture().join();
        fileHelper.writeFile(testFile, "test content", StandardCharsets.UTF_8.name())
                .toCompletionStage().toCompletableFuture().join();
        
        // 验证文件存在
        assertTrue(fileHelper.exists(testFile).toCompletionStage().toCompletableFuture().join());
        
        // 删除文件
        fileHelper.delete(testFile).toCompletionStage().toCompletableFuture().join();
        
        // 验证文件不存在
        assertFalse(fileHelper.exists(testFile).toCompletionStage().toCompletableFuture().join());
    }

    @Test
    @DisplayName("Test deleteRecursive method")
    void testDeleteRecursive() {
        // 创建目录结构
        fileHelper.mkdirs(testDir).toCompletionStage().toCompletableFuture().join();
        fileHelper.mkdirs(testDir + "/subdir").toCompletionStage().toCompletableFuture().join();
        fileHelper.writeFile(testDir + "/subdir/test.txt", "test", StandardCharsets.UTF_8.name())
                .toCompletionStage().toCompletableFuture().join();
        
        // 验证目录存在
        assertTrue(fileHelper.exists(testDir).toCompletionStage().toCompletableFuture().join());
        
        // 递归删除
        fileHelper.deleteRecursive(testDir).toCompletionStage().toCompletableFuture().join();
        
        // 验证目录不存在
        assertFalse(fileHelper.exists(testDir).toCompletionStage().toCompletableFuture().join());
    }

    @Test
    @DisplayName("Test copy method")
    void testCopy() {
        String sourceFile = testDir + "/source.txt";
        String destFile = testDir + "/dest.txt";
        String content = "Copy test content";
        
        // 创建目录和源文件
        fileHelper.mkdirs(testDir).toCompletionStage().toCompletableFuture().join();
        fileHelper.writeFile(sourceFile, content, StandardCharsets.UTF_8.name())
                .toCompletionStage().toCompletableFuture().join();
        
        // 复制文件
        fileHelper.copy(sourceFile, destFile).toCompletionStage().toCompletableFuture().join();
        
        // 验证目标文件存在且内容相同
        assertTrue(fileHelper.exists(destFile).toCompletionStage().toCompletableFuture().join());
        String copiedContent = fileHelper.readFileAsString(destFile, StandardCharsets.UTF_8.name())
                .toCompletionStage().toCompletableFuture().join();
        assertEquals(content, copiedContent);
    }

    @Test
    @DisplayName("Test move method")
    void testMove() {
        String sourceFile = testDir + "/source.txt";
        String destFile = testDir + "/dest.txt";
        String content = "Move test content";
        
        // 创建目录和源文件
        fileHelper.mkdirs(testDir).toCompletionStage().toCompletableFuture().join();
        fileHelper.writeFile(sourceFile, content, StandardCharsets.UTF_8.name())
                .toCompletionStage().toCompletableFuture().join();
        
        // 移动文件
        fileHelper.move(sourceFile, destFile).toCompletionStage().toCompletableFuture().join();
        
        // 验证源文件不存在，目标文件存在且内容相同
        assertFalse(fileHelper.exists(sourceFile).toCompletionStage().toCompletableFuture().join());
        assertTrue(fileHelper.exists(destFile).toCompletionStage().toCompletableFuture().join());
        String movedContent = fileHelper.readFileAsString(destFile, StandardCharsets.UTF_8.name())
                .toCompletionStage().toCompletableFuture().join();
        assertEquals(content, movedContent);
    }

    @Test
    @DisplayName("Test isDirectory method")
    void testIsDirectory() {
        // 创建目录
        fileHelper.mkdirs(testDir).toCompletionStage().toCompletableFuture().join();
        
        // 验证是目录
        assertTrue(fileHelper.isDirectory(testDir).toCompletionStage().toCompletableFuture().join());
        
        // 验证文件不是目录
        fileHelper.writeFile(testFile, "test", StandardCharsets.UTF_8.name())
                .toCompletionStage().toCompletableFuture().join();
        assertFalse(fileHelper.isDirectory(testFile).toCompletionStage().toCompletableFuture().join());
    }

    @Test
    @DisplayName("Test getFileSize method")
    void testGetFileSize() {
        String content = "Test content for size calculation";
        
        // 创建文件
        fileHelper.mkdirs(testDir).toCompletionStage().toCompletableFuture().join();
        fileHelper.writeFile(testFile, content, StandardCharsets.UTF_8.name())
                .toCompletionStage().toCompletableFuture().join();
        
        // 获取文件大小
        Long size = fileHelper.getFileSize(testFile).toCompletionStage().toCompletableFuture().join();
        
        assertNotNull(size);
        assertTrue(size > 0);
        assertEquals(content.getBytes(StandardCharsets.UTF_8).length, size);
    }

    @Test
    @DisplayName("Test listDir method")
    void testListDir() {
        // 创建目录和文件
        fileHelper.mkdirs(testDir).toCompletionStage().toCompletableFuture().join();
        fileHelper.writeFile(testFile, "test", StandardCharsets.UTF_8.name())
                .toCompletionStage().toCompletableFuture().join();
        
        // 列出目录内容
        var files = fileHelper.listDir(testDir).toCompletionStage().toCompletableFuture().join();
        
        assertNotNull(files);
        // 验证目录不为空
        assertFalse(files.isEmpty());
    }

    @Test
    @DisplayName("Test isRunningFromJAR method")
    void testIsRunningFromJAR() {
        boolean isRunningFromJar = fileHelper.isRunningFromJAR();
        
        // 这个方法应该返回一个布尔值，表示是否从JAR文件运行
        // 在测试环境中，通常不是从JAR运行
        assertFalse(isRunningFromJar);
    }

    @Test
    @DisplayName("Test getClassPathList method")
    void testGetClassPathList() {
        var classPathList = fileHelper.getClassPathList();
        
        assertNotNull(classPathList);
        assertFalse(classPathList.isEmpty());
        
        // 验证包含一些路径
        assertTrue(classPathList.size() > 0);
    }
} 