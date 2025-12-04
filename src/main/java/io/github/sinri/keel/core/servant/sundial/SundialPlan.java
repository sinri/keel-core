package io.github.sinri.keel.core.servant.sundial;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.core.utils.cron.KeelCronExpression;
import io.github.sinri.keel.logger.api.logger.SpecificLogger;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

/**
 * 定时任务计划
 *
 * @since 5.0.0
 */
public interface SundialPlan {
    /**
     *
     * @return 定时任务计划名称
     */
    @NotNull
    String key();

    /**
     *
     * @return 任务计划 Cron 表达式，精确到分钟
     */
    @NotNull
    KeelCronExpression cronExpression();

    /**
     * 任务计划执行逻辑。
     *
     * @param now                   本次定时任务运行对应的触发时间
     * @param sundialSpecificLogger 日晷定时任务特定日志记录器
     */
    @NotNull
    Future<Void> execute(@NotNull Keel keel, @NotNull Calendar now, @NotNull SpecificLogger<SundialSpecificLog> sundialSpecificLogger);

    @NotNull
    default ThreadingModel threadingModel() {
        return ThreadingModel.WORKER;
    }
}
