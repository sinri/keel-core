package io.github.sinri.keel.test.lab.launcher;

import io.github.sinri.keel.core.verticles.KeelVerticleImpl;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.Future;

import javax.annotation.Nonnull;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class TestMainVerticle extends KeelVerticleImpl<KeelEventLog> {

    @Override
    protected Future<Void> startVerticle() {
        Keel.asyncCallEndlessly(() -> {
            getIssueRecorder().info(r -> r.message("X"));
            return Future.succeededFuture();
        });
        return Future.succeededFuture();
    }

    @Nonnull
    @Override
    protected KeelIssueRecorder<KeelEventLog> buildIssueRecorder() {
        return KeelIssueRecordCenter.outputCenter().generateIssueRecorder(getClass().getName(), KeelEventLog::new);
    }
}
