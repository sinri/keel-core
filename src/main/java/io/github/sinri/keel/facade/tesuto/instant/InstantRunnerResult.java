package io.github.sinri.keel.facade.tesuto.instant;

import javax.annotation.Nonnull;

/**
 * Represents the result of a test unit executed by the {@link KeelInstantRunner}.
 * This class encapsulates the outcome, including whether the test was completed, failed, or skipped,
 * and provides methods to query these states.
 *
 * @since 3.0.14
 */
public class InstantRunnerResult {
    private final @Nonnull String testName;
    private Long spentTime;
    private Boolean done;
    private Throwable cause;
    private Boolean skipped;

    public InstantRunnerResult(@Nonnull String name) {
        this.testName = name;
    }

    public void declareDone() {
        this.done = true;
    }

    public void declareFailed(Throwable cause) {
        this.done = false;
        this.cause = cause;
        this.skipped = false;
    }

    public void declareSkipped() {
        this.skipped = true;
    }

    @Nonnull
    public String getTestName() {
        return testName;
    }

    public Long getSpentTime() {
        return spentTime;
    }

    public InstantRunnerResult setSpentTime(Long spentTime) {
        this.spentTime = spentTime;
        return this;
    }

    public boolean isDone() {
        return done != null && done;
    }

    public boolean isFailed() {
        return done != null && !done;
    }


    public Throwable getCause() {
        return cause;
    }

    public boolean isSkipped() {
        return skipped != null && skipped;
    }
}
