# Phase 13: 迁移准备 - Research

**Phase:** 13-migration-prep
**Date:** 2026-04-29
**Status:** Complete

<research>

## Maven to Gradle Migration Pattern

### Key Translation Points from pom.xml

**Parent POM → Gradle**
```kotlin
// pom.xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.5</version>
</parent>

// build.gradle.kts equivalent
plugins {
    id("org.springframework.boot") version "4.1.0-RC1" apply false
    id("io.spring.dependency-management") version "1.1.7"
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:4.1.0-RC1")
    }
}
```

**Properties → Version Catalog (libs.versions.toml)**
```toml
[versions]
java = "26"
spring-boot = "4.1.0-RC1"
mybatis-plus = "3.5.16"
mysql = "9.6.0"
flyway = "12.4.0"
rocketmq = "5.3.2"
redisson = "4.3.1"
jjwt = "0.13.0"
zstd = "1.5.7-7"
lombok = "1.18.44"
```

**Dependencies Translation**
| Maven | Gradle Kotlin DSL |
|-------|-------------------|
| `<dependency>` with `groupId/artifactId` | `implementation(libs.springBootStarter)` |
| `<version>${property}</version>` | `implementation(libs.mybatisPlus)` |
| `scope: runtime` | `runtimeOnly(libs.zstd)` |
| `annotationProcessorPaths` | `annotationProcessor(libs.lombok)` |

### Gradle 9.4 Kotlin DSL Patterns

**Java Toolchain Configuration**
```kotlin
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(26))
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("--enable-preview")
}
```

**Annotation Processor (Lombok + Config Processor)**
```kotlin
dependencies {
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.springBootConfigurationProcessor)
    // Or in a block:
    annotationProcessorPath = files(libs.lombok.get().files, libs.springBootConfigurationProcessor.get().files)
}
```

**Spring Boot Plugin Configuration**
```kotlin
plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

springBoot {
    buildInfo()
}

tasks.bootJar {
    archiveClassifier.set("")
    excludes += listOf("**/META-INF/*.SF", "**/META-INF/*.DSA", "**/META-INF/*.RSA")
}
```

### Version Catalog Structure

```toml
[versions]
# Gradle
gradle = "9.4"

# Spring Boot
spring-boot = "4.1.0-RC1"
spring-aop = "3.5.13"

# Database
mysql = "9.6.0"
flyway = "12.4.0"

# Messaging
rocketmq-client = "5.3.2"
rocketmq-spring-boot-starter = "2.3.5"

# Security & Auth
jjwt = "0.13.0"

# Utils
zstd = "1.5.7-7"
redisson = "4.3.1"
mybatis-plus = "3.5.16"

[libraries]
# Spring Boot Starters
spring-boot-starter-webmvc = { group = "org.springframework.boot", name = "spring-boot-starter-webmvc" }
spring-boot-starter-security = { group = "org.springframework.boot", name = "spring-boot-starter-security" }
spring-boot-starter-aop = { group = "org.springframework.boot", name = "spring-boot-starter-aop" }
spring-boot-starter-data-redis = { group = "org.springframework.boot", name = "spring-boot-starter-data-redis" }
spring-boot-starter-flyway = { group = "org.springframework.boot", name = "spring-boot-starter-flyway", version.ref = "flyway" }
spring-boot-configuration-processor = { group = "org.springframework.boot", name = "spring-boot-configuration-processor" }

# Database
mysql-connector-j = { group = "com.mysql", name = "mysql-connector-j", version.ref = "mysql" }
mybatis-plus-spring-boot4-starter = { group = "com.baomidou", name = "mybatis-plus-spring-boot4-starter", version.ref = "mybatis-plus" }
flyway-core = { group = "org.flywaydb", name = "flyway-core", version.ref = "flyway" }
flyway-mysql = { group = "org.flywaydb", name = "flyway-mysql", version.ref = "flyway" }

# Messaging
rocketmq-client = { group = "org.apache.rocketmq", name = "rocketmq-client", version.ref = "rocketmq-client" }
rocketmq-spring-boot-starter = { group = "org.apache.rocketmq", name = "rocketmq-spring-boot-starter", version.ref = "rocketmq-spring-boot-starter" }
rocketmq-tools = { group = "org.apache.rocketmq", name = "rocketmq-tools", version.ref = "rocketmq-client" }

# Security
redisson-spring-boot-starter = { group = "org.redisson", name = "redisson-spring-boot-starter", version.ref = "redisson" }
jjwt-api = { group = "io.jsonwebtoken", name = "jjwt-api", version.ref = "jjwt" }
jjwt-impl = { group = "io.jsonwebtoken", name = "jjwt-impl", version.ref = "jjwt" }
jjwt-jackson = { group = "io.jsonwebtoken", name = "jjwt-jackson", version.ref = "jjwt" }

# Utils
zstd-jni = { group = "com.github.luben", name = "zstd-jni", version.ref = "zstd" }
lombok = { group = "org.projectlombok", name = "lombok", version.ref = "lombok" }

[plugins]
spring-boot = { id = "org.springframework.boot", version.ref = "spring-boot" }
dependency-management = { id = "io.spring.dependency-management", version = "1.1.7" }
```

### Flyway Configuration in Gradle

```kotlin
plugins {
    id("org.flywaydb.flyway") version "12.4.0"
}

flyway {
    url = "jdbc:mysql://localhost:3306/admin_system?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false"
    user = System.getenv("DB_USER") ?: "root"
    password = System.getenv("DB_PASSWORD") ?: System.getenv("DB_PASSWORD") ?: ""
    driver = "com.mysql.cj.jdbc.Driver"
    locations = listOf("classpath:db/migration")
    baselineOnMigrate = true
}
```

**Note:** Hardcoded credentials in pom.xml flyway-maven-plugin must be replaced with environment variables.

### Spring Boot 4.1 Migration Considerations

1. **Preview Features**: `--enable-preview` compiler flag needs explicit configuration
2. **Configuration Processor**: Spring Boot 4.1 uses `spring-boot-configuration-processor` for @ConfigurationProperties
3. **AOP Version**: Spring AOP 3.5.13 is manually specified - verify compatibility with Spring Boot 4.1

### Gradle Wrapper Generation

```powershell
# Install Gradle 9.4 first (via sdkman or direct download)
# Then generate wrapper
cd springboot
gradle wrapper --gradle-version 9.4

# Or if Gradle is not installed, download wrapper JAR directly
# See: https://gradle.org/install/
```

### File Structure to Create

```
springboot/
├── build.gradle.kts          # Root build script
├── settings.gradle.kts        # Project settings
├── gradle.properties          # Gradle properties (org.gradle.jvmargs=--enable-preview)
├── libs.versions.toml         # Version catalog
├── gradlew                   # Unix wrapper script
├── gradlew.bat               # Windows wrapper script
└── gradle/wrapper/           # Wrapper files
    ├── gradle-wrapper.jar
    └── gradle-wrapper.properties
```

### Common Pitfalls

1. **Kotlin DSL escaping**: Strings with dots need escaping (e.g., `group = "org.flywaydb"`)
2. **Version catalog BOM**: Use `implementation(platform(libs.springBoot))` for BOM
3. **Annotation processor order**: Lombok must come before config processor
4. **--enable-preview**: Requires both compiler args AND JVM args

</research>

---

## Validation Architecture

| Dimension | Description |
|-----------|-------------|
| D1 | Gradle Wrapper 9.4 generated correctly |
| D2 | build.gradle.kts parses without errors |
| D3 | libs.versions.toml has all dependencies |
| D4 | Java toolchain 26 configured |
| D5 | annotationProcessor configured |
| D6 | Flyway plugin configured |

---

*Research completed: 2026-04-29*
*Agent: gsd-phase-researcher (fallback: direct write due to agent unavailability)*