package io.github.sinri.keel.core.servant.funnel;

import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

@ExtendWith(VertxExtension.class)
class KeelFunnelTest extends KeelJUnit5Test {


    public KeelFunnelTest(Vertx vertx) {
        super(vertx);
    }

    @Test
    public void test1(VertxTestContext testContext) {
        Checkpoint cp0 = testContext.checkpoint();
        Checkpoint cp1 = testContext.checkpoint();
        Checkpoint cp2 = testContext.checkpoint();
        Checkpoint cp3 = testContext.checkpoint();
        Checkpoint cp9 = testContext.checkpoint();

        // 使用 async 方法来等待异步操作完成
        KeelFunnel funnel = new KeelFunnel();

        funnel.getFunnelLogger().setVisibleLevel(KeelLogLevel.DEBUG);

        funnel.deployMe(new DeploymentOptions())
              .compose(s -> {
                  getUnitTestLogger().info("deployed: " + s);
                  Assertions.assertNotNull(s);
                  cp0.flag();
                  return Future.succeededFuture();
              })
              .compose(v -> {
                  getUnitTestLogger().info("point 1");

                  // 添加第一个任务
                  funnel.add(() -> {
                      return Keel.asyncSleep(1000L)
                                 .compose(sleepV -> {
                                     getUnitTestLogger().info("point 2");
                                     cp1.flag();
                                     return Future.succeededFuture();
                                 });
                  });

                  // 添加第二个任务
                  funnel.add(() -> {
                      return Keel.asyncSleep(1000L)
                                 .compose(sleepV -> {
                                     getUnitTestLogger().info("point 3");
                                     cp2.flag();
                                     return Future.succeededFuture();
                                 });
                  });

                  // 等待足够的时间让任务执行完成
                  return Keel.asyncSleep(4000L)
                             .eventually(() -> {
                                 getUnitTestLogger().info("point 4");
                                 cp3.flag();
                                 return funnel.undeployMe();
                             })
                             .onComplete(vv -> {
                                 cp9.flag();
                             });
              });
        //.onComplete(testContext.succeedingThenComplete());
    }

    @Test
    public void test2(VertxTestContext testContext) {
        Checkpoint cp0 = testContext.checkpoint();
        Checkpoint cp1 = testContext.checkpoint();
        Checkpoint cp2 = testContext.checkpoint();
        Checkpoint cp3 = testContext.checkpoint();

        // 测试funnel的连续任务处理
        KeelFunnel funnel = new KeelFunnel();

        funnel.getFunnelLogger().setVisibleLevel(KeelLogLevel.DEBUG);
        // 设置更短的睡眠时间，让funnel更快响应
        funnel.setSleepTime(100L);

        funnel.deployMe(new DeploymentOptions())
              .compose(s -> {
                  getUnitTestLogger().info("deployed: " + s);
                  cp0.flag();
                  return Future.succeededFuture();
              })
              .compose(v -> {
                  getUnitTestLogger().info("开始添加任务");

                  // 立即添加任务，避免funnel进入睡眠状态
                  funnel.add(() -> {
                      getUnitTestLogger().info("执行任务1");
                      return Keel.asyncSleep(500L)
                                 .compose(sleepV -> {
                                     getUnitTestLogger().info("任务1完成");
                                     cp1.flag();
                                     return Future.succeededFuture();
                                 });
                  });

                  funnel.add(() -> {
                      getUnitTestLogger().info("执行任务2");
                      return Keel.asyncSleep(500L)
                                 .compose(sleepV -> {
                                     getUnitTestLogger().info("任务2完成");
                                     cp2.flag();
                                     return Future.succeededFuture();
                                 });
                  });

                  // 等待任务完成
                  return Keel.asyncSleep(2000L)
                             .compose(vv -> {
                                 return funnel.undeployMe();
                             })
                             .eventually(() -> {
                                 getUnitTestLogger().info("测试完成");
                                 cp3.flag();
                                 return funnel.undeployMe();
                             });
              })
              .onComplete(testContext.succeedingThenComplete());
    }
}