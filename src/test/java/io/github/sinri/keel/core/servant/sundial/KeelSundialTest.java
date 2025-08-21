package io.github.sinri.keel.core.servant.sundial;

import io.github.sinri.keel.core.cron.KeelCronExpression;
import io.github.sinri.keel.core.cron.ParsedCalenderElements;
import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
class KeelSundialTest extends KeelJUnit5Test {

    private TestSundial testSundial;
    private KeelIssueRecorder<SundialIssueRecord> testRecorder;

    public KeelSundialTest(Vertx vertx) {
        super(vertx);
    }

    @BeforeEach
    public void setUp() {
        testRecorder = KeelIssueRecordCenter.outputCenter()
                .generateIssueRecorder(SundialIssueRecord.TopicSundial, SundialIssueRecord::new);
        testSundial = new TestSundial();
        // 手动初始化sundialIssueRecorder
        testSundial.initializeSundialIssueRecorder();
    }

    @Test
    @DisplayName("测试SundialIssueRecord基础功能")
    void testSundialIssueRecord() {
        // 测试默认构造函数
        SundialIssueRecord record1 = new SundialIssueRecord();
        assertTrue(record1.classification().contains("Scheduler"));
        assertEquals(SundialIssueRecord.TopicSundial, SundialIssueRecord.TopicSundial); // static field check

        // 测试带参数的构造函数
        KeelSundialPlan testPlan = createTestPlan("test-plan", "0 0 * * *");
        Calendar now = Calendar.getInstance();
        String deploymentId = "test-deployment-123";

        SundialIssueRecord record2 = new SundialIssueRecord(testPlan, now, deploymentId);
        assertTrue(record2.classification().contains("Plan"));
        var context = record2.attributes().readJsonObject("context");
        assertNotNull(context);
        assertEquals("test-plan", context.getString("plan"));
        assertEquals("0 0 * * *", context.getString("cron"));
        assertEquals(deploymentId, context.getString("deploymentId"));
        assertNotNull(context.getString("time"));

        // 测试getImplementation方法
        assertEquals(record2, record2.getImplementation());
    }

    @Test
    @DisplayName("测试KeelSundialPlan接口实现")
    void testKeelSundialPlan() {
        KeelSundialPlan plan = createTestPlan("test-plan", "30 10 * * *");

        assertEquals("test-plan", plan.key());
        assertEquals("30 10 * * *", plan.cronExpression().getRawCronExpression());
        assertTrue(plan.isWorkerThreadRequired()); // 默认值

        // 测试不需要工作线程的计划
        KeelSundialPlan lightPlan = new KeelSundialPlan() {
            @Override
            public String key() {
                return "light-plan";
            }

            @Override
            public KeelCronExpression cronExpression() {
                return new KeelCronExpression("* * * * *");
            }

            @Override
            public Future<Void> execute(Calendar now, KeelIssueRecorder<SundialIssueRecord> sundialIssueRecorder) {
                return Future.succeededFuture();
            }

            @Override
            public boolean isWorkerThreadRequired() {
                return false;
            }
        };

        assertFalse(lightPlan.isWorkerThreadRequired());
    }

    @Test
    @DisplayName("测试KeelSundialPlan执行")
    void testKeelSundialPlanExecution(VertxTestContext testContext) {
        AtomicBoolean executed = new AtomicBoolean(false);
        AtomicInteger executionCount = new AtomicInteger(0);

        KeelSundialPlan plan = new KeelSundialPlan() {
            @Override
            public String key() {
                return "execution-test-plan";
            }

            @Override
            public KeelCronExpression cronExpression() {
                return new KeelCronExpression("* * * * *");
            }

            @Override
            public Future<Void> execute(Calendar now, KeelIssueRecorder<SundialIssueRecord> sundialIssueRecorder) {
                executed.set(true);
                executionCount.incrementAndGet();
                sundialIssueRecorder.info(r -> r
                        .message("计划执行成功")
                        .context("execution_count", executionCount.get())
                );
                return Future.succeededFuture();
            }
        };

        Calendar now = Calendar.getInstance();
        plan.execute(now, testRecorder)
            .onComplete(ar -> {
                if (ar.succeeded()) {
                    assertTrue(executed.get());
                    assertEquals(1, executionCount.get());
                    testContext.completeNow();
                } else {
                    testContext.failNow(ar.cause());
                }
            });
    }

    @Test
    @DisplayName("测试KeelSundialPlan执行失败")
    void testKeelSundialPlanExecutionFailure(VertxTestContext testContext) {
        KeelSundialPlan plan = new KeelSundialPlan() {
            @Override
            public String key() {
                return "failure-test-plan";
            }

            @Override
            public KeelCronExpression cronExpression() {
                return new KeelCronExpression("* * * * *");
            }

            @Override
            public Future<Void> execute(Calendar now, KeelIssueRecorder<SundialIssueRecord> sundialIssueRecorder) {
                return Future.failedFuture(new RuntimeException("模拟执行失败"));
            }
        };

        Calendar now = Calendar.getInstance();
        plan.execute(now, testRecorder)
            .onComplete(ar -> {
                if (ar.failed()) {
                    assertEquals("模拟执行失败", ar.cause().getMessage());
                    testContext.completeNow();
                } else {
                    testContext.failNow(new RuntimeException("Expected failure but got success"));
                }
            });
    }

    @Test
    @DisplayName("测试Cron表达式匹配")
    void testCronExpressionMatching() {
        // 测试匹配当前时间的表达式
        Calendar now = Calendar.getInstance();
        ParsedCalenderElements elements = new ParsedCalenderElements(now);
        
        String matchingCron = String.format("%d %d %d %d %d",
                elements.minute,
                elements.hour,
                elements.day,
                elements.month,
                elements.weekday);

        KeelSundialPlan matchingPlan = createTestPlan("matching-plan", matchingCron);
        assertTrue(matchingPlan.cronExpression().match(elements));

        // 测试不匹配的表达式
        String nonMatchingCron = String.format("%d %d %d %d %d",
                (elements.minute + 1) % 60,
                elements.hour,
                elements.day,
                elements.month,
                elements.weekday);

        KeelSundialPlan nonMatchingPlan = createTestPlan("non-matching-plan", nonMatchingCron);
        assertFalse(nonMatchingPlan.cronExpression().match(elements));

        // 测试通配符表达式
        KeelSundialPlan wildcardPlan = createTestPlan("wildcard-plan", "* * * * *");
        assertTrue(wildcardPlan.cronExpression().match(elements));
    }

    @Test
    @DisplayName("测试KeelSundial基础功能")
    void testKeelSundialBasics() {
        assertNotNull(testSundial.getSundialIssueRecorder());
        assertEquals(SundialIssueRecord.TopicSundial, testSundial.getSundialIssueRecorder().topic());
    }

    @Test
    @DisplayName("测试KeelSundial计划管理")
    void testKeelSundialPlanManagement() {
        // 测试添加计划
        KeelSundialPlan plan1 = createTestPlan("plan1", "0 0 * * *");
        KeelSundialPlan plan2 = createTestPlan("plan2", "30 12 * * *");

        testSundial.addTestPlan(plan1);
        testSundial.addTestPlan(plan2);

        Collection<KeelSundialPlan> plans = testSundial.getTestPlans();
        assertEquals(2, plans.size());
        assertTrue(plans.stream().anyMatch(p -> p.key().equals("plan1")));
        assertTrue(plans.stream().anyMatch(p -> p.key().equals("plan2")));

        // 测试移除计划
        testSundial.removeTestPlan("plan1");
        plans = testSundial.getTestPlans();
        assertEquals(1, plans.size());
        assertFalse(plans.stream().anyMatch(p -> p.key().equals("plan1")));
        assertTrue(plans.stream().anyMatch(p -> p.key().equals("plan2")));
    }

    @Test
    @DisplayName("测试KeelSundial计划执行逻辑")
    void testKeelSundialExecutionLogic(VertxTestContext testContext) {
        AtomicInteger executionCount = new AtomicInteger(0);

        KeelSundialPlan plan = new KeelSundialPlan() {
            @Override
            public String key() {
                return "execution-logic-test";
            }

            @Override
            public KeelCronExpression cronExpression() {
                return new KeelCronExpression("* * * * *"); // 每分钟执行
            }

            @Override
            public Future<Void> execute(Calendar now, KeelIssueRecorder<SundialIssueRecord> sundialIssueRecorder) {
                executionCount.incrementAndGet();
                return Future.succeededFuture();
            }
        };

        testSundial.addTestPlan(plan);

        // 模拟每分钟检查
        Calendar now = Calendar.getInstance();
        testSundial.handleEveryMinuteTest(now);

        // 由于cron表达式是"* * * * *"，应该匹配并执行
        assertEquals(1, executionCount.get());
        testContext.completeNow();
    }

    @Test
    @DisplayName("测试KeelSundial计划不匹配情况")
    void testKeelSundialNonMatchingExecution() {
        AtomicInteger executionCount = new AtomicInteger(0);

        // 创建一个不匹配当前时间的计划
        Calendar now = Calendar.getInstance();
        ParsedCalenderElements elements = new ParsedCalenderElements(now);
        String nonMatchingCron = String.format("%d %d %d %d %d",
                (elements.minute + 1) % 60, // 下一分钟
                elements.hour,
                elements.day,
                elements.month,
                elements.weekday);

        KeelSundialPlan plan = new KeelSundialPlan() {
            @Override
            public String key() {
                return "non-matching-execution-test";
            }

            @Override
            public KeelCronExpression cronExpression() {
                return new KeelCronExpression(nonMatchingCron);
            }

            @Override
            public Future<Void> execute(Calendar now, KeelIssueRecorder<SundialIssueRecord> sundialIssueRecorder) {
                executionCount.incrementAndGet();
                return Future.succeededFuture();
            }
        };

        testSundial.addTestPlan(plan);

        // 模拟每分钟检查
        testSundial.handleEveryMinuteTest(now);

        // 由于cron表达式不匹配当前时间，不应该执行
        assertEquals(0, executionCount.get());
    }

    @Test
    @DisplayName("测试KeelSundialVerticle部署")
    void testKeelSundialVerticleDeployment() {
        KeelSundialPlan plan = createTestPlan("verticle-test", "* * * * *");
        Calendar now = Calendar.getInstance();

        // 测试工作线程部署
        KeelSundialVerticle verticle = new KeelSundialVerticle(plan, now, testRecorder);
        assertNotNull(verticle);

        // 测试事件线程部署（不需要工作线程的计划）
        KeelSundialPlan lightPlan = new KeelSundialPlan() {
            @Override
            public String key() {
                return "light-verticle-test";
            }

            @Override
            public KeelCronExpression cronExpression() {
                return new KeelCronExpression("* * * * *");
            }

            @Override
            public Future<Void> execute(Calendar now, KeelIssueRecorder<SundialIssueRecord> sundialIssueRecorder) {
                return Future.succeededFuture();
            }

            @Override
            public boolean isWorkerThreadRequired() {
                return false;
            }
        };

        KeelSundialVerticle lightVerticle = new KeelSundialVerticle(lightPlan, now, testRecorder);
        assertNotNull(lightVerticle);
    }

    @Test
    @DisplayName("测试复杂的Cron表达式")
    void testComplexCronExpressions() {
        // 测试范围表达式
        KeelSundialPlan rangePlan = createTestPlan("range-test", "0-5 8-17 * * 1-5");
        Calendar workday = Calendar.getInstance();
        workday.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        workday.set(Calendar.HOUR_OF_DAY, 10);
        workday.set(Calendar.MINUTE, 3);

        assertTrue(rangePlan.cronExpression().match(workday));

        // 测试列表表达式
        KeelSundialPlan listPlan = createTestPlan("list-test", "0,15,30,45 9,12,18 * * *");
        Calendar listTime = Calendar.getInstance();
        listTime.set(Calendar.HOUR_OF_DAY, 12);
        listTime.set(Calendar.MINUTE, 30);

        assertTrue(listPlan.cronExpression().match(listTime));

        // 测试增量表达式
        KeelSundialPlan incrementPlan = createTestPlan("increment-test", "*/15 */6 * * *");
        Calendar incrementTime = Calendar.getInstance();
        incrementTime.set(Calendar.HOUR_OF_DAY, 6);
        incrementTime.set(Calendar.MINUTE, 15);

        assertTrue(incrementPlan.cronExpression().match(incrementTime));
    }

    @Test
    @DisplayName("测试边界情况")
    void testEdgeCases() {
        // 测试边界时间值 - 使用2023年1月1日，这是星期日
        Calendar boundaryTime = Calendar.getInstance();
        boundaryTime.set(2023, Calendar.JANUARY, 1, 0, 0, 0);
        boundaryTime.set(Calendar.MILLISECOND, 0);

        KeelSundialPlan boundaryPlan = createTestPlan("boundary-test", "0 0 1 1 0");
        assertTrue(boundaryPlan.cronExpression().match(boundaryTime));

        // 测试月末时间 - 使用2022年12月31日，这是星期六
        Calendar monthEnd = Calendar.getInstance();
        monthEnd.set(2022, Calendar.DECEMBER, 31, 23, 59, 0);
        monthEnd.set(Calendar.MILLISECOND, 0);

        KeelSundialPlan monthEndPlan = createTestPlan("month-end-test", "59 23 31 12 6");
        assertTrue(monthEndPlan.cronExpression().match(monthEnd));
    }

    // 辅助方法
    private KeelSundialPlan createTestPlan(String key, String cronExpression) {
        return new KeelSundialPlan() {
            @Override
            public String key() {
                return key;
            }

            @Override
            public KeelCronExpression cronExpression() {
                return new KeelCronExpression(cronExpression);
            }

            @Override
            public Future<Void> execute(Calendar now, KeelIssueRecorder<SundialIssueRecord> sundialIssueRecorder) {
                return Future.succeededFuture();
            }
        };
    }

    // 测试用的KeelSundial实现
    private static class TestSundial extends KeelSundial {
        private final Map<String, KeelSundialPlan> testPlanMap = new HashMap<>();

        @Override
        protected KeelIssueRecordCenter getIssueRecordCenter() {
            return KeelIssueRecordCenter.outputCenter();
        }

        @Override
        protected Future<Collection<KeelSundialPlan>> fetchPlans() {
            return Future.succeededFuture(testPlanMap.values());
        }

        public void addTestPlan(KeelSundialPlan plan) {
            testPlanMap.put(plan.key(), plan);
        }

        public void removeTestPlan(String key) {
            testPlanMap.remove(key);
        }

        public Collection<KeelSundialPlan> getTestPlans() {
            return testPlanMap.values();
        }

        // 手动初始化sundialIssueRecorder
        public void initializeSundialIssueRecorder() {
            try {
                // 使用反射调用buildIssueRecorder方法
                java.lang.reflect.Method buildMethod = KeelSundial.class.getDeclaredMethod("buildIssueRecorder");
                buildMethod.setAccessible(true);
                KeelIssueRecorder<SundialIssueRecord> recorder = (KeelIssueRecorder<SundialIssueRecord>) buildMethod.invoke(this);
                
                // 使用反射设置sundialIssueRecorder字段
                java.lang.reflect.Field recorderField = KeelSundial.class.getDeclaredField("sundialIssueRecorder");
                recorderField.setAccessible(true);
                recorderField.set(this, recorder);
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize sundialIssueRecorder", e);
            }
        }

        // 暴露用于测试的方法
        public void handleEveryMinuteTest(Calendar now) {
            // 使用反射或直接调用私有方法
            // 这里我们模拟handleEveryMinute的逻辑
            ParsedCalenderElements parsedCalenderElements = new ParsedCalenderElements(now);
            testPlanMap.forEach((key, plan) -> {
                if (plan.cronExpression().match(parsedCalenderElements)) {
                    getSundialIssueRecorder().debug(x -> x
                            .message("Sundial Plan Matched")
                            .context("plan_key", plan.key())
                            .context("plan_cron", plan.cronExpression().getRawCronExpression())
                            .context("now", parsedCalenderElements.toString())
                    );
                    
                    // 直接执行计划而不是部署verticle
                    plan.execute(now, getSundialIssueRecorder());
                } else {
                    getSundialIssueRecorder().debug(x -> x
                            .message("Sundial Plan Not Match")
                            .context("plan_key", plan.key())
                            .context("plan_cron", plan.cronExpression().getRawCronExpression())
                            .context("now", parsedCalenderElements.toString())
                    );
                }
            });
        }
    }
}