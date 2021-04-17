/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("UnstableApiUsage", "UNUSED_VARIABLE")

import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.utils.addToStdlib.safeAs
import java.util.*

plugins {
    id("net.mamoe.maven-central-publish") version "0.3.1"
    kotlin("jvm") version "1.4.21"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    `maven-publish`
}

group = "net.mamoe"
version = "1.0-dev-17"
description = "Mirai Login Solver (Selenium)"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    fun compileAndRuntime(module: Any) {
        compileOnly(module)
        testImplementation(module)
    }

    fun mxlib(module:String):String {
        return "io.github.karlatemp.mxlib:mxlib-$module:3.0-dev-16"
    }

    compileAndRuntime(kotlin("stdlib"))

    implementation(mxlib("api"))
    implementation(mxlib("common"))
    implementation(mxlib("selenium")) {
        exclude("junit", "junit")
        exclude("classworlds", "classworlds")
        exclude("io.netty", "netty-all")
    }
    testImplementation(mxlib("logger"))
    // https://mvnrepository.com/artifact/org.slf4j/slf4j-jdk14
    testImplementation("org.slf4j:slf4j-jdk14:1.7.30")
    // https://mvnrepository.com/artifact/io.netty/netty-all
    implementation("io.netty:netty-all:4.1.56.Final")

    compileAndRuntime("net.mamoe:mirai-core-api:2.6.1")
    testImplementation("net.mamoe:mirai-core:2.6.1")
    compileAndRuntime("net.mamoe:mirai-console:2.6.1")
}

//tasks.create("packageExt", JavaExec::class) {
//    main = "net.mamoe.mirai.selenium.test.PackExt"
//    classpath = sourceSets.test.get().runtimeClasspath
//}
//
//tasks.getByName("jar").dependsOn("packageExt")
//tasks.getByName("shadowJar").dependsOn("packageExt")

tasks.getByName("shadowJar", com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
    exclude("module-info.class")
}

kotlin {
    explicitApi()
}

allprojects {
    afterEvaluate {
        configureKotlinExperimentalUsages()
        configureKotlinCompilerSettings()
        configureJvmTarget()
    }
}

tasks.create("sourcesJar", Jar::class) {
    dependsOn("classes")
    classifier = "sources"
    from(sourceSets.main.get().allSource)
}

mavenCentralPublish {
    githubProject("project-mirai", "mirai-login-solver-selenium")
    licenseFromGitHubProject("Apache-2.0", "master")
    developer("Karlatemp", email = "karlatemp@vip.qq.com")
}


fun Project.propertySafe(prop: String): Any? {
    if (hasProperty(prop)) return property(prop)
    return null
}

fun Project.configureKotlinExperimentalUsages() {
    val sourceSets = kotlinSourceSets ?: return

    for (target in sourceSets) {
        target.languageSettings.progressiveMode = true
        target.languageSettings.enableLanguageFeature("InlineClasses")
        experimentalAnnotations.forEach { a ->
            target.languageSettings.useExperimentalAnnotation(a)
        }
    }
}

@Suppress("NOTHING_TO_INLINE") // or error
fun Project.configureJvmTarget() {
    tasks.withType(KotlinJvmCompile::class.java) {
        kotlinOptions.jvmTarget = "1.8"
    }

    kotlinTargets.orEmpty().filterIsInstance<org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget>()
        .forEach { target ->
            target.compilations.all {
                kotlinOptions.jvmTarget = "1.8"
                kotlinOptions.languageVersion = "1.4"
            }
            target.testRuns["test"].executionTask.configure { useJUnitPlatform() }
        }

    extensions.findByType(JavaPluginExtension::class.java)?.run {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

fun Project.configureKotlinCompilerSettings() {
    val kotlinCompilations = kotlinCompilations ?: return
    for (kotlinCompilation in kotlinCompilations) with(kotlinCompilation) {
        if (isKotlinJvmProject) {
            @Suppress("UNCHECKED_CAST")
            this as org.jetbrains.kotlin.gradle.plugin.KotlinCompilation<KotlinJvmOptions>
        }
        kotlinOptions.freeCompilerArgs += "-Xjvm-default=all"
    }
}


val Project.kotlinSourceSets get() = extensions.findByName("kotlin").safeAs<KotlinProjectExtension>()?.sourceSets

val Project.kotlinTargets
    get() =
        extensions.findByName("kotlin").safeAs<KotlinSingleTargetExtension>()?.target?.let { listOf(it) }
            ?: extensions.findByName("kotlin").safeAs<KotlinMultiplatformExtension>()?.targets

val Project.isKotlinJvmProject: Boolean get() = extensions.findByName("kotlin") is KotlinJvmProjectExtension
val Project.isKotlinMpp: Boolean get() = extensions.findByName("kotlin") is KotlinMultiplatformExtension

val Project.kotlinCompilations
    get() = kotlinTargets?.flatMap { it.compilations }

val experimentalAnnotations = arrayOf(
    "kotlin.RequiresOptIn",
    "kotlin.contracts.ExperimentalContracts",
    "kotlin.experimental.ExperimentalTypeInference",
    "kotlin.ExperimentalUnsignedTypes",
    "kotlin.time.ExperimentalTime",

    "kotlinx.serialization.ExperimentalSerializationApi",

    "net.mamoe.mirai.utils.MiraiInternalApi",
    "net.mamoe.mirai.utils.MiraiExperimentalApi",
    "net.mamoe.mirai.LowLevelApi",
    "net.mamoe.mirai.utils.UnstableExternalImage",

    "net.mamoe.mirai.message.data.ExperimentalMessageKey"
)
