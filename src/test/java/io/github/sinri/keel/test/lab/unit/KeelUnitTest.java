package io.github.sinri.keel.test.lab.unit;


import io.github.sinri.keel.facade.tesuto.instant.InstantRunUnit;
import io.github.sinri.keel.facade.tesuto.instant.InstantRunnerResult;
import io.github.sinri.keel.facade.tesuto.instant.KeelInstantRunner;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.List;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class KeelUnitTest extends KeelInstantRunner {

    @Override
    protected @Nonnull Future<Void> starting() {
        Keel.getConfiguration().loadPropertiesFile("config.properties");
        System.out.println("prepared");
        return Future.succeededFuture();
    }

    @InstantRunUnit
    public Future<Void> test1() {
        System.out.println("test1");
        return Keel.asyncSleep(1000L);
    }

    @InstantRunUnit
    public Future<Void> test2() {
        System.out.println("test2");
        return Keel.asyncSleep(2000L)
                .compose(v -> {
                    return Future.failedFuture(new RuntimeException("ddd"));
                });
    }

    @Nonnull
    @Override
    protected Future<Void> ending(List<InstantRunnerResult> testUnitResults) {
        System.out.println("cleaned with " + testUnitResults.size() + " results");
        return Future.succeededFuture();
    }
}
