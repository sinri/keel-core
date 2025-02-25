package io.github.sinri.keel.test.unittest.integration.mysql.statement;

import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.component.TableCreateDefinitionForColumn;
import io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.create.CreateTableStatement;
import org.junit.jupiter.api.Test;

public class CreateTableStatementTest extends KeelUnitTest {
    @Test
    public void test1() {
        CreateTableStatement.setSqlComponentSeparator("\n");
        String sql = new CreateTableStatement()
                .setTableName("t1")
                .addDefinition(new TableCreateDefinitionForColumn()
                        .setColumnName("c1")
                        .setDataType("bigint(20)")
                        .setAutoIncrement(true)
                )
                .addDefinition(new TableCreateDefinitionForColumn()
                        .setColumnName("c2")
                        .setDataType("varchar(20)")
                        .setDefaultExpression("123'12313")
                        .setComment("gf62_+#~#@%'daf\"uvadf7i6gf")
                        .setNullable(true)
                )
                .addPrimaryKeyDefinition(x -> x.addKeyPart("c1"))
                .toString();
        getUnitTestLogger().info("sql: " + sql);
    }
}
