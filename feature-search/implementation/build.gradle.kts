import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose.compiler)
  alias(libs.plugins.hilt.android)
  alias(libs.plugins.ksp)
}

android {
  namespace = "com.github.pepitoria.blinkoapp.search.implementation"
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
      jvmTarget = JvmTarget.fromTarget(libs.versions.jvmTarget.get())
    }
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
}

dependencies {
  implementation(project(":feature-search:api"))
  implementation(project(":feature-tags:api"))
  implementation(project(":core:presentation"))
  implementation(project(":core:domain"))

  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)

  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.ui.tooling.preview)
  implementation(libs.androidx.ui.tooling.android)
  implementation(libs.androidx.animation)

  implementation(libs.androidx.material3)

  implementation(libs.timber)

  // Dagger hilt
  implementation(libs.hilt.android)
  implementation(libs.androidx.hilt.navigation.compose)
  ksp(libs.hilt.compiler)

  // testing
  testImplementation(libs.junit.jupiter)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
}