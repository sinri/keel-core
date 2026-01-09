package io.github.sinri.keel.core.servant.sundial;

import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;
import java.util.List;

@NullMarked
public class TestSundial extends Sundial {
    private static final TestVirtualThreadSundialPlan testSundialPlan = new TestVirtualThreadSundialPlan();

    public TestSundial() {
        super();
    }

    @Override
    protected LoggerFactory getLoggerFactory() {
        return LoggerFactory.getShared();
    }

    @Override
    protected Future<Collection<SundialPlan>> fetchPlans() {
        return Future.succeededFuture(List.of(testSundialPlan));
    }
}
