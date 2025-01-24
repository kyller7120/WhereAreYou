plugins {
    id("com.android.application")
    id("com.google.gms.google-services") version "4.4.2" apply true
}

android {
    namespace = "l.m.dev.whereareyou"
    compileSdk = 35

    defaultConfig {
        applicationId = "l.m.dev.whereareyou"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.8.0"))  // Solo una vez
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("androidx.activity:activity:1.10.0")

    // Firebase SDK
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-database:21.0.0")  // Usa la versión correcta
    implementation("com.google.firebase:firebase-firestore")

    // FirebaseUI
    implementation("com.firebaseui:firebase-ui-auth:7.2.0")


    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    //Principales
    implementation ("com.google.firebase:firebase-auth:21.0.5")
    implementation ("com.google.android.gms:play-services-auth:20.4.0")
}
