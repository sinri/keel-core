package io.github.sinri.keel.test.lab.cache;

import io.github.sinri.keel.core.cache.NotCached;
import io.github.sinri.keel.core.cache.impl.KeelCacheDalet;
import io.github.sinri.keel.facade.tesuto.instant.InstantRunUnit;
import io.github.sinri.keel.facade.tesuto.instant.KeelInstantRunner;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.Map;

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
        return dalet.getDeploymentId()
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
                        return dalet.undeploy()
                                    .compose(undeployed -> {
                                        // from now on, variable dalet should be not used.
                                        getInstantLogger().notice("undeployed");
                                        return Future.succeededFuture();
                                    }, throwable -> {
                                        getInstantLogger().exception(throwable);
                                        return Future.failedFuture(throwable);
                                    });
                    });
    }

    private static class Dalet extends KeelCacheDalet<String, String> {
        public Dalet() {
            super(() -> {
                var map = Map.of(
                        "last_cache_time", String.valueOf(System.currentTimeMillis()),
                        "last_cache_date", new Date().toString()
                );
                return Future.succeededFuture(map);
            }, 3_000L);
        }
    }
}
