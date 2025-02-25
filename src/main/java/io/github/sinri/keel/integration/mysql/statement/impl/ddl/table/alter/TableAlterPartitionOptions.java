package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter;

public class TableAlterPartitionOptions {
    private final String raw;

    public TableAlterPartitionOptions(String raw) {
        this.raw = raw;
    }

    @Override
    public String toString() {
        return raw;
    }
}
