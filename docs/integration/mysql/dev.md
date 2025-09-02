# MySQL Dev Package - 开发工具包

**版本**: 4.1.0+

MySQL 开发工具包提供了一套完整的代码生成工具，用于自动生成类型安全的表行类（Table Row Class）。这些工具可以帮助开发者快速创建与数据库表结构对应的 Java 实体类，减少手动编写代码的工作量，并确保类型安全。

## 核心组件

### TableRowClassSourceCodeGenerator

**位置**: `io.github.sinri.keel.integration.mysql.dev.TableRowClassSourceCodeGenerator`

源代码生成器是整个开发工具包的核心类，负责协调整个代码生成过程。

**主要功能**：

- 连接数据库并分析表结构信息
- 根据字段类型生成对应的 Java 类型
- 处理特殊字段（枚举、加密字段等）
- 生成符合编码规范的 Java 源代码
- 支持多种配置选项和自定义规则

**核心方法**：

```java
// 为指定模式设置生成范围
TableRowClassSourceCodeGenerator forSchema(String schema)

// 为指定表设置生成范围
TableRowClassSourceCodeGenerator forTable(String table)
TableRowClassSourceCodeGenerator forTables(Collection<String> tables)

// 排除指定表
TableRowClassSourceCodeGenerator excludeTables(Collection<String> tables)

// 设置构建标准处理器
TableRowClassSourceCodeGenerator setStandardHandler(Handler<TableRowClassBuildStandard> standardHandler)

// 生成代码到指定目录
Future<Void> generate(String packagePath)
```

### TableRowClassBuildStandard

**位置**: `io.github.sinri.keel.integration.mysql.dev.TableRowClassBuildStandard`

构建标准配置类，定义代码生成的规范和约定。

**主要配置项**：

| 配置项                          | 类型      | 默认值   | 描述            |
|------------------------------|---------|-------|---------------|
| `provideConstSchema`         | boolean | true  | 是否生成模式名常量     |
| `provideConstTable`          | boolean | true  | 是否生成表名常量      |
| `provideConstSchemaAndTable` | boolean | false | 是否生成组合常量      |
| `vcsFriendly`                | boolean | false | 是否生成版本控制友好的代码 |
| `strictEnumPackage`          | String  | null  | 严格枚举类的包路径     |
| `envelopePackage`            | String  | null  | 封装类的包路径       |

**配置说明**：

1. **模式和表名常量**：
   ```java
   // provideConstSchema = true
   public static final String SCHEMA = "myapp";
   
   // provideConstTable = true  
   public static final String TABLE = "users";
   
   // provideConstSchemaAndTable = true
   public static final String SCHEMA_AND_TABLE = "myapp.users";
   ```

2. **版本控制友好**：
    - 生成的代码具有稳定的格式
    - 字段顺序固定
    - 注释格式统一

3. **包路径配置**：
    - `strictEnumPackage`: 严格枚举类的包路径
    - `envelopePackage`: 封装类的包路径

### 辅助类

#### TableRowClassFieldStrictEnum

**位置**: `io.github.sinri.keel.integration.mysql.dev.TableRowClassFieldStrictEnum`

用于处理严格枚举字段的辅助类。通过字段注释中的 `Enum<EnumClass>` 标记来使用。

#### TableRowClassFieldLooseEnum

**位置**: `io.github.sinri.keel.integration.mysql.dev.TableRowClassFieldLooseEnum`

用于处理松散枚举字段的辅助类。通过字段注释中的 `Enum{VALUE1,VALUE2}` 标记来使用。

## 支持的数据类型映射

### 基本数据类型

| MySQL 类型    | Java 类型      | 备注     |
|-------------|--------------|--------|
| `TINYINT`   | `Boolean`    | 长度为1时  |
| `TINYINT`   | `Integer`    | 长度大于1时 |
| `SMALLINT`  | `Integer`    |        |
| `MEDIUMINT` | `Integer`    |        |
| `INT`       | `Integer`    |        |
| `BIGINT`    | `Long`       |        |
| `FLOAT`     | `Float`      |        |
| `DOUBLE`    | `Double`     |        |
| `DECIMAL`   | `BigDecimal` |        |
| `VARCHAR`   | `String`     |        |
| `CHAR`      | `String`     |        |
| `TEXT`      | `String`     |        |
| `LONGTEXT`  | `String`     |        |

### 日期时间类型

| MySQL 类型    | Java 类型         | 备注 |
|-------------|-----------------|----|
| `DATE`      | `LocalDate`     |    |
| `TIME`      | `LocalTime`     |    |
| `DATETIME`  | `LocalDateTime` |    |
| `TIMESTAMP` | `LocalDateTime` |    |
| `YEAR`      | `Integer`       |    |

### 特殊类型

| MySQL 类型   | Java 类型       | 备注     |
|------------|---------------|--------|
| `ENUM`     | 自定义枚举类        | 生成严格枚举 |
| `SET`      | `Set<String>` |        |
| `JSON`     | `JsonObject`  |        |
| `BLOB`     | `byte[]`      |        |
| `LONGBLOB` | `byte[]`      |        |

## 使用指南

### 基本使用

```java
// 1. 创建数据源连接
NamedMySQLConnection connection = // ... 获取数据库连接

// 2. 创建代码生成器
TableRowClassSourceCodeGenerator generator = new TableRowClassSourceCodeGenerator(connection);

// 3. 配置生成范围
generator.forSchema("myapp")
         .forTables(Arrays.asList("users", "orders", "products"));

// 4. 设置构建标准（可选）
generator.setStandardHandler(standard -> {
    standard.setProvideConstSchema(true)
            .setProvideConstTable(true)
            .setVcsFriendly(true)
            .setStrictEnumPackage("com.example.enums")
            .setEnvelopePackage("com.example.crypto");
});

// 5. 生成代码
generator.generate("src/main/java/com/example/entity");
```

### 高级配置

```java
// 自定义构建标准
TableRowClassBuildStandard standard = new TableRowClassBuildStandard()
    .setProvideConstSchema(true)
    .setProvideConstTable(true)
    .setProvideConstSchemaAndTable(false)
    .setVcsFriendly(true)
    .setStrictEnumPackage("com.example.enums")
    .setEnvelopePackage("com.example.crypto");

// 应用到生成器
generator.setStandardHandler(s -> {
    s.setProvideConstSchema(standard.isProvideConstSchema())
     .setProvideConstTable(standard.isProvideConstTable())
     .setProvideConstSchemaAndTable(standard.isProvideConstSchemaAndTable())
     .setVcsFriendly(standard.isVcsFriendly())
     .setStrictEnumPackage(standard.getStrictEnumPackage())
     .setEnvelopePackage(standard.getEnvelopePackage());
});
```

### 字段特殊标记

在数据库表字段的注释中，可以使用以下标记来控制代码生成：

#### 1. 松散枚举

```sql
-- 字段注释中添加 Enum{值1,值2,值3}
COMMENT 'Enum{ACTIVE,INACTIVE,PENDING} 用户状态'
```

#### 2. 严格枚举

```sql
-- 字段注释中添加 Enum<完整类名>
COMMENT 'Enum<com.example.enums.UserStatus> 用户状态'
```

#### 3. AES加密字段

```sql
-- 字段注释中添加 AES<加密类名>
COMMENT 'AES<UserPasswordEnvelope> 用户密码'
```

#### 4. 废弃字段

```sql
-- 字段注释中添加 @deprecated
COMMENT '用户昵称 @deprecated 已废弃，请使用display_name'
```

## 生成的代码示例

```java
package com.example.entity;

import io.github.sinri.keel.integration.mysql.result.row.AbstractTableRow;
import io.vertx.core.json.JsonObject;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * 用户表
 * (´^ω^`)
 * SCHEMA: myapp
 * TABLE: users
 * (*￣∇￣*)
 * NOTICE BY KEEL:
 * 	To avoid being rewritten, do not modify this file manually.
 * @see TableRowClassSourceCodeGenerator
 */
public class UsersTableRow extends AbstractTableRow {
    
    public static final String SCHEMA = "myapp";
    public static final String TABLE = "users";
    
    public UsersTableRow(JsonObject tableRow) {
        super(tableRow);
    }
    
    @Override
    @Nonnull
    public String sourceTableName() {
        return TABLE;
    }
    
    public String sourceSchemaName() {
        return SCHEMA;
    }
    
    /*
     * 用户ID
     * 
     * Field `id` of type `bigint(20) unsigned`.
     */
    @Nonnull
    public Long getId() {
        return Objects.requireNonNull(readLong("id"));
    }
    
    /*
     * 用户名
     * 
     * Field `username` of type `varchar(50)`.
     */
    @Nonnull
    public String getUsername() {
        return Objects.requireNonNull(readString("username"));
    }
    
    // ... 其他字段方法
}
```

## 最佳实践

1. **包名规范**：使用统一的包名规范，如 `com.company.project.entity`

2. **字段命名**：遵循 Java 命名规范，使用驼峰命名法

3. **注释完整**：为表和字段添加有意义的注释

4. **版本控制**：启用 `vcsFriendly` 选项以生成版本控制友好的代码

5. **类型安全**：优先使用强类型，避免使用 `Object` 类型

6. **枚举处理**：对于状态字段，使用严格枚举类型

7. **加密字段**：敏感字段使用加密处理

## 注意事项

1. **数据库连接**：确保有足够的权限访问数据库元数据

2. **字符集**：确保数据库和代码的字符集一致

3. **类型映射**：某些特殊的 MySQL 类型可能需要手动处理

4. **依赖管理**：生成的代码可能依赖特定的库，确保项目中包含必要的依赖

5. **代码审查**：生成的代码应该经过适当的代码审查

## 扩展功能

### 自定义字段处理

如需扩展字段处理功能，可以通过数据库字段注释中的特殊标记来实现：

- **松散枚举**：`Enum{VALUE1,VALUE2}` - 在类内部生成枚举
- **严格枚举**：`Enum<com.example.EnumClass>` - 引用外部枚举类
- **AES加密**：`AES<com.example.CryptoClass>` - 添加解密方法
- **字段废弃**：`@deprecated` - 标记字段为废弃状态

通过这些标记，可以满足大多数特殊需求的代码生成场景。 