buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        // buildscript 无法访问 libs.versions.toml，此处直接写版本号
        classpath("org.flywaydb:flyway-mysql:12.5.0")
    }
}

plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dm)
    alias(libs.plugins.flyway)
    checkstyle
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
    implementation(platform(libs.spring.boot.bom))
    // Spring Boot Starters
    implementation(libs.spring.boot.web)
    implementation(libs.spring.boot.aop)
    implementation(libs.spring.boot.security)
    implementation(libs.spring.boot.redis)
    implementation(libs.spring.boot.flyway)
    implementation(libs.spring.messaging)

    // Database
    implementation(libs.mysql.connector)
    implementation(libs.mybatis.plus)
    implementation(libs.flyway.core)
    implementation(libs.flyway.mysql)

    // Messaging
    implementation(libs.rocketmq.client)
    implementation(libs.rocketmq.spring)
    implementation(libs.rocketmq.tools)

    // Security & Cache
    implementation(libs.redisson)

    // JWT
    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)

    // Utils
    runtimeOnly(libs.zstd.jni)

    // Annotation Processors
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.spring.boot.config.processor)

    // Test
    testImplementation(libs.spring.boot.test)
    testImplementation(libs.spring.security.test)
    testImplementation(libs.archunit)
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("--enable-preview")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

checkstyle {
    toolVersion = libs.versions.checkstyle.get()
    isIgnoreFailures = true
}

springBoot {
    buildInfo()
}

flyway {
    url = "jdbc:mysql://localhost:3306/admin_system?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false"
    user = System.getenv("DB_USER") ?: "root"
    password = System.getenv("DB_PASSWORD") ?: "123456"
    driver = "com.mysql.cj.jdbc.Driver"
    locations = arrayOf("classpath:db/migration")
    baselineOnMigrate = true
}
