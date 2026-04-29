plugins {
    `java-library`
    id("org.springframework.boot") version "4.1.0-RC1"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.flywaydb.flyway") version "12.4.0"
}

val javaVersion = JavaLanguageVersion.of(26)

java {
    toolchain {
        languageVersion.set(javaVersion)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework:spring-messaging")

    // Database
    implementation("com.mysql:mysql-connector-j:9.7.0")
    implementation("com.baomidou:mybatis-plus-spring-boot4-starter:3.5.16")
    implementation("org.flywaydb:flyway-core:12.4.0")
    implementation("org.flywaydb:flyway-mysql:12.4.0")

    // Messaging
    implementation("org.apache.rocketmq:rocketmq-client:5.3.2")
    implementation("org.apache.rocketmq:rocketmq-spring-boot-starter:2.3.5")
    implementation("org.apache.rocketmq:rocketmq-tools:5.3.2")

    // Security & Cache
    implementation("org.redisson:redisson-spring-boot-starter:4.3.1")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")

    // Utils
    runtimeOnly("com.github.luben:zstd-jni:1.5.7-7")

    // Annotation Processors
    compileOnly("org.projectlombok:lombok:1.18.44")
    annotationProcessor("org.projectlombok:lombok:1.18.44")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("--enable-preview")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

springBoot {
    buildInfo()
}

flyway {
    url = "jdbc:mysql://localhost:3306/admin_system?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false"
    user = System.getenv("DB_USER") ?: "root"
    password = System.getenv("DB_PASSWORD") ?: ""
    driver = "com.mysql.cj.jdbc.Driver"
    locations = arrayOf("classpath:db/migration")
    baselineOnMigrate = true
}