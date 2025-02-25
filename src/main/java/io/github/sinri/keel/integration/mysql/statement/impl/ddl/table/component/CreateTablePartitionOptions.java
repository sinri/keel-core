package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.component;

/**
 * It is not standardized.
 *
 * @since 4.0.4
 */
public class CreateTablePartitionOptions {
    private final String raw;

    public CreateTablePartitionOptions(String raw) {
        this.raw = raw;
    }

    @Override
    public String toString() {
        return raw;
    }
}
