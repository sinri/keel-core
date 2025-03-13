package io.github.sinri.keel.test.lab.intravenous;

import io.github.sinri.keel.core.servant.intravenous.KeelIntravenous;
import io.github.sinri.keel.facade.tesuto.instant.InstantRunUnit;
import io.github.sinri.keel.facade.tesuto.instant.KeelInstantRunner;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;

import javax.annotation.Nonnull;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class KeelIntravenousMockTest extends KeelInstantRunner {
    private KeelIntravenous<MyIntravenousDrop> myIntravenous;

    @Nonnull
    @Override
    protected Future<Void> starting() {
        return super.starting()
                    .compose(v -> {
                        myIntravenous = KeelIntravenous.instant(drop -> {
                            getInstantLogger().info("Starting instant " + drop.i);
                            return Keel.asyncSleep(2000L)
                                       .compose(slept -> {
                                           getInstantLogger().info("Ending instant " + drop.i);
                                           return Future.succeededFuture();
                                       });
                        });
                        return myIntravenous.deployMe(new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER))
                                            .compose(deploymentId -> {
                                                getInstantLogger().info("deploymentId: " + deploymentId);
                                                return Future.succeededFuture();
                                            });
                    });
    }

    @InstantRunUnit
    public Future<Void> run() {
        return Keel.asyncCallStepwise(4, i -> {
            myIntravenous.add(new MyIntravenousDrop(i));
            if (i == 2) myIntravenous.shutdown();
            return Keel.asyncSleep(3000L);
        });
    }

    private static class MyIntravenousDrop {
        public final long i;

        public MyIntravenousDrop(long i) {
            this.i = i;
        }
    }
}
