package io.github.sinri.keel.core.servant.sundial;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.core.utils.cron.KeelCronExpression;
import io.github.sinri.keel.logger.api.logger.SpecificLogger;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;
import org.jspecify.annotations.NullMarked;

import java.util.Calendar;

/**
 * 定时任务计划
 *
 * @since 5.0.0
 */
@NullMarked
public interface SundialPlan {
    /**
     *
     * @return 定时任务计划名称
     */
    String key();

    /**
     *
     * @return 任务计划 Cron 表达式，精确到分钟
     */
    KeelCronExpression cronExpression();

    /**
     * 任务计划执行逻辑。
     *
     * @param now                   本次定时任务运行对应的触发时间
     * @param sundialSpecificLogger 日晷定时任务特定日志记录器
     */
    Future<Void> execute(Keel keel, Calendar now, SpecificLogger<SundialSpecificLog> sundialSpecificLogger);

    default ThreadingModel threadingModel() {
        return ThreadingModel.WORKER;
    }
}
