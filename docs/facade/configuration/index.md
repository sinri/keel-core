# Configuration

The Keel Configuration system provides a hierarchical configuration management solution through the `KeelConfigElement` class. This system allows for flexible configuration structures where each element can have a name, value, and child elements, supporting various data types and navigation through keychains.

## Overview

`KeelConfigElement` is the core class of Keel's configuration system, designed to:

- Represent hierarchical configuration structures
- Support multiple data types (String, Integer, Long, Float, Double, Boolean)
- Provide keychain-based navigation through configuration trees
- Load configuration from various sources (Properties files, JSON objects, Vert.x Config)
- Offer type-safe value retrieval with default value support

## Key Features

### Hierarchical Structure
Each configuration element can contain child elements, creating a tree-like structure that mirrors complex configuration needs.

### Type Safety
Built-in methods for retrieving values as specific types with automatic parsing and error handling.

### Default Values
All read methods support default values to ensure robust configuration handling.

### Multiple Data Sources
Support for loading configuration from:
- Properties files
- JSON objects
- Vert.x Config system
- Direct property objects

## Basic Usage

### Creating Configuration Elements

```java
// Create a root configuration element
KeelConfigElement config = new KeelConfigElement("root");

// Add child elements
config.ensureChild("database")
      .setValue("localhost:5432");

config.ensureChild("server")
      .ensureChild("port")
      .setValue("8080");
```

### Reading Values

```java
// Read string values
String dbHost = config.readString(List.of("database")); // "localhost:5432"
String serverPort = config.readString("server.port"); // Using dot notation

// Read with default values
int port = config.readInteger(List.of("server", "port"), 3000);
boolean debug = config.readBoolean("debug", false);
```

### Loading from Properties Files

```java
KeelConfigElement config = new KeelConfigElement("config");
config.loadPropertiesFile("application.properties");

// Properties file content:
// server.host=localhost
// server.port=8080
// database.url=jdbc:mysql://localhost:3306/mydb

String host = config.readString(List.of("server", "host"));
int port = config.readInteger(List.of("server", "port"), 8080);
```

### Loading from JSON

```java
JsonObject jsonConfig = new JsonObject()
    .put("name", "app")
    .put("children", new JsonArray()
        .add(new JsonObject()
            .put("name", "server")
            .put("children", new JsonArray()
                .add(new JsonObject()
                    .put("name", "port")
                    .put("value", "8080")))));

KeelConfigElement config = KeelConfigElement.fromJsonObject(jsonConfig);
```

## Data Type Support

### String Values
```java
String value = config.readString(keychain);
String valueWithDefault = config.readString(keychain, "default");
```

### Integer Values
```java
Integer value = config.readInteger(keychain);
int valueWithDefault = config.readInteger(keychain, 0);
```

### Long Values
```java
Long value = config.readLong(keychain);
long valueWithDefault = config.readLong(keychain, 0L);
```

### Float Values
```java
Float value = config.readFloat(keychain);
float valueWithDefault = config.readFloat(keychain, 0.0f);
```

### Double Values
```java
Double value = config.readDouble(keychain);
double valueWithDefault = config.readDouble(keychain, 0.0);
```

### Boolean Values
```java
Boolean value = config.readBoolean(keychain);
boolean valueWithDefault = config.readBoolean(keychain, false);
```

Boolean values support multiple true representations:
- "YES" (case-insensitive)
- "TRUE" (case-insensitive)
- "ON" (case-insensitive)
- "1"

## Advanced Features

### Keychain Navigation

Keychains allow navigation through the configuration hierarchy:

```java
// Using List<String>
List<String> keychain = List.of("server", "database", "connection", "timeout");
int timeout = config.readInteger(keychain, 30);

// Using varargs
KeelConfigElement element = config.extract("server", "database", "connection");
```

### Vert.x Config Integration

```java
ConfigRetrieverOptions options = new ConfigRetrieverOptions()
    .addStore(new ConfigStoreOptions()
        .setType("file")
        .setConfig(new JsonObject().put("path", "config.json")));

Future<KeelConfigElement> configFuture = KeelConfigElement.retrieve(options);
configFuture.onSuccess(config -> {
    // Use the loaded configuration
    String value = config.readString("some.key");
});
```

### Dynamic Configuration Management

```java
// Add children dynamically
config.ensureChild("newSection")
      .setValue("newValue");

// Remove children
config.removeChild("oldSection");

// Copy configuration
KeelConfigElement copy = new KeelConfigElement(originalConfig);
```

## JSON Serialization

Convert configuration to JSON for storage or transmission:

```java
JsonObject json = config.toJsonObject();
// Later restore from JSON
KeelConfigElement restored = KeelConfigElement.fromJsonObject(json);
```

## Error Handling

The configuration system is designed to be robust:

- **Null Safety**: All methods handle null values gracefully
- **Type Conversion**: Automatic type conversion with fallback to default values
- **Missing Keys**: Returns null or default values for non-existent keys
- **Parse Errors**: NumberFormatException handling for numeric conversions

## Best Practices

1. **Use Default Values**: Always provide sensible defaults for configuration values
2. **Hierarchical Organization**: Structure configuration logically using child elements
3. **Type Safety**: Use appropriate read methods for expected data types
4. **Resource Management**: Close ConfigRetriever instances when using Vert.x integration
5. **Validation**: Validate critical configuration values after loading

## Thread Safety

`KeelConfigElement` uses `ConcurrentHashMap` for storing children, making it thread-safe for concurrent read operations. However, structural modifications (adding/removing children) should be synchronized if performed concurrently.

## Integration with Keel Framework

The configuration system integrates seamlessly with the Keel framework through the `KeelInstance.Keel` reference, providing access to the Vert.x instance for advanced configuration retrieval scenarios.

