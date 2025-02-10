package io.github.sinri.keel.facade.tesuto.instant;

import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 3.0.10 Used for universal testing scenario, which could be run in IDE(A).
 * @since 4.0.0 renamed from KeelTest
 */
abstract public class KeelInstantRunner {
    /**
     * @since 3.2.0
     */
    private static KeelIssueRecorder<KeelEventLog> issueRecorder;

    /**
     * It is designed to be called by the subclasses in develop environment (e.g. in IDE).
     */
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, InstantiationException, IllegalAccessException {
        String calledClass = System.getProperty("sun.java.command");

        issueRecorder = KeelIssueRecordCenter.outputCenter()
                                             .generateIssueRecorder("KeelInstantRunner", KeelEventLog::new);
        issueRecorder.setRecordFormatter(eventLog -> {
            eventLog.classification("preparing");
        });

        issueRecorder.debug(r -> r.message("Keel Instant Runner Class: " + calledClass));

        Class<?> aClass = Class.forName(calledClass);

        issueRecorder.debug(r -> r.message("Reflected Class: " + aClass));

        Constructor<?> constructor = aClass.getConstructor();
        var testInstance = constructor.newInstance();

        Method[] methods = aClass.getMethods();

        List<InstantRunUnitWrapper> testUnits = new ArrayList<>();

        for (Method method : methods) {
            InstantRunUnit annotation = method.getAnnotation(InstantRunUnit.class);
            if (annotation == null) {
                continue;
            }
            if (!method.getReturnType().isAssignableFrom(Future.class)) {
                continue;
            }

            InstantRunUnitSkipped skipAnnotated = method.getAnnotation(InstantRunUnitSkipped.class);
            testUnits.add(new InstantRunUnitWrapper(method, annotation, skipAnnotated));
        }

        if (testUnits.isEmpty()) {
            issueRecorder.fatal(r -> r.message("At least one public method with @InstantRunUnit is required."));
            System.exit(1);
        }

        VertxOptions vertxOptions = ((KeelInstantRunner) testInstance).buildVertxOptions();
        Keel.initializeVertxStandalone(vertxOptions);

        AtomicInteger totalPassedRef = new AtomicInteger();
        List<InstantRunnerResult> testUnitResults = new ArrayList<>();

        Future.succeededFuture()
              .compose(v -> {
                  issueRecorder.info(r -> r.message("STARTING..."));
                  return ((KeelInstantRunner) testInstance).starting();
              })
              .compose(v -> {
                  issueRecorder.info(r -> r.message("RUNNING INSTANT UNITS..."));

                  return Keel.asyncCallIteratively(testUnits.iterator(), testUnit -> {
                                 issueRecorder.setRecordFormatter(eventLogger -> {
                                     eventLogger.classification(testUnit.getName());
                                 });
                                 return testUnit.runTest((KeelInstantRunner) testInstance)
                                                .compose(testUnitResult -> {
                                                    testUnitResults.add(testUnitResult);
                                                    return Future.succeededFuture();
                                                });
                             })
                             .onComplete(vv -> {
                                 issueRecorder.setRecordFormatter(eventLogger -> {
                                     eventLogger.classification("conclusion");
                                 });
                                 AtomicInteger totalNonSkippedRef = new AtomicInteger(0);
                                 testUnitResults.forEach(testUnitResult -> {
                                     if (testUnitResult.isSkipped()) {
                                         issueRecorder.info(r -> r.message("☐\tUNIT [" + testUnitResult.getTestName() + "] SKIPPED. Spent " + testUnitResult.getSpentTime() + " ms;"));
                                     } else {
                                         totalNonSkippedRef.incrementAndGet();
                                         if (!testUnitResult.isFailed()) {
                                             totalPassedRef.incrementAndGet();
                                             issueRecorder.info(r -> r.message("☑︎\tUNIT [" + testUnitResult.getTestName() + "] PASSED. Spent " + testUnitResult.getSpentTime() + " ms;"));
                                         } else {
                                             issueRecorder.error(r -> r.message("☒\tUNIT [" + testUnitResult.getTestName() + "] FAILED. Spent " + testUnitResult.getSpentTime() + " ms;"));
                                             issueRecorder.exception(testUnitResult.getCause(), r -> r.message(
                                                     "CAUSED BY THIS"));
                                         }
                                     }
                                 });
                                 issueRecorder.notice(r -> r.message("PASSED RATE: " + totalPassedRef.get() + " / " + totalNonSkippedRef.get() + " i.e. " + (100.0 * totalPassedRef.get() / totalNonSkippedRef.get()) + "%"));
                             });
              })
              .onFailure(throwable -> {
                  issueRecorder.exception(throwable, r -> r.message("ERROR OCCURRED DURING TESTING"));
              })
              .eventually(() -> {
                  return ((KeelInstantRunner) testInstance).ending(testUnitResults);
              })
              .eventually(() -> {
                  return Keel.getVertx().close();
              });
    }

    /**
     * @since 4.0.2
     */
    public static KeelIssueRecorder<KeelEventLog> getIssueRecorder() {
        return issueRecorder;
    }

    protected @Nonnull VertxOptions buildVertxOptions() {
        return new VertxOptions();
    }

    /**
     * @since 3.2.0
     */
    protected void setLogger(@Nonnull KeelIssueRecorder<KeelEventLog> issueRecorder) {
        KeelInstantRunner.issueRecorder = issueRecorder;
    }

    protected @Nonnull Future<Void> starting() {
        return Future.succeededFuture();
    }

    protected @Nonnull Future<Void> ending(List<InstantRunnerResult> testUnitResults) {
        return Future.succeededFuture();
    }

}
