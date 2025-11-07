package io.github.sinri.keel.integration.elasticsearch;


import io.github.sinri.keel.base.configuration.KeelConfigElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 3.0.7
 */
public class ElasticSearchConfig extends KeelConfigElement {

    public ElasticSearchConfig(KeelConfigElement configuration) {
        super(configuration);
    }

    public ElasticSearchConfig(String esKey) {
        this(Keel.getConfiguration().extract("es", esKey));
    }

    /*
    String esUsername = Keel.config("es.kumori.username");
        String esPassword = Keel.config("es.kumori.password");
        String esClusterHost = Keel.config("es.kumori.cluster.host");
        int esClusterPort = Objects.requireNonNullElse(Integer.valueOf(Keel.config("es.kumori.cluster.port")), 9200);
        String esClusterScheme = Objects.requireNonNullElse(Keel.config("es.kumori.cluster.scheme"), "http");
        int esPoolSize = Objects.requireNonNullElse(Integer.valueOf(Keel.config("es.kumori.pool.size")), 16);
        int esRestMaxConnection = Objects.requireNonNullElse(Integer.valueOf(Keel.config("es.kumori.rest.maxConnection")), 320);
        int esRestMaxConnectionPerRoute = Objects.requireNonNullElse(Integer.valueOf(Keel.config("es.kumori.rest.maxConnectionPerRoute")), 160);

     */

    public String username() {
        return readString("username", null);
    }

    public String password() {
        return readString("password", null);
    }

    public @Nonnull String clusterHost() {
        return Objects.requireNonNull(readString(List.of("cluster", "host"), null));
    }

    public int clusterPort() {
        return readInteger(List.of("cluster", "port"), 9200);
    }

    public @Nonnull String clusterScheme() {
        return Objects.requireNonNull(readString(List.of("cluster", "scheme"), "http"));
    }

    public @Nonnull String clusterApiUrl(@Nonnull String endpoint) {
        return this.clusterScheme() + "://" + this.clusterHost() + ":" + this.clusterPort() + endpoint;
    }

    public @Nullable String opaqueId() {
        return readString("opaqueId", null);
    }

    /**
     * @return Version Components in List
     * @since 3.2.20
     */
    public @Nullable List<Integer> version() {
        var x = readString(List.of("version"));
        if (x == null) return null;
        List<Integer> l = new ArrayList<>();
        for (var c : x.split("[.]")) {
            int i = Integer.parseInt(c.trim());
            l.add(i);
        }
        return l;
    }
}
