// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.3.0" apply false

    id("com.google.gms.google-services") version "4.4.2" apply false
}
buildscript {

    repositories {
        // Make sure that you have the following two repositories
        google()  // Google's Maven repository
        mavenCentral()  // Maven Central repository
        maven { url = uri("https://maven.facebook.com") }
    }

    dependencies {

        // Add the Maven coordinates and latest version of the plugin
        classpath ("com.android.tools.build:gradle:8.3.0")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()  // Opcional, dependiendo de las dependencias
    }
}
