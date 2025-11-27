// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Update to a recent, compatible version
    id("com.android.application") version "8.13.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false // Añadir el plugin de Hilt
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
 