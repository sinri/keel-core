package io.github.sinri.keel.integration.mysql.statement.component;

import io.github.sinri.keel.core.ValueBox;
import io.github.sinri.keel.integration.mysql.condition.*;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 1.9 all the callback function could return null safely. by Sinri 2020-02-07
 */
public class ConditionsComponent {
    protected final List<MySQLCondition> conditions;

    public ConditionsComponent() {
        conditions = new ArrayList<>();
    }

    public ConditionsComponent(@Nonnull ConditionsComponent another) {
        this.conditions = new ArrayList<>(another.conditions);
    }

    public boolean isEmpty() {
        return this.conditions.isEmpty();
    }

    /**
     * @param expression Not be quoted, may be fields, functions, etc.
     * @param value      Be quoted, number, string.
     * @since 3.1.8
     */
    public ConditionsComponent expressionEqualsLiteralValue(@Nonnull String expression, @Nonnull Object value) {
        return this.comparison(compareCondition -> compareCondition
                .compareExpression(expression)
                .operator(CompareCondition.OP_EQ)
                .againstLiteralValue(value)
        );
    }

    /**
     * @since 4.1.0
     */
    public ConditionsComponent expressionEqualsLiteralValueIfNonNull(@Nonnull String expression, @Nonnull ValueBox<Object> valueBox) {
        if (valueBox.isValueSetAndNotNull()) {
            return expressionEqualsLiteralValue(expression, valueBox.getNonNullValue());
        } else {
            return this;
        }
    }

    /**
     * @param expression Not be quoted, may be fields, functions, etc.
     * @param value      Be quoted after stringify. BigDecimal would not be plain string.
     * @since 3.1.8
     */
    public ConditionsComponent expressionNotLiteralValue(@Nonnull String expression, @Nonnull Object value) {
        return this.comparison(compareCondition -> compareCondition
                .compareExpression(expression)
                .operator(CompareCondition.OP_NEQ)
                .againstLiteralValue(value)
        );
    }

    /**
     * @since 4.1.0
     */
    public ConditionsComponent expressionNotLiteralValueIfNonNull(@Nonnull String expression, @Nonnull ValueBox<Object> valueBox) {
        if (valueBox.isValueSetAndNotNull()) {
            return expressionNotLiteralValue(expression, valueBox.getNonNullValue());
        } else {
            return this;
        }
    }

    /**
     * @param expression Not be quoted, may be fields, functions, etc.
     * @param value      Be quoted, numeric.
     * @since 3.1.8
     */
    public ConditionsComponent expressionEqualsNumericValue(@Nonnull String expression, @Nonnull Number value) {
        return this.comparison(compareCondition -> compareCondition
                .compareExpression(expression)
                .operator(CompareCondition.OP_EQ)
                .againstNumericValue(value)
        );
    }

    /**
     * @since 4.1.0
     */
    public ConditionsComponent expressionEqualsNumericValueIfNonNull(@Nonnull String expression, @Nonnull ValueBox<Number> valueBox) {
        if (valueBox.isValueSetAndNotNull()) {
            return expressionEqualsNumericValue(expression, valueBox.getNonNullValue());
        } else {
            return this;
        }
    }

    /**
     * @param expression Not be quoted, may be fields, functions, etc.
     * @param value      Be quoted, numeric. BigDecimal would not be plain string.
     * @since 3.1.8
     */
    public ConditionsComponent expressionNotNumericValue(@Nonnull String expression, @Nonnull Number value) {
        return this.comparison(compareCondition -> compareCondition
                .compareExpression(expression)
                .operator(CompareCondition.OP_NEQ)
                .againstNumericValue(value)
        );
    }

    public ConditionsComponent expressionNotNumericValueIfNonNull(@Nonnull String expression, @Nonnull ValueBox<Number> valueBox) {
        if (valueBox.isValueSetAndNotNull()) {
            return expressionNotNumericValue(expression, valueBox.getNonNullValue());
        } else {
            return this;
        }
    }

    /**
     * @param expression Not be quoted, may be fields, functions, etc.
     * @since 3.1.8
     */
    public ConditionsComponent expressionIsNull(@Nonnull String expression) {
        return this.comparison(compareCondition -> compareCondition
                .compareExpression(expression)
                .isNull()
        );
    }

    /**
     * @param expression Not be quoted, may be fields, functions, etc.
     * @since 3.1.8
     */
    public ConditionsComponent expressionIsNotNull(@Nonnull String expression) {
        return this.comparison(compareCondition -> compareCondition
                .compareExpression(expression)
                .not()
                .isNull()
        );
    }


    public ConditionsComponent comparison(@Nonnull Function<CompareCondition, CompareCondition> function) {
        CompareCondition condition = function.apply(new CompareCondition());
        if (condition != null) {
            conditions.add(condition);
        }
        return this;
    }

    public ConditionsComponent comparison(@Nonnull String operator, @Nonnull Function<CompareCondition, CompareCondition> function) {
        CompareCondition condition = function.apply(new CompareCondition(operator));
        if (condition != null) {
            conditions.add(condition);
        }
        return this;
    }

    /**
     * @param expression Not be quoted, may be fields, functions, etc.
     * @param values     Be quoted each, as number or string.
     * @since 3.1.8
     */
    public ConditionsComponent expressionAmongLiteralValues(@Nonnull String expression, @Nonnull Collection<?> values) {
        return this.among(amongstCondition -> amongstCondition
                .elementAsExpression(expression)
                .amongstLiteralValueList(values)
        );
    }

    /**
     * @since 4.1.0
     */
    public ConditionsComponent expressionAmongLiteralValuesIfNotEmpty(@Nonnull String expression, @Nonnull Collection<?> values) {
        if (values.isEmpty()) return this;
        return expressionAmongLiteralValues(expression, values);
    }

    /**
     * @param expression Not be quoted, may be fields, functions, etc.
     * @param values     Be quoted each, as number or string.
     * @since 3.1.8
     */
    public ConditionsComponent expressionAmongNumericValues(@Nonnull String expression, @Nonnull Collection<? extends Number> values) {
        return this.among(amongstCondition -> amongstCondition
                .elementAsExpression(expression)
                .amongstNumericValueList(values)
        );
    }

    /**
     * @since 4.1.0
     */
    public ConditionsComponent expressionAmongNumericValuesIfNotEmpty(@Nonnull String expression, @Nonnull Collection<? extends Number> values) {
        if (values.isEmpty()) return this;
        return expressionAmongNumericValues(expression, values);
    }

    /**
     * @param expression Not be quoted, may be fields, functions, etc.
     * @param values     Be quoted each, as number or string.
     * @since 3.1.8
     */
    public ConditionsComponent expressionNotInLiteralValues(@Nonnull String expression, @Nonnull Collection<?> values) {
        return this.among(amongstCondition -> amongstCondition
                .elementAsExpression(expression)
                .not()
                .amongstLiteralValueList(values)
        );
    }

    /**
     * @since 4.1.0
     */
    public ConditionsComponent expressionNotInLiteralValuesIfNotEmpty(@Nonnull String expression, @Nonnull Collection<?> values) {
        if (values.isEmpty()) return this;
        return expressionNotInLiteralValues(expression, values);
    }

    /**
     * @param expression Not be quoted, may be fields, functions, etc.
     * @param values     Be quoted each, as number or string.
     * @since 3.1.8
     */
    public ConditionsComponent expressionNotInNumericValues(@Nonnull String expression, @Nonnull Collection<? extends Number> values) {
        return this.among(amongstCondition -> amongstCondition
                .elementAsExpression(expression)
                .not()
                .amongstNumericValueList(values)
        );
    }

    /**
     * @since 4.1.0
     */
    public ConditionsComponent expressionNotInNumericValuesIfNotEmpty(@Nonnull String expression, @Nonnull Collection<? extends Number> values) {
        if (values.isEmpty()) return this;
        return expressionNotInNumericValues(expression, values);
    }

    public ConditionsComponent among(@Nonnull Function<AmongstCondition, AmongstCondition> function) {
        AmongstCondition condition = function.apply(new AmongstCondition());
        if (condition != null) {
            conditions.add(condition);
        }
        return this;
    }

    public ConditionsComponent intersection(@Nonnull Function<GroupCondition, GroupCondition> function) {
        GroupCondition condition = function.apply(new GroupCondition(GroupCondition.JUNCTION_FOR_AND));
        if (condition != null) {
            conditions.add(condition);
        }
        return this;
    }

    public ConditionsComponent union(@Nonnull Function<GroupCondition, GroupCondition> function) {
        GroupCondition condition = function.apply(new GroupCondition(GroupCondition.JUNCTION_FOR_OR));
        if (condition != null) {
            conditions.add(condition);
        }
        return this;
    }

    public ConditionsComponent raw(@Nonnull String raw) {
        if (!raw.isBlank()) {
            conditions.add(new RawCondition(raw));
        }
        return this;
    }

    @Override
    public String toString() {
        if (conditions.isEmpty()) return "";
        return Keel.stringHelper().joinStringArray(conditions, " and ");
    }
}
