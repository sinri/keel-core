# KeelDigestHelper

`KeelDigestHelper` 是 Keel 框架中的摘要算法工具类，提供 MD5、SHA 系列、HMAC 等多种哈希摘要算法，用于数据完整性验证、密码存储、数字签名等安全场景。

## 版本信息

- **引入版本**: 2.8
- **设计模式**: 单例模式
- **线程安全**: 是
- **性能优化**: MD5 算法使用 FastThreadLocal 优化（4.0.0+）
- **当前版本**: 4.1.0

## 主要功能

### 1. MD5 摘要算法
- 高性能的 MD5 哈希计算（使用 FastThreadLocal 优化）
- 支持大小写输出格式
- 支持字符串和字节数组输入（4.1.0+）
- 提供静态方法获取 MD5 MessageDigest 实例（4.1.0+）
- 广泛用于文件校验和数据完整性验证

### 2. SHA 系列摘要算法
- SHA-1 摘要算法
- SHA-512 摘要算法
- 支持大小写输出格式

### 3. HMAC 算法
- HMAC-SHA1 算法
- 支持 Base64 和十六进制输出
- 用于消息认证和 API 签名

### 4. 通用摘要算法
- 支持任意 Java 标准摘要算法
- 灵活的算法选择和输出格式
- 支持字符串和字节数组输入（4.1.0+）

## 获取实例

```java
import static io.github.sinri.keel.facade.KeelInstance.Keel;

// 通过 Keel 实例获取
KeelDigestHelper digestHelper = Keel.digestHelper();
```

## MD5 摘要算法

### 基本 MD5 使用

```java
// 小写 MD5 摘要（字符串输入）
String text = "Hello World";
String md5Lower = Keel.digestHelper().md5(text);
System.out.println("MD5 (小写): " + md5Lower);
// 输出: b10a8db164e0754105b7a99be72e3fe5

// 大写 MD5 摘要（字符串输入）
String md5Upper = Keel.digestHelper().MD5(text);
System.out.println("MD5 (大写): " + md5Upper);
// 输出: B10A8DB164E0754105B7A99BE72E3FE5

// 小写 MD5 摘要（字节数组输入，4.1.0+）
byte[] data = text.getBytes();
String md5LowerFromBytes = Keel.digestHelper().md5(data);
System.out.println("MD5 (字节数组): " + md5LowerFromBytes);

// 大写 MD5 摘要（字节数组输入，4.1.0+）
String md5UpperFromBytes = Keel.digestHelper().MD5(data);
System.out.println("MD5 (字节数组大写): " + md5UpperFromBytes);
```

### 静态方法获取 MD5 MessageDigest（4.1.0+）

```java
import java.security.MessageDigest;

// 获取 MD5 MessageDigest 实例（高性能，使用 FastThreadLocal）
MessageDigest md5Digest = KeelDigestHelper.getMD5MessageDigest();
byte[] data = "Hello World".getBytes();
byte[] digestedBytes = md5Digest.digest(data);
String result = Keel.binaryHelper().encodeHexWithLowerDigits(digestedBytes);
System.out.println("直接使用 MessageDigest: " + result);
```

### MD5 文件校验

```java
public class MD5FileChecker {
    
    // 计算文件内容的 MD5
    public String calculateFileMD5(String filePath) {
        try {
            byte[] fileContent = Keel.fileHelper().readFileAsByteArray(filePath, false);
            // 使用字节数组版本的 md5 方法（4.1.0+）
            return Keel.digestHelper().md5(fileContent);
        } catch (Exception e) {
            throw new RuntimeException("文件 MD5 计算失败", e);
        }
    }
    
    // 验证文件完整性
    public boolean verifyFileIntegrity(String filePath, String expectedMD5) {
        String actualMD5 = calculateFileMD5(filePath);
        return actualMD5.equalsIgnoreCase(expectedMD5);
    }
    
    // 批量文件 MD5 计算
    public Map<String, String> calculateMultipleFilesMD5(List<String> filePaths) {
        Map<String, String> md5Map = new HashMap<>();
        for (String filePath : filePaths) {
            try {
                String md5 = calculateFileMD5(filePath);
                md5Map.put(filePath, md5);
            } catch (Exception e) {
                System.err.println("计算文件 MD5 失败: " + filePath + ", " + e.getMessage());
            }
        }
        return md5Map;
    }
}
```

## SHA 系列摘要算法

### SHA-1 摘要

```java
// 小写 SHA-1 摘要
String text = "Hello World";
String sha1Lower = Keel.digestHelper().sha1(text);
System.out.println("SHA-1 (小写): " + sha1Lower);
// 输出: 0a4d55a8d778e5022fab701977c5d840bbc486d0

// 大写 SHA-1 摘要
String sha1Upper = Keel.digestHelper().SHA1(text);
System.out.println("SHA-1 (大写): " + sha1Upper);
// 输出: 0A4D55A8D778E5022FAB701977C5D840BBC486D0
```

### SHA-512 摘要

```java
// 小写 SHA-512 摘要
String text = "Hello World";
String sha512Lower = Keel.digestHelper().sha512(text);
System.out.println("SHA-512 (小写): " + sha512Lower);

// 大写 SHA-512 摘要
String sha512Upper = Keel.digestHelper().SHA512(text);
System.out.println("SHA-512 (大写): " + sha512Upper);
```

### 通用摘要算法

```java
public class GenericDigestExamples {
    
    public void demonstrateGenericDigest() {
        String text = "Hello World";
        byte[] data = text.getBytes();
        
        try {
            // SHA-256 摘要（字符串输入）
            String sha256Lower = Keel.digestHelper().digestToLower("SHA-256", text);
            String sha256Upper = Keel.digestHelper().digestToUpper("SHA-256", text);
            System.out.println("SHA-256 (小写): " + sha256Lower);
            System.out.println("SHA-256 (大写): " + sha256Upper);
            
            // SHA-256 摘要（字节数组输入，4.1.0+）
            String sha256LowerFromBytes = Keel.digestHelper().digestToLower("SHA-256", data);
            String sha256UpperFromBytes = Keel.digestHelper().digestToUpper("SHA-256", data);
            System.out.println("SHA-256 (字节数组小写): " + sha256LowerFromBytes);
            System.out.println("SHA-256 (字节数组大写): " + sha256UpperFromBytes);
            
            // SHA-384 摘要
            String sha384Lower = Keel.digestHelper().digestToLower("SHA-384", text);
            System.out.println("SHA-384 (小写): " + sha384Lower);
            
            // MD2 摘要（如果系统支持）
            String md2Lower = Keel.digestHelper().digestToLower("MD2", text);
            System.out.println("MD2 (小写): " + md2Lower);
            
        } catch (Exception e) {
            System.err.println("摘要计算失败: " + e.getMessage());
        }
    }
}
```

## HMAC 算法

### HMAC-SHA1 基本使用

```java
// HMAC-SHA1 Base64 输出
String message = "Hello World";
String secretKey = "mySecretKey";

String hmacBase64 = Keel.digestHelper().hmac_sha1_base64(message, secretKey);
System.out.println("HMAC-SHA1 (Base64): " + hmacBase64);

// HMAC-SHA1 十六进制输出（小写）
String hmacHexLower = Keel.digestHelper().hmac_sha1_hex(message, secretKey);
System.out.println("HMAC-SHA1 (十六进制小写): " + hmacHexLower);

// HMAC-SHA1 十六进制输出（大写）
String hmacHexUpper = Keel.digestHelper().HMAC_SHA1_HEX(message, secretKey);
System.out.println("HMAC-SHA1 (十六进制大写): " + hmacHexUpper);
```

### API 签名验证

```java
public class APISignatureService {
    
    private final String secretKey;
    
    public APISignatureService(String secretKey) {
        this.secretKey = secretKey;
    }
    
    // 生成 API 请求签名
    public String generateSignature(String method, String uri, String timestamp, String body) {
        // 构建签名字符串
        String signatureString = method + "\n" + uri + "\n" + timestamp + "\n" + body;
        
        // 生成 HMAC-SHA1 签名
        return Keel.digestHelper().hmac_sha1_base64(signatureString, secretKey);
    }
    
    // 验证 API 请求签名
    public boolean verifySignature(String method, String uri, String timestamp, 
                                 String body, String providedSignature) {
        String expectedSignature = generateSignature(method, uri, timestamp, body);
        return expectedSignature.equals(providedSignature);
    }
    
    // 生成带时间戳的签名
    public Map<String, String> generateTimestampedSignature(String method, String uri, String body) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String signature = generateSignature(method, uri, timestamp, body);
        
        Map<String, String> result = new HashMap<>();
        result.put("timestamp", timestamp);
        result.put("signature", signature);
        return result;
    }
}
```

## 实际应用场景

### 1. 密码存储和验证

```java
public class PasswordHashService {
    
    private final String salt;
    
    public PasswordHashService() {
        // 生成随机盐值
        this.salt = Keel.stringHelper().generateRandomString(16);
    }
    
    // 哈希密码存储
    public String hashPassword(String plainPassword) {
        String saltedPassword = plainPassword + salt;
        return Keel.digestHelper().sha512(saltedPassword);
    }
    
    // 验证密码
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        String computedHash = hashPassword(plainPassword);
        return computedHash.equals(hashedPassword);
    }
    
    // 使用用户特定盐值的密码哈希
    public String hashPasswordWithUserSalt(String plainPassword, String userId) {
        String userSalt = Keel.digestHelper().md5(userId + salt);
        String saltedPassword = plainPassword + userSalt;
        return Keel.digestHelper().sha512(saltedPassword);
    }
}
```

### 2. 数据完整性验证

```java
public class DataIntegrityService {
    
    // 生成数据校验和（使用 SHA-256）
    public String generateChecksum(String data) {
        try {
            return Keel.digestHelper().digestToLower("SHA-256", data);
        } catch (Exception e) {
            throw new RuntimeException("校验和生成失败", e);
        }
    }
    
    // 验证数据完整性
    public boolean verifyDataIntegrity(String data, String expectedChecksum) {
        String actualChecksum = generateChecksum(data);
        return actualChecksum.equals(expectedChecksum);
    }
    
    // 批量数据完整性验证
    public Map<String, Boolean> verifyMultipleData(Map<String, String> dataChecksumMap) {
        Map<String, Boolean> results = new HashMap<>();
        
        dataChecksumMap.forEach((data, expectedChecksum) -> {
            boolean isValid = verifyDataIntegrity(data, expectedChecksum);
            results.put(data, isValid);
        });
        
        return results;
    }
    
    // 生成文件指纹
    public String generateFileFingerprint(String filePath) {
        try {
            byte[] fileContent = Keel.fileHelper().readFileAsByteArray(filePath, false);
            // 使用字节数组版本提高性能（4.1.0+）
            return Keel.digestHelper().digestToLower("SHA-512", fileContent);
        } catch (Exception e) {
            throw new RuntimeException("文件指纹生成失败", e);
        }
    }
}
```

### 3. 缓存键生成

```java
public class CacheKeyGenerator {
    
    // 生成缓存键
    public String generateCacheKey(String... params) {
        String combined = String.join(":", params);
        return Keel.digestHelper().md5(combined);
    }
    
    // 生成用户相关的缓存键
    public String generateUserCacheKey(String userId, String operation, String... params) {
        String[] allParams = new String[params.length + 2];
        allParams[0] = userId;
        allParams[1] = operation;
        System.arraycopy(params, 0, allParams, 2, params.length);
        
        return generateCacheKey(allParams);
    }
    
    // 生成带过期时间的缓存键
    public String generateTimedCacheKey(long ttlMinutes, String... params) {
        // 将时间戳按分钟对齐，实现时间窗口缓存
        long timeWindow = System.currentTimeMillis() / (ttlMinutes * 60 * 1000);
        String[] timedParams = new String[params.length + 1];
        timedParams[0] = String.valueOf(timeWindow);
        System.arraycopy(params, 0, timedParams, 1, params.length);
        
        return generateCacheKey(timedParams);
    }
}
```

### 4. 唯一标识符生成

```java
public class UniqueIdentifierService {
    
    // 生成基于内容的唯一 ID
    public String generateContentBasedId(String content) {
        return Keel.digestHelper().sha1(content);
    }
    
    // 生成基于多个参数的唯一 ID
    public String generateCompositeId(String... components) {
        String combined = String.join("|", components);
        return Keel.digestHelper().md5(combined);
    }
    
    // 生成带时间戳的唯一 ID
    public String generateTimestampedId(String content) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String combined = content + ":" + timestamp;
        return Keel.digestHelper().sha1(combined);
    }
    
    // 生成短 ID（取哈希值前8位）
    public String generateShortId(String content) {
        String fullHash = Keel.digestHelper().md5(content);
        return fullHash.substring(0, 8);
    }
}
```

### 5. 数字签名和验证

```java
public class DigitalSignatureService {
    
    private final String signingKey;
    
    public DigitalSignatureService(String signingKey) {
        this.signingKey = signingKey;
    }
    
    // 对数据进行数字签名
    public String signData(String data) {
        return Keel.digestHelper().hmac_sha1_hex(data, signingKey);
    }
    
    // 验证数字签名
    public boolean verifySignature(String data, String signature) {
        String expectedSignature = signData(data);
        return expectedSignature.equals(signature);
    }
    
    // 对 JSON 数据进行签名
    public String signJsonData(JsonObject jsonData) {
        // 对 JSON 进行排序以确保一致性
        String sortedJson = Keel.jsonHelper().getJsonForObjectWhoseItemKeysSorted(jsonData);
        return signData(sortedJson);
    }
    
    // 生成带签名的数据包
    public JsonObject createSignedDataPacket(JsonObject data) {
        String signature = signJsonData(data);
        JsonObject signedPacket = new JsonObject();
        signedPacket.put("data", data);
        signedPacket.put("signature", signature);
        signedPacket.put("timestamp", System.currentTimeMillis());
        return signedPacket;
    }
}
```

## 性能优化和最佳实践

### 1. 算法选择建议

```java
public class DigestAlgorithmGuide {
    
    public void demonstrateAlgorithmSelection() {
        String data = "测试数据";
        
        // MD5: 速度快，但安全性较低，适用于非安全场景的校验
        String md5 = Keel.digestHelper().md5(data);
        System.out.println("MD5 (快速校验): " + md5);
        
        // SHA-1: 安全性中等，兼容性好
        String sha1 = Keel.digestHelper().sha1(data);
        System.out.println("SHA-1 (兼容性): " + sha1);
        
        // SHA-256: 安全性高，推荐用于安全场景
        try {
            String sha256 = Keel.digestHelper().digestToLower("SHA-256", data);
            System.out.println("SHA-256 (推荐): " + sha256);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // SHA-512: 最高安全性，适用于高安全要求场景
        String sha512 = Keel.digestHelper().sha512(data);
        System.out.println("SHA-512 (高安全): " + sha512);
    }
}
```

### 2. 批量处理优化

```java
public class BatchDigestProcessor {
    
    // 批量 MD5 计算
    public Map<String, String> batchMD5(List<String> dataList) {
        Map<String, String> results = new HashMap<>();
        for (String data : dataList) {
            results.put(data, Keel.digestHelper().md5(data));
        }
        return results;
    }
    
    // 并行批量处理
    public Map<String, String> parallelBatchMD5(List<String> dataList) {
        return dataList.parallelStream()
            .collect(Collectors.toMap(
                data -> data,
                data -> Keel.digestHelper().md5(data)
            ));
    }
    
    // 使用字节数组优化大文件处理（4.1.0+）
    public Map<String, String> batchFileMD5(List<String> filePaths) {
        Map<String, String> results = new HashMap<>();
        for (String filePath : filePaths) {
            try {
                byte[] fileContent = Keel.fileHelper().readFileAsByteArray(filePath, false);
                String md5 = Keel.digestHelper().md5(fileContent);
                results.put(filePath, md5);
            } catch (Exception e) {
                System.err.println("文件 MD5 计算失败: " + filePath + ", " + e.getMessage());
            }
        }
        return results;
    }
}
```

## 安全注意事项

### 1. 盐值使用

```java
public class SecureHashingPractices {
    
    // ❌ 错误：不使用盐值
    public String insecureHash(String password) {
        return Keel.digestHelper().md5(password);
    }
    
    // ✅ 正确：使用盐值和安全算法
    public String secureHash(String password, String salt) {
        try {
            return Keel.digestHelper().digestToLower("SHA-256", password + salt);
        } catch (Exception e) {
            throw new RuntimeException("密码哈希失败", e);
        }
    }
    
    // ✅ 正确：使用随机盐值
    public Map<String, String> secureHashWithRandomSalt(String password) {
        String salt = Keel.stringHelper().generateRandomString(16);
        try {
            String hash = Keel.digestHelper().digestToLower("SHA-256", password + salt);
            
            Map<String, String> result = new HashMap<>();
            result.put("hash", hash);
            result.put("salt", salt);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("密码哈希失败", e);
        }
    }
}
```

### 2. 时间攻击防护

```java
public class TimingAttackProtection {
    
    // 防止时间攻击的签名验证
    public boolean secureVerifySignature(String data, String providedSignature, String key) {
        String expectedSignature = Keel.digestHelper().hmac_sha1_hex(data, key);
        
        // 使用常量时间比较
        return constantTimeEquals(expectedSignature, providedSignature);
    }
    
    // 常量时间字符串比较
    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
```

## API 参考

### 核心方法

#### MD5 算法
- `String md5(String raw)` - 小写 MD5 摘要（字符串输入）
- `String md5(byte[] raw)` - 小写 MD5 摘要（字节数组输入，4.1.0+）
- `String MD5(String raw)` - 大写 MD5 摘要（字符串输入）
- `String MD5(byte[] raw)` - 大写 MD5 摘要（字节数组输入，4.1.0+）
- `static MessageDigest getMD5MessageDigest()` - 获取 MD5 MessageDigest 实例（4.1.0+）

#### SHA 算法
- `String sha1(String raw)` - 小写 SHA-1 摘要
- `String SHA1(String raw)` - 大写 SHA-1 摘要
- `String sha512(String raw)` - 小写 SHA-512 摘要
- `String SHA512(String raw)` - 大写 SHA-512 摘要

#### 通用摘要算法
- `String digestToLower(String algorithm, String raw)` - 小写通用摘要（字符串输入）
- `String digestToLower(String algorithm, byte[] raw)` - 小写通用摘要（字节数组输入，4.1.0+）
- `String digestToUpper(String algorithm, String raw)` - 大写通用摘要（字符串输入）
- `String digestToUpper(String algorithm, byte[] raw)` - 大写通用摘要（字节数组输入，4.1.0+）

#### HMAC 算法
- `String hmac_sha1_base64(String raw, String key)` - HMAC-SHA1 Base64 输出
- `String hmac_sha1_hex(String raw, String key)` - HMAC-SHA1 小写十六进制输出
- `String HMAC_SHA1_HEX(String raw, String key)` - HMAC-SHA1 大写十六进制输出

## 注意事项

1. **算法选择**: 
   - MD5: 仅用于非安全场景的快速校验
   - SHA-1: 兼容性场景，安全性中等
   - SHA-256/SHA-512: 推荐用于安全场景

2. **性能考虑**: 
   - MD5 使用了 FastThreadLocal 优化，性能最佳
   - 4.1.0+ 版本支持字节数组输入，避免不必要的字符串转换

3. **安全实践**: 
   - 密码存储必须使用盐值
   - 避免在签名验证中出现时间攻击
   - 选择合适的算法强度

4. **编码格式**: 注意输入数据的字符编码，建议使用 UTF-8

5. **方法选择**:
   - 对于大量数据或文件处理，优先使用字节数组版本的方法
   - 需要 SHA-256 等算法时，使用 `digestToLower/Upper` 方法

## 版本历史

- **2.8**: 引入 KeelDigestHelper 基础功能
- **2.9**: 添加异常处理和堆栈跟踪功能
- **3.0.11**: 添加 SHA-512 和通用摘要算法支持
- **4.0.0**: MD5 算法使用 FastThreadLocal 优化性能
- **4.1.0**: 
  - 添加字节数组输入支持的 `md5(byte[])` 和 `MD5(byte[])` 方法
  - 添加字节数组输入支持的 `digestToLower(String, byte[])` 和 `digestToUpper(String, byte[])` 方法
  - 公开静态方法 `getMD5MessageDigest()` 供高级用户使用
- **当前版本**: 4.1.0 - 支持完整的摘要算法功能和性能优化 