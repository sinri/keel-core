package io.github.sinri.keel.core.servant.sundial;

import io.github.sinri.keel.core.verticles.KeelVerticleImpl;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.Calendar;

/**
 * @since 3.2.4
 * @since 3.2.5 Used in KeelSundial
 * @since 4.0.0 become abstract
 */
final class KeelSundialVerticle extends KeelVerticleImpl<SundialIssueRecord> {
    private final KeelSundialPlan sundialPlan;
    private final Calendar now;
    private final KeelIssueRecordCenter issueRecordCenter;

    public KeelSundialVerticle(@Nonnull KeelSundialPlan sundialPlan, @Nonnull Calendar now,
                               @Nonnull KeelIssueRecordCenter issueRecordCenter) {
        this.sundialPlan = sundialPlan;
        this.now = now;
        this.issueRecordCenter = issueRecordCenter;
    }

    @Nonnull
    @Override
    protected KeelIssueRecorder<SundialIssueRecord> buildIssueRecorder() {
        return issueRecordCenter.generateIssueRecorder(SundialIssueRecord.TopicSundial,
                () -> new SundialIssueRecord(sundialPlan, now, deploymentID()));
    }

    @Override
    protected Future<Void> startVerticle() {
        Future.succeededFuture()
              .compose(v -> {
                  return sundialPlan.execute(now);
              })
              .onComplete(ar -> {
                  undeployMe();
              });
        return Future.succeededFuture();
    }


}
