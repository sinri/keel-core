# MySQL Condition 包文档

## 概述

`io.github.sinri.keel.integration.mysql.condition` 包提供了用于构建 MySQL WHERE 条件的类和接口。该包支持各种类型的条件表达式，包括比较条件、逻辑组合条件、IN/NOT IN 条件以及原始 SQL 条件。

## 核心接口

### MySQLCondition

所有条件类的基础接口，定义了生成 SQL 条件表达式的基本契约。

```java
public interface MySQLCondition {
    String toString();
}
```

- **作用**: 生成 SQL 的条件表达式文本
- **异常**: 如果生成失败，会抛出 `KeelSQLGenerateError` 异常
- **版本**: 自 2.8 版本起成为接口

## 条件类型

### 1. CompareCondition - 比较条件

用于构建各种比较操作的条件，如等于、不等于、大于、小于、LIKE 等。

#### 支持的操作符

| 常量 | 值 | 描述 |
|------|-----|------|
| `OP_EQ` | `=` | 等于 |
| `OP_NEQ` | `<>` | 不等于 |
| `OP_NULL_SAFE_EQ` | `<=>` | NULL 安全等于 |
| `OP_GT` | `>` | 大于 |
| `OP_EGT` | `>=` | 大于等于 |
| `OP_LT` | `<` | 小于 |
| `OP_ELT` | `<=` | 小于等于 |
| `OP_IS` | `IS` | IS 操作符 |
| `OP_LIKE` | `LIKE` | LIKE 操作符 |

#### 主要方法

**操作符设置方法 (3.1.8+)**:
- `beEqual()` - 设置为等于操作
- `beNotEqual()` - 设置为不等于操作
- `beEqualNullSafe()` - 设置为 NULL 安全等于
- `beGreaterThan()` - 设置为大于操作
- `beEqualOrGreaterThan()` - 设置为大于等于操作
- `beLessThan()` - 设置为小于操作
- `beEqualOrLessThan()` - 设置为小于等于操作

**左侧表达式设置**:
- `compareExpression(Object leftSide)` - 设置左侧为表达式（不加引号）
- `compareValue(Object leftSide)` - 设置左侧为值（自动加引号）

**右侧表达式设置**:
- `againstExpression(String rightSide)` - 设置右侧为表达式
- `againstLiteralValue(Object rightSide)` - 设置右侧为字面值（自动加引号）
- `againstNumericValue(Number rightSide)` - 设置右侧为数值

**特殊条件方法**:
- `isNull()` - IS NULL 条件
- `isTrue()` - IS TRUE 条件
- `isFalse()` - IS FALSE 条件
- `isUnknown()` - IS UNKNOWN 条件

**字符串匹配方法**:
- `contains(String rightSide)` - 包含匹配（LIKE '%value%'）
- `hasPrefix(String rightSide)` - 前缀匹配（LIKE 'value%'）
- `hasSuffix(String rightSide)` - 后缀匹配（LIKE '%value'）

**便捷方法 (3.1.8+)**:
- `expressionEqualsLiteralValue(String expression, Object value)` - 表达式等于字面值
- `expressionEqualsNumericValue(String expression, Number value)` - 表达式等于数值

**取反操作**:
- `not()` - 对整个条件取反

#### 使用示例

```java
// 基本比较
CompareCondition condition1 = new CompareCondition()
    .compareExpression("age")
    .beGreaterThan()
    .againstNumericValue(18);
// 生成: age > 18

// 字符串匹配
CompareCondition condition2 = new CompareCondition()
    .compareExpression("name")
    .contains("John");
// 生成: name LIKE '%John%'

// NULL 检查
CompareCondition condition3 = new CompareCondition()
    .compareExpression("email")
    .isNull();
// 生成: email IS NULL

// 取反条件
CompareCondition condition4 = new CompareCondition()
    .compareExpression("status")
    .beEqual()
    .againstLiteralValue("active")
    .not();
// 生成: NOT (status = 'active')
```

### 2. GroupCondition - 逻辑组合条件

用于将多个条件通过 AND 或 OR 逻辑连接。

#### 连接类型

| 常量 | 值 | 描述 |
|------|-----|------|
| `JUNCTION_FOR_AND` | `AND` | AND 逻辑连接 |
| `JUNCTION_FOR_OR` | `OR` | OR 逻辑连接 |

#### 主要方法

- `GroupCondition(String junction)` - 构造函数，指定连接类型
- `GroupCondition(String junction, List<MySQLCondition> conditions)` - 构造函数，指定连接类型和初始条件列表
- `add(MySQLCondition condition)` - 添加单个条件
- `add(List<MySQLCondition> conditions)` - 添加条件列表

#### 使用示例

```java
// AND 组合
GroupCondition andGroup = new GroupCondition(GroupCondition.JUNCTION_FOR_AND)
    .add(new CompareCondition().compareExpression("age").beGreaterThan().againstNumericValue(18))
    .add(new CompareCondition().compareExpression("status").beEqual().againstLiteralValue("active"));
// 生成: (age > 18 AND status = 'active')

// OR 组合
GroupCondition orGroup = new GroupCondition(GroupCondition.JUNCTION_FOR_OR)
    .add(new CompareCondition().compareExpression("type").beEqual().againstLiteralValue("admin"))
    .add(new CompareCondition().compareExpression("type").beEqual().againstLiteralValue("manager"));
// 生成: (type = 'admin' OR type = 'manager')

// 嵌套组合
GroupCondition nestedGroup = new GroupCondition(GroupCondition.JUNCTION_FOR_AND)
    .add(new CompareCondition().compareExpression("active").beEqual().againstNumericValue(1))
    .add(orGroup);
// 生成: (active = 1 AND (type = 'admin' OR type = 'manager'))
```

### 3. AmongstCondition - IN/NOT IN 条件

用于构建 IN 和 NOT IN 条件，支持值列表和子查询。

#### 主要方法

**元素设置**:
- `elementAsExpression(String element)` - 设置元素为表达式
- `elementAsValue(String element)` - 设置元素为字符串值
- `elementAsValue(Number element)` - 设置元素为数值

**目标集合设置**:
- `amongstLiteralValueList(Collection<?> targetSet)` - 设置为字面值列表
- `amongstNumericValueList(Collection<? extends Number> targetSet)` - 设置为数值列表
- `amongstExpressionList(List<String> values)` - 设置为表达式列表
- `amongstReadStatement(ReadStatementMixin readStatement)` - 设置为子查询

**取反操作**:
- `not()` - 转换为 NOT IN

#### 使用示例

```java
// 基本 IN 条件
AmongstCondition condition1 = new AmongstCondition()
    .elementAsExpression("user_id")
    .amongstNumericValueList(Arrays.asList(1, 2, 3, 4, 5));
// 生成: user_id IN (1,2,3,4,5)

// NOT IN 条件
AmongstCondition condition2 = new AmongstCondition()
    .elementAsExpression("status")
    .amongstLiteralValueList(Arrays.asList("deleted", "banned"))
    .not();
// 生成: user_id NOT IN ('deleted','banned')

// 子查询 IN 条件
AmongstCondition condition3 = new AmongstCondition()
    .elementAsExpression("department_id")
    .amongstReadStatement(selectStatement);
// 生成: department_id IN (SELECT ...)
```

### 4. RawCondition - 原始 SQL 条件

用于直接使用原始 SQL 条件表达式，适用于复杂或特殊的 SQL 条件。

#### 主要方法

- `RawCondition()` - 默认构造函数
- `RawCondition(String rawConditionExpression)` - 构造函数，指定原始表达式
- `setRawConditionExpression(String rawConditionExpression)` - 设置原始表达式

#### 使用示例

```java
// 原始 SQL 条件
RawCondition condition1 = new RawCondition("YEAR(created_at) = 2023");
// 生成: YEAR(created_at) = 2023

// 复杂函数条件
RawCondition condition2 = new RawCondition("MATCH(title, content) AGAINST('search term' IN BOOLEAN MODE)");
// 生成: MATCH(title, content) AGAINST('search term' IN BOOLEAN MODE)
```

## 版本变更说明

### 3.1.8 版本更新

- 引入了新的类型安全方法，如 `againstLiteralValue()` 和 `againstNumericValue()`
- 添加了便捷的操作符设置方法，如 `beEqual()`, `beGreaterThan()` 等
- 标记了多个旧方法为 `@Deprecated`，建议使用新的类型安全方法

### 3.2.4 版本更新

- `AmongstCondition` 添加了 `amongstReadStatement()` 方法，支持子查询

## 最佳实践

1. **类型安全**: 优先使用 3.1.8+ 版本引入的类型安全方法
2. **避免 SQL 注入**: 使用 `againstLiteralValue()` 而不是直接拼接字符串
3. **数值处理**: 对于数值类型，使用 `againstNumericValue()` 或 `amongstNumericValueList()`
4. **复杂条件**: 使用 `GroupCondition` 组合多个简单条件
5. **特殊需求**: 当标准条件无法满足需求时，使用 `RawCondition`

## 异常处理

所有条件类在生成 SQL 时如果遇到错误，都会抛出 `KeelSQLGenerateError` 异常。常见的错误情况包括：

- `AmongstCondition` 的目标集合为空
- 必要的参数未设置
- SQL 语法错误

## 注意事项

1. 所有条件对象都是可变的，支持链式调用
2. `toString()` 方法会生成最终的 SQL 条件表达式
3. 引号处理由 `Quoter` 类自动完成，无需手动处理
4. 通配符转义在 LIKE 操作中自动处理
