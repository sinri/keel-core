# KeelAuthenticationHelper

`KeelAuthenticationHelper` 是 Keel 框架中的身份认证工具类，提供密码哈希验证和 Google Authenticator TOTP（基于时间的一次性密码）双因子认证功能。

## 版本信息

- **引入版本**: 2.9.4
- **设计模式**: 单例模式
- **线程安全**: 是

## 主要功能

### 1. 密码哈希与验证
- 兼容 PHP 的 BCrypt 密码哈希算法
- 安全的密码存储和验证机制

### 2. Google Authenticator 支持
- 同步和异步两种实现方式
- TOTP 双因子认证
- 支持自定义配置参数

## 获取实例

```java
import static io.github.sinri.keel.facade.KeelInstance.Keel;

// 通过 Keel 实例获取
KeelAuthenticationHelper authHelper = Keel.authenticationHelper();
```

## 密码哈希功能

### 密码哈希

使用 BCrypt 算法对密码进行哈希处理，兼容 PHP 的 `password_hash()` 函数：

```java
// 对密码进行哈希
String password = "mySecretPassword";
String hashedPassword = Keel.authenticationHelper().php_password_hash(password);
System.out.println("Hashed: " + hashedPassword);
// 输出类似: $2a$10$N9qo8uLOickgx2ZMRZoMye...
```

### 密码验证

验证明文密码与哈希值是否匹配，兼容 PHP 的 `password_verify()` 函数：

```java
String password = "mySecretPassword";
String hashedPassword = "$2a$10$N9qo8uLOickgx2ZMRZoMye..."; // 之前生成的哈希

boolean isValid = Keel.authenticationHelper().php_password_verify(password, hashedPassword);
if (isValid) {
    System.out.println("密码验证成功");
} else {
    System.out.println("密码验证失败");
}
```

### 完整的用户注册和登录示例

```java
public class UserAuthService {
    
    // 用户注册
    public void registerUser(String username, String password) {
        // 对密码进行哈希
        String hashedPassword = Keel.authenticationHelper().php_password_hash(password);
        
        // 保存到数据库
        saveUserToDatabase(username, hashedPassword);
        System.out.println("用户注册成功");
    }
    
    // 用户登录
    public boolean loginUser(String username, String password) {
        // 从数据库获取用户的哈希密码
        String storedHash = getUserHashFromDatabase(username);
        if (storedHash == null) {
            return false; // 用户不存在
        }
        
        // 验证密码
        return Keel.authenticationHelper().php_password_verify(password, storedHash);
    }
}
```

## Google Authenticator 功能

### 同步版本 GoogleAuthenticator

#### 基本使用（默认配置）

```java
// 获取默认配置的 GoogleAuthenticator（窗口大小为1）
GoogleAuthenticator authenticator = Keel.authenticationHelper().getGoogleAuthenticator();

// 生成新的密钥凭证
GoogleAuthenticatorKey credentials = authenticator.createCredentials();
String secretKey = credentials.getKey();
int verificationCode = credentials.getVerificationCode();
List<Integer> scratchCodes = credentials.getScratchCodes();

System.out.println("密钥: " + secretKey);
System.out.println("验证码: " + verificationCode);
System.out.println("备用码: " + scratchCodes);
```

#### 自定义配置

```java
// 使用自定义配置
GoogleAuthenticator authenticator = Keel.authenticationHelper().getGoogleAuthenticator(
    configBuilder -> {
        configBuilder.setWindowSize(3);  // 设置时间窗口大小
        configBuilder.setCodeDigits(6);  // 设置验证码位数
        configBuilder.setTimeStepSizeInMillis(30000); // 设置时间步长（30秒）
    }
);
```

#### TOTP 验证

```java
// 生成当前时间的 TOTP 密码
String secretKey = "JBSWY3DPEHPK3PXP"; // Base32 编码的密钥
int currentTotp = authenticator.getTotpPassword(secretKey);
System.out.println("当前 TOTP: " + currentTotp);

// 生成指定时间的 TOTP 密码
long specificTime = System.currentTimeMillis();
int totpAtTime = authenticator.getTotpPassword(secretKey, specificTime);

// 验证 TOTP 密码
boolean isValid = authenticator.authorize(secretKey, currentTotp);
if (isValid) {
    System.out.println("TOTP 验证成功");
} else {
    System.out.println("TOTP 验证失败");
}
```

#### 生成 QR 码 URL

```java
import io.github.sinri.keel.core.helper.authenticator.googleauth.GoogleAuthenticatorQRGenerator;

GoogleAuthenticatorKey credentials = authenticator.createCredentials();

// 生成 QR 码 URL
String qrCodeUrl = GoogleAuthenticatorQRGenerator.getOtpAuthURL(
    "MyApp",           // 发行者名称
    "user@example.com", // 账户名称
    credentials        // 凭证
);

System.out.println("QR 码 URL: " + qrCodeUrl);

// 生成 otpauth URI
String otpAuthUri = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(
    "MyApp",
    "user@example.com",
    credentials
);
System.out.println("OTP Auth URI: " + otpAuthUri);
```

### 异步版本 AsyncGoogleAuthenticator

#### 基本使用

```java
import io.vertx.core.Future;

// 获取异步版本的 GoogleAuthenticator
AsyncGoogleAuthenticator asyncAuth = Keel.authenticationHelper().getAsyncGoogleAuthenticator();

// 生成密钥（同步操作）
GoogleAuthenticatorKey credentials = asyncAuth.createCredentials();
String secretKey = credentials.getKey();

// 异步验证 TOTP
Future<Boolean> authResult = asyncAuth.authorize(secretKey, 123456);
authResult.onSuccess(isValid -> {
    if (isValid) {
        System.out.println("异步 TOTP 验证成功");
    } else {
        System.out.println("异步 TOTP 验证失败");
    }
}).onFailure(throwable -> {
    System.err.println("验证过程中发生错误: " + throwable.getMessage());
});
```

#### 自定义配置的异步版本

```java
AsyncGoogleAuthenticator asyncAuth = Keel.authenticationHelper().getAsyncGoogleAuthenticator(
    configBuilder -> {
        configBuilder.setWindowSize(5);  // 更大的时间窗口
        configBuilder.setCodeDigits(8);  // 8位验证码
    }
);
```

### 完整的双因子认证示例

```java
public class TwoFactorAuthService {
    
    private final AsyncGoogleAuthenticator authenticator;
    
    public TwoFactorAuthService() {
        this.authenticator = Keel.authenticationHelper().getAsyncGoogleAuthenticator();
    }
    
    // 为用户启用双因子认证
    public GoogleAuthenticatorKey enableTwoFactor(String userId) {
        GoogleAuthenticatorKey credentials = authenticator.createCredentials();
        
        // 保存密钥到数据库
        saveUserSecret(userId, credentials.getKey());
        
        // 返回凭证供用户设置 Google Authenticator
        return credentials;
    }
    
    // 验证用户的双因子认证码
    public Future<Boolean> verifyTwoFactor(String userId, int totpCode) {
        String secretKey = getUserSecret(userId);
        if (secretKey == null) {
            return Future.succeededFuture(false);
        }
        
        return authenticator.authorize(secretKey, totpCode);
    }
    
    // 完整的登录流程
    public Future<Boolean> authenticateUser(String username, String password, int totpCode) {
        // 第一步：验证用户名密码
        boolean passwordValid = Keel.authenticationHelper()
            .php_password_verify(password, getUserPasswordHash(username));
        
        if (!passwordValid) {
            return Future.succeededFuture(false);
        }
        
        // 第二步：验证 TOTP
        return verifyTwoFactor(username, totpCode);
    }
    
    private void saveUserSecret(String userId, String secret) {
        // 实现保存逻辑
    }
    
    private String getUserSecret(String userId) {
        // 实现获取逻辑
        return null;
    }
    
    private String getUserPasswordHash(String username) {
        // 实现获取用户密码哈希的逻辑
        return null;
    }
}
```

## 配置选项

### GoogleAuthenticatorConfig 配置参数

```java
GoogleAuthenticator authenticator = Keel.authenticationHelper().getGoogleAuthenticator(
    configBuilder -> {
        // 时间窗口大小（默认1，推荐1-3）
        configBuilder.setWindowSize(1);
        
        // 验证码位数（默认6）
        configBuilder.setCodeDigits(6);
        
        // 时间步长，毫秒（默认30000，即30秒）
        configBuilder.setTimeStepSizeInMillis(30000);
        
        // 密钥位数（默认160）
        configBuilder.setSecretBits(160);
        
        // 备用码数量（默认5）
        configBuilder.setNumberOfScratchCodes(5);
        
        // 密钥表示方式（BASE32 或 BASE64）
        configBuilder.setKeyRepresentation(GoogleAuthenticatorConfig.KeyRepresentation.BASE32);
        
        // HMAC 哈希函数
        configBuilder.setHmacHashFunction(GoogleAuthenticatorConfig.HmacHashFunction.HmacSHA1);
    }
);
```

## 最佳实践

### 1. 密码安全
```java
// ✅ 正确：使用强密码策略
public boolean isStrongPassword(String password) {
    return password.length() >= 8 && 
           password.matches(".*[A-Z].*") && 
           password.matches(".*[a-z].*") && 
           password.matches(".*[0-9].*") && 
           password.matches(".*[!@#$%^&*()].*");
}

// ✅ 正确：每次都重新哈希，不要重用哈希值
String hash1 = Keel.authenticationHelper().php_password_hash("password");
String hash2 = Keel.authenticationHelper().php_password_hash("password");
// hash1 != hash2，这是正常的
```

### 2. TOTP 安全
```java
// ✅ 正确：安全存储密钥
public void storeSecretSecurely(String userId, String secret) {
    // 加密存储密钥
    String encryptedSecret = encryptSecret(secret);
    database.save(userId, encryptedSecret);
}

// ✅ 正确：合理的时间窗口
GoogleAuthenticator authenticator = Keel.authenticationHelper().getGoogleAuthenticator(
    configBuilder -> configBuilder.setWindowSize(1) // 不要设置过大的窗口
);

// ✅ 正确：防止重放攻击
public class TOTPValidator {
    private final Set<String> usedCodes = new ConcurrentHashMap<>();
    
    public boolean validateWithReplayProtection(String userId, int code) {
        String key = userId + ":" + code + ":" + (System.currentTimeMillis() / 30000);
        if (usedCodes.contains(key)) {
            return false; // 防止重放攻击
        }
        
        boolean valid = authenticator.authorize(getSecret(userId), code);
        if (valid) {
            usedCodes.add(key);
            // 清理过期的记录
            cleanupExpiredCodes();
        }
        return valid;
    }
}
```

### 3. 错误处理
```java
// ✅ 正确：妥善处理异常
public Future<Boolean> safeVerifyTOTP(String secret, int code) {
    try {
        return asyncAuth.authorize(secret, code)
            .recover(throwable -> {
                logger.error("TOTP 验证失败", throwable);
                return Future.succeededFuture(false);
            });
    } catch (Exception e) {
        logger.error("TOTP 验证异常", e);
        return Future.succeededFuture(false);
    }
}
```

## 注意事项

1. **密钥安全**: Google Authenticator 的密钥必须安全存储，建议加密保存
2. **时间同步**: TOTP 依赖于时间，确保服务器时间准确
3. **窗口大小**: 不要设置过大的时间窗口，以免降低安全性
4. **重放攻击**: 在生产环境中应实现防重放机制
5. **备用码**: 妥善管理备用码，使用后应立即失效
6. **用户体验**: 提供清晰的设置指导和错误提示

## 相关类

- `GoogleAuthenticator`: 同步版本的 Google Authenticator
- `AsyncGoogleAuthenticator`: 异步版本的 Google Authenticator  
- `GoogleAuthenticatorKey`: 认证密钥凭证
- `GoogleAuthenticatorConfig`: 配置类
- `GoogleAuthenticatorQRGenerator`: QR 码生成工具

## 参考资料

- [RFC 6238 - TOTP: Time-Based One-Time Password Algorithm](https://tools.ietf.org/html/rfc6238)
- [Google Authenticator Key URI Format](https://github.com/google/google-authenticator/wiki/Key-Uri-Format)
- [BCrypt Algorithm](https://en.wikipedia.org/wiki/Bcrypt)

