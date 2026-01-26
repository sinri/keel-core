package io.github.sinri.keel.core.servant.sundial;

import io.github.sinri.keel.base.async.Keel;
import io.github.sinri.keel.core.utils.cron.KeelCronExpression;
import io.github.sinri.keel.logger.api.LateObject;
import io.github.sinri.keel.logger.api.logger.SpecificLogger;
import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;

import java.util.Calendar;

@NullMarked
public class TestWorkerSundialPlan implements SundialPlan {
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
        return keel.asyncCallStepwise(3, i -> {
            System.out.println("WorkerSundialPlan " + key() + " executed [" + i + "] at " + now.getTime());
            return keel.asyncSleep(1000);
        });
    }

    public Keel getKeel() {
        return lateKeel.get();
    }
}
