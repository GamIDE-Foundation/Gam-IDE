plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("com.google.dagger.hilt.android")
    kotlin("kapt")
}

android {
    namespace = "dev.trindadedev.robokide"
    compileSdk = 34
    
    defaultConfig {
        applicationId = "dev.trindadedev.robokide"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        
        vectorDrawables { 
            useSupportLibrary = true
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }
    
    signingConfigs {
        getByName("debug") {
            storeFile = file(layout.buildDirectory.dir("../testkey.keystore"))
            storePassword = "testkey"
            keyAlias = "testkey"
            keyPassword = "testkey"
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

dependencies {

    val robok_language_version = "1.0.0"
    val material_version = "1.13.0-alpha04"
    val appcompat_version = "1.7.0-alpha03"
    val kotlin_version = "2.0.0"
    val kotlin_coroutines_version = "1.9.0-RC"
    val okhttp3_version = "4.12.0"
    val activity_version = "1.9.0"
    val editorGroupId = "io.github.Rosemoe.sora-editor"
    
    // androidx
    implementation("androidx.appcompat:appcompat:$appcompat_version")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.fragment:fragment-ktx:1.8.1")
    
    // google
    implementation("com.google.android.material:material:$material_version")
    implementation("com.google.code.gson:gson:2.11.0")
    
    // jetbrains
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlin_coroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$kotlin_coroutines_version")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version")
    
    // squareup
    implementation("com.squareup.okhttp3:okhttp:$okhttp3_version")    
    
    // dagger
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")
    
    // filepicker
    implementation("com.github.Ruan625Br:FilePickerSphere:1.0.0")
    
    // Sora editor
    implementation(platform("$editorGroupId:bom:0.23.4"))
    implementation("$editorGroupId:editor")
    implementation("$editorGroupId:editor-lsp")
    implementation("$editorGroupId:language-java")
    implementation("$editorGroupId:language-treesitter")
    implementation("$editorGroupId:language-textmate")
    
    // robok
    // implementation("com.github.Robok-Foundation:Robok-Language:$robok_language_version")
    implementation(project(":language"))

    // Add desugaring dependency
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
}