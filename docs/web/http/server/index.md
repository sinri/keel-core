# Keel HTTP Server Architecture Documentation

## Overview

Keel HTTP Server provides a comprehensive, annotation-driven web framework built on top of Vert.x. The architecture consists of three main components:

1. **KeelHttpServer** - Base HTTP server implementation
2. **Receptionist** - Request handling and routing system
3. **PreHandler** - Request preprocessing pipeline

## KeelHttpServer

`KeelHttpServer` is an abstract base class that extends `KeelVerticleImpl` and provides the foundation for HTTP server functionality.

### Key Features

- **Configuration-driven setup** with flexible port and options configuration
- **Integrated logging** with dedicated issue recording
- **Graceful shutdown** handling with main service detection
- **Exception handling** at the server level

### Configuration Options

```java
// Configuration keys
CONFIG_HTTP_SERVER_PORT = "http_server_port"        // Default: 8080
CONFIG_HTTP_SERVER_OPTIONS = "http_server_options"  // Custom HttpServerOptions
CONFIG_IS_MAIN_SERVICE = "is_main_service"          // Default: true
```

### Implementation Pattern

```java
public class MyHttpServer extends KeelHttpServer {
    @Override
    protected void configureRoutes(Router router) {
        // Load receptionist classes
        KeelWebReceptionistLoader.loadPackage(router, "com.example.api", KeelWebReceptionist.class);
        
        // Add custom routes
        router.route("/health").handler(ctx -> ctx.response().end("OK"));
    }
}
```

### Lifecycle Management

- **Startup**: Creates HTTP server, configures routes, starts listening
- **Shutdown**: Gracefully closes server connections and triggers system shutdown if main service
- **Error Handling**: Logs exceptions and fails gracefully with configurable behavior

## Receptionist System

The Receptionist system provides annotation-driven request handling with automatic route discovery and registration.

### Core Components

#### 1. KeelWebReceptionist (Abstract Base)

The base class for all request handlers providing:

- **Automatic logging** with request/response tracking
- **Request context management** with user and authentication info
- **Cookie handling** utilities
- **Response management** through integrated responder

```java
public abstract class MyReceptionist extends KeelWebReceptionist {
    public MyReceptionist(RoutingContext routingContext) {
        super(routingContext);
    }
    
    @Override
    protected KeelIssueRecordCenter issueRecordCenter() {
        return KeelIssueRecordCenter.outputCenter();
    }
    
    @Override
    public void handle() {
        // Request handling logic
        respondOnSuccess(result);
    }
}
```

#### 2. KeelWebFutureReceptionist (Async Handler)

Extends the base receptionist with `Future`-based async handling:

```java
@ApiMeta(routePath = "/api/async-endpoint", allowMethods = {"POST"})
public class AsyncReceptionist extends KeelWebFutureReceptionist {
    public AsyncReceptionist(RoutingContext routingContext) {
        super(routingContext);
    }
    
    @Override
    protected Future<Object> handleForFuture() {
        return Future.succeededFuture()
            .compose(v -> someAsyncOperation())
            .map(result -> processResult(result));
    }
}
```

### ApiMeta Annotation

Defines API endpoint metadata for automatic route registration:

```java
@ApiMeta(
    routePath = "/api/users/{id}",
    allowMethods = {"GET", "PUT", "DELETE"},
    timeout = 30000,
    requestBodyNeeded = false,
    virtualHost = "api.example.com",
    isDeprecated = false,
    remark = "User management endpoint"
)
```

**Parameters:**
- `routePath`: URL pattern (supports Vert.x path parameters)
- `allowMethods`: Allowed HTTP methods (default: POST)
- `timeout`: Request timeout in milliseconds (default: 10s, 0 = no timeout)
- `requestBodyNeeded`: Whether body parsing is required (default: true)
- `virtualHost`: Virtual host restriction (optional)
- `isDeprecated`: Marks endpoint as deprecated
- `remark`: Documentation string

### Request Body Handling

`AbstractRequestBody` provides automatic content-type detection and parsing:

```java
public class UserRequestBody extends AbstractRequestBody {
    private String name;
    private String email;
    
    public UserRequestBody(RoutingContext routingContext) {
        super(routingContext);
    }
    
    // Getters and setters...
}

// Usage in receptionist
UserRequestBody requestBody = new UserRequestBody(getRoutingContext());
```

**Supported Content Types:**
- `application/json` - Parsed as JSON object
- `application/x-www-form-urlencoded` - Parsed as form data
- `multipart/form-data` - Handled with multipart support

### Response Handling

The `KeelWebResponder` interface manages response generation:

```java
// Success response
getResponder().respondOnSuccess(data);

// Error response
getResponder().respondOnFailure(new Exception("Processing failed"));

// Custom response handling
getResponder().respondOnFailure(throwable, new ValueBox<>(additionalData));
```

### Automatic Route Loading

`KeelWebReceptionistLoader` provides package-based route discovery:

```java
// Load all receptionist classes from package
KeelWebReceptionistLoader.loadPackage(router, "com.example.api", KeelWebReceptionist.class);

// Load specific class
KeelWebReceptionistLoader.loadClass(router, MyReceptionist.class);
```

**Loading Process:**
1. Scans package for `KeelWebReceptionist` subclasses
2. Reads `@ApiMeta` annotations
3. Creates routes with appropriate HTTP methods
4. Configures prehandler chains
5. Registers request handlers

## PreHandler System

The PreHandler system provides a configurable request preprocessing pipeline that executes before reaching the receptionist.

### PreHandlerChain Architecture

The `PreHandlerChain` class defines handler execution order following Vert.x Web standards:

```java
public class CustomPreHandlerChain extends PreHandlerChain {
    public CustomPreHandlerChain() {
        // Add authentication
        authenticationHandlers.add(new JWTAuthenticationHandler());
        
        // Add authorization
        authorizationHandlers.add(new RoleBasedAuthorizationHandler());
        
        // Add custom handlers
        userHandlers.add(new CustomValidationHandler());
    }
}
```

### Handler Types and Execution Order

1. **Platform Handlers** - Basic request setup
   - `KeelPlatformHandler` (automatic)
   - `TimeoutHandler` (if timeout configured)
   - `ResponseTimeHandler` (automatic)

2. **Security Policy Handlers** - CORS, CSP, etc.

3. **Protocol Upgrade Handlers** - WebSocket upgrades

4. **Body Handlers** - Request body parsing (if enabled)

5. **Multi-Tenant Handlers** - Tenant isolation

6. **Authentication Handlers** - User identification

7. **Input Trust Handlers** - Input validation

8. **Authorization Handlers** - Permission checking

9. **User Handlers** - Custom preprocessing

### KeelPlatformHandler

Automatically added to all routes, provides:

```java
// Request ID generation
routingContext.put(KEEL_REQUEST_ID, generatedId);

// Request timing
routingContext.put(KEEL_REQUEST_START_TIME, System.currentTimeMillis());
```

### KeelAuthenticationHandler

Abstract base for custom authentication implementations:

```java
public class JWTAuthenticationHandler extends KeelAuthenticationHandler {
    @Override
    protected Future<AuthenticateResult> handleRequest(RoutingContext routingContext) {
        String token = extractToken(routingContext);
        
        return validateToken(token)
            .map(userInfo -> AuthenticateResult.createAuthenticatedResult(userInfo))
            .recover(throwable -> Future.succeededFuture(
                AuthenticateResult.createAuthenticateFailedResult(throwable)
            ));
    }
}
```

**AuthenticateResult Options:**
- `createAuthenticatedResult()` - Success with no user data
- `createAuthenticatedResult(JsonObject)` - Success with user principle
- `createAuthenticateFailedResult(Throwable)` - Authentication failed
- `createAuthenticateFailedResult(int, Throwable)` - Failed with custom status code

### PreHandlerChainMeta Annotation

Links custom prehandler chains to receptionist classes:

```java
@PreHandlerChainMeta(CustomPreHandlerChain.class)
@ApiMeta(routePath = "/api/secure-endpoint")
public class SecureReceptionist extends KeelWebReceptionist {
    // Implementation...
}
```

## Integration Example

Complete example showing all components working together:

```java
// 1. HTTP Server
public class ApiServer extends KeelHttpServer {
    @Override
    protected void configureRoutes(Router router) {
        KeelWebReceptionistLoader.loadPackage(router, "com.example.api", KeelWebReceptionist.class);
    }
}

// 2. Custom PreHandler Chain
public class ApiPreHandlerChain extends PreHandlerChain {
    public ApiPreHandlerChain() {
        authenticationHandlers.add(new ApiKeyAuthenticationHandler());
        authorizationHandlers.add(new RateLimitingHandler());
    }
}

// 3. Request Body
public class CreateUserRequest extends AbstractRequestBody {
    private String username;
    private String email;
    
    public CreateUserRequest(RoutingContext routingContext) {
        super(routingContext);
    }
    // Getters and setters...
}

// 4. Receptionist
@PreHandlerChainMeta(ApiPreHandlerChain.class)
@ApiMeta(
    routePath = "/api/users",
    allowMethods = {"POST"},
    timeout = 15000
)
public class CreateUserReceptionist extends KeelWebFutureReceptionist {
    public CreateUserReceptionist(RoutingContext routingContext) {
        super(routingContext);
    }
    
    @Override
    protected KeelIssueRecordCenter issueRecordCenter() {
        return KeelIssueRecordCenter.outputCenter();
    }
    
    @Override
    protected Future<Object> handleForFuture() {
        CreateUserRequest request = new CreateUserRequest(getRoutingContext());
        
        return validateRequest(request)
            .compose(v -> createUser(request))
            .map(user -> new JsonObject()
                .put("success", true)
                .put("userId", user.getId())
            );
    }
    
    private Future<Void> validateRequest(CreateUserRequest request) {
        if (request.getUsername() == null || request.getEmail() == null) {
            return Future.failedFuture(new IllegalArgumentException("Missing required fields"));
        }
        return Future.succeededFuture();
    }
    
    private Future<User> createUser(CreateUserRequest request) {
        // User creation logic
        return userService.create(request.getUsername(), request.getEmail());
    }
}
```

## Request Lifecycle

1. **Request Arrival** → HTTP Server receives request
2. **Platform Setup** → `KeelPlatformHandler` adds request ID and timing
3. **PreHandler Chain** → Sequential execution of configured handlers
4. **Route Matching** → Vert.x routes to appropriate receptionist
5. **Receptionist Creation** → New instance created with routing context
6. **Request Handling** → Business logic execution
7. **Response Generation** → Success/failure response sent
8. **Logging** → Request/response details logged automatically

## Logging and Monitoring

The framework provides comprehensive logging through `ReceptionistIssueRecord`:

```java
// Automatic request logging
r.setRequest(method, path, receptionistClass, query, body)

// Response logging
r.setResponse(responseBody)

// Response status tracking
r.setRespondInfo(statusCode, statusMessage, ended, closed)
```

**Request Tracking Features:**
- Unique request ID generation
- Request timing measurement
- Client IP chain parsing
- User session tracking
- Error cause recording

## Best Practices

1. **Receptionist Design**
   - Keep handlers focused on single responsibilities
   - Use `KeelWebFutureReceptionist` for async operations
   - Implement proper error handling and validation

2. **PreHandler Usage**
   - Design reusable prehandler chains for common patterns
   - Keep authentication logic separate from business logic
   - Use appropriate handler types for specific concerns

3. **Configuration**
   - Use configuration files for environment-specific settings
   - Implement proper timeout values based on operation complexity
   - Configure appropriate logging levels for different environments

4. **Performance**
   - Minimize prehandler chain complexity
   - Use appropriate timeout values
   - Implement proper resource cleanup in error scenarios

This architecture provides a robust, scalable foundation for building HTTP APIs with comprehensive request processing, authentication, and monitoring capabilities.
