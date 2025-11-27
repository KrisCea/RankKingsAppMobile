pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral() // This is the crucial line for the BCrypt library
    }
}

rootProject.name = "Rankkings"
include(":app")
