package io.github.sinri.keel.integration.mysql.condition;

import io.github.sinri.keel.integration.mysql.Quoter;
import io.github.sinri.keel.integration.mysql.exception.KeelSQLGenerateError;
import io.github.sinri.keel.integration.mysql.statement.mixin.ReadStatementMixin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class AmongstCondition implements MySQLCondition {
    public static final String OP_IN = "IN";
    protected final List<String> targetSet;
    protected String element;
    protected boolean inverseOperator;

    public AmongstCondition() {
        this.inverseOperator = false;
        this.targetSet = new ArrayList<>();
    }

    public AmongstCondition not() {
        this.inverseOperator = true;
        return this;
    }

    public AmongstCondition elementAsExpression(@Nonnull String element) {
        this.element = element;
        return this;
    }

    public AmongstCondition elementAsValue(@Nullable String element) {
        this.element = new Quoter(element).toString();
        return this;
    }

    public AmongstCondition elementAsValue(@Nullable Number element) {
        this.element = new Quoter(element).toString();
        return this;
    }

    /**
     * @since 3.1.8
     */
    public AmongstCondition amongstLiteralValueList(@Nonnull Collection<?> targetSet) {
        for (Object next : targetSet) {
            //this.targetSet.add(new Quoter(String.valueOf(next)).toString());
            this.amongstLiteralValue(next);
        }
        return this;
    }

    /**
     * @since 3.1.8
     */
    public AmongstCondition amongstNumericValueList(@Nonnull Collection<? extends Number> targetSet) {
        for (Number next : targetSet) {
            //this.targetSet.add(new Quoter(String.valueOf(next)).toString());
            this.amongstNumericValue(next);
        }
        return this;
    }

    /**
     * @since 3.1.8
     */
    protected AmongstCondition amongstLiteralValue(@Nullable Object value) {
        if (value == null) {
            this.targetSet.add("NULL");
        } else {
            this.targetSet.add(new Quoter(String.valueOf(value)).toString());
        }
        return this;
    }

    /**
     * @since 3.1.8
     */
    protected AmongstCondition amongstNumericValue(@Nullable Number value) {
        if (value == null) {
            this.targetSet.add("NULL");
        } else {
            if (value instanceof BigDecimal) {
                this.targetSet.add(((BigDecimal) value).toPlainString());
            } else {
                this.targetSet.add(value.toString());
            }
        }
        return this;
    }

    /**
     * @since 3.1.8 protected
     */
    protected AmongstCondition amongstExpression(@Nonnull String value) {
        this.targetSet.add(Objects.requireNonNull(value));
        return this;
    }

    /**
     * @since 3.1.8 renamed from `amongstExpression`
     */
    public AmongstCondition amongstExpressionList(@Nonnull List<String> values) {
        values.forEach(x -> this.amongstExpression(Objects.requireNonNull(x)));
        return this;
    }

    /**
     * @param readStatement A READ Statement, such as SELECT.
     * @since 3.2.4
     */
    public AmongstCondition amongstReadStatement(@Nonnull ReadStatementMixin readStatement) {
        return this.amongstExpression(readStatement.toString());
    }

    /**
     * 生成SQL的比较条件表达式文本。如果出错，则抛出 KeelSQLGenerateError 异常。
     *
     * @throws KeelSQLGenerateError sql generate error
     */
    @Override
    public String toString() {
        if (targetSet.isEmpty()) {
            throw new KeelSQLGenerateError("AmongstCondition Target Set Empty");
        }

        String s = element;
        if (inverseOperator) {
            s += " NOT";
        }
        s += " " + OP_IN + " (" + Keel.stringHelper().joinStringArray(targetSet, ",") + ")";
        return s;
    }
}
