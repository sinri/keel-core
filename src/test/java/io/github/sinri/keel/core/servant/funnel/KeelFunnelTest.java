package io.github.sinri.keel.core.servant.funnel;

import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import org.junit.jupiter.api.Test;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

class KeelFunnelTest extends KeelUnitTest {

    @Test
    public void test1() {
        // 使用 async 方法来等待异步操作完成
        async(() -> {
            KeelFunnel funnel = new KeelFunnel();
            
            funnel.getFunnelLogger().setVisibleLevel(KeelLogLevel.DEBUG);

            return funnel.deployMe(new DeploymentOptions())
                  .compose(s -> {
                      getUnitTestLogger().info("deployed: " + s);
                      return Future.succeededFuture();
                  })
                  .compose(v -> {
                      getUnitTestLogger().info("point 1");
                      
                      // 添加第一个任务
                      funnel.add(() -> {
                          return Keel.asyncSleep(1000L)
                                     .compose(sleepV -> {
                                         getUnitTestLogger().info("point 2");
                                         return Future.succeededFuture();
                                     });
                      });
                      
                      // 添加第二个任务
                      funnel.add(() -> {
                          return Keel.asyncSleep(1000L)
                                     .compose(sleepV -> {
                                         getUnitTestLogger().info("point 3");
                                         return Future.succeededFuture();
                                     });
                      });
                      
                      // 等待足够的时间让任务执行完成
                      return Keel.asyncSleep(4000L)
                                 .eventually(() -> {
                                     getUnitTestLogger().info("point 4");
                                     return funnel.undeployMe();
                                 });
                  });
        });
    }

    @Test
    public void test2() {
        // 测试funnel的连续任务处理
        async(() -> {
            KeelFunnel funnel = new KeelFunnel();
            
            funnel.getFunnelLogger().setVisibleLevel(KeelLogLevel.DEBUG);
            // 设置更短的睡眠时间，让funnel更快响应
            funnel.setSleepTime(100L);

            return funnel.deployMe(new DeploymentOptions())
                  .compose(s -> {
                      getUnitTestLogger().info("deployed: " + s);
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
                                         return Future.succeededFuture();
                                     });
                      });
                      
                      funnel.add(() -> {
                          getUnitTestLogger().info("执行任务2");
                          return Keel.asyncSleep(500L)
                                     .compose(sleepV -> {
                                         getUnitTestLogger().info("任务2完成");
                                         return Future.succeededFuture();
                                     });
                      });
                      
                      // 等待任务完成
                      return Keel.asyncSleep(2000L)
                                 .eventually(() -> {
                                     getUnitTestLogger().info("测试完成");
                                     return funnel.undeployMe();
                                 });
                  });
        });
    }

}