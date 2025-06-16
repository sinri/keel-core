# KeelJsonHelper

`KeelJsonHelper` 是 Keel 框架中的 JSON 数据处理工具类，提供深度读写、键链访问、排序功能、异常处理、格式化输出等全面的 JSON 操作功能。

## 版本信息

- **引入版本**: 2.6
- **设计模式**: 单例模式
- **线程安全**: 是

## 主要功能

### 1. 深度读写操作
- 支持多层嵌套的 JSON 对象和数组操作
- 通过键链（keychain）方式访问深层嵌套数据
- 动态创建嵌套结构

### 2. 排序功能
- JSON 对象键排序
- JSON 数组元素排序
- 递归排序嵌套结构

### 3. 异常处理
- 异常信息的 JSON 格式化
- 堆栈跟踪过滤和美化
- 支持忽略特定包的堆栈信息

### 4. 格式化输出
- 美化的 JSON 字符串输出
- 块状显示格式
- 层级缩进显示

## 获取实例

```java
import static io.github.sinri.keel.facade.KeelInstance.Keel;

// 通过 Keel 实例获取
KeelJsonHelper jsonHelper = Keel.jsonHelper();
```

## 基本读写操作

### 简单读写

```java
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

// 创建 JSON 对象
JsonObject jsonObject = new JsonObject();

// 写入简单值
Keel.jsonHelper().writeIntoJsonObject(jsonObject, "name", "张三");
Keel.jsonHelper().writeIntoJsonObject(jsonObject, "age", 25);
Keel.jsonHelper().writeIntoJsonObject(jsonObject, "active", true);

System.out.println("JSON 对象: " + jsonObject.encodePrettily());

// 读取简单值
String name = (String) Keel.jsonHelper().readFromJsonObject(jsonObject, "name");
Integer age = (Integer) Keel.jsonHelper().readFromJsonObject(jsonObject, "age");
Boolean active = (Boolean) Keel.jsonHelper().readFromJsonObject(jsonObject, "active");

System.out.println("姓名: " + name + ", 年龄: " + age + ", 活跃: " + active);
```

### 数组操作

```java
// 创建 JSON 数组
JsonArray jsonArray = new JsonArray();

// 写入数组元素
Keel.jsonHelper().writeIntoJsonArray(jsonArray, 0, "第一个元素");
Keel.jsonHelper().writeIntoJsonArray(jsonArray, 1, "第二个元素");
Keel.jsonHelper().writeIntoJsonArray(jsonArray, -1, "追加元素"); // -1 表示追加

System.out.println("JSON 数组: " + jsonArray.encodePrettily());

// 读取数组元素
String firstElement = (String) Keel.jsonHelper().readFromJsonArray(jsonArray, 0);
String secondElement = (String) Keel.jsonHelper().readFromJsonArray(jsonArray, 1);

System.out.println("第一个元素: " + firstElement);
System.out.println("第二个元素: " + secondElement);
```

## 键链（Keychain）深度访问

### 深度写入

```java
// 创建复杂的嵌套结构
JsonObject complexObject = new JsonObject();

// 使用键链写入深层嵌套数据
List<Object> keychain1 = Arrays.asList("user", "profile", "name");
Keel.jsonHelper().writeIntoJsonObject(complexObject, keychain1, "李四");

List<Object> keychain2 = Arrays.asList("user", "profile", "age");
Keel.jsonHelper().writeIntoJsonObject(complexObject, keychain2, 30);

List<Object> keychain3 = Arrays.asList("user", "settings", "theme");
Keel.jsonHelper().writeIntoJsonObject(complexObject, keychain3, "dark");

List<Object> keychain4 = Arrays.asList("user", "hobbies", 0);
Keel.jsonHelper().writeIntoJsonObject(complexObject, keychain4, "编程");

List<Object> keychain5 = Arrays.asList("user", "hobbies", 1);
Keel.jsonHelper().writeIntoJsonObject(complexObject, keychain5, "阅读");

System.out.println("复杂对象: " + complexObject.encodePrettily());
```

### 深度读取

```java
// 从深层嵌套结构读取数据
List<Object> nameKeychain = Arrays.asList("user", "profile", "name");
String userName = (String) Keel.jsonHelper().readFromJsonObject(complexObject, nameKeychain);

List<Object> ageKeychain = Arrays.asList("user", "profile", "age");
Integer userAge = (Integer) Keel.jsonHelper().readFromJsonObject(complexObject, ageKeychain);

List<Object> themeKeychain = Arrays.asList("user", "settings", "theme");
String theme = (String) Keel.jsonHelper().readFromJsonObject(complexObject, themeKeychain);

List<Object> hobbyKeychain = Arrays.asList("user", "hobbies", 0);
String firstHobby = (String) Keel.jsonHelper().readFromJsonObject(complexObject, hobbyKeychain);

System.out.println("用户名: " + userName);
System.out.println("年龄: " + userAge);
System.out.println("主题: " + theme);
System.out.println("第一个爱好: " + firstHobby);
```

## JSON 排序功能

### 对象键排序

```java
// 创建无序的 JSON 对象
JsonObject unorderedObject = new JsonObject();
unorderedObject.put("zebra", "斑马");
unorderedObject.put("apple", "苹果");
unorderedObject.put("banana", "香蕉");
unorderedObject.put("cat", "猫");

System.out.println("原始对象: " + unorderedObject.encode());

// 获取键排序后的 JSON 字符串
String sortedJson = Keel.jsonHelper().getJsonForObjectWhoseItemKeysSorted(unorderedObject);
System.out.println("键排序后: " + sortedJson);
```

### 数组元素排序

```java
// 创建无序的 JSON 数组
JsonArray unorderedArray = new JsonArray();
unorderedArray.add("zebra");
unorderedArray.add("apple");
unorderedArray.add("banana");
unorderedArray.add("cat");

System.out.println("原始数组: " + unorderedArray.encode());

// 获取元素排序后的 JSON 字符串
String sortedArrayJson = Keel.jsonHelper().getJsonForArrayWhoseItemsSorted(unorderedArray);
System.out.println("元素排序后: " + sortedArrayJson);
```

## 异常处理和格式化

### 异常信息 JSON 化

```java
// 模拟一个异常
try {
    throw new RuntimeException("这是一个测试异常", 
        new IllegalArgumentException("参数错误", 
            new NullPointerException("空指针异常")));
} catch (Exception e) {
    // 将异常转换为 JSON 格式
    JsonObject exceptionJson = Keel.jsonHelper().renderThrowableChain(e);
    System.out.println("异常 JSON: " + exceptionJson.encodePrettily());
    
    // 忽略特定包的堆栈信息
    Set<String> ignorablePackages = Set.of("java.lang", "sun.reflect");
    JsonObject filteredExceptionJson = Keel.jsonHelper().renderThrowableChain(e, ignorablePackages);
    System.out.println("过滤后的异常 JSON: " + filteredExceptionJson.encodePrettily());
}
```

### 格式化输出

```java
// 创建复杂的 JSON 结构
JsonObject complexData = new JsonObject();
complexData.put("application", new JsonObject()
    .put("name", "MyApp")
    .put("version", "1.0.0"));

// 使用块状格式显示
String blockFormat = Keel.jsonHelper().renderJsonToStringBlock("应用配置", complexData);
System.out.println("块状格式:");
System.out.println(blockFormat);
```

## 注意事项

1. **类型安全**: 从 JSON 读取数据时注意类型转换，建议进行类型检查
2. **空值处理**: 键链访问可能返回 null，要做好空值检查
3. **性能考虑**: 深度嵌套操作有性能开销，频繁操作建议缓存键链
4. **内存使用**: 大型 JSON 对象的排序和格式化会消耗较多内存
5. **异常处理**: JSON 操作可能抛出异常，要做好异常处理

## 版本历史

- **2.6**: 引入 KeelJsonHelper 基础功能
- **2.4**: 添加 JSON 排序功能
- **2.9**: 添加异常处理和堆栈跟踪功能
- **3.0.0**: 添加格式化输出和块状显示功能
- **当前版本**: 支持完整的 JSON 处理功能和性能优化 