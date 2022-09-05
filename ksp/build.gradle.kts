import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    explicitApi = null
}

dependencies {
    implementation(libs.kotlin.stdlibJDK8)
    implementation(libs.ksp)

    testImplementation(kotlin("test"))
    testImplementation(libs.assertj)
    testImplementation(libs.classgraph)
    testImplementation(libs.kotlinCompileTesting) {
        exclude(
            group = libs.classgraph.get().module.group,
            module = libs.classgraph.get().module.name
        )
        exclude(
            group = libs.kotlin.stdlibJDK8.get().module.group,
            module = libs.kotlin.stdlibJDK8.get().module.name
        )
    }
    testImplementation(libs.kotlinCompileTestingKsp)

    testRuntimeOnly(projects.transformativeKsp)
    testRuntimeOnly(projects.transformativeTypes)
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}