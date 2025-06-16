# KeelReflectionHelper

`KeelReflectionHelper` 是 Keel 框架中的反射工具类，提供注解处理、类扫描、动态加载、类型检查等反射相关的实用功能。

## 版本信息

- **引入版本**: 2.6
- **设计模式**: 单例模式
- **线程安全**: 是

## 主要功能

### 1. 注解处理
- 类和方法注解的获取和处理
- 支持重复注解（3.1.8+）
- 默认注解值处理

### 2. 类扫描
- 包内类的扫描和继承关系检查
- 支持文件系统和 JAR 文件中的类扫描
- 类的可分配性检查和类型验证

### 3. 动态加载
- 支持文件系统和 JAR 文件中的类动态加载
- 运行时 JAR 文件类扫描
- 类路径管理和处理

## 获取实例

```java
import static io.github.sinri.keel.facade.KeelInstance.Keel;

// 通过 Keel 实例获取
KeelReflectionHelper reflectionHelper = Keel.reflectionHelper();
```

## 注解处理功能

### 方法注解获取

```java
import java.lang.reflect.Method;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// 定义示例注解
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiEndpoint {
    String path() default "";
    String method() default "GET";
    boolean auth() default true;
}

// 示例类
public class UserController {
    @ApiEndpoint(path = "/users", method = "GET", auth = false)
    public void getUsers() {
        // 方法实现
    }
    
    @ApiEndpoint(path = "/users", method = "POST")
    public void createUser() {
        // 方法实现
    }
}

// 获取方法注解
public void processMethodAnnotations() throws NoSuchMethodException {
    Method getUsersMethod = UserController.class.getMethod("getUsers");
    Method createUserMethod = UserController.class.getMethod("createUser");
    
    // 获取注解（带默认值）
    ApiEndpoint getUsersAnnotation = Keel.reflectionHelper()
        .getAnnotationOfMethod(getUsersMethod, ApiEndpoint.class);
    
    if (getUsersAnnotation != null) {
        System.out.println("路径: " + getUsersAnnotation.path());
        System.out.println("方法: " + getUsersAnnotation.method());
        System.out.println("需要认证: " + getUsersAnnotation.auth());
    }
    
    // 获取注解（带自定义默认值）
    ApiEndpoint defaultEndpoint = new ApiEndpoint() {
        @Override
        public String path() { return "/default"; }
        @Override
        public String method() { return "GET"; }
        @Override
        public boolean auth() { return true; }
        @Override
        public Class<? extends java.lang.annotation.Annotation> annotationType() {
            return ApiEndpoint.class;
        }
    };
    
    ApiEndpoint annotation = Keel.reflectionHelper()
        .getAnnotationOfMethod(createUserMethod, ApiEndpoint.class, defaultEndpoint);
    
    System.out.println("注解信息: " + annotation.path() + " " + annotation.method());
}
```

### 类注解获取

```java
// 定义类级别注解
@Retention(RetentionPolicy.RUNTIME)
public @interface RestController {
    String value() default "";
    String prefix() default "";
}

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RequestMappings.class)
public @interface RequestMapping {
    String path();
    String method() default "GET";
}

@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMappings {
    RequestMapping[] value();
}

// 示例类
@RestController(value = "userController", prefix = "/api/v1")
@RequestMapping(path = "/users", method = "GET")
@RequestMapping(path = "/users", method = "POST")
public class AnnotatedUserController {
    // 类实现
}

// 处理类注解
public void processClassAnnotations() {
    Class<?> controllerClass = AnnotatedUserController.class;
    
    // 获取单个注解
    RestController restController = Keel.reflectionHelper()
        .getAnnotationOfClass(controllerClass, RestController.class);
    
    if (restController != null) {
        System.out.println("控制器名称: " + restController.value());
        System.out.println("路径前缀: " + restController.prefix());
    }
    
    // 获取重复注解（3.1.8+）
    RequestMapping[] mappings = Keel.reflectionHelper()
        .getAnnotationsOfClass(controllerClass, RequestMapping.class);
    
    System.out.println("请求映射数量: " + mappings.length);
    for (RequestMapping mapping : mappings) {
        System.out.println("映射: " + mapping.method() + " " + mapping.path());
    }
}
```

## 类扫描功能

### 查找子类和实现类

```java
// 定义基础接口和类
public interface Service {
    void execute();
}

public abstract class BaseService implements Service {
    protected String name;
    
    public BaseService(String name) {
        this.name = name;
    }
}

public class UserService extends BaseService {
    public UserService() {
        super("UserService");
    }
    
    @Override
    public void execute() {
        System.out.println("执行用户服务");
    }
}

public class OrderService extends BaseService {
    public OrderService() {
        super("OrderService");
    }
    
    @Override
    public void execute() {
        System.out.println("执行订单服务");
    }
}

// 扫描包中的实现类
public void scanServiceImplementations() {
    String packageName = "com.example.service";
    
    // 查找 Service 接口的所有实现类
    Set<Class<? extends Service>> serviceClasses = Keel.reflectionHelper()
        .seekClassDescendantsInPackage(packageName, Service.class);
    
    System.out.println("找到 " + serviceClasses.size() + " 个服务实现:");
    
    for (Class<? extends Service> serviceClass : serviceClasses) {
        System.out.println("服务类: " + serviceClass.getName());
        
        try {
            // 尝试实例化
            Service service = serviceClass.getDeclaredConstructor().newInstance();
            service.execute();
        } catch (Exception e) {
            System.err.println("无法实例化服务: " + serviceClass.getName());
        }
    }
    
    // 查找 BaseService 的子类
    Set<Class<? extends BaseService>> baseServiceClasses = Keel.reflectionHelper()
        .seekClassDescendantsInPackage(packageName, BaseService.class);
    
    System.out.println("找到 " + baseServiceClasses.size() + " 个 BaseService 子类");
}
```

### 类型检查

```java
public void checkClassAssignability() {
    // 检查类的可分配性
    boolean isServiceAssignable = Keel.reflectionHelper()
        .isClassAssignable(Service.class, UserService.class);
    System.out.println("UserService 是否实现 Service: " + isServiceAssignable);
    
    boolean isBaseServiceAssignable = Keel.reflectionHelper()
        .isClassAssignable(BaseService.class, UserService.class);
    System.out.println("UserService 是否继承 BaseService: " + isBaseServiceAssignable);
    
    boolean isObjectAssignable = Keel.reflectionHelper()
        .isClassAssignable(Object.class, UserService.class);
    System.out.println("UserService 是否继承 Object: " + isObjectAssignable);
    
    // 反向检查（应该返回 false）
    boolean reverseCheck = Keel.reflectionHelper()
        .isClassAssignable(UserService.class, Service.class);
    System.out.println("Service 是否实现 UserService: " + reverseCheck);
}
```

## 实际应用场景

### 1. 依赖注入容器

```java
public class SimpleIoCContainer {
    private final Map<Class<?>, Object> instances = new ConcurrentHashMap<>();
    private final Map<Class<?>, Class<?>> bindings = new ConcurrentHashMap<>();
    
    // 自动扫描并注册服务
    public void scanAndRegister(String packageName) {
        Set<Class<? extends Service>> serviceClasses = Keel.reflectionHelper()
            .seekClassDescendantsInPackage(packageName, Service.class);
        
        for (Class<? extends Service> serviceClass : serviceClasses) {
            // 检查是否为具体类（非抽象类）
            if (!Modifier.isAbstract(serviceClass.getModifiers())) {
                register(Service.class, serviceClass);
                System.out.println("注册服务: " + serviceClass.getSimpleName());
            }
        }
    }
    
    public <T> void register(Class<T> interfaceClass, Class<? extends T> implementationClass) {
        if (Keel.reflectionHelper().isClassAssignable(interfaceClass, implementationClass)) {
            bindings.put(interfaceClass, implementationClass);
        } else {
            throw new IllegalArgumentException(
                implementationClass.getName() + " 不实现 " + interfaceClass.getName()
            );
        }
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getInstance(Class<T> clazz) {
        Object instance = instances.get(clazz);
        if (instance != null) {
            return (T) instance;
        }
        
        Class<?> implementationClass = bindings.get(clazz);
        if (implementationClass == null) {
            implementationClass = clazz;
        }
        
        try {
            instance = implementationClass.getDeclaredConstructor().newInstance();
            instances.put(clazz, instance);
            return (T) instance;
        } catch (Exception e) {
            throw new RuntimeException("无法创建实例: " + implementationClass.getName(), e);
        }
    }
}
```

### 2. 注解驱动的路由系统

```java
public class AnnotationBasedRouter {
    private final Map<String, RouteHandler> routes = new HashMap<>();
    
    public void scanControllers(String packageName) {
        Set<Class<? extends Object>> controllerClasses = Keel.reflectionHelper()
            .seekClassDescendantsInPackage(packageName, Object.class);
        
        for (Class<?> controllerClass : controllerClasses) {
            RestController restController = Keel.reflectionHelper()
                .getAnnotationOfClass(controllerClass, RestController.class);
            
            if (restController != null) {
                registerController(controllerClass, restController);
            }
        }
    }
    
    private void registerController(Class<?> controllerClass, RestController restController) {
        String prefix = restController.prefix();
        
        Method[] methods = controllerClass.getDeclaredMethods();
        for (Method method : methods) {
            ApiEndpoint endpoint = Keel.reflectionHelper()
                .getAnnotationOfMethod(method, ApiEndpoint.class);
            
            if (endpoint != null) {
                String fullPath = prefix + endpoint.path();
                String httpMethod = endpoint.method();
                String routeKey = httpMethod + ":" + fullPath;
                
                routes.put(routeKey, new RouteHandler(controllerClass, method, endpoint));
                System.out.println("注册路由: " + routeKey);
            }
        }
    }
    
    public RouteHandler findRoute(String httpMethod, String path) {
        String routeKey = httpMethod + ":" + path;
        return routes.get(routeKey);
    }
    
    public static class RouteHandler {
        private final Class<?> controllerClass;
        private final Method method;
        private final ApiEndpoint endpoint;
        
        public RouteHandler(Class<?> controllerClass, Method method, ApiEndpoint endpoint) {
            this.controllerClass = controllerClass;
            this.method = method;
            this.endpoint = endpoint;
        }
        
        public void handle() throws Exception {
            Object controller = controllerClass.getDeclaredConstructor().newInstance();
            method.invoke(controller);
        }
        
        // getters...
    }
}
```

### 3. 配置类扫描器

```java
@Retention(RetentionPolicy.RUNTIME)
public @interface Configuration {
    String value() default "";
}

@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigProperty {
    String key();
    String defaultValue() default "";
}

public class ConfigurationScanner {
    public void scanConfigurations(String packageName) {
        Set<Class<? extends Object>> configClasses = Keel.reflectionHelper()
            .seekClassDescendantsInPackage(packageName, Object.class);
        
        for (Class<?> configClass : configClasses) {
            Configuration config = Keel.reflectionHelper()
                .getAnnotationOfClass(configClass, Configuration.class);
            
            if (config != null) {
                processConfigurationClass(configClass, config);
            }
        }
    }
    
    private void processConfigurationClass(Class<?> configClass, Configuration config) {
        System.out.println("处理配置类: " + configClass.getSimpleName());
        
        Field[] fields = configClass.getDeclaredFields();
        for (Field field : fields) {
            ConfigProperty property = field.getAnnotation(ConfigProperty.class);
            if (property != null) {
                System.out.println("配置属性: " + property.key() + 
                    " (默认值: " + property.defaultValue() + ")");
            }
        }
    }
}
```

### 4. 插件系统

```java
public interface Plugin {
    String getName();
    void initialize();
    void execute();
}

public class PluginManager {
    private final List<Plugin> plugins = new ArrayList<>();
    
    public void loadPlugins(String packageName) {
        Set<Class<? extends Plugin>> pluginClasses = Keel.reflectionHelper()
            .seekClassDescendantsInPackage(packageName, Plugin.class);
        
        for (Class<? extends Plugin> pluginClass : pluginClasses) {
            if (!Modifier.isAbstract(pluginClass.getModifiers()) && 
                !pluginClass.isInterface()) {
                
                try {
                    Plugin plugin = pluginClass.getDeclaredConstructor().newInstance();
                    plugins.add(plugin);
                    plugin.initialize();
                    System.out.println("加载插件: " + plugin.getName());
                } catch (Exception e) {
                    System.err.println("无法加载插件: " + pluginClass.getName());
                }
            }
        }
    }
    
    public void executeAllPlugins() {
        for (Plugin plugin : plugins) {
            try {
                plugin.execute();
            } catch (Exception e) {
                System.err.println("插件执行失败: " + plugin.getName());
            }
        }
    }
    
    public List<Plugin> getPlugins() {
        return new ArrayList<>(plugins);
    }
}
```

## 性能优化建议

### 1. 结果缓存

```java
public class CachedReflectionHelper {
    private final Map<String, Set<Class<?>>> scanCache = new ConcurrentHashMap<>();
    private final Map<String, Object> annotationCache = new ConcurrentHashMap<>();
    
    public <T> Set<Class<? extends T>> seekClassDescendantsCached(
            String packageName, Class<T> baseClass) {
        String cacheKey = packageName + ":" + baseClass.getName();
        
        @SuppressWarnings("unchecked")
        Set<Class<? extends T>> cached = (Set<Class<? extends T>>) scanCache.get(cacheKey);
        
        if (cached == null) {
            cached = Keel.reflectionHelper().seekClassDescendantsInPackage(packageName, baseClass);
            scanCache.put(cacheKey, (Set<Class<?>>) cached);
        }
        
        return cached;
    }
    
    public <T extends Annotation> T getAnnotationCached(Class<?> clazz, Class<T> annotationClass) {
        String cacheKey = clazz.getName() + ":" + annotationClass.getName();
        
        @SuppressWarnings("unchecked")
        T cached = (T) annotationCache.get(cacheKey);
        
        if (cached == null) {
            cached = Keel.reflectionHelper().getAnnotationOfClass(clazz, annotationClass);
            if (cached != null) {
                annotationCache.put(cacheKey, cached);
            }
        }
        
        return cached;
    }
}
```

### 2. 并行扫描

```java
public class ParallelClassScanner {
    public <T> Set<Class<? extends T>> parallelScan(
            List<String> packageNames, Class<T> baseClass) {
        
        return packageNames.parallelStream()
            .flatMap(packageName -> 
                Keel.reflectionHelper()
                    .seekClassDescendantsInPackage(packageName, baseClass)
                    .stream()
            )
            .collect(Collectors.toSet());
    }
}
```

## 安全注意事项

1. **类加载安全**: 扫描未知包时要注意类加载安全性
2. **权限检查**: 反射操作可能需要特定的安全权限
3. **异常处理**: 类加载失败时要妥善处理异常
4. **内存泄漏**: 缓存类引用时注意内存泄漏问题

## 版本历史

- **2.6**: 初始版本，提供基础的反射功能
- **2.8**: 新增类注解获取功能
- **3.0.6**: 新增类扫描功能
- **3.0.10**: 新增类可分配性检查
- **3.1.8**: 支持重复注解处理
- **3.2.11**: 优化类扫描性能
- **3.2.12.1**: 重写类扫描实现

## 相关工具类

- `KeelFileHelper`: 文件系统和 JAR 文件操作
- `KeelStringHelper`: 字符串处理和命名转换
- `KeelJsonHelper`: 注解信息的 JSON 序列化 