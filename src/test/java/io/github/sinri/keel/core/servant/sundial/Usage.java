package io.github.sinri.keel.core.servant.sundial;

import io.github.sinri.keel.tesuto.KeelInstantRunner;
import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class Usage extends KeelInstantRunner {
    private TestSundial testSundial;

    @Override
    protected Future<Void> beforeRun() {
        this.testSundial = new TestSundial(getKeel());
        return this.testSundial.deployMe()
                               .compose(id -> {
                                   getLogger().info("sundial deployed: " + id);
                                   return Future.succeededFuture();
                               });
    }

    @Override
    protected Future<Void> run() throws Exception {
        return getKeel().asyncSleep(3 * 60_000L)
                        .compose(v -> {
                            getLogger().info("time up");
                            return Future.succeededFuture();
                        });
    }
}
