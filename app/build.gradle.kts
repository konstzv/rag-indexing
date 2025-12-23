plugins {
    kotlin("jvm") version "1.9.22"
    application
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":core"))

    // Add coroutines for runBlocking
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Add datetime for Clock
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")

    // Add Ktor client (needed for OllamaClient references)
    implementation("io.ktor:ktor-client-core:2.3.7")
}

application {
    mainClass.set("com.rag.MainKt")
}
