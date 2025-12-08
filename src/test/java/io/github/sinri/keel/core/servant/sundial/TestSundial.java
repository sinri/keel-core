package io.github.sinri.keel.core.servant.sundial;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class TestSundial extends Sundial {
    private static final TestSundialPlan testSundialPlan = new TestSundialPlan();

    public TestSundial(@NotNull Keel keel) {
        super(keel);
    }

    @Override
    protected @NotNull LoggerFactory getLoggerFactory() {
        return getKeel().getLoggerFactory();
    }

    @Override
    protected @NotNull Future<Collection<SundialPlan>> fetchPlans() {
        return Future.succeededFuture(List.of(testSundialPlan));
    }
}
