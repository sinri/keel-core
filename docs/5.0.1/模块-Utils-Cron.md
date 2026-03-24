# 模块说明：Utils — Cron（cron）

**包**：`io.github.sinri.keel.core.utils.cron`  
**版本线**：5.0.1

## 职责

解析 **Cron 表达式** 并在给定日历时间上 **匹配**；与 **`Sundial`**、**`CronWatchman`** 等调度组件共用同一套时间语义。

## 主要类型

| 类型                       | 说明                     |
|--------------------------|------------------------|
| `KeelCronExpression`     | Cron 解析与匹配             |
| `ParsedCalenderElements` | 从 `Calendar` 抽取字段供匹配使用 |

## 典型用法

- 构造或解析表达式后，对 **`ParsedCalenderElements`** 或等价时间结构调用 **`match`**（见 `KeelCronExpression` JavaDoc）。
- **`Sundial`** 内部使用 **`KeelCronExpression.parseCalenderToElements`** 等做分钟级对齐。

## 相关文档

- [模块-Servant-Sundial](./模块-Servant-Sundial.md)
- [模块-Maids-Watchman](./模块-Maids-Watchman.md)
