package io.github.sinri.keel.test.lab.elasticsearch;

import io.github.sinri.keel.facade.tesuto.instant.InstantRunUnit;
import io.github.sinri.keel.facade.tesuto.instant.KeelInstantRunner;
import io.github.sinri.keel.integration.elasticsearch.ESApiMixin;
import io.github.sinri.keel.integration.elasticsearch.ElasticSearchKit;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;

import javax.annotation.Nonnull;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class ESCatIndicesTest extends KeelInstantRunner {
    @Nonnull
    @Override
    protected Future<Void> starting() {
        Keel.getConfiguration().loadPropertiesFile("config.properties");
        return super.starting();
    }

    @InstantRunUnit
    public Future<Void> test() {
        // {"client_code":"ai-test","timestamp":1712023360984,"checksum":"d6abf7d98af34907d97f6a6578a429b5","http_method":"GET","endpoint":"/_cat/indices"}
        ESApiMixin.ESApiQueries esApiQueries = new ESApiMixin.ESApiQueries();
        //esApiQueries.put("format", "application/json");
        return new ElasticSearchKit("kumori")
                .call(
                        HttpMethod.GET,
                        //"/_cat/indices",
                        "/*",
                        esApiQueries,
                        null
                )
                .compose(resp -> {
                    getLogger().info(x -> x.message("resp").context(resp));
                    return Future.succeededFuture();
                });
    }
}
