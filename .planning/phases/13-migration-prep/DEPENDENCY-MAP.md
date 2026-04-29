# Maven to Gradle Dependency Mapping

**Source:** springboot/pom.xml
**Target:** springboot/build.gradle.kts + springboot/libs.versions.toml

## Dependency Mapping Table

| Maven | Gradle (libs.versions.toml) | 状态 |
|-------|---------------------------|------|
| org.springframework.boot:spring-boot-starter-webmvc | spring-boot-starter-webmvc | ✓ |
| org.springframework.boot:spring-boot-starter-security | spring-boot-starter-security | ✓ |
| org.springframework.boot:spring-boot-starter-aop:3.5.13 | spring-boot-starter-aop (version: 3.5.13) | ✓ |
| com.mysql:mysql-connector-j:9.6.0 | mysql-connector-j (version: 9.6.0) | ✓ |
| com.baomidou:mybatis-plus-spring-boot4-starter:3.5.16 | mybatis-plus-spring-boot4-starter (version: 3.5.16) | ✓ |
| org.springframework.boot:spring-boot-starter-flyway:4.0.6 | spring-boot-starter-flyway | ✓ |
| org.flywaydb:flyway-core | flyway-core | ✓ |
| org.flywaydb:flyway-mysql | flyway-mysql | ✓ |
| org.springframework.boot:spring-boot-starter-data-redis | spring-boot-starter-data-redis | ✓ |
| org.apache.rocketmq:rocketmq-client:5.3.2 | rocketmq-client (version: 5.3.2) | ✓ |
| org.apache.rocketmq:rocketmq-spring-boot-starter:2.3.5 | rocketmq-spring-boot-starter (version: 2.3.5) | ✓ |
| org.apache.rocketmq:rocketmq-tools:5.3.2 | rocketmq-tools (version: 5.3.2) | ✓ |
| org.redisson:redisson-spring-boot-starter:4.3.1 | redisson-spring-boot-starter (version: 4.3.1) | ✓ |
| org.springframework:spring-messaging | spring-messaging | ✓ |
| io.jsonwebtoken:jjwt-api:0.13.0 | jjwt-api (version: 0.13.0) | ✓ |
| io.jsonwebtoken:jjwt-impl:0.13.0 (runtime) | jjwt-impl (runtimeOnly) | ✓ |
| io.jsonwebtoken:jjwt-jackson:0.13.0 (runtime) | jjwt-jackson (runtimeOnly) | ✓ |
| com.github.luben:zstd-jni:1.5.7-7 (runtime) | zstd-jni (runtimeOnly) | ✓ |
| org.projectlombok:lombok:1.18.44 | lombok (annotationProcessor) | ✓ |
| org.springframework.boot:spring-boot-starter-test (test) | spring-boot-starter-test (testImplementation) | ✓ |
| org.springframework.security:spring-security-test (test) | spring-security-test (testImplementation) | ✓ |

## Maven Plugin to Gradle Plugin Mapping

| Maven Plugin | Gradle Plugin | 状态 |
|--------------|---------------|------|
| spring-boot-maven-plugin | org.springframework.boot plugin | ✓ |
| maven-compiler-plugin (JDK 26, --enable-preview) | java toolchain + KotlinCompile args | ✓ |
| flyway-maven-plugin | org.flywaydb.flyway plugin | ✓ |

## Configuration Mapping

### Maven Properties → Version Catalog

| Maven | Gradle |
|-------|--------|
| java.version=26 | java=26 |
| mybatis-plus.version=3.5.16 | mybatis-plus=3.5.16 |
| mysql.version=9.6.0 | mysql=9.6.0 |
| jjwt.version=0.13.0 | jjwt=0.13.0 |
| zstd.version=1.5.7-7 | zstd=1.5.7-7 |
| rocketmq.version=2.3.5 | rocketmq-spring-boot-starter=2.3.5 |
| redisson.version=4.3.1 | redisson=4.3.1 |

### Maven Compiler Config → Gradle

| Maven | Gradle |
|-------|--------|
| jdkToolchain.version=26 | java.toolchain.languageVersion.set(JavaLanguageVersion.of(26)) |
| annotationProcessorPaths: lombok | annotationProcessor(libs.lombok) |
| annotationProcessorPaths: spring-boot-configuration-processor | annotationProcessor(libs.springBootConfigurationProcessor) |
| compilerArgs: --enable-preview | tasks.withType<KotlinCompile> { compilerArgs.add("--enable-preview") } |

### Maven Spring Boot Plugin Config → Gradle

| Maven | Gradle |
|-------|--------|
| excludes lombok from boot.jar | bootJar { excludes += ... } |

## Notes

1. **Version Catalog**: 使用 libs.versions.toml 集中管理版本
2. **Flyway**: 从 flyway-maven-plugin 迁移到 org.flywaydb.flyway Gradle 插件
3. **Credentials**: Maven 配置中的硬编码密码 (root/123456) 已在 Gradle 中移除，改用环境变量 DB_PASSWORD
4. **--enable-preview**: 同时在 gradle.properties (org.gradle.jvmargs) 和 KotlinCompile 任务中配置