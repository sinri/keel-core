# package io.github.sinri.keel.core.json

此 package 的类提供了基于 JSON 对象的数据实体在Vert.x体系下基于Jackson的JSON类的上层封装。主要理念是，不定义fields，将具体的字段存储交给底层的JSON对象，通过定义相关getter和setter来实现读写具体内容，同时通过通用读写兼容未事先定义的field。

可以分为两大体系

* 不可修改的JSON对象内容读取机制封装 [UnmodifiableJsonifiableEntity](./UnmodifiableJsonifiableEntity.md)
* JSON对象内容读写机制封装 [JsonifiableEntity](./JsonifiableEntity.md)

## Classes

### Interfaces

#### JsonifiableEntity<E>
主要的JSON实体接口，定义了可以与JSON对象相互转换的实体契约。

**继承关系:**
- `extends UnmodifiableJsonifiableEntity` - 继承不可修改JSON实体接口
- `extends ClusterSerializable` - 支持集群序列化
- `extends SelfInterface<E>` - 自引用接口

**功能:** 扩展了基础的只读功能，提供了处理JSON数据和缓冲区序列化的额外方法。

#### UnmodifiableJsonifiableEntity
表示不可修改的JSON实体接口，可以转换为JSON对象。

**继承关系:**
- `extends Iterable<Map.Entry<String, Object>>` - 支持迭代操作
- `extends Shareable` - 支持共享数据

**功能:** 提供了从底层JSON结构读取各种类型值的方法。

### Abstract Classes

#### JsonifiableEntityImpl<E>
`JsonifiableEntity`接口的抽象实现类，提供了基本的JSON对象操作功能。

**继承关系:**
- `implements JsonifiableEntity<E>` - 实现JsonifiableEntity接口

**功能:** 提供了toJsonObject()、reloadDataFromJsonObject()和toString()方法的具体实现。

### Concrete Classes

#### SimpleJsonifiableEntity ⚠️ 已弃用
`JsonifiableEntity`接口的简单实现，提供了将实体与JSON对象相互转换的基本功能。

**继承关系:**
- `extends JsonifiableEntityImpl<SimpleJsonifiableEntity>` - 继承抽象实现类

**功能:** 可以被其他类扩展以添加特定属性和方法，同时保持处理JSON数据的能力。

> **弃用说明**: 自4.1.0版本起，此类已被标记为弃用。建议用户定义详细的实现类而不是使用这个通用实现。

#### UnmodifiableJsonifiableEntityImpl
`UnmodifiableJsonifiableEntity`接口的实现类，提供了围绕JsonObject的只读包装器。

**继承关系:**
- `implements UnmodifiableJsonifiableEntity` - 实现不可修改JSON实体接口

**功能:** 该类确保底层JSON对象不能被修改，提供了安全且不可变的表示。

#### JsonifiableSerializer
针对`UnmodifiableJsonifiableEntity`及其相关类的Jackson Databind序列化器。

**继承关系:**
- `extends JsonSerializer<UnmodifiableJsonifiableEntity>` - 继承Jackson序列化器

**功能:** 提供自定义的JSON序列化逻辑，确保序列化结果的一致性和性能优化。

## 类层次结构图

```
UnmodifiableJsonifiableEntity (interface)
├── extends Iterable<Map.Entry<String, Object>>
├── extends Shareable
├── ← JsonifiableEntity<E> (interface)
│   ├── extends ClusterSerializable
│   ├── extends SelfInterface<E>
│   └── ← JsonifiableEntityImpl<E> (abstract class)
│       └── ← SimpleJsonifiableEntity (concrete class)
└── ← UnmodifiableJsonifiableEntityImpl (concrete class)
```

## 主要功能

- **JSON转换**: 实体与JSON对象之间的双向转换
- **数据读取**: 支持读取各种数据类型（String、Number、Boolean、JsonObject、JsonArray等）
- **缓冲区序列化**: 支持与Buffer的序列化和反序列化
- **集群支持**: 实现了ClusterSerializable接口，支持EventBus消息传递
- **迭代支持**: 实现了Iterable接口，支持foreach操作
- **不可变性**: 提供只读包装器确保数据安全
- **Jackson序列化**: 通过JsonifiableSerializer提供自定义序列化支持

## 相关文档

- [JsonifiableEntity](./JsonifiableEntity.md) - 主要的JSON实体接口文档
- [JsonifiableSerializer](./JsonifiableSerializer.md) - Jackson序列化器文档
- [UnmodifiableJsonifiableEntity](./UnmodifiableJsonifiableEntity.md) - 只读JSON实体接口文档

