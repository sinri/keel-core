package io.github.sinri.keel.integration.mysql.statement.templated;

import io.github.sinri.keel.integration.mysql.statement.AbstractStatement;
import io.github.sinri.keel.integration.mysql.statement.mixin.ModifyStatementMixin;
import io.vertx.core.Handler;

import javax.annotation.Nonnull;

/**
 * @since 3.0.8
 */
public class TemplatedModifyStatement extends AbstractStatement implements ModifyStatementMixin, TemplatedStatement {
    private final String templateSql;
    private final TemplateArgumentMapping argumentMapping;

    public TemplatedModifyStatement(@Nonnull String templateSql) {
        this.templateSql = templateSql;
        this.argumentMapping = new TemplateArgumentMapping();
    }

    @Override
    public String toString() {
        return this.build();
    }

    @Override
    public String getSqlTemplate() {
        return this.templateSql;
    }

    @Override
    public TemplateArgumentMapping getArguments() {
        return argumentMapping;
    }

    public TemplatedModifyStatement bindArguments(@Nonnull Handler<TemplateArgumentMapping> binder) {
        binder.handle(this.argumentMapping);
        return this;
    }
}
