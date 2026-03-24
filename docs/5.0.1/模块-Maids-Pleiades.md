# 模块说明：Maids — Pleiades（昴宿）

**包**：`io.github.sinri.keel.core.maids.pleiades`  
**版本线**：5.0.1  
**运行环境**：Vert.x **集群**（EventBus）

## 职责

基于 **EventBus** 的 **集群消息消费** 骨架：子类声明监听地址与处理逻辑，发送端通过 **`MessageProducer`** 投递消息。

## 主要类型

| 类型            | 说明                                                        |
|---------------|-----------------------------------------------------------|
| `Pleiades<T>` | 抽象 Verticle：`startVerticle` 注册 consumer，`stopVerticle` 注销 |

## 子类需实现

- **`getAddress()`**：消费者地址（集群内需约定全局唯一或按业务划分）。
- **`handleMessage(Message<T>)`**：消息处理。
- **`buildPleiadesLogger()`**：日志实例。

## 发送端工厂

- **`Pleiades.generateMessageProducer(Vertx vertx, String address)`**
- **`Pleiades.generateMessageProducer(Vertx, String address, DeliveryOptions)`**

用于在 **任意节点** 取得指向该地址的 **`MessageProducer<T>`**，消息由集群路由到消费者。

## 注意事项

- 消息类型 **`T`** 必须可被 EventBus **编解码**（集群间序列化）。
- 与 **点对点 / 发布订阅** 的差异由 Vert.x **`DeliveryOptions`** 与地址约定决定。

## 相关文档

- [Maids README](../../src/main/java/io/github/sinri/keel/core/maids/README.md)
- [文档目录](index.md)
