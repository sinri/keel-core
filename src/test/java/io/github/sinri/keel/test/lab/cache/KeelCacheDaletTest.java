package io.github.sinri.keel.test.lab.cache;

import io.github.sinri.keel.core.cache.NotCached;
import io.github.sinri.keel.core.cache.impl.KeelCacheDalet;
import io.github.sinri.keel.facade.tesuto.instant.InstantRunUnit;
import io.github.sinri.keel.facade.tesuto.instant.KeelInstantRunner;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;

import javax.annotation.Nonnull;
import java.util.Date;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class KeelCacheDaletTest extends KeelInstantRunner {

    @Nonnull
    @Override
    protected Future<Void> starting() {
        Keel.getLogger().setVisibleLevel(KeelLogLevel.DEBUG);
        return super.starting();
    }

    @InstantRunUnit
    public Future<Void> test1() {
        Dalet dalet = new Dalet();
        return dalet.deployMe(new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER))
                .compose(deploymentId -> {
                    getInstantLogger().notice("deployment id: " + deploymentId);
                    return Future.succeededFuture();
                }, throwable -> {
                    getInstantLogger().exception(throwable);
                    return Future.failedFuture(throwable);
                })
                .compose(v -> {
                    return Keel.asyncCallStepwise(10, i -> {
                        try {
                            getInstantLogger().info("[" + i + "] " + dalet.read("last_cache_time"));
                        } catch (NotCached e) {
                            throw new RuntimeException(e);
                        }
                        return Keel.asyncSleep(1000L);
                    });
                })
                .compose(v -> {
                    return dalet.undeployMe()
                            .compose(undeployed -> {
                                getInstantLogger().notice("undeployed");
                                return Future.succeededFuture();
                            }, throwable -> {
                                getInstantLogger().exception(throwable);
                                return Future.failedFuture(throwable);
                            });
                })
                .compose(v -> {
                    return Keel.asyncCallStepwise(10, i -> {
                        try {
                            getInstantLogger().info("[" + i + "] " + dalet.read("last_cache_time"));
                        } catch (NotCached e) {
                            throw new RuntimeException(e);
                        }
                        return Keel.asyncSleep(1000L);
                    });
                });
    }

    private static class Dalet extends KeelCacheDalet {

        @Override
        public Future<Void> fullyUpdate() {
            this.save("last_cache_time", String.valueOf(System.currentTimeMillis()));
            this.save("last_cache_date", new Date().toString());
            Keel.getLogger().info("updated cache");
            return Future.succeededFuture();
        }

        @Override
        protected long regularUpdatePeriod() {
            return 3_000L;
        }

        //        @Nonnull
        //        @Override
        //        protected KeelIssueRecorder<KeelEventLog> buildIssueRecorder() {
        //            return KeelIssueRecordCenter.outputCenter().generateIssueRecorder("Dalet", KeelEventLog::new);
        //        }
    }
}
