import java.io.FileInputStream
import java.util.Properties

plugins {
  alias(libs.plugins.androidApplication)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose.compiler)
  alias(libs.plugins.hilt.android)
  alias(libs.plugins.ksp)
}

android {
  namespace = "com.github.pepitoria.blinkoapp"
  compileSdk = libs.versions.compileSdk.get().toInt()

  defaultConfig {
    applicationId = "com.github.pepitoria.blinkoapp"
    minSdk = libs.versions.minSdk.get().toInt()
    targetSdk = libs.versions.targetSdk.get().toInt()
    versionCode = getCodeVersion()
    versionName = getVersionNameFromGit()

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables {
      useSupportLibrary = true
    }
  }

  signingConfigs {
    create("release") {
      val properties = Properties().apply {
        load(File("../blinkoapp.signing.properties").reader())
      }
      storeFile = File(properties.getProperty("storeFilePath"))
      storePassword = properties.getProperty("storePassword")
      keyPassword = properties.getProperty("keyPassword")
      keyAlias = properties.getProperty("keyAlias")
    }
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
      signingConfig = signingConfigs.getByName("release")
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }

    debug {
      applicationIdSuffix = ".debug"
      isDebuggable = true
      isMinifyEnabled = false
      resValue("string", "app_name", "BlinkoApp Debug")
    }

  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions {
    jvmTarget = libs.versions.jvmTarget.get()
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
//    composeOptions {
//        kotlinCompilerExtensionVersion = libs.versions.kotlin.compiler.extension.get()
//    }
  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
}

tasks.register("newRelease") {
  group = "build"
  description = "Genera el APK de release, lo renombra y lo mueve a otro directorio."

  dependsOn("assembleRemoteRelease")

  doLast {
    incrementCodeVersion()
    val versionCode = getCodeVersion()
    val versionName = getVersionNameFromGit()

    val apkFile = file("build/outputs/apk/remote/release/app-remote-release.apk")
    val destinationDir = file("../")

    if (apkFile.exists()) {
      destinationDir.mkdirs()
      val renamedApk = File(destinationDir, "BlinkoApp-$versionName-($versionCode).apk")
      apkFile.copyTo(renamedApk, overwrite = true)
      println("APK moved to: ${renamedApk.absolutePath}")
    } else {
      println("release APK NOT FOUND.")
    }
  }
}

fun getVersionNameFromGit(): String {
  val process = Runtime.getRuntime().exec("git describe --tags --abbrev=0")
  return process.inputStream.bufferedReader().readText().trim()
}

fun getCodeVersion(): Int {
  val versionPropsFile = file("version.properties")

  val versionProps = Properties()

  if (versionPropsFile.canRead()) {
    versionProps.load(FileInputStream(versionPropsFile))
  } else {
    versionProps["VERSION_CODE"] = "0"
  }

  return versionProps["VERSION_CODE"].toString().toInt()
}

fun incrementCodeVersion() {
  val versionPropsFile = file("version.properties")

  val versionProps = Properties()

  if (versionPropsFile.canRead()) {
    versionProps.load(FileInputStream(versionPropsFile))
  } else {
    versionProps["VERSION_CODE"] = "0"
  }

  val code = versionProps["VERSION_CODE"].toString().toInt() + 1

  versionProps["VERSION_CODE"] = code.toString()
  versionProps.store(versionPropsFile.writer(), null)
}

dependencies {
  implementation(project(":domain"))

  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.ui.tooling.preview)
  implementation(libs.androidx.material3)

  implementation(libs.timber)

  // Dagger hilt
  implementation(libs.hilt.android)
  implementation(libs.androidx.hilt.navigation.compose)
  ksp(libs.hilt.compiler)

  implementation(libs.richtext.commonmark)

  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.ui.test.junit4)
  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)
}