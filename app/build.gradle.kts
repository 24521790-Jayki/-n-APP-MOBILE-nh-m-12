plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.group12"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.group12"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Media3 for TTS service
    implementation("androidx.media3:media3-session:1.4.1")
    implementation("androidx.media3:media3-common:1.4.1")
    // Room persistence library
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    // EPUB parsing library and HTML helper (exclude xmlpull duplicate class)
    implementation("nl.siegmann.epublib:epublib-core:3.1") {
        exclude(group = "xmlpull", module = "xmlpull")
    }
    implementation("org.jsoup:jsoup:1.17.2")
    // Compression utilities for unzip
    implementation("org.apache.commons:commons-compress:1.26.0")
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)

    // REST API
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
