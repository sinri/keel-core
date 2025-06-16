# Cron 包文档

## 概述

`io.github.sinri.keel.core.cron` 包提供了 Cron 表达式解析和匹配功能。该包包含两个主要类，用于处理定时任务调度中的时间匹配逻辑。

## 类说明

### KeelCronExpression

**功能描述**：Cron 表达式解析器和匹配器，支持标准 Cron 语法的解析和时间匹配。

**版本信息**：自 2.9.3 版本从 `io.github.sinri.keel.servant.sundial` 迁移至此包。

#### 构造方法

```java
public KeelCronExpression(@Nonnull String rawCronExpression)
```

**参数**：
- `rawCronExpression`：原始 Cron 表达式字符串，由 5 个空格分隔的字段组成：
  - 分钟 (0-59)
  - 小时 (0-23)
  - 日期 (1-31)
  - 月份 (1-12)
  - 星期 (0-6，其中 0 表示星期日)

**支持的语法**：
- 具体值：如 `5`
- 通配符：`*` 表示所有可能值
- 范围：如 `1-5` 表示 1 到 5
- 列表：如 `1,3,5` 表示 1、3、5
- 步长：如 `*/5` 表示每 5 个单位

**示例**：
- `"0 0 1 1 0"`：每年 1 月 1 日午夜执行
- `"0 0 * * *"`：每天午夜执行
- `"*/15 * * * *"`：每 15 分钟执行一次

**异常**：
- `RuntimeException`：当 Cron 表达式格式无效时抛出
- `IllegalArgumentException`：当字段值超出范围时抛出

#### 主要方法

##### match(Calendar currentCalendar)

```java
public boolean match(@Nonnull Calendar currentCalendar)
```

**功能**：判断给定的 Calendar 对象是否匹配 Cron 表达式。

**参数**：
- `currentCalendar`：要匹配的 Calendar 对象

**返回值**：如果匹配返回 `true`，否则返回 `false`

##### match(ParsedCalenderElements parsedCalenderElements)

```java
public boolean match(@Nonnull ParsedCalenderElements parsedCalenderElements)
```

**功能**：判断给定的解析后日历元素是否匹配 Cron 表达式。

**参数**：
- `parsedCalenderElements`：解析后的日历元素对象

**返回值**：如果匹配返回 `true`，否则返回 `false`

##### parseCalenderToElements(Calendar currentCalendar)

```java
public static ParsedCalenderElements parseCalenderToElements(@Nonnull Calendar currentCalendar)
```

**功能**：将 Calendar 对象解析为 ParsedCalenderElements 实例。

**参数**：
- `currentCalendar`：要解析的 Calendar 对象

**返回值**：包含解析后日期时间组件的 ParsedCalenderElements 实例

**版本信息**：自 3.2.4 版本添加

##### getRawCronExpression()

```java
@Nonnull
public String getRawCronExpression()
```

**功能**：获取用于初始化此实例的原始 Cron 表达式。

**返回值**：原始 Cron 表达式字符串

#### 内部字段

- `minuteOptions`：分钟选项集合 (Set<Integer>)
- `hourOptions`：小时选项集合 (Set<Integer>)
- `dayOptions`：日期选项集合 (Set<Integer>)
- `monthOptions`：月份选项集合 (Set<Integer>)
- `weekdayOptions`：星期选项集合 (Set<Integer>)

### ParsedCalenderElements

**功能描述**：表示解析后的日历元素，包含分钟、小时、日期、月份、星期和秒。该类用于封装日期时间组件，以便与 Cron 表达式进行匹配。

**版本信息**：自 4.0.0 版本添加

#### 字段

```java
public final int minute;    // 分钟 (0-59)
public final int hour;      // 小时 (0-23)
public final int day;       // 日期 (1-31)
public final int month;     // 月份 (1-12)
public final int weekday;   // 星期 (0-6，0 表示星期日)
public final int second;    // 秒 (0-59)，用于调试
```

#### 构造方法

##### 完整构造方法

```java
public ParsedCalenderElements(int minute, int hour, int day, int month, int weekday, int second)
```

**参数**：
- `minute`：分钟组件 (0-59)
- `hour`：小时组件 (0-23)
- `day`：日期组件 (1-31)
- `month`：月份组件 (1-12)
- `weekday`：星期组件 (0-6，0 表示星期日)
- `second`：秒组件 (0-59)，用于调试

##### 简化构造方法

```java
public ParsedCalenderElements(int minute, int hour, int day, int month, int weekday)
```

**参数**：
- `minute`：分钟组件 (0-59)
- `hour`：小时组件 (0-23)
- `day`：日期组件 (1-31)
- `month`：月份组件 (1-12)
- `weekday`：星期组件 (0-6，0 表示星期日)

**说明**：秒组件默认设置为 0。

##### Calendar 构造方法

```java
public ParsedCalenderElements(@Nonnull Calendar currentCalendar)
```

**参数**：
- `currentCalendar`：要提取日期时间组件的 Calendar 对象

**功能**：从提供的 Calendar 对象中提取并设置分钟、小时、日期、月份、星期和秒组件。月份调整为基于 1 的索引（1 月 = 1），星期调整为基于 0 的索引（星期日 = 0）。

#### 主要方法

##### toString()

```java
@Override
public String toString()
```

**功能**：返回解析后日历元素的字符串表示。

**返回值**：格式为 `"(second) minute hour day month weekday"` 的字符串

## 使用示例

### 基本用法

```java
import io.github.sinri.keel.core.cron.KeelCronExpression;
import io.github.sinri.keel.core.cron.ParsedCalenderElements;
import java.util.Calendar;

// 创建 Cron 表达式：每天午夜执行
KeelCronExpression cronExpr = new KeelCronExpression("0 0 * * *");

// 检查当前时间是否匹配
Calendar now = Calendar.getInstance();
boolean matches = cronExpr.match(now);

// 使用解析后的日历元素
ParsedCalenderElements elements = new ParsedCalenderElements(now);
boolean matchesElements = cronExpr.match(elements);

System.out.println("当前时间: " + elements.toString());
System.out.println("是否匹配: " + matches);
```

### 复杂 Cron 表达式示例

```java
// 每周一到周五的上午 9 点执行
KeelCronExpression workdayMorning = new KeelCronExpression("0 9 * * 1-5");

// 每 15 分钟执行一次
KeelCronExpression every15Minutes = new KeelCronExpression("*/15 * * * *");

// 每月 1 号和 15 号的中午 12 点执行
KeelCronExpression twiceMonthly = new KeelCronExpression("0 12 1,15 * *");

// 每年 1 月 1 日午夜执行
KeelCronExpression newYear = new KeelCronExpression("0 0 1 1 *");
```

### 错误处理

```java
try {
    // 无效的 Cron 表达式（字段数量不正确）
    KeelCronExpression invalid = new KeelCronExpression("0 0 * *");
} catch (RuntimeException e) {
    System.err.println("无效的 Cron 表达式格式");
}

try {
    // 无效的字段值（超出范围）
    KeelCronExpression outOfRange = new KeelCronExpression("60 0 * * *");
} catch (IllegalArgumentException e) {
    System.err.println("字段值超出允许范围");
}
```

## 注意事项

1. **时间格式**：该包使用标准的 5 字段 Cron 表达式格式，不支持秒字段（6 字段格式）。
2. **星期表示**：星期使用 0-6 表示，其中 0 表示星期日。
3. **月份表示**：月份使用 1-12 表示，其中 1 表示 1 月。
4. **线程安全**：类的实例是不可变的，可以安全地在多线程环境中使用。
5. **性能考虑**：Cron 表达式在构造时解析，匹配操作使用预计算的集合，性能较好。

## 相关包

- `io.github.sinri.keel.core.servant.sundial`：定时任务调度相关功能
- `java.util.Calendar`：Java 标准日历类
- `java.util.regex.Pattern`：正则表达式模式匹配
