plugins {
    alias(libs.plugins.agp.lib)
    alias(libs.plugins.kotlin)
    id("maven-publish")
}

android {
    namespace = "org.robok.aapt2"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.android.jvm.get().toInt())
        targetCompatibility = JavaVersion.toVersion(libs.versions.android.jvm.get().toInt())
    }

    kotlinOptions {
        jvmTarget = libs.versions.android.jvm.get()
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

dependencies {
    implementation(fileTree("libs") { include("*.jar") })
    
    implementation(libs.material)
    implementation(libs.appcompat)
    
    implementation(libs.gson)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "com.github.robok-engine"
            artifactId = "robok-aapt2"
            version  = "0.0.1"
            
            from(components.findByName("release"))
        }
    }
}