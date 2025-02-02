package io.github.sinri.keel.test.lab.launcher;

import io.github.sinri.keel.core.verticles.KeelVerticleImplWithEventLogger;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class TestMainVerticle extends KeelVerticleImplWithEventLogger {

    @Override
    protected void startAsKeelVerticle(Promise<Void> startPromise) {
        Keel.asyncCallEndlessly(() -> {
            getLogger().info(r -> r.message("X"));
            return Future.succeededFuture();
        });
        startPromise.complete();
    }

    @Override
    protected KeelEventLogger buildEventLogger() {
        return KeelIssueRecordCenter.outputCenter().generateEventLogger(getClass().getName());
    }
}
