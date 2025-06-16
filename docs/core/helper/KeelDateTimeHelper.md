# KeelDateTimeHelper

`KeelDateTimeHelper` 是 Keel 框架中的日期时间处理工具类，提供日期格式化、时间戳转换、时区处理、Cron 表达式匹配等全面的日期时间操作功能。

## 版本信息

- **引入版本**: 2.6
- **设计模式**: 单例模式
- **线程安全**: 是

## 主要功能

### 1. 日期格式化
- 支持 MySQL、GMT、ISO8601 等多种标准格式
- 自定义日期时间格式
- 当前时间和指定时间的格式化

### 2. 时间戳转换
- Unix 时间戳与日期字符串的相互转换
- 支持毫秒级时间戳

### 3. 时区处理
- 支持不同时区的时间转换和格式化
- GMT 时间处理

### 4. Cron 表达式
- Cron 表达式的匹配和验证
- 当前时间与 Cron 表达式的匹配检查

## 预定义格式常量

```java
// MySQL 相关格式
KeelDateTimeHelper.MYSQL_DATETIME_MS_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS"
KeelDateTimeHelper.MYSQL_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss"
KeelDateTimeHelper.MYSQL_DATE_PATTERN = "yyyy-MM-dd"
KeelDateTimeHelper.MYSQL_TIME_PATTERN = "HH:mm:ss"

// 其他标准格式
KeelDateTimeHelper.GMT_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z"
KeelDateTimeHelper.ISO8601_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'"
```

## 获取实例

```java
import static io.github.sinri.keel.facade.KeelInstance.Keel;

// 通过 Keel 实例获取
KeelDateTimeHelper dateTimeHelper = Keel.dateTimeHelper();
```

## 当前时间获取

### 获取当前日期时间

```java
// 获取当前 MySQL 格式的日期时间
String currentDateTime = Keel.dateTimeHelper().getCurrentDatetime();
System.out.println("当前日期时间: " + currentDateTime);
// 输出: 2024-01-15 14:30:25

// 获取当前日期
String currentDate = Keel.dateTimeHelper().getCurrentDate();
System.out.println("当前日期: " + currentDate);
// 输出: 2024-01-15

// 获取自定义格式的当前时间
String customFormat = Keel.dateTimeHelper().getCurrentDateExpression("yyyyMMdd_HHmmss");
System.out.println("自定义格式: " + customFormat);
// 输出: 20240115_143025
```

### 获取 GMT 时间

```java
// 获取当前 GMT 时间
String gmtTime = Keel.dateTimeHelper().getGMTDateTimeExpression();
System.out.println("GMT 时间: " + gmtTime);
// 输出: Mon, 15 Jan 2024 06:30:25 GMT

// 指定时区的 GMT 时间
ZoneId zoneId = ZoneId.of("Asia/Shanghai");
String gmtTimeWithZone = Keel.dateTimeHelper().getGMTDateTimeExpression(zoneId);
System.out.println("指定时区 GMT 时间: " + gmtTimeWithZone);
```

## 日期格式化

### Date 对象格式化

```java
import java.util.Date;

// 格式化 Date 对象
Date now = new Date();
String mysqlFormat = Keel.dateTimeHelper().getDateExpression(now, 
    KeelDateTimeHelper.MYSQL_DATETIME_PATTERN);
System.out.println("MySQL 格式: " + mysqlFormat);

String iso8601Format = Keel.dateTimeHelper().getDateExpression(now, 
    KeelDateTimeHelper.ISO8601_PATTERN);
System.out.println("ISO8601 格式: " + iso8601Format);

// 自定义格式
String customFormat = Keel.dateTimeHelper().getDateExpression(now, "yyyy年MM月dd日 HH时mm分ss秒");
System.out.println("中文格式: " + customFormat);
// 输出: 2024年01月15日 14时30分25秒
```

### 时间戳格式化

```java
// 格式化时间戳
long timestamp = System.currentTimeMillis();
String formattedTime = Keel.dateTimeHelper().getDateExpression(timestamp, 
    KeelDateTimeHelper.MYSQL_DATETIME_PATTERN);
System.out.println("时间戳格式化: " + formattedTime);

// 格式化历史时间戳
long historicalTimestamp = 1642204800000L; // 2022-01-15 00:00:00
String historicalTime = Keel.dateTimeHelper().getDateExpression(historicalTimestamp, 
    "yyyy-MM-dd HH:mm:ss");
System.out.println("历史时间: " + historicalTime);
```

## LocalDateTime 处理

### LocalDateTime 转 MySQL 格式

```java
import java.time.LocalDateTime;

LocalDateTime now = LocalDateTime.now();

// 转换为 MySQL 日期时间格式
String mysqlDateTime = Keel.dateTimeHelper().toMySQLDatetime(now);
System.out.println("MySQL 日期时间: " + mysqlDateTime);
// 输出: 2024-01-15 14:30:25

// 使用指定格式转换
String customMysqlFormat = Keel.dateTimeHelper().toMySQLDatetime(now, 
    KeelDateTimeHelper.MYSQL_DATETIME_MS_PATTERN);
System.out.println("MySQL 毫秒格式: " + customMysqlFormat);
// 输出: 2024-01-15 14:30:25.123

// 仅日期格式
String dateOnly = Keel.dateTimeHelper().toMySQLDatetime(now, 
    KeelDateTimeHelper.MYSQL_DATE_PATTERN);
System.out.println("仅日期: " + dateOnly);
// 输出: 2024-01-15
```

### LocalDateTime 表达式转换

```java
// 从 LocalDateTime 表达式转换为 MySQL 格式
String localDateTimeExpression = "2024-01-15T14:30:25";
String mysqlFormat = Keel.dateTimeHelper().getMySQLFormatLocalDateTimeExpression(localDateTimeExpression);
System.out.println("转换结果: " + mysqlFormat);
// 输出: 2024-01-15 14:30:25
```

## 字符串解析

### 解析日期字符串

```java
import java.util.Date;

// 解析 MySQL 格式的日期字符串
String dateString = "2024-01-15 14:30:25";
Date parsedDate = Keel.dateTimeHelper().parseExpressionToDateInstance(dateString, 
    KeelDateTimeHelper.MYSQL_DATETIME_PATTERN);
System.out.println("解析的日期: " + parsedDate);

// 解析自定义格式的日期字符串
String customDateString = "2024年01月15日";
Date customParsedDate = Keel.dateTimeHelper().parseExpressionToDateInstance(customDateString, 
    "yyyy年MM月dd日");
System.out.println("自定义解析: " + customParsedDate);

// 解析 ISO8601 格式
String iso8601String = "2024-01-15T14:30:25Z";
Date iso8601Date = Keel.dateTimeHelper().parseExpressionToDateInstance(iso8601String, 
    KeelDateTimeHelper.ISO8601_PATTERN);
System.out.println("ISO8601 解析: " + iso8601Date);
```

## 时区处理

### Instant 时区转换

```java
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

// 获取当前 UTC 时间戳
Instant now = Instant.now();

// 转换为不同时区的时间表示
String utcTime = Keel.dateTimeHelper().getInstantExpression(now, ZoneOffset.UTC, 
    KeelDateTimeHelper.MYSQL_DATETIME_PATTERN);
System.out.println("UTC 时间: " + utcTime);

String shanghaiTime = Keel.dateTimeHelper().getInstantExpression(now, ZoneId.of("Asia/Shanghai"), 
    KeelDateTimeHelper.MYSQL_DATETIME_PATTERN);
System.out.println("上海时间: " + shanghaiTime);

String newYorkTime = Keel.dateTimeHelper().getInstantExpression(now, ZoneId.of("America/New_York"), 
    KeelDateTimeHelper.MYSQL_DATETIME_PATTERN);
System.out.println("纽约时间: " + newYorkTime);

// 自定义格式的时区转换
String customFormat = Keel.dateTimeHelper().getInstantExpression(now, ZoneId.of("Europe/London"), 
    "yyyy-MM-dd HH:mm:ss z");
System.out.println("伦敦时间: " + customFormat);
```

## Cron 表达式处理

### Cron 表达式匹配

```java
import io.github.sinri.keel.core.cron.KeelCronExpression;

// 检查当前时间是否匹配 Cron 表达式
String cronExpression = "0 30 14 * * ?"; // 每天 14:30:00

boolean isMatch = Keel.dateTimeHelper().isNowMatchCronExpression(cronExpression);
System.out.println("当前时间匹配 Cron: " + isMatch);

// 使用 KeelCronExpression 对象
KeelCronExpression cron = new KeelCronExpression("0 0 12 * * ?"); // 每天中午12点
boolean isNoonMatch = Keel.dateTimeHelper().isNowMatchCronExpression(cron);
System.out.println("当前是否中午12点: " + isNoonMatch);
```

### 复杂 Cron 表达式示例

```java
public class CronExamples {
    
    public void demonstrateCronExpressions() {
        // 每分钟执行
        checkCron("0 * * * * ?", "每分钟执行");
        
        // 每小时的第30分钟执行
        checkCron("0 30 * * * ?", "每小时30分执行");
        
        // 每天上午9点执行
        checkCron("0 0 9 * * ?", "每天上午9点执行");
        
        // 每周一上午9点执行
        checkCron("0 0 9 ? * MON", "每周一上午9点执行");
        
        // 每月1号上午9点执行
        checkCron("0 0 9 1 * ?", "每月1号上午9点执行");
        
        // 工作日上午9点执行
        checkCron("0 0 9 ? * MON-FRI", "工作日上午9点执行");
    }
    
    private void checkCron(String cronExpression, String description) {
        boolean matches = Keel.dateTimeHelper().isNowMatchCronExpression(cronExpression);
        System.out.println(description + ": " + matches);
    }
}
```

## 实际应用场景

### 1. 日志时间戳

```java
public class LogTimestampService {
    
    // 生成日志时间戳
    public String generateLogTimestamp() {
        return Keel.dateTimeHelper().getCurrentDateExpression(
            KeelDateTimeHelper.MYSQL_DATETIME_MS_PATTERN);
    }
    
    // 生成文件名时间戳
    public String generateFileTimestamp() {
        return Keel.dateTimeHelper().getCurrentDateExpression("yyyyMMdd_HHmmss");
    }
    
    // 生成可读的时间描述
    public String generateReadableTimestamp() {
        return Keel.dateTimeHelper().getCurrentDateExpression("yyyy年MM月dd日 HH:mm:ss");
    }
}
```

### 2. 数据库时间处理

```java
public class DatabaseTimeService {
    
    // 生成数据库插入时间
    public String getDatabaseInsertTime() {
        return Keel.dateTimeHelper().getCurrentDatetime();
    }
    
    // 解析数据库时间字符串
    public Date parseDatabaseTime(String dbTimeString) {
        return Keel.dateTimeHelper().parseExpressionToDateInstance(dbTimeString, 
            KeelDateTimeHelper.MYSQL_DATETIME_PATTERN);
    }
    
    // 格式化数据库查询时间范围
    public String formatTimeRange(Date startTime, Date endTime) {
        String start = Keel.dateTimeHelper().getDateExpression(startTime, 
            KeelDateTimeHelper.MYSQL_DATETIME_PATTERN);
        String end = Keel.dateTimeHelper().getDateExpression(endTime, 
            KeelDateTimeHelper.MYSQL_DATETIME_PATTERN);
        return String.format("时间范围: %s 到 %s", start, end);
    }
}
```

### 3. API 响应时间格式化

```java
public class APIResponseTimeService {
    
    // 生成 API 响应的时间戳
    public String getAPITimestamp() {
        return Keel.dateTimeHelper().getCurrentDateExpression(
            KeelDateTimeHelper.ISO8601_PATTERN);
    }
    
    // 生成 HTTP 头部的时间格式
    public String getHTTPHeaderTime() {
        return Keel.dateTimeHelper().getGMTDateTimeExpression();
    }
    
    // 生成用户友好的时间显示
    public String getUserFriendlyTime(long timestamp) {
        return Keel.dateTimeHelper().getDateExpression(timestamp, "MM月dd日 HH:mm");
    }
}
```

### 4. 定时任务调度

```java
public class ScheduledTaskService {
    
    // 检查是否到达执行时间
    public boolean shouldExecuteTask(String cronExpression) {
        return Keel.dateTimeHelper().isNowMatchCronExpression(cronExpression);
    }
    
    // 记录任务执行时间
    public void logTaskExecution(String taskName) {
        String timestamp = Keel.dateTimeHelper().getCurrentDatetime();
        System.out.println(String.format("[%s] 执行任务: %s", timestamp, taskName));
    }
    
    // 计算下次执行时间（简化示例）
    public String getNextExecutionTime(String cronExpression) {
        // 这里可以结合 KeelCronExpression 计算下次执行时间
        return "下次执行时间计算逻辑";
    }
}
```

### 5. 时区转换服务

```java
public class TimeZoneConversionService {
    
    // 将本地时间转换为不同时区
    public String convertToTimeZone(String localTime, String targetTimeZone) {
        try {
            // 解析本地时间
            Date localDate = Keel.dateTimeHelper().parseExpressionToDateInstance(localTime, 
                KeelDateTimeHelper.MYSQL_DATETIME_PATTERN);
            
            // 转换为 Instant
            Instant instant = localDate.toInstant();
            
            // 转换为目标时区
            return Keel.dateTimeHelper().getInstantExpression(instant, 
                ZoneId.of(targetTimeZone), KeelDateTimeHelper.MYSQL_DATETIME_PATTERN);
        } catch (Exception e) {
            throw new RuntimeException("时区转换失败", e);
        }
    }
    
    // 获取多个时区的当前时间
    public Map<String, String> getCurrentTimeInMultipleZones() {
        Instant now = Instant.now();
        Map<String, String> timeZones = new HashMap<>();
        
        timeZones.put("UTC", Keel.dateTimeHelper().getInstantExpression(now, 
            ZoneOffset.UTC, KeelDateTimeHelper.MYSQL_DATETIME_PATTERN));
        timeZones.put("Shanghai", Keel.dateTimeHelper().getInstantExpression(now, 
            ZoneId.of("Asia/Shanghai"), KeelDateTimeHelper.MYSQL_DATETIME_PATTERN));
        timeZones.put("New York", Keel.dateTimeHelper().getInstantExpression(now, 
            ZoneId.of("America/New_York"), KeelDateTimeHelper.MYSQL_DATETIME_PATTERN));
        timeZones.put("London", Keel.dateTimeHelper().getInstantExpression(now, 
            ZoneId.of("Europe/London"), KeelDateTimeHelper.MYSQL_DATETIME_PATTERN));
        
        return timeZones;
    }
}
```

## 性能优化建议

### 1. 格式化器缓存

```java
public class OptimizedDateTimeService {
    
    // 缓存常用的格式化器
    private static final DateTimeFormatter MYSQL_FORMATTER = 
        DateTimeFormatter.ofPattern(KeelDateTimeHelper.MYSQL_DATETIME_PATTERN);
    
    // 高频调用的时间格式化
    public String fastFormatCurrentTime() {
        return LocalDateTime.now().format(MYSQL_FORMATTER);
    }
}
```

### 2. 批量时间处理

```java
public class BatchTimeProcessor {
    
    // 批量格式化时间戳
    public List<String> formatTimestamps(List<Long> timestamps, String pattern) {
        return timestamps.stream()
            .map(timestamp -> Keel.dateTimeHelper().getDateExpression(timestamp, pattern))
            .collect(Collectors.toList());
    }
}
```

## 注意事项

1. **时区处理**: 在处理跨时区应用时，建议统一使用 UTC 时间存储，显示时再转换为本地时区
2. **格式化性能**: 频繁的日期格式化操作建议缓存 DateTimeFormatter 实例
3. **Cron 表达式**: Cron 表达式的匹配是基于当前系统时间，确保系统时间准确
4. **日期解析**: 解析用户输入的日期字符串时要做好异常处理
5. **夏令时**: 处理夏令时变化的地区时要特别注意时间计算

## 版本历史

- **2.6**: 引入 KeelDateTimeHelper 基础功能
- **2.7**: 添加 LocalDateTime 处理功能
- **2.9.1**: 添加 GMT 时间处理
- **3.0.1**: 增强时区处理和格式化功能
- **3.0.10**: 添加当前日期时间快捷方法
- **3.0.11**: 增强日期解析功能
- **4.0.0**: 添加 Cron 表达式匹配功能
- **4.0.6**: 添加 Instant 时区转换功能
- **当前版本**: 支持完整的日期时间处理功能 