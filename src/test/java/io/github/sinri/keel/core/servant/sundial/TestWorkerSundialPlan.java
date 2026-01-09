package io.github.sinri.keel.core.servant.sundial;

import io.github.sinri.keel.core.utils.cron.KeelCronExpression;
import io.github.sinri.keel.logger.api.LateObject;
import io.github.sinri.keel.logger.api.logger.SpecificLogger;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.jspecify.annotations.NullMarked;

import java.util.Calendar;

@NullMarked
public class TestWorkerSundialPlan implements SundialPlan {
    private final LateObject<Vertx> lateVertx = new LateObject<>();

    @Override
    public String key() {
        return getClass().getName();
    }

    @Override
    public KeelCronExpression cronExpression() {
        return new KeelCronExpression("* * * * *");
    }

    @Override
    public Future<Void> execute(Vertx vertx, Calendar now, SpecificLogger<SundialSpecificLog> sundialSpecificLogger) {
        lateVertx.set(vertx);
        return asyncCallStepwise(3, i -> {
            System.out.println("WorkerSundialPlan " + key() + " executed [" + i + "] at " + now.getTime());
            return asyncSleep(1000);
        });
    }

    @Override
    public Vertx getVertx() {
        return lateVertx.get();
    }
}
