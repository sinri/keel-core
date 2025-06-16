# SelfInterface

## 概述

`SelfInterface<T>` 是一个泛型接口，提供了获取实现实例的方法。这个接口主要用于流式接口（fluent interfaces）或混入（mixins）模式，其中实现类需要返回 `this` 来支持方法链式调用。

## 接口定义

```java
package io.github.sinri.keel.core;

import javax.annotation.Nonnull;

/**
 * A generic interface that provides a method to get the implementation instance.
 * This is useful for fluent interfaces or mixins where the implementing class
 * needs to return `this` to allow method chaining.
 *
 * @param <T> the type of the implementing class
 * @since 3.1.10
 */
public interface SelfInterface<T> {
    /**
     * @return the implementation instance, such as `this` in implemented class.
     */
    @Nonnull
    T getImplementation();
}
```

## 核心特性

### 泛型参数
- `<T>`: 实现类的类型，通常是实现该接口的类本身

### 方法
- `getImplementation()`: 返回实现实例，通常在实现类中返回 `this`

## 使用场景

### 1. 流式接口（Fluent Interface）
支持方法链式调用，使代码更加简洁和可读：

```java
public class FluentBuilder implements SelfInterface<FluentBuilder> {
    @Override
    public FluentBuilder getImplementation() {
        return this;
    }
    
    public FluentBuilder setProperty(String value) {
        // 设置属性
        return getImplementation(); // 支持链式调用
    }
}
```

### 2. 混入模式（Mixin Pattern）
在多重继承场景中，提供统一的自引用机制：

```java
public interface ActionMixin<T> extends SelfInterface<T> {
    default T performAction() {
        // 执行操作
        return getImplementation();
    }
}
```

### 3. 实体类增强
为实体类提供自引用能力，支持流式操作：

```java
public class Entity implements SelfInterface<Entity> {
    @Override
    public Entity getImplementation() {
        return this;
    }
    
    public Entity write(String key, Object value) {
        // 写入数据
        return getImplementation();
    }
}
```

## 实际应用示例

### 1. JsonifiableEntity 中的应用

```java
public interface JsonifiableEntity<E>
        extends UnmodifiableJsonifiableEntity, ClusterSerializable, SelfInterface<E> {
    
    default E write(String key, Object value) {
        this.toJsonObject().put(key, value);
        return getImplementation(); // 支持链式调用
    }
}
```

**使用示例：**
```java
entity.write("name", "John")
      .write("age", 30)
      .write("email", "john@example.com");
```

### 2. NamedActionMixinInterface 中的应用

```java
public interface NamedActionMixinInterface<C extends NamedMySQLConnection, W>
        extends SelfInterface<W> {
    @Nonnull
    C getNamedSqlConnection();
}
```

### 3. KeelIssueRecordCore 中的应用

```java
interface KeelIssueRecordCore<T> extends SelfInterface<T> {
    T timestamp(long timestamp);
    T exception(@Nonnull Throwable throwable);
    T classification(@Nonnull List<String> classification);
    T level(@Nonnull KeelLogLevel level);
}
```

**使用示例：**
```java
record.timestamp(System.currentTimeMillis())
      .level(KeelLogLevel.ERROR)
      .classification("database", "connection")
      .exception(new SQLException("Connection failed"));
```

## 实现指南

### 基本实现
```java
public class MyClass implements SelfInterface<MyClass> {
    @Override
    public MyClass getImplementation() {
        return this;
    }
    
    public MyClass doSomething() {
        // 执行操作
        return getImplementation();
    }
}
```

### 抽象类实现
```java
public abstract class AbstractBase<T> implements SelfInterface<T> {
    // 子类需要实现 getImplementation() 方法
    
    public T commonOperation() {
        // 通用操作
        return getImplementation();
    }
}

public class ConcreteClass extends AbstractBase<ConcreteClass> {
    @Override
    public ConcreteClass getImplementation() {
        return this;
    }
}
```

## 设计优势

### 1. 类型安全
- 通过泛型确保返回类型的正确性
- 编译时检查，避免运行时类型错误

### 2. 代码复用
- 在接口和抽象类中定义通用方法
- 通过 `getImplementation()` 返回正确的实例类型

### 3. 流式编程支持
- 支持方法链式调用
- 提高代码的可读性和简洁性

### 4. 混入模式支持
- 在多重继承场景中提供统一的自引用机制
- 支持组合式的功能扩展

## 最佳实践

### 1. 命名约定
- 泛型参数通常使用实现类的类型
- 方法名保持简洁明了

### 2. 返回值处理
- 始终返回 `getImplementation()` 而不是直接返回 `this`
- 确保返回值的类型安全

### 3. 文档注释
- 为实现类添加清晰的文档说明
- 说明方法链式调用的用法

### 4. 异常处理
- 在链式调用中合理处理异常
- 保证方法调用的连续性

## 版本信息

- **引入版本**: 3.1.10
- **包路径**: `io.github.sinri.keel.core`
- **依赖**: `javax.annotation.Nonnull`

## 相关接口

- `UnmodifiableJsonifiableEntity`: 只读JSON实体接口
- `ClusterSerializable`: 集群序列化接口
- `JsonifiableEntity`: JSON实体接口（继承了SelfInterface）

## 注意事项

1. **泛型类型**: 确保泛型参数 `T` 与实现类的类型一致
2. **空值检查**: `getImplementation()` 方法标注了 `@Nonnull`，不应返回 null
3. **继承关系**: 在复杂的继承层次中，确保正确实现 `getImplementation()` 方法
4. **性能考虑**: 该接口的方法调用开销很小，适合频繁调用的场景