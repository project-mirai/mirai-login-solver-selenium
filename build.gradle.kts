@file:Suppress("UnstableApiUsage", "UNUSED_VARIABLE")

import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.utils.addToStdlib.safeAs
import java.util.Date

plugins {
    kotlin("jvm") version "1.4.21"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.5"
}

group = "net.mamoe"
version = "1.0-dev-6"

repositories {
    mavenCentral()
    mavenLocal()
    maven(url = "https://dl.bintray.com/karlatemp/misc")
    jcenter()
}

dependencies {
    fun compileAndRuntime(module: Any) {
        compileOnly(module)
        testImplementation(module)
    }
    compileAndRuntime(kotlin("stdlib"))
    implementation("io.github.karlatemp.mxlib:mxlib-api:3.0-dev-2")
    implementation("io.github.karlatemp.mxlib:mxlib-selenium:3.0-dev-2") {
        exclude("junit", "junit")
        exclude("classworlds", "classworlds")
    }
    compileAndRuntime("net.mamoe:mirai-core-api:2.0-M2-dev-2")
    testImplementation("net.mamoe:mirai-core:2.0-M2-dev-2")
    compileAndRuntime("net.mamoe:mirai-console:2.0-M1")
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

publishing {
    publications.register("artifact", MavenPublication::class.java) {
        from(components["java"])
        artifact(tasks.getByName("sourcesJar"))
        val chromeExt = File("src/main/resources/mirai-selenium-ext.zip")
        if (chromeExt.isFile) {
            artifact(object :
                org.gradle.api.publish.maven.internal.artifact.FileBasedMavenArtifact(chromeExt) {
                override fun getDefaultExtension() = "crx"
            })
        }
    }
}

bintray {
    setPublications("artifact")

    user = (project.propertySafe("bintray.user") ?: System.getenv("USERNAME")).toString()
    key = (project.propertySafe("bintray.key") ?: System.getenv("TOKEN")).toString()
    override = true
    publish = true
    pkg.apply {
        repo = "mirai"
        name = rootProject.name
        setLicenses("AGPL-v3")
        vcsUrl = "https://github.com/project-mirai/mirai-login-solver-selenium.git"
        version.apply {
            name = project.version.toString()
            desc = project.version.toString()
            released = Date().toString()
        }
    }
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
