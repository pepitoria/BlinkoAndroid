import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.hilt.android)
  alias(libs.plugins.ksp)
  alias(libs.plugins.ktlint)
}

android {
  namespace = "com.github.pepitoria.blinkoapp.shared.storage"
  compileSdk = libs.versions.compileSdk.get().toInt()

  defaultConfig {
    minSdk = libs.versions.minSdk.get().toInt()

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")
  }

  flavorDimensions += "environment"

  productFlavors {
    create("remote") {
      dimension = "environment"
    }

    create("mockLocal") {
      dimension = "environment"
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlin {
    compilerOptions {
      jvmTarget = JvmTarget.fromTarget(libs.versions.jvmTarget.get())
    }
  }
}

dependencies {
  implementation(project(":shared-domain"))

  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)

  // Dagger hilt
  implementation(libs.hilt.android)
  implementation(libs.androidx.hilt.navigation.compose)
  ksp(libs.hilt.compiler)
}
