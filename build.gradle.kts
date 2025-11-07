plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.asciidoctor)
}

group = "com.pocopi"
version = "1.0.0"
description = "pocopi-api"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

extra["snippetsDir"] = file("build/generated-snippets")

dependencies {
    // spring boot

    implementation(libs.spring.boot.starter.data.jdbc)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.web)

    developmentOnly(libs.spring.boot.devtools)
    developmentOnly(libs.spring.boot.docker.compose)
    developmentOnly(libs.spring.boot.starter.actuator)

    annotationProcessor(libs.spring.boot.configuration.processor)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.restdocs.mockmvc)
    testImplementation(libs.spring.security.test)

    // db

    implementation(libs.flyway.core)
    implementation(libs.flyway.mysql)

    runtimeOnly(libs.mysql.connector)

    // others

    compileOnly(libs.jjwt.api)
    compileOnly(libs.lombok)

    annotationProcessor(libs.lombok)

    implementation(libs.dotenv)
    implementation(libs.json.schema.validator)
    implementation(libs.openApi)
    implementation(libs.tika.core)

    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)

    // testing

    testRuntimeOnly(libs.junit.launcher)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.test {
    outputs.dir(project.extra["snippetsDir"]!!)
}

tasks.asciidoctor {
    inputs.dir(project.extra["snippetsDir"]!!)
    dependsOn(tasks.test)
}
