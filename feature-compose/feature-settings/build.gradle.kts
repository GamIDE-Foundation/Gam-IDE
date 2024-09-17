plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    kotlin("plugin.serialization") version "2.0.20"
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.mikepenz.aboutlibraries.plugin")
}

android {
    namespace = "org.gampiot.robok.feature.settings.compose"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    
    buildFeatures {
        compose = true
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
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    kotlinOptions {
        jvmTarget = "18"
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.5")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    
    implementation("com.google.android.material:material:1.13.0-alpha05")
    
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.2")
    
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    val koinVersion = "4.0.0"
    implementation("io.insert-koin:koin-android:$koinVersion")
    implementation("io.insert-koin:koin-androidx-compose:$koinVersion")
    
    implementation("io.coil-kt:coil-compose:2.7.0")
    
    // compose
    implementation(platform("androidx.compose:compose-bom:2024.09.01"))
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("androidx.navigation:navigation-compose:2.8.0")
    
    implementation("io.github.fornewid:material-motion-compose-core:2.0.1")
    
    val aboutLibrariesVersion = "11.2.3"
    implementation("com.mikepenz:aboutlibraries-compose:$aboutLibrariesVersion")
    implementation("com.mikepenz:aboutlibraries-compose-m3:$aboutLibrariesVersion")
    implementation("com.mikepenz:aboutlibraries-core:$aboutLibrariesVersion")
    
    implementation(project(":feature:feature-res:strings"))
    implementation(project(":feature-compose:feature-component"))
}

kapt {
  correctErrorTypes = true
}