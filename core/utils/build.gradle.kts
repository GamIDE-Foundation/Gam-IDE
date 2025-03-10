plugins {
  alias(libs.plugins.agp.lib)
  alias(libs.plugins.kotlin)
  alias(libs.plugins.kotlin.serialization)
}

android {
  namespace = "org.robok.engine.core.utils"
  compileSdk = libs.versions.android.compileSdk.get().toInt()

  defaultConfig {
    minSdk = libs.versions.android.minSdk.get().toInt()
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildFeatures {
    viewBinding = true
  }

  compileOptions {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.android.jvm.get().toInt())
    targetCompatibility = JavaVersion.toVersion(libs.versions.android.jvm.get().toInt())
  }

  kotlinOptions {
    jvmTarget = libs.versions.android.jvm.get()
  }
}

dependencies {
  implementation(libs.google.material)
  implementation(libs.androidx.appcompat)
  implementation(libs.ktx.serialization.json)
}