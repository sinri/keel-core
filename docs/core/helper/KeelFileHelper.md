# KeelFileHelper

`KeelFileHelper` 是 Keel 框架中的文件系统操作工具类，基于 Vert.x 提供异步文件操作、JAR 文件处理、压缩文件操作、临时文件管理等全面的文件系统功能。

## 版本信息

- **引入版本**: 2.6
- **设计模式**: 单例模式
- **线程安全**: 是
- **异步支持**: 基于 Vert.x Future 的异步操作

## 主要功能

### 1. 异步文件操作
- 文件读写、复制、移动、删除
- 目录创建和管理
- 文件属性查询

### 2. JAR 文件处理
- JAR 文件内容遍历和资源提取
- 运行环境检测（是否从 JAR 运行）
- 类路径扫描和包内类文件查找

### 3. 压缩文件操作
- ZIP 和 JAR 文件的创建、提取和内容列表
- 支持单个文件提取和批量操作

### 4. 临时文件管理
- 临时文件和目录的创建与管理
- 自动清理机制

### 5. 安全性保障
- 路径遍历攻击防护
- 文件路径验证和规范化

## 获取实例

```java
import static io.github.sinri.keel.facade.KeelInstance.Keel;

// 通过 Keel 实例获取
KeelFileHelper fileHelper = Keel.fileHelper();
```

## 基本文件操作

### 文件存在性检查

```java
// 检查文件是否存在
Keel.fileHelper().exists("config.json")
    .onSuccess(exists -> {
        if (exists) {
            System.out.println("文件存在");
        } else {
            System.out.println("文件不存在");
        }
    })
    .onFailure(e -> System.err.println("检查失败: " + e.getMessage()));
```

### 目录创建

```java
// 创建目录（包括父目录）
Keel.fileHelper().mkdirs("data/logs/2024")
    .onSuccess(v -> System.out.println("目录创建成功"))
    .onFailure(e -> System.err.println("目录创建失败: " + e.getMessage()));
```

### 文件删除

```java
// 删除单个文件
Keel.fileHelper().delete("temp.txt")
    .onSuccess(v -> System.out.println("文件删除成功"))
    .onFailure(e -> System.err.println("文件删除失败: " + e.getMessage()));

// 递归删除目录
Keel.fileHelper().deleteRecursive("temp_directory")
    .onSuccess(v -> System.out.println("目录删除成功"))
    .onFailure(e -> System.err.println("目录删除失败: " + e.getMessage()));
```

### 文件复制和移动

```java
// 复制文件
Keel.fileHelper().copy("source.txt", "destination.txt")
    .onSuccess(v -> System.out.println("文件复制成功"))
    .onFailure(e -> System.err.println("文件复制失败: " + e.getMessage()));

// 移动文件
Keel.fileHelper().move("old_location.txt", "new_location.txt")
    .onSuccess(v -> System.out.println("文件移动成功"))
    .onFailure(e -> System.err.println("文件移动失败: " + e.getMessage()));
```

## 文件读写操作

### 读取文件

```java
// 读取文件为字符串（UTF-8）
Keel.fileHelper().readFileAsString("config.json", "UTF-8")
    .onSuccess(content -> {
        System.out.println("文件内容: " + content);
    })
    .onFailure(e -> System.err.println("读取失败: " + e.getMessage()));

// 读取文件为字节数组（同步方法）
try {
    byte[] data = Keel.fileHelper().readFileAsByteArray("image.jpg", false);
    System.out.println("文件大小: " + data.length + " 字节");
} catch (Exception e) {
    System.err.println("读取失败: " + e.getMessage());
}

// 从 JAR 中读取资源文件
try {
    byte[] resourceData = Keel.fileHelper().readFileAsByteArray("config/default.properties", true);
    String resourceContent = new String(resourceData, "UTF-8");
    System.out.println("资源内容: " + resourceContent);
} catch (Exception e) {
    System.err.println("资源读取失败: " + e.getMessage());
}
```

### 写入文件

```java
// 写入字符串到文件
String content = "Hello, World!";
Keel.fileHelper().writeFile("output.txt", content, "UTF-8")
    .onSuccess(v -> System.out.println("写入成功"))
    .onFailure(e -> System.err.println("写入失败: " + e.getMessage()));

// 追加内容到文件
String appendContent = "\nNew line added";
Keel.fileHelper().appendFile("output.txt", appendContent)
    .onSuccess(v -> System.out.println("追加成功"))
    .onFailure(e -> System.err.println("追加失败: " + e.getMessage()));
```

## 文件属性查询

### 获取文件属性

```java
// 获取文件属性
Keel.fileHelper().getFileProps("document.pdf")
    .onSuccess(props -> {
        System.out.println("文件大小: " + props.size() + " 字节");
        System.out.println("创建时间: " + new Date(props.creationTime()));
        System.out.println("修改时间: " + new Date(props.lastModifiedTime()));
        System.out.println("是否为目录: " + props.isDirectory());
    })
    .onFailure(e -> System.err.println("获取属性失败: " + e.getMessage()));

// 获取文件大小
Keel.fileHelper().getFileSize("large_file.zip")
    .onSuccess(size -> System.out.println("文件大小: " + size + " 字节"))
    .onFailure(e -> System.err.println("获取大小失败: " + e.getMessage()));

// 检查是否为目录
Keel.fileHelper().isDirectory("data")
    .onSuccess(isDir -> {
        if (isDir) {
            System.out.println("这是一个目录");
        } else {
            System.out.println("这是一个文件");
        }
    });

// 获取最后修改时间
Keel.fileHelper().getLastModifiedTime("config.json")
    .onSuccess(time -> {
        Date modifiedDate = new Date(time);
        System.out.println("最后修改时间: " + modifiedDate);
    });
```

### 目录列表

```java
// 列出目录内容
Keel.fileHelper().listDir("data")
    .onSuccess(files -> {
        System.out.println("目录内容:");
        files.forEach(file -> System.out.println("  " + file));
    })
    .onFailure(e -> System.err.println("列表失败: " + e.getMessage()));
```

## 临时文件管理

### 创建临时文件

```java
// 创建临时文件
Keel.fileHelper().createTempFile("temp_", ".txt")
    .onSuccess(tempFilePath -> {
        System.out.println("临时文件路径: " + tempFilePath);
        
        // 使用临时文件
        return Keel.fileHelper().writeFile(tempFilePath, "临时数据", "UTF-8");
    })
    .onSuccess(v -> System.out.println("临时文件写入成功"))
    .onFailure(e -> System.err.println("临时文件操作失败: " + e.getMessage()));
```

## JAR 文件操作

### JAR 环境检测

```java
// 检查是否从 JAR 运行
boolean isRunningFromJar = Keel.fileHelper().isRunningFromJAR();
System.out.println("是否从 JAR 运行: " + isRunningFromJar);

// 获取类路径列表
List<String> classPathList = Keel.fileHelper().getClassPathList();
System.out.println("类路径:");
classPathList.forEach(path -> System.out.println("  " + path));
```

### JAR 资源访问

```java
// 获取 JAR 中资源的 URL
URL resourceUrl = Keel.fileHelper().getUrlOfFileInRunningJar("config/application.properties");
if (resourceUrl != null) {
    System.out.println("资源 URL: " + resourceUrl);
} else {
    System.out.println("资源未找到");
}

// 遍历 JAR 中的目录
List<JarEntry> entries = Keel.fileHelper().traversalInRunningJar("config/");
System.out.println("config/ 目录下的文件:");
entries.forEach(entry -> System.out.println("  " + entry.getName()));
```

### 包内类文件扫描

```java
// 扫描包内的类文件
Set<String> classFiles = Keel.fileHelper().seekPackageClassFilesInRunningJar("io.github.sinri.keel.core.helper");
System.out.println("Helper 包中的类:");
classFiles.forEach(className -> System.out.println("  " + className));
```

## ZIP 文件操作

### 创建 ZIP 文件

```java
// 创建 ZIP 文件
Keel.fileHelper().createZip("source_directory", "archive.zip")
    .onSuccess(v -> System.out.println("ZIP 文件创建成功"))
    .onFailure(e -> System.err.println("ZIP 创建失败: " + e.getMessage()));

// 从单个文件创建 ZIP
Keel.fileHelper().createZip("document.pdf", "document.zip")
    .onSuccess(v -> System.out.println("文件压缩成功"))
    .onFailure(e -> System.err.println("文件压缩失败: " + e.getMessage()));
```

### 提取 ZIP 文件

```java
// 提取整个 ZIP 文件
Keel.fileHelper().extractZip("archive.zip", "extracted_files")
    .onSuccess(v -> System.out.println("ZIP 文件提取成功"))
    .onFailure(e -> System.err.println("ZIP 提取失败: " + e.getMessage()));

// 提取 ZIP 中的单个文件
Keel.fileHelper().extractZipEntry("archive.zip", "config.json", "extracted_config.json")
    .onSuccess(v -> System.out.println("文件提取成功"))
    .onFailure(e -> System.err.println("文件提取失败: " + e.getMessage()));
```

### ZIP 文件内容查看

```java
// 列出 ZIP 文件内容
Keel.fileHelper().listZipContents("archive.zip")
    .onSuccess(entries -> {
        System.out.println("ZIP 文件内容:");
        entries.forEach(entry -> System.out.println("  " + entry));
    })
    .onFailure(e -> System.err.println("列表失败: " + e.getMessage()));
```

## JAR 文件操作

### 提取 JAR 文件

```java
// 提取 JAR 文件
Keel.fileHelper().extractJar("application.jar", "extracted_jar")
    .onSuccess(v -> System.out.println("JAR 文件提取成功"))
    .onFailure(e -> System.err.println("JAR 提取失败: " + e.getMessage()));
```

### 创建 JAR 文件

```java
// 创建 JAR 文件
Keel.fileHelper().createJar("compiled_classes", "application.jar")
    .onSuccess(v -> System.out.println("JAR 文件创建成功"))
    .onFailure(e -> System.err.println("JAR 创建失败: " + e.getMessage()));
```

## 注意事项

1. **异步操作**: 所有文件操作都是异步的，使用 Future 处理结果
2. **路径安全**: 注意防范路径遍历攻击，验证文件路径的安全性
3. **资源管理**: 及时清理临时文件，避免磁盘空间泄露
4. **错误处理**: 文件操作可能失败，要做好异常处理
5. **字符编码**: 文本文件操作时注意字符编码，建议使用 UTF-8
6. **大文件处理**: 处理大文件时注意内存使用，考虑流式处理

## 版本历史

- **2.6**: 引入 KeelFileHelper 基础功能
- **3.0.0**: 添加临时文件创建功能
- **3.2.11**: 增强 JAR 文件处理能力
- **3.2.12.1**: 重构 JAR 和类路径处理逻辑
- **4.0.12**: 大幅增强文件操作功能，添加 ZIP/JAR 处理、安全验证等
- **4.0.13**: 优化删除操作，分离递归删除和普通删除
- **当前版本**: 支持完整的文件系统操作和安全保障 