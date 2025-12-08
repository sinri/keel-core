package io.github.sinri.keel.core.servant.sundial;

import io.github.sinri.keel.tesuto.KeelInstantRunner;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

public class Usage extends KeelInstantRunner {
    private TestSundial testSundial;

    @Override
    protected @NotNull Future<Void> beforeRun() {
        this.testSundial = new TestSundial(getKeel());
        return this.testSundial.deployMe()
                               .compose(id -> {
                                   getLogger().info("sundial deployed: " + id);
                                   return Future.succeededFuture();
                               });
    }

    @Override
    protected @NotNull Future<Void> run() throws Exception {
        return getKeel().asyncSleep(3 * 60_000L)
                        .compose(v -> {
                            getLogger().info("time up");
                            return Future.succeededFuture();
                        });
    }
}
