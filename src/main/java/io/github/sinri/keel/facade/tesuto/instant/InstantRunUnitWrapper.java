package io.github.sinri.keel.facade.tesuto.instant;

import io.vertx.core.Future;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;

/**
 * A wrapper class for methods annotated with {@link InstantRunUnit} and optionally with {@link InstantRunUnitSkipped}.
 * This class is responsible for managing the execution of a test unit, including skipping the test if necessary,
 * and recording the result of the test.
 *
 * @since 3.0.10
 */
class InstantRunUnitWrapper {
    @Nonnull
    private final Method method;
    @Nonnull
    private final InstantRunUnit annotation;
    @Nullable
    private final InstantRunUnitSkipped skipAnnotated;
    private final InstantRunnerResult testUnitResult;


    public InstantRunUnitWrapper(
            @Nonnull Method method,
            @Nonnull InstantRunUnit annotation,
            @Nullable InstantRunUnitSkipped skipAnnotated
    ) {
        this.method = method;
        this.annotation = annotation;
        this.testUnitResult = new InstantRunnerResult(method.getName());
        this.skipAnnotated = skipAnnotated;
    }

    public Future<InstantRunnerResult> runTest(KeelInstantRunner testInstance) {
        long startTime = System.currentTimeMillis();

        return Future.succeededFuture(isSkip())
                     .compose(toSkip -> {
                         if (toSkip) {
                             long endTime = System.currentTimeMillis();
                             this.testUnitResult.setSpentTime(endTime - startTime);
                             this.testUnitResult.declareSkipped();
                             return Future.succeededFuture();
                         } else {
                             return Future.succeededFuture()
                                          .compose(vv -> {
                                              //                                    testInstance.getLogger()
                                              //                                    .setDynamicEventLogFormatter
                                              //                                    (keelEventLog -> {
                                              //                                        keelEventLog.classification
                                              //                                        (this.getName());
                                              //                                    });
                                              try {
                                                  return (Future<?>) this.method.invoke(testInstance);
                                              } catch (Throwable e) {
                                                  throw new RuntimeException(e);
                                              }
                                          })
                                          .compose(passed -> {
                                              long endTime = System.currentTimeMillis();
                                              this.testUnitResult.setSpentTime(endTime - startTime).declareDone();
                                              return Future.succeededFuture();
                                          }, throwable -> {
                                              long endTime = System.currentTimeMillis();
                                              this.testUnitResult.setSpentTime(endTime - startTime)
                                                                 .declareFailed(throwable);
                                              return Future.succeededFuture();
                                          });
                         }
                     })
                     .compose(v -> {
                         return Future.succeededFuture(testUnitResult);
                     });
    }

    public String getName() {
        return this.method.getName();
    }

    public boolean isSkip() {
        // todo: later remove deprecated `InstantRunUnit::skip`.
        return (this.skipAnnotated != null) || this.annotation.skip();
    }
}
