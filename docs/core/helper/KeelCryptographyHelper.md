# KeelCryptographyHelper

`KeelCryptographyHelper` 是 Keel 框架中的加密工具类，提供 AES 对称加密和 RSA 非对称加密功能，为应用程序提供安全的数据加密解决方案。

## 版本信息

- **引入版本**: 2.8
- **设计模式**: 单例模式
- **线程安全**: 是

## 主要功能

### 1. AES 对称加密
- 支持多种 AES 加密算法模式
- 提供易用的加密解密接口
- 支持自定义密钥

### 2. RSA 非对称加密
- RSA 密钥对生成
- 公钥加密，私钥解密
- 数字签名功能

## 获取实例

```java
import static io.github.sinri.keel.facade.KeelInstance.Keel;

// 通过 Keel 实例获取
KeelCryptographyHelper cryptoHelper = Keel.cryptographyHelper();
```

## AES 对称加密

### 支持的加密算法

AES 加密支持以下算法模式：

```java
import io.github.sinri.keel.core.helper.encryption.aes.KeelAes;

// 可用的 AES 算法模式
KeelAes.SupportedCipherAlgorithm.AES_CBC_PKCS5Padding
KeelAes.SupportedCipherAlgorithm.AES_ECB_PKCS5Padding
KeelAes.SupportedCipherAlgorithm.AES_GCM_NoPadding
// ... 其他支持的算法模式
```

### 基本 AES 加密使用

```java
import io.github.sinri.keel.core.helper.encryption.aes.KeelAes;

// 创建 AES 加密实例
String secretKey = "MySecretKey12345"; // 16字节密钥
KeelAes aes = Keel.cryptographyHelper().aes(
    KeelAes.SupportedCipherAlgorithm.AES_CBC_PKCS5Padding, 
    secretKey
);

// 加密数据
String plainText = "这是需要加密的敏感数据";
try {
    String encryptedData = aes.encrypt(plainText);
    System.out.println("加密结果: " + encryptedData);
    
    // 解密数据
    String decryptedData = aes.decrypt(encryptedData);
    System.out.println("解密结果: " + decryptedData);
    
    // 验证
    System.out.println("加解密一致: " + plainText.equals(decryptedData));
} catch (Exception e) {
    System.err.println("加密操作失败: " + e.getMessage());
}
```

### 不同算法模式的使用

```java
public class AESModeExamples {
    
    // CBC 模式加密
    public void demonstrateCBC() {
        String key = "1234567890123456"; // 16字节密钥
        KeelAes aesCBC = Keel.cryptographyHelper().aes(
            KeelAes.SupportedCipherAlgorithm.AES_CBC_PKCS5Padding, 
            key
        );
        
        try {
            String plainText = "CBC模式加密测试";
            String encrypted = aesCBC.encrypt(plainText);
            String decrypted = aesCBC.decrypt(encrypted);
            
            System.out.println("CBC 原文: " + plainText);
            System.out.println("CBC 密文: " + encrypted);
            System.out.println("CBC 解密: " + decrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // ECB 模式加密
    public void demonstrateECB() {
        String key = "1234567890123456";
        KeelAes aesECB = Keel.cryptographyHelper().aes(
            KeelAes.SupportedCipherAlgorithm.AES_ECB_PKCS5Padding, 
            key
        );
        
        try {
            String plainText = "ECB模式加密测试";
            String encrypted = aesECB.encrypt(plainText);
            String decrypted = aesECB.decrypt(encrypted);
            
            System.out.println("ECB 原文: " + plainText);
            System.out.println("ECB 密文: " + encrypted);
            System.out.println("ECB 解密: " + decrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // GCM 模式加密（推荐用于新项目）
    public void demonstrateGCM() {
        String key = "1234567890123456";
        KeelAes aesGCM = Keel.cryptographyHelper().aes(
            KeelAes.SupportedCipherAlgorithm.AES_GCM_NoPadding, 
            key
        );
        
        try {
            String plainText = "GCM模式加密测试";
            String encrypted = aesGCM.encrypt(plainText);
            String decrypted = aesGCM.decrypt(encrypted);
            
            System.out.println("GCM 原文: " + plainText);
            System.out.println("GCM 密文: " + encrypted);
            System.out.println("GCM 解密: " + decrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### AES 密钥管理

```java
public class AESKeyManager {
    
    // 生成随机密钥
    public String generateRandomKey(int keyLength) {
        return Keel.stringHelper().generateRandomString(keyLength, 
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789");
    }
    
    // 从密码派生密钥
    public String deriveKeyFromPassword(String password) {
        // 使用密码的哈希作为密钥
        String hash = Keel.digestHelper().sha1(password);
        return hash.substring(0, 16); // 取前16字节作为AES-128密钥
    }
    
    // 密钥强度验证
    public boolean validateKeyStrength(String key) {
        return key != null && (key.length() == 16 || key.length() == 24 || key.length() == 32);
    }
}
```

## RSA 非对称加密

### 基本 RSA 使用

```java
import io.github.sinri.keel.core.helper.encryption.rsa.KeelRSA;

// 创建 RSA 实例
KeelRSA rsa = Keel.cryptographyHelper().rsa();

try {
    // 生成密钥对
    rsa.generateKeyPair(2048); // 2048位密钥
    
    // 获取公钥和私钥
    String publicKey = rsa.getPublicKeyAsString();
    String privateKey = rsa.getPrivateKeyAsString();
    
    System.out.println("公钥: " + publicKey);
    System.out.println("私钥: " + privateKey);
    
    // 使用公钥加密
    String plainText = "RSA加密测试数据";
    String encryptedData = rsa.encryptWithPublicKey(plainText);
    System.out.println("加密结果: " + encryptedData);
    
    // 使用私钥解密
    String decryptedData = rsa.decryptWithPrivateKey(encryptedData);
    System.out.println("解密结果: " + decryptedData);
    
    // 验证
    System.out.println("加解密一致: " + plainText.equals(decryptedData));
    
} catch (Exception e) {
    System.err.println("RSA操作失败: " + e.getMessage());
}
```

### RSA 数字签名

```java
public class RSASignatureExample {
    
    public void demonstrateDigitalSignature() {
        try {
            KeelRSA rsa = Keel.cryptographyHelper().rsa();
            rsa.generateKeyPair(2048);
            
            String message = "需要签名的重要消息";
            
            // 使用私钥签名
            String signature = rsa.signWithPrivateKey(message);
            System.out.println("数字签名: " + signature);
            
            // 使用公钥验证签名
            boolean isValid = rsa.verifyWithPublicKey(message, signature);
            System.out.println("签名验证结果: " + isValid);
            
            // 验证篡改检测
            String tamperedMessage = message + "被篡改";
            boolean isTamperedValid = rsa.verifyWithPublicKey(tamperedMessage, signature);
            System.out.println("篡改消息验证结果: " + isTamperedValid);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### RSA 密钥持久化

```java
public class RSAKeyPersistence {
    
    // 保存密钥到文件
    public void saveKeysToFile(KeelRSA rsa, String publicKeyFile, String privateKeyFile) {
        try {
            String publicKey = rsa.getPublicKeyAsString();
            String privateKey = rsa.getPrivateKeyAsString();
            
            // 保存公钥
            Keel.fileHelper().writeFile(publicKeyFile, publicKey, "UTF-8")
                .onSuccess(v -> System.out.println("公钥保存成功"))
                .onFailure(e -> System.err.println("公钥保存失败: " + e.getMessage()));
            
            // 保存私钥
            Keel.fileHelper().writeFile(privateKeyFile, privateKey, "UTF-8")
                .onSuccess(v -> System.out.println("私钥保存成功"))
                .onFailure(e -> System.err.println("私钥保存失败: " + e.getMessage()));
                
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // 从文件加载密钥
    public void loadKeysFromFile(KeelRSA rsa, String publicKeyFile, String privateKeyFile) {
        // 加载公钥
        Keel.fileHelper().readFileAsString(publicKeyFile, "UTF-8")
            .compose(publicKeyContent -> {
                try {
                    rsa.loadPublicKeyFromString(publicKeyContent);
                    System.out.println("公钥加载成功");
                    return Keel.fileHelper().readFileAsString(privateKeyFile, "UTF-8");
                } catch (Exception e) {
                    throw new RuntimeException("公钥加载失败", e);
                }
            })
            .onSuccess(privateKeyContent -> {
                try {
                    rsa.loadPrivateKeyFromString(privateKeyContent);
                    System.out.println("私钥加载成功");
                } catch (Exception e) {
                    System.err.println("私钥加载失败: " + e.getMessage());
                }
            })
            .onFailure(e -> System.err.println("密钥加载失败: " + e.getMessage()));
    }
}
```

## 实际应用场景

### 1. 用户密码加密存储

```java
public class UserPasswordService {
    
    private final KeelAes aes;
    
    public UserPasswordService() {
        // 从配置文件或环境变量获取密钥
        String encryptionKey = System.getenv("USER_PASSWORD_KEY");
        this.aes = Keel.cryptographyHelper().aes(
            KeelAes.SupportedCipherAlgorithm.AES_GCM_NoPadding, 
            encryptionKey
        );
    }
    
    // 加密用户密码
    public String encryptPassword(String plainPassword) {
        try {
            return aes.encrypt(plainPassword);
        } catch (Exception e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }
    
    // 验证用户密码
    public boolean verifyPassword(String plainPassword, String encryptedPassword) {
        try {
            String decrypted = aes.decrypt(encryptedPassword);
            return plainPassword.equals(decrypted);
        } catch (Exception e) {
            return false;
        }
    }
}
```

### 2. API 通信加密

```java
public class SecureAPIClient {
    
    private final KeelRSA clientRSA;
    private final KeelAes sessionAES;
    
    public SecureAPIClient() throws Exception {
        // 客户端 RSA 密钥对
        this.clientRSA = Keel.cryptographyHelper().rsa();
        this.clientRSA.generateKeyPair(2048);
        
        // 会话 AES 密钥
        String sessionKey = Keel.stringHelper().generateRandomString(16);
        this.sessionAES = Keel.cryptographyHelper().aes(
            KeelAes.SupportedCipherAlgorithm.AES_CBC_PKCS5Padding, 
            sessionKey
        );
    }
    
    // 安全发送数据
    public String sendSecureData(String data, String serverPublicKey) {
        try {
            // 1. 使用 AES 加密数据
            String encryptedData = sessionAES.encrypt(data);
            
            // 2. 使用服务器公钥加密 AES 密钥
            KeelRSA serverRSA = Keel.cryptographyHelper().rsa();
            serverRSA.loadPublicKeyFromString(serverPublicKey);
            String encryptedSessionKey = serverRSA.encryptWithPublicKey(sessionAES.getKey());
            
            // 3. 组合加密数据和加密密钥
            return encryptedSessionKey + ":" + encryptedData;
            
        } catch (Exception e) {
            throw new RuntimeException("安全数据发送失败", e);
        }
    }
}
```

### 3. 文件加密存储

```java
public class SecureFileStorage {
    
    private final KeelAes fileAES;
    
    public SecureFileStorage(String masterKey) {
        this.fileAES = Keel.cryptographyHelper().aes(
            KeelAes.SupportedCipherAlgorithm.AES_GCM_NoPadding, 
            masterKey
        );
    }
    
    // 加密文件内容
    public Future<Void> encryptFile(String sourceFile, String encryptedFile) {
        return Keel.fileHelper().readFileAsString(sourceFile, "UTF-8")
            .compose(content -> {
                try {
                    String encryptedContent = fileAES.encrypt(content);
                    return Keel.fileHelper().writeFile(encryptedFile, encryptedContent, "UTF-8");
                } catch (Exception e) {
                    return Future.failedFuture(e);
                }
            });
    }
    
    // 解密文件内容
    public Future<String> decryptFile(String encryptedFile) {
        return Keel.fileHelper().readFileAsString(encryptedFile, "UTF-8")
            .compose(encryptedContent -> {
                try {
                    String decryptedContent = fileAES.decrypt(encryptedContent);
                    return Future.succeededFuture(decryptedContent);
                } catch (Exception e) {
                    return Future.failedFuture(e);
                }
            });
    }
}
```

## 安全最佳实践

### 1. 密钥管理
```java
public class SecurityBestPractices {
    
    // ❌ 错误：硬编码密钥
    private static final String BAD_KEY = "hardcodedkey123";
    
    // ✅ 正确：从环境变量获取密钥
    private String getEncryptionKey() {
        String key = System.getenv("ENCRYPTION_KEY");
        if (key == null || key.length() < 16) {
            throw new IllegalStateException("加密密钥未配置或长度不足");
        }
        return key;
    }
    
    // ✅ 正确：密钥轮换
    public void rotateEncryptionKey(String oldKey, String newKey) {
        KeelAes oldAES = Keel.cryptographyHelper().aes(
            KeelAes.SupportedCipherAlgorithm.AES_GCM_NoPadding, oldKey);
        KeelAes newAES = Keel.cryptographyHelper().aes(
            KeelAes.SupportedCipherAlgorithm.AES_GCM_NoPadding, newKey);
        
        // 重新加密所有数据...
    }
}
```

### 2. 算法选择
- **AES-GCM**: 推荐用于新项目，提供认证加密
- **AES-CBC**: 兼容性好，需要额外的完整性验证
- **AES-ECB**: 不推荐，存在安全风险
- **RSA**: 适用于密钥交换和数字签名，不适合大量数据加密

### 3. 错误处理
```java
public class SecureErrorHandling {
    
    public String secureDecrypt(String encryptedData) {
        try {
            KeelAes aes = Keel.cryptographyHelper().aes(
                KeelAes.SupportedCipherAlgorithm.AES_GCM_NoPadding, 
                getEncryptionKey()
            );
            return aes.decrypt(encryptedData);
        } catch (Exception e) {
            // ❌ 错误：暴露详细错误信息
            // throw new RuntimeException("解密失败: " + e.getMessage(), e);
            
            // ✅ 正确：记录详细错误，返回通用错误
            Keel.getLogger().exception(e);
            throw new RuntimeException("数据解密失败");
        }
    }
}
```

## 注意事项

1. **密钥安全**: 永远不要在代码中硬编码密钥，使用环境变量或安全的密钥管理系统
2. **算法选择**: 优先选择 AES-GCM 等认证加密算法
3. **密钥长度**: AES 推荐使用 256 位密钥，RSA 推荐使用 2048 位或更高
4. **错误处理**: 避免在错误信息中泄露敏感信息
5. **性能考虑**: RSA 加密性能较低，大量数据建议使用 AES + RSA 混合加密

## 版本历史

- **2.8**: 引入 KeelCryptographyHelper 和 AES 加密功能
- **3.0.1**: 添加 RSA 非对称加密支持
- **当前版本**: 支持多种加密算法和完整的密钥管理功能 