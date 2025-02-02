package io.github.sinri.keel.test.lab.logger.issue;

import io.github.sinri.keel.facade.tesuto.instant.InstantRunUnit;
import io.github.sinri.keel.facade.tesuto.instant.KeelInstantRunner;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.List;

public class IssueStdOutLoggerTest extends KeelInstantRunner {
    private KeelIssueRecordCenter center;

    @Nonnull
    @Override
    protected Future<Void> starting() {
        center = KeelIssueRecordCenter.outputCenter();

        return Future.succeededFuture();
    }

    @InstantRunUnit
    public Future<Void> testForStdOutSyncAlef() {
        KeelIssueRecorder<AlefIssueRecord> recorder = center.generateIssueRecorder("StandoutOutputSync", AlefIssueRecord::new);
        recorder.record(issue -> {
            issue.classification(List.of("IssueLoggerTest", "testForStdOutSync"));
        });
        return Future.succeededFuture();
    }

    @InstantRunUnit
    public Future<Void> testForStdOutSyncBet() {
        KeelIssueRecorder<BetIssueRecord> recorder = center.generateIssueRecorder("StandoutOutputSync", () -> {
            return new BetIssueRecord("testForStdOutSyncBet");
        });
        recorder.warning(t -> {
            t.message("Who is the boss?").setData(1);
        });

        recorder.exception(new NullPointerException("TEST"), t -> t.setData(999));

        return Future.succeededFuture();
    }
}
