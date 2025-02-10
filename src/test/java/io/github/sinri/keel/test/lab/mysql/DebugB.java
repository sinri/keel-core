package io.github.sinri.keel.test.lab.mysql;

import io.github.sinri.keel.facade.tesuto.instant.InstantRunUnit;
import io.github.sinri.keel.facade.tesuto.instant.KeelInstantRunner;
import io.github.sinri.keel.integration.mysql.DynamicNamedMySQLConnection;
import io.github.sinri.keel.integration.mysql.KeelMySQLDataSourceProvider;
import io.github.sinri.keel.integration.mysql.NamedMySQLDataSource;
import io.github.sinri.keel.integration.mysql.exception.KeelSQLResultRowIndexError;
import io.github.sinri.keel.integration.mysql.statement.AnyStatement;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class DebugB extends KeelInstantRunner {
    private NamedMySQLDataSource<DynamicNamedMySQLConnection> dataSource;

    @Nonnull
    @Override
    protected Future<Void> starting() {
        return super.starting()
                .compose(v -> {
                    Keel.getConfiguration().loadPropertiesFile("config.properties");
                    dataSource = KeelMySQLDataSourceProvider.initializeDynamicNamedMySQLDataSource("cs");
                    return Future.succeededFuture();
                });
    }

    @InstantRunUnit
    public Future<Void> insertIt() {
        return dataSource.withConnection(dynamicNamedMySQLConnection -> {
                    return AnyStatement.insert(w -> {
                                w
                                        .intoTable("cornerstone.lab_1")
                                        .macroWriteOneRow(r -> r
                                                .put("j1", new JsonObject().put("a", "b"))
                                                .put("j2", new JsonArray()
                                                        .add(1)
                                                        .add(2.3)
                                                        .add(false)
                                                        .add(null)
                                                        .add("asd")
                                                        .add(new JsonObject()
                                                                .put("k", "v")
                                                        )
                                                )
                                                .put("j3", null)
                                                .put("j4", "234")
                                        );
                                           getIssueRecorder().info("sql: " + w);
                            })
                            .executeForLastInsertedID(dynamicNamedMySQLConnection);
                })
                .compose(id -> {
                    getIssueRecorder().info("id: " + id);
                    return Future.succeededFuture();
                });
    }

    @InstantRunUnit
    public Future<Void> readIt() {
        return dataSource.withConnection(dynamicNamedMySQLConnection -> {
                    return AnyStatement.select(s -> s.from("cornerstone.lab_1")
                                    .where(conditionsComponent -> conditionsComponent
                                            .expressionEqualsNumericValue("id", 1))
                            )
                            .execute(dynamicNamedMySQLConnection);
                })
                .compose(resultMatrix -> {
                    try {
                        var row = resultMatrix.getFirstRow();
                        getIssueRecorder().info("j1: " + row.getValue("j1").getClass());
                        getIssueRecorder().info("j2: " + row.getValue("j2").getClass());
                        //getLogger().info("j3: " + row.getValue("j3").getClass());
                        getIssueRecorder().info("j4: " + row.getValue("j4").getClass());
                    } catch (KeelSQLResultRowIndexError e) {
                        throw new RuntimeException(e);
                    }
                    return Future.succeededFuture();
                });


    }
}
