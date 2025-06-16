# ValueBox

## 概述

`ValueBox<T>` 是一个通用的值容器类，用于存储可选过期时间的值。该类提供了线程安全的值存储和检索功能，支持值的设置、获取、清除以及过期检查。

**包路径**: `io.github.sinri.keel.core.ValueBox`  
**版本**: 自 3.0.19 引入，3.1.0 版本增强

## 主要特性

- **泛型支持**: 支持存储任意类型的值
- **过期机制**: 支持设置值的生存时间，过期后自动清除
- **线程安全**: 所有方法都使用 `synchronized` 关键字保证线程安全
- **空值支持**: 允许存储 `null` 值
- **方法链式调用**: 支持链式调用模式

## 构造函数

### 1. 默认构造函数
```java
public ValueBox()
```
创建一个空的 ValueBox 实例，内部状态被清除。

### 2. 带初始值的构造函数
```java
public ValueBox(@Nullable T value)
```
创建一个包含指定初始值且无过期时间的 ValueBox 实例。

**参数**:
- `value`: 初始值，可以为 null

### 3. 带初始值和生存时间的构造函数
```java
public ValueBox(@Nullable T value, long lifetime)
```
创建一个包含指定初始值和过期时间的 ValueBox 实例。

**参数**:
- `value`: 初始值，可以为 null
- `lifetime`: 生存时间（毫秒），0 或负数表示永不过期

## 核心方法

### 值设置方法

#### `setValue(T value)`
```java
public synchronized ValueBox<T> setValue(@Nullable T value)
```
设置值，无过期时间。

**参数**:
- `value`: 要存储的值

**返回**: 当前 ValueBox 实例（支持链式调用）

#### `setValue(T value, long lifetime)`
```java
public synchronized ValueBox<T> setValue(@Nullable T value, long lifetime)
```
设置值并指定生存时间。

**参数**:
- `value`: 要存储的值
- `lifetime`: 生存时间（毫秒），0 或负数表示永不过期

**返回**: 当前 ValueBox 实例（支持链式调用）

### 值获取方法

#### `getValue()`
```java
@Nullable
public synchronized T getValue()
```
获取存储的值。如果值未设置或已过期，抛出 `IllegalStateException`。

**返回**: 存储的值
**异常**: `IllegalStateException` - 当值未设置时

#### `getValueOrElse(T fallbackForInvalid)` (3.1.0+)
```java
@Nullable
public synchronized T getValueOrElse(@Nullable T fallbackForInvalid)
```
获取存储的值，如果值未设置或已过期，返回指定的备用值。

**参数**:
- `fallbackForInvalid`: 备用值

**返回**: 存储的值或备用值

### 状态检查方法

#### `isValueAlreadySet()`
```java
public synchronized boolean isValueAlreadySet()
```
检查值是否已设置且未过期。如果值已过期，会自动清除。

**返回**: 如果值已设置且未过期返回 true，否则返回 false

#### `isValueSetToNull()`
```java
public synchronized boolean isValueSetToNull()
```
检查值是否被设置为 null。

**返回**: 如果值已设置且为 null 返回 true，否则返回 false

### 清除方法

#### `clear()`
```java
public synchronized ValueBox<T> clear()
```
清除存储的值和所有内部状态。

**返回**: 当前 ValueBox 实例（支持链式调用）

## 过期机制

ValueBox 支持基于时间的自动过期机制：

- **expire 字段**: 存储过期时间戳（毫秒）
- **过期检查**: 在 `isValueAlreadySet()` 方法中自动检查并清除过期值
- **过期规则**:
  - `expire <= 0`: 永不过期
  - `expire > System.currentTimeMillis()`: 未过期
  - `expire <= System.currentTimeMillis()`: 已过期，自动清除

## 使用示例

### 基本用法
```java
// 创建并设置值
ValueBox<String> box = new ValueBox<>();
box.setValue("Hello World");

// 检查和获取值
if (box.isValueAlreadySet()) {
    String value = box.getValue();
    System.out.println(value); // 输出: Hello World
}
```

### 带过期时间的用法
```java
// 设置5秒后过期的值
ValueBox<Integer> box = new ValueBox<>(42, 5000);

// 立即检查
System.out.println(box.isValueAlreadySet()); // true

// 5秒后检查
Thread.sleep(5000);
System.out.println(box.isValueAlreadySet()); // false，值已过期并被清除
```

### 使用备用值
```java
ValueBox<String> box = new ValueBox<>();
String result = box.getValueOrElse("默认值");
System.out.println(result); // 输出: 默认值
```

### 链式调用
```java
ValueBox<String> box = new ValueBox<String>()
    .setValue("初始值")
    .clear()
    .setValue("新值", 10000);
```

### 处理 null 值
```java
ValueBox<String> box = new ValueBox<>(null);
System.out.println(box.isValueSetToNull()); // true
System.out.println(box.isValueAlreadySet()); // true
```

## 线程安全

ValueBox 类的所有公共方法都使用 `synchronized` 关键字修饰，确保在多线程环境下的安全使用：

- 所有字段都使用 `volatile` 关键字修饰
- 所有方法都是同步的，避免竞态条件
- 过期检查和清除操作是原子性的

## 注意事项

1. **异常处理**: `getValue()` 方法在值未设置时会抛出 `IllegalStateException`，建议先使用 `isValueAlreadySet()` 检查或使用 `getValueOrElse()` 方法
2. **过期检查**: 过期检查只在调用 `isValueAlreadySet()` 时进行，不是后台自动进行的
3. **时间精度**: 过期时间基于 `System.currentTimeMillis()`，精度为毫秒级
4. **内存管理**: 过期的值会在检查时自动清除，有助于内存回收

## 版本历史

- **3.0.19**: 初始版本
- **3.1.0**: 
  - 添加 `getValueOrElse()` 方法
  - 完善过期机制文档