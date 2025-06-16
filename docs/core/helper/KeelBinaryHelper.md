# KeelBinaryHelper

`KeelBinaryHelper` 是 Keel 框架中的二进制数据处理工具类，提供十六进制编码、Base64 编码、Base32 编码等多种二进制数据转换功能。

## 版本信息

- **引入版本**: 1.11
- **设计模式**: 单例模式
- **线程安全**: 是

## 主要功能

### 1. 十六进制编码
- 支持大小写字母的十六进制字符串转换
- 支持 Buffer 和字节数组的处理
- 支持指定范围的编码

### 2. Base64 编码/解码
- 标准 Base64 格式的编码和解码
- 支持字节数组和字符串转换

### 3. Base32 编码/解码
- Base32 格式的编码和解码
- 支持字节数组和字符串转换

## 获取实例

```java
import static io.github.sinri.keel.facade.KeelInstance.Keel;

// 通过 Keel 实例获取
KeelBinaryHelper binaryHelper = Keel.binaryHelper();
```

## 十六进制编码功能

### 小写十六进制编码

```java
// 字节数组转小写十六进制
byte[] data = "Hello World".getBytes();
String hexLower = Keel.binaryHelper().encodeHexWithLowerDigits(data);
System.out.println("小写十六进制: " + hexLower);
// 输出: 48656c6c6f20576f726c64

// Buffer 转小写十六进制
Buffer buffer = Buffer.buffer("Hello World");
String hexFromBuffer = Keel.binaryHelper().encodeHexWithLowerDigits(buffer);
System.out.println("Buffer 十六进制: " + hexFromBuffer);

// 指定范围的十六进制编码
Buffer largeBuffer = Buffer.buffer("Hello World, this is a test");
String partialHex = Keel.binaryHelper().encodeHexWithLowerDigits(largeBuffer, 0, 5);
System.out.println("部分十六进制: " + partialHex);
// 输出: 48656c6c6f (对应 "Hello")
```

### 大写十六进制编码

```java
// 字节数组转大写十六进制
byte[] data = "Hello World".getBytes();
String hexUpper = Keel.binaryHelper().encodeHexWithUpperDigits(data);
System.out.println("大写十六进制: " + hexUpper);
// 输出: 48656C6C6F20576F726C64

// Buffer 转大写十六进制
Buffer buffer = Buffer.buffer("Hello World");
String hexFromBuffer = Keel.binaryHelper().encodeHexWithUpperDigits(buffer);
System.out.println("Buffer 十六进制: " + hexFromBuffer);

// 指定范围的大写十六进制编码
String partialHex = Keel.binaryHelper().encodeHexWithUpperDigits(buffer, 6, 5);
System.out.println("部分十六进制: " + partialHex);
// 输出: 576F726C64 (对应 "World")
```

## Base64 编码功能

### Base64 编码

```java
// 字节数组 Base64 编码
String text = "Hello World";
byte[] data = text.getBytes();

// 编码为字节数组
byte[] encodedBytes = Keel.binaryHelper().encodeWithBase64(data);
System.out.println("Base64 字节数组: " + Arrays.toString(encodedBytes));

// 编码为字符串
String encodedString = Keel.binaryHelper().encodeWithBase64ToString(data);
System.out.println("Base64 字符串: " + encodedString);
// 输出: SGVsbG8gV29ybGQ=
```

### Base64 解码

```java
// Base64 解码
String base64String = "SGVsbG8gV29ybGQ=";
byte[] base64Bytes = base64String.getBytes();

byte[] decodedData = Keel.binaryHelper().decodeWithBase64(base64Bytes);
String originalText = new String(decodedData);
System.out.println("解码结果: " + originalText);
// 输出: Hello World
```

### 完整的 Base64 编解码示例

```java
public class Base64Example {
    
    public void demonstrateBase64() {
        String originalText = "这是一个中文测试文本";
        
        // 编码
        String encoded = Keel.binaryHelper().encodeWithBase64ToString(originalText.getBytes());
        System.out.println("原文: " + originalText);
        System.out.println("编码: " + encoded);
        
        // 解码
        byte[] decoded = Keel.binaryHelper().decodeWithBase64(encoded.getBytes());
        String decodedText = new String(decoded);
        System.out.println("解码: " + decodedText);
        
        // 验证
        System.out.println("编解码一致: " + originalText.equals(decodedText));
    }
}
```

## Base32 编码功能

### Base32 编码

```java
// 字节数组 Base32 编码
String text = "Hello World";
byte[] data = text.getBytes();

// 编码为字节数组
byte[] encodedBytes = Keel.binaryHelper().encodeWithBase32(data);
System.out.println("Base32 字节数组: " + Arrays.toString(encodedBytes));

// 编码为字符串
String encodedString = Keel.binaryHelper().encodeWithBase32ToString(data);
System.out.println("Base32 字符串: " + encodedString);
// 输出: JBSWY3DPEBLW64TMMQ======
```

### Base32 解码

```java
// Base32 解码
String base32String = "JBSWY3DPEBLW64TMMQ======";
byte[] base32Bytes = base32String.getBytes();

// 解码为字节数组
byte[] decodedData = Keel.binaryHelper().decodeWithBase32(base32Bytes);
String originalText = new String(decodedData);
System.out.println("解码结果: " + originalText);
// 输出: Hello World

// 解码为字符串
String decodedString = Keel.binaryHelper().decodeWithBase32ToString(base32Bytes);
System.out.println("解码字符串: " + decodedString);
```

### 完整的 Base32 编解码示例

```java
public class Base32Example {
    
    public void demonstrateBase32() {
        String originalText = "Base32 编码测试";
        
        // 编码
        String encoded = Keel.binaryHelper().encodeWithBase32ToString(originalText.getBytes());
        System.out.println("原文: " + originalText);
        System.out.println("编码: " + encoded);
        
        // 解码
        String decoded = Keel.binaryHelper().decodeWithBase32ToString(encoded.getBytes());
        System.out.println("解码: " + decoded);
        
        // 验证
        System.out.println("编解码一致: " + originalText.equals(decoded));
    }
}
```

## 实际应用场景

### 1. 数据传输编码

```java
public class DataTransferService {
    
    // 发送数据前编码
    public String encodeForTransfer(byte[] data) {
        return Keel.binaryHelper().encodeWithBase64ToString(data);
    }
    
    // 接收数据后解码
    public byte[] decodeFromTransfer(String encodedData) {
        return Keel.binaryHelper().decodeWithBase64(encodedData.getBytes());
    }
}
```

### 2. 文件内容哈希显示

```java
public class FileHashDisplay {
    
    public void displayFileHash(byte[] fileContent) {
        // 计算文件哈希
        String hash = Keel.digestHelper().md5(new String(fileContent));
        
        // 转换为十六进制显示
        byte[] hashBytes = hash.getBytes();
        String hexDisplay = Keel.binaryHelper().encodeHexWithUpperDigits(hashBytes);
        
        System.out.println("文件哈希 (十六进制): " + hexDisplay);
    }
}
```

### 3. 二进制数据调试

```java
public class BinaryDebugger {
    
    public void debugBinaryData(byte[] data) {
        System.out.println("=== 二进制数据调试 ===");
        System.out.println("原始字节数组: " + Arrays.toString(data));
        System.out.println("十六进制 (小写): " + Keel.binaryHelper().encodeHexWithLowerDigits(data));
        System.out.println("十六进制 (大写): " + Keel.binaryHelper().encodeHexWithUpperDigits(data));
        System.out.println("Base64 编码: " + Keel.binaryHelper().encodeWithBase64ToString(data));
        System.out.println("Base32 编码: " + Keel.binaryHelper().encodeWithBase32ToString(data));
        
        // 如果是文本数据，尝试显示原文
        try {
            String text = new String(data, "UTF-8");
            System.out.println("UTF-8 文本: " + text);
        } catch (Exception e) {
            System.out.println("非 UTF-8 文本数据");
        }
    }
}
```

## 注意事项

1. **编码格式**: Base64 和 Base32 编码会增加数据大小，Base64 增加约 33%，Base32 增加约 60%
2. **字符集**: 处理文本数据时注意字符集编码，建议使用 UTF-8
3. **性能**: 十六进制编码性能最高，Base64 次之，Base32 相对较慢
4. **用途选择**: 
   - 十六进制：调试、哈希值显示
   - Base64：数据传输、存储
   - Base32：用户友好的编码（如 Google Authenticator 密钥）

## 版本历史

- **1.11**: 引入十六进制编码功能
- **2.9.4**: 添加 Base64 和 Base32 编码功能
- **当前版本**: 支持 Buffer 和字节数组的完整编码功能 