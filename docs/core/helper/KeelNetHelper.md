# KeelNetHelper

`KeelNetHelper` 是 Keel 框架中的网络工具类，提供 IP 地址处理、本机信息获取、Web 客户端 IP 解析等网络相关的实用功能。

## 版本信息

- **引入版本**: 2.8
- **设计模式**: 单例模式
- **线程安全**: 是

## 主要功能

### 1. IP 地址处理
- IPv4 地址与数字的相互转换
- IP 地址字节数组转换
- 支持网络地址计算

### 2. 本机信息获取
- 本地主机地址获取
- 主机名和规范名称获取
- 网络接口信息查询

### 3. Web 客户端 IP 解析
- 支持 X-Forwarded-For 头解析
- 客户端 IP 链提取
- 代理环境下的真实 IP 获取

## 获取实例

```java
import static io.github.sinri.keel.facade.KeelInstance.Keel;

// 通过 Keel 实例获取
KeelNetHelper netHelper = Keel.netHelper();
```

## IP 地址转换功能

### IPv4 地址转数字

将 IPv4 地址字符串转换为长整型数字：

```java
// IPv4 地址转数字
String ipv4 = "192.168.1.100";
Long ipNumber = Keel.netHelper().convertIPv4ToNumber(ipv4);
System.out.println("IP 数字: " + ipNumber);
// 输出: 3232235876

// 处理无效 IP 地址
String invalidIp = "999.999.999.999";
Long result = Keel.netHelper().convertIPv4ToNumber(invalidIp);
System.out.println("无效 IP 结果: " + result);
// 输出: null
```

### 数字转 IPv4 地址

将长整型数字转换回 IPv4 地址字符串：

```java
// 数字转 IPv4 地址
long ipNumber = 3232235876L;
String ipv4 = Keel.netHelper().convertNumberToIPv4(ipNumber);
System.out.println("IP 地址: " + ipv4);
// 输出: 192.168.1.100

// 处理无效数字
long invalidNumber = -1L;
String result = Keel.netHelper().convertNumberToIPv4(invalidNumber);
System.out.println("无效数字结果: " + result);
// 输出: null
```

### IP 地址字节数组转换

将 IP 地址转换为字节数组格式：

```java
// 从数字转换为字节数组
long ipNumber = 3232235876L; // 192.168.1.100
byte[] addressBytes = Keel.netHelper().convertIPv4ToAddressBytes(ipNumber);
System.out.println("字节数组: " + Arrays.toString(addressBytes));
// 输出: [-64, -88, 1, 100] (对应 192, 168, 1, 100)

// 从字符串转换为字节数组
String ipv4 = "10.0.0.1";
byte[] bytes = Keel.netHelper().convertIPv4ToAddressBytes(ipv4);
System.out.println("字节数组: " + Arrays.toString(bytes));
// 输出: [10, 0, 0, 1]
```

## 本机信息获取

### 获取本地主机地址

```java
// 获取本地主机 IP 地址
String localAddress = Keel.netHelper().getLocalHostAddress();
System.out.println("本地 IP 地址: " + localAddress);
// 输出: 127.0.0.1 或实际的本机 IP

// 处理获取失败的情况
if (localAddress != null) {
    System.out.println("成功获取本地 IP: " + localAddress);
} else {
    System.out.println("无法获取本地 IP 地址");
}
```

### 获取主机名信息

```java
// 获取主机名
String hostName = Keel.netHelper().getLocalHostName();
System.out.println("主机名: " + hostName);
// 输出: MacBook-Pro.local 或其他主机名

// 获取规范主机名
String canonicalName = Keel.netHelper().getLocalHostCanonicalName();
System.out.println("规范主机名: " + canonicalName);
// 输出: localhost 或完整域名

// 完整的主机信息获取
public void printHostInfo() {
    String address = Keel.netHelper().getLocalHostAddress();
    String name = Keel.netHelper().getLocalHostName();
    String canonical = Keel.netHelper().getLocalHostCanonicalName();
    
    System.out.println("=== 主机信息 ===");
    System.out.println("IP 地址: " + (address != null ? address : "未知"));
    System.out.println("主机名: " + (name != null ? name : "未知"));
    System.out.println("规范名: " + (canonical != null ? canonical : "未知"));
}
```

## Web 客户端 IP 解析

### 解析客户端 IP 链

在 Web 应用中解析客户端的真实 IP 地址，支持代理和负载均衡环境：

```java
import io.vertx.ext.web.RoutingContext;

// 在 Vert.x Web 路由处理器中使用
public void handleRequest(RoutingContext ctx) {
    // 解析客户端 IP 链
    List<String> clientIpChain = Keel.netHelper().parseWebClientIPChain(ctx);
    
    System.out.println("客户端 IP 链: " + clientIpChain);
    // 输出可能包含: [真实客户端IP, 代理1IP, 代理2IP, 直连IP]
    
    // 获取最可能的真实客户端 IP（链中第一个）
    String realClientIp = clientIpChain.isEmpty() ? "未知" : clientIpChain.get(0);
    System.out.println("真实客户端 IP: " + realClientIp);
    
    // 记录完整的 IP 链用于审计
    String ipChainStr = String.join(" -> ", clientIpChain);
    System.out.println("IP 传递链: " + ipChainStr);
}
```

### 处理 X-Forwarded-For 头

```java
// 模拟不同的代理环境
public void demonstrateProxyScenarios() {
    // 场景1: 直接连接
    // X-Forwarded-For: 无
    // Remote Address: 192.168.1.100
    // 结果: [192.168.1.100]
    
    // 场景2: 单层代理
    // X-Forwarded-For: 203.0.113.1
    // Remote Address: 192.168.1.1
    // 结果: [203.0.113.1, 192.168.1.1]
    
    // 场景3: 多层代理
    // X-Forwarded-For: 203.0.113.1, 198.51.100.1
    // Remote Address: 192.168.1.1
    // 结果: [203.0.113.1, 198.51.100.1, 192.168.1.1]
}
```

## 实际应用场景

### 1. 网络访问控制

```java
public class IPAccessController {
    private final Set<String> allowedIpRanges = Set.of("192.168.1.0/24", "10.0.0.0/8");
    
    public boolean isIpAllowed(String clientIp) {
        Long ipNumber = Keel.netHelper().convertIPv4ToNumber(clientIp);
        if (ipNumber == null) {
            return false; // 无效 IP 拒绝访问
        }
        
        // 检查是否在允许的 IP 范围内
        return checkIpInRanges(ipNumber, allowedIpRanges);
    }
    
    private boolean checkIpInRanges(Long ipNumber, Set<String> ranges) {
        // 实现 IP 范围检查逻辑
        for (String range : ranges) {
            if (isIpInRange(ipNumber, range)) {
                return true;
            }
        }
        return false;
    }
}
```

### 2. 地理位置服务

```java
public class GeoLocationService {
    public String getLocationByIp(String clientIp) {
        Long ipNumber = Keel.netHelper().convertIPv4ToNumber(clientIp);
        if (ipNumber == null) {
            return "未知位置";
        }
        
        // 根据 IP 数字范围查询地理位置数据库
        return queryLocationDatabase(ipNumber);
    }
    
    private String queryLocationDatabase(Long ipNumber) {
        // 实现地理位置查询逻辑
        if (ipNumber >= 3232235520L && ipNumber <= 3232235775L) {
            return "局域网 192.168.1.0/24";
        }
        return "公网地址";
    }
}
```

### 3. 请求日志记录

```java
public class RequestLogger {
    public void logRequest(RoutingContext ctx) {
        List<String> ipChain = Keel.netHelper().parseWebClientIPChain(ctx);
        String realIp = ipChain.isEmpty() ? "未知" : ipChain.get(0);
        String userAgent = ctx.request().getHeader("User-Agent");
        String method = ctx.request().method().name();
        String path = ctx.request().path();
        
        // 记录详细的请求信息
        String logEntry = String.format(
            "[%s] %s %s - IP: %s (链: %s) - UA: %s",
            Keel.dateTimeHelper().getCurrentDatetime(),
            method,
            path,
            realIp,
            String.join(" -> ", ipChain),
            userAgent
        );
        
        System.out.println(logEntry);
    }
}
```

### 4. 负载均衡健康检查

```java
public class HealthChecker {
    public void checkNetworkConnectivity() {
        String localIp = Keel.netHelper().getLocalHostAddress();
        String hostName = Keel.netHelper().getLocalHostName();
        
        if (localIp != null && hostName != null) {
            System.out.println("网络连接正常");
            System.out.println("服务器信息: " + hostName + " (" + localIp + ")");
        } else {
            System.err.println("网络连接异常，无法获取主机信息");
        }
    }
}
```

## 性能优化建议

### 1. IP 转换缓存

```java
public class CachedNetHelper {
    private final Map<String, Long> ipToNumberCache = new ConcurrentHashMap<>();
    private final Map<Long, String> numberToIpCache = new ConcurrentHashMap<>();
    
    public Long convertIPv4ToNumberCached(String ipv4) {
        return ipToNumberCache.computeIfAbsent(ipv4, 
            ip -> Keel.netHelper().convertIPv4ToNumber(ip));
    }
    
    public String convertNumberToIPv4Cached(Long number) {
        return numberToIpCache.computeIfAbsent(number, 
            num -> Keel.netHelper().convertNumberToIPv4(num));
    }
}
```

### 2. 批量 IP 处理

```java
public class BatchIpProcessor {
    public Map<String, Long> convertMultipleIPs(List<String> ipList) {
        return ipList.parallelStream()
            .collect(Collectors.toMap(
                ip -> ip,
                ip -> Keel.netHelper().convertIPv4ToNumber(ip),
                (existing, replacement) -> existing
            ));
    }
}
```

## 安全注意事项

1. **IP 伪造防护**: X-Forwarded-For 头可能被伪造，在安全敏感场景中需要验证
2. **私有 IP 过滤**: 在处理客户端 IP 时，注意过滤私有 IP 地址
3. **输入验证**: 对 IP 地址字符串进行格式验证，防止注入攻击
4. **日志脱敏**: 记录 IP 地址时考虑隐私保护需求

## 版本历史

- **2.8**: 初始版本，提供基础的 IP 转换功能
- **2.9.1**: 新增本地主机信息获取方法
- **2.9.2**: 新增 Web 客户端 IP 链解析功能

## 相关工具类

- `KeelStringHelper`: 字符串处理和验证
- `KeelJsonHelper`: JSON 格式的网络数据处理
- `KeelDateTimeHelper`: 网络请求时间戳处理 