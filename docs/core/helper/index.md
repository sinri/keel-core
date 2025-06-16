# package io.github.sinri.keel.core.helper

## KeelHelpersInterface 类能力摘要

`KeelHelpersInterface` 是 Keel 框架的核心工具接口，提供了一套完整的工具类集合，涵盖了日常开发中的各种常用功能。该接口采用单例模式，通过默认方法提供对各种工具类的便捷访问。
通过接口实例 `io.github.sinri.keel.facade.KeelInstance#Keel` 访问各种工具。

### 核心工具类概览

#### 1. KeelBinaryHelper - 二进制数据处理
- **十六进制编码/解码**：支持大小写字母的十六进制字符串转换
- **Base64 编码/解码**：标准 Base64 格式的编码和解码操作
- **Base32 编码/解码**：Base32 格式的编码和解码操作
- **数据格式转换**：字节数组与各种编码格式之间的相互转换

#### 2. KeelDateTimeHelper - 日期时间处理
- **日期格式化**：支持 MySQL、GMT、ISO8601 等多种标准格式
- **时间戳转换**：Unix 时间戳与日期字符串的相互转换
- **时区处理**：支持不同时区的时间转换和格式化
- **Cron 表达式**：支持 Cron 表达式的匹配和验证
- **预定义格式常量**：提供常用的日期时间格式模板

#### 3. KeelFileHelper - 文件系统操作
- **异步文件操作**：基于 Vert.x 的异步文件读写、复制、移动、删除
- **JAR 文件处理**：JAR 文件内容遍历、资源提取、运行环境检测
- **压缩文件操作**：ZIP 和 JAR 文件的创建、提取和内容列表
- **临时文件管理**：临时文件和目录的创建与管理
- **类路径操作**：类路径扫描和包内类文件查找
- **文件属性查询**：文件大小、修改时间、创建时间等属性获取

#### 4. KeelJsonHelper - JSON 数据处理
- **深度读写**：支持多层嵌套的 JSON 对象和数组操作
- **键链访问**：通过键链（keychain）方式访问深层嵌套数据
- **排序功能**：JSON 对象键排序和数组元素排序
- **异常处理**：异常信息的 JSON 格式化和堆栈跟踪过滤
- **格式化输出**：美化的 JSON 字符串输出和块状显示

#### 5. KeelNetHelper - 网络工具
- **IP 地址处理**：IPv4 地址与数字的相互转换
- **本机信息获取**：本地主机地址、主机名、规范名称获取
- **Web 客户端 IP 解析**：支持 X-Forwarded-For 头的客户端 IP 链解析
- **网络地址转换**：IP 地址字节数组转换和处理

#### 6. KeelReflectionHelper - 反射工具
- **注解处理**：类和方法注解的获取和处理，支持重复注解
- **类扫描**：包内类的扫描和继承关系检查
- **动态加载**：支持文件系统和 JAR 文件中的类动态加载
- **类型检查**：类的可分配性检查和类型验证

#### 7. KeelStringHelper - 字符串处理
- **数组连接**：数组和集合元素的字符串连接
- **命名转换**：下划线命名与驼峰命名的相互转换
- **编码处理**：Base64、Base32、URL 编码等多种编码方式
- **正则表达式**：正则表达式匹配和查找
- **字符串验证**：邮箱格式、数字格式等验证
- **文本处理**：字符串截断、反转、去空格、大小写转换
- **随机字符串**：指定长度和字符集的随机字符串生成
- **异常格式化**：异常堆栈信息的格式化输出
- **特殊编码**：NyaCode 编码、HTTP 实体转义等

#### 8. KeelCryptographyHelper - 加密工具
- **AES 加密**：支持多种 AES 加密算法模式
- **RSA 加密**：RSA 非对称加密功能
- **加密算法封装**：提供易用的加密工具类接口

#### 9. KeelDigestHelper - 摘要算法
- **MD5 摘要**：高性能的 MD5 哈希计算（使用 FastThreadLocal 优化）
- **SHA 系列**：SHA-1、SHA-512 等摘要算法
- **HMAC 算法**：HMAC-SHA1 的 Base64 和十六进制输出
- **通用摘要**：支持任意摘要算法的计算

#### 10. KeelRuntimeHelper - 运行时信息
- **内存监控**：JVM 堆内存、非堆内存、物理内存使用情况
- **CPU 监控**：CPU 时间片统计和系统负载平均值
- **垃圾回收**：GC 统计信息和性能监控
- **对象大小**：使用 JOL 库计算对象大小和深度大小
- **系统信息**：基于 OSHI 库的系统硬件信息获取

#### 11. KeelAuthenticationHelper - 身份认证
- **密码哈希**：兼容 PHP 的 BCrypt 密码哈希和验证
- **Google Authenticator**：TOTP 双因子认证支持
- **同步/异步**：提供同步和异步两种 Google Authenticator 实现
- **配置灵活**：支持自定义时间窗口等配置参数

#### 12. KeelRandomHelper - 随机数生成
- **PRNG 支持**：基于 Vert.x 的伪随机数生成器
- **线程安全**：使用原子引用确保线程安全
- **延迟初始化**：首次使用时才初始化随机数生成器

### 使用方式

```java
// 通过接口实例 io.github.sinri.keel.facade.KeelInstance#Keel 访问各种工具
import static io.github.sinri.keel.facade.KeelInstance.Keel;
// 推荐的使用方式
Keel.stringHelper().joinStringArray(array, ",");
Keel.dateTimeHelper().getCurrentDatetime();
Keel.fileHelper().readFileAsString("config.json", "UTF-8");
```

### 版本信息
- **引入版本**：3.1.0
- **废弃说明**：静态实例 `KeelHelpers` 将在 4.0.0 版本中移除
- **设计模式**：单例模式 + 接口默认方法
- **线程安全**：所有工具类都是线程安全的

### 特性优势
1. **统一接口**：通过单一接口访问所有工具类
2. **延迟加载**：工具类采用单例模式，按需初始化
3. **类型安全**：完整的泛型支持和空值注解
4. **异步支持**：文件操作等支持 Vert.x 异步模式
5. **性能优化**：关键路径使用 FastThreadLocal 等优化技术
6. **扩展性强**：基于接口设计，易于扩展和测试
