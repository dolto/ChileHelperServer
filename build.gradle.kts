plugins {
    kotlin("jvm") version "1.9.20"
    application
}

group = "kr.ac.wku.chile"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

allprojects {
    repositories {
        mavenCentral()
        maven("https://jitpack.io" )
    }
}

dependencies {
    testImplementation(kotlin("test"))
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation ("com.machinezoo.sourceafis:sourceafis:3.18.0")
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation ("com.github.ZenLiuCN:lz-string4k:1.0.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("MainKt")
}