plugins {
    kotlin("jvm") version "1.9.22" apply false
    kotlin("plugin.serialization") version "1.9.22" apply false
}

allprojects {
    group = "com.rag"
    version = "1.0.0"

    repositories {
        mavenCentral()
    }
}
