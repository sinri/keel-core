# KeelRandomHelper

`KeelRandomHelper` 是 Keel 框架中的随机数生成工具类，基于 Vert.x 的伪随机数生成器（PRNG）提供线程安全的随机数生成功能。

## 版本信息

- **引入版本**: 3.0.1
- **设计模式**: 单例模式
- **线程安全**: 是
- **延迟初始化**: 是（3.2.11 版本优化）

## 主要功能

### 1. PRNG 支持
- 基于 Vert.x 的伪随机数生成器
- 高质量的随机数生成算法
- 适用于密码学和安全应用

### 2. 线程安全
- 使用原子引用确保线程安全
- 支持多线程并发访问
- 无锁设计，性能优异

### 3. 延迟初始化
- 首次使用时才初始化随机数生成器
- 自动检测 Vertx 环境
- 智能选择初始化策略

## 获取实例

```java
import static io.github.sinri.keel.facade.KeelInstance.Keel;

// 通过 Keel 实例获取
KeelRandomHelper randomHelper = Keel.randomHelper();
```

## 基础随机数生成

### 获取 PRNG 实例

```java
import io.vertx.ext.auth.prng.VertxContextPRNG;

// 获取伪随机数生成器实例
VertxContextPRNG prng = Keel.randomHelper().getPRNG();

// PRNG 实例是线程安全的，可以在多个线程中使用
System.out.println("PRNG 实例: " + prng.getClass().getSimpleName());
```

### 生成随机字节

```java
// 生成指定长度的随机字节数组
VertxContextPRNG prng = Keel.randomHelper().getPRNG();

// 生成 16 字节的随机数据
byte[] randomBytes = new byte[16];
prng.nextBytes(randomBytes);
System.out.println("随机字节: " + Arrays.toString(randomBytes));

// 生成 32 字节的随机数据（适用于密钥生成）
byte[] keyBytes = new byte[32];
prng.nextBytes(keyBytes);
String hexKey = Keel.binaryHelper().encodeHexWithLowerDigits(keyBytes);
System.out.println("随机密钥: " + hexKey);
```

### 生成随机整数

```java
VertxContextPRNG prng = Keel.randomHelper().getPRNG();

// 生成随机整数
int randomInt = prng.nextInt();
System.out.println("随机整数: " + randomInt);

// 生成指定范围内的随机整数
int randomInRange = prng.nextInt(100); // 0-99
System.out.println("0-99 范围随机数: " + randomInRange);

// 生成指定范围内的随机整数（包含最小值，不包含最大值）
int min = 10, max = 50;
int randomBetween = prng.nextInt(max - min) + min;
System.out.println("10-49 范围随机数: " + randomBetween);
```

### 生成随机长整型

```java
VertxContextPRNG prng = Keel.randomHelper().getPRNG();

// 生成随机长整型
long randomLong = prng.nextLong();
System.out.println("随机长整型: " + randomLong);

// 生成正数长整型
long positiveLong = Math.abs(prng.nextLong());
System.out.println("正数长整型: " + positiveLong);
```

### 生成随机浮点数

```java
VertxContextPRNG prng = Keel.randomHelper().getPRNG();

// 生成 0.0 到 1.0 之间的随机浮点数
float randomFloat = prng.nextFloat();
System.out.println("随机浮点数: " + randomFloat);

// 生成 0.0 到 1.0 之间的随机双精度浮点数
double randomDouble = prng.nextDouble();
System.out.println("随机双精度: " + randomDouble);

// 生成指定范围的随机浮点数
double min = 1.5, max = 10.5;
double randomInRange = min + (max - min) * prng.nextDouble();
System.out.println("1.5-10.5 范围随机数: " + randomInRange);
```

## 实际应用场景

### 1. 会话 ID 生成

```java
public class SessionManager {
    public String generateSessionId() {
        VertxContextPRNG prng = Keel.randomHelper().getPRNG();
        
        // 生成 32 字节的随机会话 ID
        byte[] sessionBytes = new byte[32];
        prng.nextBytes(sessionBytes);
        
        // 转换为十六进制字符串
        return Keel.binaryHelper().encodeHexWithLowerDigits(sessionBytes);
    }
    
    public String generateShortSessionId() {
        VertxContextPRNG prng = Keel.randomHelper().getPRNG();
        
        // 生成较短的会话 ID（16 字节）
        byte[] sessionBytes = new byte[16];
        prng.nextBytes(sessionBytes);
        
        // 使用 Base64 编码以获得更短的字符串
        return Keel.binaryHelper().encodeWithBase64ToString(sessionBytes)
            .replace("+", "-")
            .replace("/", "_")
            .replace("=", "");
    }
}
```

### 2. 随机密码生成

```java
public class PasswordGenerator {
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()_+-=[]{}|;:,.<>?";
    
    public String generateSecurePassword(int length) {
        String allChars = UPPERCASE + LOWERCASE + DIGITS + SPECIAL;
        return generateRandomString(length, allChars);
    }
    
    public String generateSimplePassword(int length) {
        String simpleChars = UPPERCASE + LOWERCASE + DIGITS;
        return generateRandomString(length, simpleChars);
    }
    
    private String generateRandomString(int length, String charset) {
        VertxContextPRNG prng = Keel.randomHelper().getPRNG();
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            int index = prng.nextInt(charset.length());
            password.append(charset.charAt(index));
        }
        
        return password.toString();
    }
    
    // 生成符合复杂度要求的密码
    public String generateComplexPassword(int length) {
        if (length < 4) {
            throw new IllegalArgumentException("密码长度至少为 4");
        }
        
        VertxContextPRNG prng = Keel.randomHelper().getPRNG();
        StringBuilder password = new StringBuilder();
        
        // 确保至少包含每种类型的字符
        password.append(UPPERCASE.charAt(prng.nextInt(UPPERCASE.length())));
        password.append(LOWERCASE.charAt(prng.nextInt(LOWERCASE.length())));
        password.append(DIGITS.charAt(prng.nextInt(DIGITS.length())));
        password.append(SPECIAL.charAt(prng.nextInt(SPECIAL.length())));
        
        // 填充剩余长度
        String allChars = UPPERCASE + LOWERCASE + DIGITS + SPECIAL;
        for (int i = 4; i < length; i++) {
            int index = prng.nextInt(allChars.length());
            password.append(allChars.charAt(index));
        }
        
        // 打乱字符顺序
        return shuffleString(password.toString());
    }
    
    private String shuffleString(String input) {
        VertxContextPRNG prng = Keel.randomHelper().getPRNG();
        char[] chars = input.toCharArray();
        
        for (int i = chars.length - 1; i > 0; i--) {
            int j = prng.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        
        return new String(chars);
    }
}
```

### 3. 随机令牌生成

```java
public class TokenGenerator {
    public String generateApiToken() {
        VertxContextPRNG prng = Keel.randomHelper().getPRNG();
        
        // 生成 API 令牌（64 字节）
        byte[] tokenBytes = new byte[64];
        prng.nextBytes(tokenBytes);
        
        return "ak_" + Keel.binaryHelper().encodeHexWithLowerDigits(tokenBytes);
    }
    
    public String generateRefreshToken() {
        VertxContextPRNG prng = Keel.randomHelper().getPRNG();
        
        // 生成刷新令牌（48 字节）
        byte[] tokenBytes = new byte[48];
        prng.nextBytes(tokenBytes);
        
        return "rt_" + Keel.binaryHelper().encodeWithBase64ToString(tokenBytes)
            .replace("+", "-")
            .replace("/", "_")
            .replace("=", "");
    }
    
    public String generateVerificationCode(int length) {
        VertxContextPRNG prng = Keel.randomHelper().getPRNG();
        StringBuilder code = new StringBuilder();
        
        // 生成纯数字验证码
        for (int i = 0; i < length; i++) {
            code.append(prng.nextInt(10));
        }
        
        return code.toString();
    }
}
```

### 4. 随机采样和洗牌

```java
public class RandomSampler {
    public <T> List<T> randomSample(List<T> source, int sampleSize) {
        if (sampleSize >= source.size()) {
            return new ArrayList<>(source);
        }
        
        VertxContextPRNG prng = Keel.randomHelper().getPRNG();
        List<T> result = new ArrayList<>();
        List<T> temp = new ArrayList<>(source);
        
        for (int i = 0; i < sampleSize; i++) {
            int index = prng.nextInt(temp.size());
            result.add(temp.remove(index));
        }
        
        return result;
    }
    
    public <T> void shuffle(List<T> list) {
        VertxContextPRNG prng = Keel.randomHelper().getPRNG();
        
        for (int i = list.size() - 1; i > 0; i--) {
            int j = prng.nextInt(i + 1);
            Collections.swap(list, i, j);
        }
    }
    
    public <T> T randomChoice(List<T> choices) {
        if (choices.isEmpty()) {
            throw new IllegalArgumentException("选择列表不能为空");
        }
        
        VertxContextPRNG prng = Keel.randomHelper().getPRNG();
        int index = prng.nextInt(choices.size());
        return choices.get(index);
    }
    
    // 加权随机选择
    public <T> T weightedRandomChoice(Map<T, Double> weightedChoices) {
        if (weightedChoices.isEmpty()) {
            throw new IllegalArgumentException("选择映射不能为空");
        }
        
        double totalWeight = weightedChoices.values().stream()
            .mapToDouble(Double::doubleValue)
            .sum();
        
        VertxContextPRNG prng = Keel.randomHelper().getPRNG();
        double randomValue = prng.nextDouble() * totalWeight;
        
        double currentWeight = 0;
        for (Map.Entry<T, Double> entry : weightedChoices.entrySet()) {
            currentWeight += entry.getValue();
            if (randomValue <= currentWeight) {
                return entry.getKey();
            }
        }
        
        // 理论上不应该到达这里
        return weightedChoices.keySet().iterator().next();
    }
}
```

### 5. 随机测试数据生成

```java
public class TestDataGenerator {
    private static final String[] FIRST_NAMES = {
        "张", "李", "王", "刘", "陈", "杨", "赵", "黄", "周", "吴"
    };
    
    private static final String[] LAST_NAMES = {
        "伟", "芳", "娜", "敏", "静", "丽", "强", "磊", "军", "洋"
    };
    
    public String generateRandomName() {
        VertxContextPRNG prng = Keel.randomHelper().getPRNG();
        
        String firstName = FIRST_NAMES[prng.nextInt(FIRST_NAMES.length)];
        String lastName = LAST_NAMES[prng.nextInt(LAST_NAMES.length)];
        
        return firstName + lastName;
    }
    
    public String generateRandomEmail() {
        VertxContextPRNG prng = Keel.randomHelper().getPRNG();
        String[] domains = {"gmail.com", "163.com", "qq.com", "sina.com"};
        
        StringBuilder email = new StringBuilder();
        
        // 生成用户名部分
        for (int i = 0; i < 8; i++) {
            if (prng.nextBoolean()) {
                email.append((char) ('a' + prng.nextInt(26)));
            } else {
                email.append(prng.nextInt(10));
            }
        }
        
        email.append("@");
        email.append(domains[prng.nextInt(domains.length)]);
        
        return email.toString();
    }
    
    public String generateRandomPhoneNumber() {
        VertxContextPRNG prng = Keel.randomHelper().getPRNG();
        StringBuilder phone = new StringBuilder("1");
        
        // 第二位数字（3-9）
        phone.append(3 + prng.nextInt(7));
        
        // 剩余 9 位数字
        for (int i = 0; i < 9; i++) {
            phone.append(prng.nextInt(10));
        }
        
        return phone.toString();
    }
    
    public int generateRandomAge(int min, int max) {
        VertxContextPRNG prng = Keel.randomHelper().getPRNG();
        return min + prng.nextInt(max - min + 1);
    }
}
```

## 性能优化建议

### 1. 实例复用

```java
public class OptimizedRandomService {
    private final VertxContextPRNG prng;
    
    public OptimizedRandomService() {
        // 在构造函数中获取 PRNG 实例并复用
        this.prng = Keel.randomHelper().getPRNG();
    }
    
    public byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        prng.nextBytes(bytes);
        return bytes;
    }
    
    public String generateRandomHex(int byteLength) {
        byte[] bytes = generateRandomBytes(byteLength);
        return Keel.binaryHelper().encodeHexWithLowerDigits(bytes);
    }
}
```

### 2. 批量生成

```java
public class BatchRandomGenerator {
    public List<String> generateMultipleTokens(int count, int tokenLength) {
        VertxContextPRNG prng = Keel.randomHelper().getPRNG();
        List<String> tokens = new ArrayList<>(count);
        
        for (int i = 0; i < count; i++) {
            byte[] tokenBytes = new byte[tokenLength];
            prng.nextBytes(tokenBytes);
            tokens.add(Keel.binaryHelper().encodeHexWithLowerDigits(tokenBytes));
        }
        
        return tokens;
    }
}
```

## 安全注意事项

1. **密码学用途**: VertxContextPRNG 适用于密码学应用，提供高质量的随机数
2. **种子安全**: 系统会自动处理种子初始化，无需手动设置
3. **线程安全**: 可以在多线程环境中安全使用
4. **状态保护**: 避免序列化 PRNG 实例，以防止状态泄露

## 版本历史

- **3.0.1**: 初始版本，提供基础的 PRNG 功能
- **3.2.11**: 优化为延迟初始化，提升启动性能

## 相关工具类

- `KeelBinaryHelper`: 随机字节的编码转换
- `KeelStringHelper`: 随机字符串生成
- `KeelCryptographyHelper`: 密码学随机数应用 