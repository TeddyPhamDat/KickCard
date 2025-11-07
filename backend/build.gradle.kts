plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    java // Use Java plugin
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    // Keep compatibility fields for IDEs and other tooling
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.microsoft.sqlserver:mssql-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.cloudinary:cloudinary-http44:1.34.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    // PayOS integration via REST API (removed problematic SDK)
    implementation("org.springframework.boot:spring-boot-starter-webflux") // For WebClient
    implementation("commons-codec:commons-codec:1.15") // For HMAC signature
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.h2database:h2")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

// Ensure javac produces Java 17 bytecode even when using a newer JDK (e.g., JDK22).
// This avoids needing Gradle to auto-download a toolchain.
tasks.withType<org.gradle.api.tasks.compile.JavaCompile>().configureEach {
    options.release.set(17)
}
