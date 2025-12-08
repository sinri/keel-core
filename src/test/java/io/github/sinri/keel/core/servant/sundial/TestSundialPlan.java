package io.github.sinri.keel.core.servant.sundial;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.core.utils.cron.KeelCronExpression;
import io.github.sinri.keel.logger.api.logger.SpecificLogger;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

public class TestSundialPlan implements SundialPlan {
    @Override
    public @NotNull String key() {
        return getClass().getName();
    }

    @Override
    public @NotNull KeelCronExpression cronExpression() {
        return new KeelCronExpression("* * * * *");
    }

    @Override
    public @NotNull Future<Void> execute(@NotNull Keel keel, @NotNull Calendar now, @NotNull SpecificLogger<SundialSpecificLog> sundialSpecificLogger) {
        for(int i=0;i<10;i++){
            keel.asyncSleep(1000).await();
            sundialSpecificLogger.info("SundialPlan " + key() + " executed [" + i + "] at " + now.getTime());
        }
        return Future.succeededFuture();
    }

    @Override
    public @NotNull ThreadingModel threadingModel() {
        return ThreadingModel.VIRTUAL_THREAD;
    }
}
