package io.github.sinri.keel.facade.tesuto.instant;

import io.vertx.core.Future;

import java.lang.reflect.Method;

/**
 * @since 3.0.10
 */
class InstantRunUnitWrapper {
    private final Method method;
    private final InstantRunUnit annotation;
    private final InstantRunnerResult testUnitResult;


    public InstantRunUnitWrapper(Method method, InstantRunUnit annotation) {
        this.method = method;
        this.annotation = annotation;
        this.testUnitResult = new InstantRunnerResult(method.getName());
    }

    public Future<InstantRunnerResult> runTest(KeelInstantRunner testInstance) {
        long startTime = System.currentTimeMillis();

        return Future.succeededFuture(annotation.skip())
                .compose(toSkip -> {
                    if (toSkip) {
                        long endTime = System.currentTimeMillis();
                        this.testUnitResult.setSpentTime(endTime - startTime);
                        this.testUnitResult.declareSkipped();
                        return Future.succeededFuture();
                    } else {
                        return Future.succeededFuture()
                                .compose(vv -> {
//                                    testInstance.getLogger().setDynamicEventLogFormatter(keelEventLog -> {
//                                        keelEventLog.classification(this.getName());
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
                                    this.testUnitResult.setSpentTime(endTime - startTime).declareFailed(throwable);
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
        return this.annotation.skip();
    }
}
