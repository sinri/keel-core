package io.github.sinri.keel.integration.mysql.condition;

import io.github.sinri.keel.integration.mysql.Quoter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;

public class CompareCondition implements MySQLCondition {
    public static final String OP_EQ = "=";
    public static final String OP_NEQ = "<>";
    public static final String OP_NULL_SAFE_EQ = "<=>";
    public static final String OP_GT = ">";
    public static final String OP_EGT = ">=";
    public static final String OP_LT = "<";
    public static final String OP_ELT = "<=";
    public static final String OP_IS = "IS";
    public static final String OP_LIKE = "LIKE";
    protected String leftSide;
    protected String operator;
    protected String rightSide;
    protected boolean inverseOperator;

    public CompareCondition() {
        this.leftSide = null;
        this.operator = null;
        this.rightSide = null;
        this.inverseOperator = false;
    }

    public CompareCondition(@Nonnull String operator) {
        this.leftSide = null;
        this.operator = operator;
        this.rightSide = null;
        this.inverseOperator = false;
    }

    /**
     * @since 3.1.8
     */
    public CompareCondition beEqual() {
        this.operator = OP_EQ;
        return this;
    }

    /**
     * @since 3.1.8
     */
    public CompareCondition beNotEqual() {
        this.operator = OP_NEQ;
        return this;
    }

    /**
     * @since 3.1.8
     */
    public CompareCondition beEqualNullSafe() {
        this.operator = OP_NULL_SAFE_EQ;
        return this;
    }

    /**
     * @since 3.1.8
     */
    public CompareCondition beGreaterThan() {
        this.operator = OP_GT;
        return this;
    }

    /**
     * @since 3.1.8
     */
    public CompareCondition beEqualOrGreaterThan() {
        this.operator = OP_EGT;
        return this;
    }

    /**
     * @since 3.1.8
     */
    public CompareCondition beLessThan() {
        this.operator = OP_LT;
        return this;
    }

    /**
     * @since 3.1.8
     */
    public CompareCondition beEqualOrLessThan() {
        this.operator = OP_ELT;
        return this;
    }

    public CompareCondition not() {
        this.inverseOperator = true;
        return this;
    }

    public CompareCondition compareExpression(@Nonnull Object leftSide) {
        this.leftSide = leftSide.toString();
        return this;
    }

    public CompareCondition compareValue(@Nullable Object leftSide) {
        this.leftSide = String.valueOf(new Quoter(String.valueOf(leftSide)));
        return this;
    }

    public CompareCondition operator(@Nonnull String operator) {
        this.operator = operator;
        return this;
    }

    public CompareCondition againstExpression(@Nonnull String rightSide) {
        this.rightSide = rightSide;
        return this;
    }

    /**
     * @since 3.1.8
     */
    public CompareCondition againstLiteralValue(@Nullable Object rightSide) {
        this.rightSide = String.valueOf(new Quoter(String.valueOf(rightSide)));
        return this;
    }

    /**
     * @since 3.1.8
     */
    public CompareCondition againstNumericValue(@Nonnull Number rightSide) {
        if (rightSide instanceof BigDecimal) {
            this.rightSide = ((BigDecimal) rightSide).toPlainString();
        } else {
            this.rightSide = rightSide.toString();
        }
        return this;
    }

    public CompareCondition isNull() {
        this.operator = OP_IS;
        this.rightSide = "NULL";
        return this;
    }

    public CompareCondition isTrue() {
        this.operator = OP_IS;
        this.rightSide = "TRUE";
        return this;
    }

    public CompareCondition isFalse() {
        this.operator = OP_IS;
        this.rightSide = "FALSE";
        return this;
    }

    public CompareCondition isUnknown() {
        this.operator = OP_IS;
        this.rightSide = "UNKNOWN";
        return this;
    }

    public CompareCondition contains(@Nonnull String rightSide) {
        this.operator = OP_LIKE;
        String x = Quoter.escapeStringWithWildcards(rightSide);
        this.rightSide = "'%" + x + "%'";
        return this;
    }

    public CompareCondition hasPrefix(@Nonnull String rightSide) {
        this.operator = "like";
        String x = Quoter.escapeStringWithWildcards(rightSide);
        this.rightSide = "'" + x + "%'";
        return this;
    }

    public CompareCondition hasSuffix(@Nonnull String rightSide) {
        this.operator = "like";
        String x = Quoter.escapeStringWithWildcards(rightSide);
        this.rightSide = "'%" + x + "'";
        return this;
    }

    /**
     * @since 3.1.8
     */
    public CompareCondition expressionEqualsLiteralValue(@Nonnull String expression, @Nullable Object value) {
        if (value == null) {
            return this.compareExpression(expression).isNull();
        }
        return this
                .compareExpression(expression)
                .beEqual()
                .againstLiteralValue(value);
    }

    /**
     * @since 3.1.8
     */
    public CompareCondition expressionEqualsNumericValue(@Nonnull String expression, @Nonnull Number value) {
        return this
                .compareExpression(expression)
                .beEqual()
                .againstNumericValue(value);
    }

    /**
     * 生成SQL的条件表达式文本
     */
    @Override
    public String toString() {
        String x = leftSide + " " + operator + " " + rightSide;
        if (inverseOperator) {
            x = "NOT (" + x + ")";
        }
        return x;
    }
}
