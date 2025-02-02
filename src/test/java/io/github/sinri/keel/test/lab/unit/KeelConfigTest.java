package io.github.sinri.keel.test.lab.unit;

import io.github.sinri.keel.facade.tesuto.instant.InstantRunUnit;
import io.github.sinri.keel.facade.tesuto.instant.KeelInstantRunner;
import io.vertx.core.Future;

import javax.annotation.Nonnull;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class KeelConfigTest extends KeelInstantRunner {

    @Nonnull
    @Override
    protected Future<Void> starting() {
        Keel.getConfiguration().loadPropertiesFile("config.properties");
        return Future.succeededFuture();
    }

    @InstantRunUnit
    public Future<Void> readTest() {
        getLogger().info(x -> x.message("all").context(Keel.getConfiguration().toJsonObject()));
        getLogger().info("email.smtp.default_smtp_name: " + Keel.config("email.smtp.default_smtp_name"));
        return Future.succeededFuture();
    }
}
