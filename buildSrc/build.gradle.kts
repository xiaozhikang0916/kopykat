import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    kotlin("jvm") version embeddedKotlinVersion
}

val gradleJvmTarget = "11"
val gradleKotlinTarget = "1.8"

val kotlinVersion = "1.8.10"

dependencies {
    implementation(platform(libs.kotlin.bom))

    // Set the *Maven coordinates* of Gradle plugins here.
    // This should be the only place where Gradle plugins versions are defined.

    implementation(libs.gradlePlugin.kotlinJvm)
    implementation(libs.gradlePlugin.ktLint)
    implementation(libs.gradlePlugin.dokka)
}


tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = gradleJvmTarget
        apiVersion = gradleKotlinTarget
        languageVersion = gradleKotlinTarget
    }
}


kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(gradleJvmTarget))
    }
}


kotlinDslPluginOptions {
    jvmTarget.set(gradleJvmTarget)
}
