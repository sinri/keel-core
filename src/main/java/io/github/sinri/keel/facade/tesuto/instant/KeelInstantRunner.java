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
import java.util.concurrent.CountDownLatch;
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
    private static KeelIssueRecorder<KeelEventLog> instantLogger;

    /**
     * It is designed to be called by the subclasses in develop environment (e.g. in IDE).
     */
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, InstantiationException, IllegalAccessException {
        String calledClass = System.getProperty("sun.java.command");

        instantLogger = KeelIssueRecordCenter.outputCenter()
                                             .generateIssueRecorder("KeelInstantRunner", KeelEventLog::new);
        instantLogger.setRecordFormatter(eventLog -> {
            eventLog.classification("preparing");
        });

        instantLogger.debug(r -> r.message("Keel Instant Runner Class: " + calledClass));

        Class<?> aClass = Class.forName(calledClass);

        instantLogger.debug(r -> r.message("Reflected Class: " + aClass));

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
            instantLogger.fatal(r -> r.message("At least one public method with @InstantRunUnit is required."));
            System.exit(1);
        }

        VertxOptions vertxOptions = ((KeelInstantRunner) testInstance).buildVertxOptions();
        Keel.initializeVertxStandalone(vertxOptions);

        AtomicInteger totalPassedRef = new AtomicInteger();
        List<InstantRunnerResult> testUnitResults = new ArrayList<>();

        CountDownLatch countDownLatch = new CountDownLatch(testUnits.size());

        Future.succeededFuture()
              .compose(v -> {
                  instantLogger.info(r -> r.message("STARTING..."));
                  return ((KeelInstantRunner) testInstance).starting();
              })
              .compose(v -> {
                  instantLogger.info(r -> r.message("RUNNING INSTANT UNITS..."));

                  return Keel.asyncCallIteratively(testUnits.iterator(), testUnit -> {
                                 instantLogger.setRecordFormatter(eventLogger -> {
                                     eventLogger.classification(testUnit.getName());
                                 });
                                 return testUnit.runTest((KeelInstantRunner) testInstance)
                                                .compose(testUnitResult -> {
                                                    testUnitResults.add(testUnitResult);
                                                    return Future.succeededFuture();
                                                })
                                                .eventually(() -> {
                                                    countDownLatch.countDown();
                                                    return Future.succeededFuture();
                                                })
                                                .compose(vv -> {
                                                    return Future.succeededFuture();
                                                });
                             })
                             .onComplete(vv -> {
                                 instantLogger.setRecordFormatter(eventLogger -> {
                                     eventLogger.classification("conclusion");
                                 });
                                 AtomicInteger totalNonSkippedRef = new AtomicInteger(0);
                                 testUnitResults.forEach(testUnitResult -> {
                                     if (testUnitResult.isSkipped()) {
                                         instantLogger.info(r -> r.message("☐\tUNIT [" + testUnitResult.getTestName() + "] SKIPPED. Spent " + testUnitResult.getSpentTime() + " ms;"));
                                     } else {
                                         totalNonSkippedRef.incrementAndGet();
                                         if (!testUnitResult.isFailed()) {
                                             totalPassedRef.incrementAndGet();
                                             instantLogger.info(r -> r.message("☑︎\tUNIT [" + testUnitResult.getTestName() + "] PASSED. Spent " + testUnitResult.getSpentTime() + " ms;"));
                                         } else {
                                             instantLogger.error(r -> r.message("☒\tUNIT [" + testUnitResult.getTestName() + "] FAILED. Spent " + testUnitResult.getSpentTime() + " ms;"));
                                             instantLogger.exception(testUnitResult.getCause(), r -> r.message(
                                                     "CAUSED BY THIS"));
                                         }
                                     }
                                 });
                                 instantLogger.notice(r -> r.message("PASSED RATE: " + totalPassedRef.get() + " / " + totalNonSkippedRef.get() + " i.e. " + (100.0 * totalPassedRef.get() / totalNonSkippedRef.get()) + "%"));
                             });
              })
              .onFailure(throwable -> {
                  instantLogger.exception(throwable, r -> r.message("ERROR OCCURRED DURING TESTING"));
              })
              .eventually(() -> {
                  return ((KeelInstantRunner) testInstance).ending(testUnitResults);
              })
        //              .eventually(() -> {
        //                  return Keel.getVertx().close();
        //              })
        ;

        try {
            // Wait for the async operation to complete
            countDownLatch.await();
        } catch (InterruptedException e) {
            getInstantLogger().exception(e);
        } finally {
            Keel.close();
        }
    }

    /**
     * @since 4.0.2
     */
    public static KeelIssueRecorder<KeelEventLog> getInstantLogger() {
        return instantLogger;
    }

    protected @Nonnull VertxOptions buildVertxOptions() {
        return new VertxOptions();
    }

    //    /**
    //     * @since 3.2.0
    //     */
    //    protected void setInstantLogger(@Nonnull KeelIssueRecorder<KeelEventLog> logger) {
    //        KeelInstantRunner.instantLogger = logger;
    //    }

    protected @Nonnull Future<Void> starting() {
        return Future.succeededFuture();
    }

    protected @Nonnull Future<Void> ending(List<InstantRunnerResult> testUnitResults) {
        return Future.succeededFuture();
    }

}
