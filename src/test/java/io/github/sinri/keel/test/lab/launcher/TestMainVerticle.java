package io.github.sinri.keel.test.lab.launcher;

import io.github.sinri.keel.core.async.KeelAsyncKit;
import io.github.sinri.keel.core.verticles.KeelVerticleImplWithEventLogger;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.vertx.core.Future;

public class TestMainVerticle extends KeelVerticleImplWithEventLogger {


    @Override
    protected void startAsKeelVerticle() {
        KeelAsyncKit.endless(() -> {
            getLogger().info(r -> r.message("X"));
            return Future.succeededFuture();
        });
    }

    @Override
    protected KeelEventLogger buildEventLogger() {
        return KeelIssueRecordCenter.outputCenter().generateEventLogger(getClass().getName());
    }
}
