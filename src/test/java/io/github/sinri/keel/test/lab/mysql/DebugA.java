package io.github.sinri.keel.test.lab.mysql;

import io.github.sinri.keel.facade.tesuto.instant.InstantRunUnit;
import io.github.sinri.keel.facade.tesuto.instant.KeelInstantRunner;
import io.github.sinri.keel.integration.mysql.KeelMySQLConfiguration;
import io.vertx.core.Future;

import javax.annotation.Nonnull;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class DebugA extends KeelInstantRunner {

    @Nonnull
    @Override
    protected Future<Void> starting() {
        Keel.getConfiguration().loadPropertiesFile("config.properties");
        return Future.succeededFuture();
    }

    @InstantRunUnit
    public Future<Void> test1() {
        KeelMySQLConfiguration pioneerConfig = KeelMySQLConfiguration.loadConfigurationForDataSource(Keel.getConfiguration(), "pioneer");
        return pioneerConfig.instantQuery(
                        "select 1"
                )
                .compose(resultMatrix -> {
                    getLogger().info(x -> x.message("lalala: ").context(j -> j.put("result_matrix", resultMatrix.toJsonArray())));
                    return Future.succeededFuture();
                });
    }
}
