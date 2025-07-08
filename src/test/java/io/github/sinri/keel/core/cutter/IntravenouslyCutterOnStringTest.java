package io.github.sinri.keel.core.cutter;

import io.github.sinri.keel.core.servant.intravenous.KeelIntravenous;
import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class IntravenouslyCutterOnStringTest extends KeelUnitTest {
    
    private List<String> processedStrings;
    private AtomicInteger processedCount;
    private AtomicBoolean processingFailed;
    private KeelIntravenous.SingleDropProcessor<String> processor;
    private DeploymentOptions deploymentOptions;

    @BeforeEach
    @Override
    public void setUp() {
        processedStrings = new ArrayList<>();
        processedCount = new AtomicInteger(0);
        processingFailed = new AtomicBoolean(false);
        
        // 创建一个测试用的处理器
        processor = drop -> {
            processedStrings.add(drop);
            processedCount.incrementAndGet();
            getUnitTestLogger().debug("Processed: " + drop);
            return Future.succeededFuture();
        };
        
        deploymentOptions = new DeploymentOptions();
    }

    @Test
    void testConstructorWithTimeout() {
        // 测试带超时的构造函数
        IntravenouslyCutterOnString cutter = new IntravenouslyCutterOnString(processor, 1000, deploymentOptions);
        
        assertNotNull(cutter);
        assertNotNull(cutter.getSingleDropProcessor());
        assertEquals(processor, cutter.getSingleDropProcessor());
    }

    @Test
    void testConstructorWithoutTimeout() {
        // 测试不带超时的构造函数
        IntravenouslyCutterOnString cutter = new IntravenouslyCutterOnString(processor, deploymentOptions);
        
        assertNotNull(cutter);
        assertNotNull(cutter.getSingleDropProcessor());
        assertEquals(processor, cutter.getSingleDropProcessor());
    }

    @Test
    void testAcceptFromStreamWithSingleSegment() throws Exception {
        IntravenouslyCutterOnString cutter = new IntravenouslyCutterOnString(processor, deploymentOptions);
        
        // 发送包含单个段的数据
        Buffer buffer = Buffer.buffer("Hello World\n\n");
        cutter.acceptFromStream(buffer);
        
        // 停止并等待处理完成
        cutter.stopHere();
        cutter.waitForAllHandled().toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
        
        assertEquals(1, processedStrings.size());
        assertEquals("Hello World", processedStrings.get(0));
    }

    @Test
    void testAcceptFromStreamWithMultipleSegments() throws Exception {
        IntravenouslyCutterOnString cutter = new IntravenouslyCutterOnString(processor, deploymentOptions);
        
        // 发送包含多个段的数据
        Buffer buffer = Buffer.buffer("First\n\nSecond\n\nThird\n\n");
        cutter.acceptFromStream(buffer);
        
        cutter.stopHere();
        cutter.waitForAllHandled().toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
        
        assertEquals(3, processedStrings.size());
        assertEquals("First", processedStrings.get(0));
        assertEquals("Second", processedStrings.get(1));
        assertEquals("Third", processedStrings.get(2));
    }

    @Test
    void testAcceptFromStreamWithPartialData() throws Exception {
        IntravenouslyCutterOnString cutter = new IntravenouslyCutterOnString(processor, deploymentOptions);
        
        // 分多次发送数据
        cutter.acceptFromStream(Buffer.buffer("Partial"));
        cutter.acceptFromStream(Buffer.buffer(" Data"));
        cutter.acceptFromStream(Buffer.buffer("\n\nComplete"));
        cutter.acceptFromStream(Buffer.buffer("\n\n"));
        
        cutter.stopHere();
        cutter.waitForAllHandled().toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
        
        assertEquals(2, processedStrings.size());
        assertEquals("Partial Data", processedStrings.get(0));
        assertEquals("Complete", processedStrings.get(1));
    }

    @Test
    void testAcceptFromStreamWithNoDelimiter() throws Exception {
        IntravenouslyCutterOnString cutter = new IntravenouslyCutterOnString(processor, deploymentOptions);
        
        // 发送不包含分隔符的数据
        Buffer buffer = Buffer.buffer("No delimiter data");
        cutter.acceptFromStream(buffer);
        
        cutter.stopHere();
        cutter.waitForAllHandled().toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
        
        assertEquals(1, processedStrings.size());
        assertEquals("No delimiter data", processedStrings.get(0));
    }

    @Test
    void testAcceptFromStreamWithEmptyBuffer() throws Exception {
        IntravenouslyCutterOnString cutter = new IntravenouslyCutterOnString(processor, deploymentOptions);
        
        // 发送空缓冲区，然后发送一些实际内容
        Buffer emptyBuffer = Buffer.buffer("");
        cutter.acceptFromStream(emptyBuffer);
        
        // 发送一些实际内容
        Buffer contentBuffer = Buffer.buffer("content\n\n");
        cutter.acceptFromStream(contentBuffer);
        
        cutter.stopHere();
        cutter.waitForAllHandled().toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
        
        assertEquals(1, processedStrings.size());
                assertEquals("content", processedStrings.get(0));
    }

    @Test
    void testAcceptFromStreamWithOnlyDelimiters() throws Exception {
        IntravenouslyCutterOnString cutter = new IntravenouslyCutterOnString(processor, deploymentOptions);
        
        // 发送只包含分隔符的数据
        Buffer buffer = Buffer.buffer("\n\n\n\n");
        cutter.acceptFromStream(buffer);
        
        cutter.stopHere();
        cutter.waitForAllHandled().toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
        
        assertEquals(2, processedStrings.size());
        assertEquals("", processedStrings.get(0));
        assertEquals("", processedStrings.get(1));
    }

    @Test
    void testStopHereWithoutThrowable() throws Exception {
        IntravenouslyCutterOnString cutter = new IntravenouslyCutterOnString(processor, deploymentOptions);
        
        Buffer buffer = Buffer.buffer("Test data\n\n");
        cutter.acceptFromStream(buffer);
        
        // 正常停止
        cutter.stopHere();
        Future<Void> result = cutter.waitForAllHandled();
        
        result.toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
        assertTrue(result.succeeded());
        assertEquals(1, processedStrings.size());
        assertEquals("Test data", processedStrings.get(0));
    }

    @Test
    void testStopHereWithThrowable() throws Exception {
        IntravenouslyCutterOnString cutter = new IntravenouslyCutterOnString(processor, deploymentOptions);
        
        Buffer buffer = Buffer.buffer("Test data\n\n");
        cutter.acceptFromStream(buffer);
        
        // 带异常停止
        RuntimeException testException = new RuntimeException("Test exception");
        cutter.stopHere(testException);
        Future<Void> result = cutter.waitForAllHandled();
        
        try {
            result.toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
            fail("Expected exception");
        } catch (Exception e) {
            assertInstanceOf(RuntimeException.class, e.getCause());
            assertEquals("Test exception", e.getCause().getMessage());
        }
        
        assertEquals(1, processedStrings.size());
        assertEquals("Test data", processedStrings.get(0));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testTimeoutFunctionality() throws Exception {
        // 测试超时功能 - 设置很短的超时时间
        IntravenouslyCutterOnString cutter = new IntravenouslyCutterOnString(processor, 100, deploymentOptions);
        
        Buffer buffer = Buffer.buffer("Test data\n\n");
        cutter.acceptFromStream(buffer);
        
        // 等待超时触发
        Future<Void> result = cutter.waitForAllHandled();
        
        try {
            result.toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
            fail("Expected timeout exception");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof IntravenouslyCutter.Timeout);
        }
        
        assertEquals(1, processedStrings.size());
        assertEquals("Test data", processedStrings.get(0));
    }

    @Test
    void testProcessingWithUnicodeCharacters() throws Exception {
        IntravenouslyCutterOnString cutter = new IntravenouslyCutterOnString(processor, deploymentOptions);
        
        // 测试Unicode字符
        Buffer buffer = Buffer.buffer("Hello 世界\n\n测试 🌍\n\n");
        cutter.acceptFromStream(buffer);
        
        cutter.stopHere();
        cutter.waitForAllHandled().toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
        
        assertEquals(2, processedStrings.size());
        assertEquals("Hello 世界", processedStrings.get(0));
        assertEquals("测试 🌍", processedStrings.get(1));
    }

    @Test
    void testLargeDataProcessing() throws Exception {
        IntravenouslyCutterOnString cutter = new IntravenouslyCutterOnString(processor, deploymentOptions);
        
        // 生成大量数据
        StringBuilder sb = new StringBuilder();
        int segmentCount = 1000;
        for (int i = 0; i < segmentCount; i++) {
            sb.append("Segment ").append(i).append("\n\n");
        }
        
        Buffer buffer = Buffer.buffer(sb.toString());
        cutter.acceptFromStream(buffer);
        
        cutter.stopHere();
        cutter.waitForAllHandled().toCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
        
        assertEquals(segmentCount, processedStrings.size());
        for (int i = 0; i < segmentCount; i++) {
            assertEquals("Segment " + i, processedStrings.get(i));
        }
    }

    @Test
    void testMultipleStopCalls() throws Exception {
        IntravenouslyCutterOnString cutter = new IntravenouslyCutterOnString(processor, deploymentOptions);
        
        Buffer buffer = Buffer.buffer("Test data\n\n");
        cutter.acceptFromStream(buffer);
        
        // 多次调用停止
        cutter.stopHere();
        cutter.stopHere();
        cutter.stopHere();
        
        Future<Void> result = cutter.waitForAllHandled();
        result.toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
        
        assertTrue(result.succeeded());
        assertEquals(1, processedStrings.size());
        assertEquals("Test data", processedStrings.get(0));
    }

    @Test
    void testProcessingFailure() throws Exception {
        // 创建会失败的处理器
        KeelIntravenous.SingleDropProcessor<String> failingProcessor = drop -> {
            processedStrings.add(drop);
            if (drop.equals("fail")) {
                processingFailed.set(true);
                return Future.failedFuture(new RuntimeException("Processing failed"));
            }
            return Future.succeededFuture();
        };
        
        IntravenouslyCutterOnString cutter = new IntravenouslyCutterOnString(failingProcessor, deploymentOptions);
        
        Buffer buffer = Buffer.buffer("success\n\nfail\n\nafter_fail\n\n");
        cutter.acceptFromStream(buffer);
        
        cutter.stopHere();
        cutter.waitForAllHandled().toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
        
        assertTrue(processingFailed.get());
        // 即使处理失败，数据仍应被添加到列表中
        assertTrue(processedStrings.contains("success"));
        assertTrue(processedStrings.contains("fail"));
        assertTrue(processedStrings.contains("after_fail"));
    }
}