package io.github.sinri.keel.core.servant.sundial;

import io.github.sinri.keel.base.async.Keel;
import io.github.sinri.keel.core.utils.cron.KeelCronExpression;
import io.github.sinri.keel.logger.api.LateObject;
import io.github.sinri.keel.logger.api.logger.SpecificLogger;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;
import org.jspecify.annotations.NullMarked;

import java.util.Calendar;

@NullMarked
public class TestVirtualThreadSundialPlan implements SundialPlan {
    private final LateObject<Keel> lateKeel = new LateObject<>();

    @Override
    public String key() {
        return getClass().getName();
    }

    @Override
    public KeelCronExpression cronExpression() {
        return new KeelCronExpression("* * * * *");
    }

    @Override
    public Future<Void> execute(Keel keel, Calendar now, SpecificLogger<SundialSpecificLog> sundialSpecificLogger) {
        lateKeel.set(keel);
        for (int i = 0; i < 10; i++) {
            keel.asyncSleep(1000).await();
            sundialSpecificLogger.info("SundialPlan " + key() + " executed [" + i + "] at " + now.getTime());
        }
        return Future.succeededFuture();
    }

    @Override
    public ThreadingModel expectedThreadingModel() {
        return ThreadingModel.VIRTUAL_THREAD;
    }

    public Keel getKeel() {
        return lateKeel.get();
    }
}
