plugins {
    id("com.android.library")
    id("kotlin-android")
    id("maven-publish")
}

group = "robok.aapt2"

android {
    namespace = "robok.aapt2"
    compileSdk = 35
    
    defaultConfig {
        minSdk = 21
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    
    packaging {
        resources {
            // Exclude all instances of 'plugin.properties'
            excludes += "plugin.properties"

            // Alternatively, keep the first occurrence
            // pickFirsts += "plugin.properties"
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "17"
}

dependencies {
    implementation(fileTree("libs") { include("*.jar") })
    
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    
    implementation("com.google.code.gson:gson:2.8.7")
    
    implementation(project(":feature:feature-util"))
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.github.gampiot-inc"
            artifactId = "robok-aapt2"
            version = "1.0.0"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}