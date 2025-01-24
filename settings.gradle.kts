pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()  // Este es adecuado para resolver plugins de Gradle
    }
}


rootProject.name = "WhereAreYou"
include(":app")  // Asegura que solo el módulo app se incluye en el proyecto raíz
