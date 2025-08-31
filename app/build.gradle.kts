plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Seit Kotlin 2.x: Compose-Compiler über Plugin
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "de.m3usuite.remote"
    compileSdk = 35

    defaultConfig {
        applicationId = "de.m3usuite.remote"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // Debug-spezifische Flags, z.B. Network Security Debug Overrides
        }
    }

    // Compose aktivieren
    buildFeatures {
        compose = true
        // buildConfig = true // falls du BuildConfig-Felder willst
    }

    // Compose-Compiler (optional: Reports/Flags)
    composeCompiler {
        reportsDestination = layout.buildDirectory.dir("compose_compiler_reports")
    }

    // Java/Kotlin Sprachebene
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // --- Compose BOM (August 2025, Compose 1.9) ---
    implementation(platform("androidx.compose:compose-bom:2025.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Activity/Navigation/Lifecycle
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.navigation:navigation-compose:2.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.3")

    // Core / Splashscreen
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.core:core-splashscreen:1.0.1")

    // DataStore + (optional) Security Crypto
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation("androidx.security:security-crypto-ktx:1.1.0")

    // WorkManager (Hintergrundjobs)
    implementation("androidx.work:work-runtime-ktx:2.10.1")

    // OkHttp 5 (BOM)
    implementation(platform("com.squareup.okhttp3:okhttp-bom:5.1.0"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")

    // SSHJ für SSH/Terminal
    implementation("com.hierynomus:sshj:0.40.0")

    // (Optional) Coil 3 für Bilder/QR, falls gebraucht
    implementation("io.coil-kt.coil3:coil-compose-android:3.3.0")

    // Logging
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Compose UI Tests (optional)
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
