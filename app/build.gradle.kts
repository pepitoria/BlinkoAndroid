import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Properties
import java.util.zip.CRC32

plugins {
  alias(libs.plugins.androidApplication)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose.compiler)
  alias(libs.plugins.hilt.android)
  alias(libs.plugins.ksp)
}

buildscript {
  dependencies {
    classpath(libs.okhttp3.okhttp)
  }
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
  kotlin {
    compilerOptions {
      jvmTarget = JvmTarget.fromTarget(libs.versions.jvmTarget.get())
    }
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
  description = "Generates the release APK, renames it and copies it to the project root folder."

  dependsOn("assembleRemoteRelease")

  doLast {
    generateNewRelease()
  }
}

tasks.register("uploadApkToGitHub") {
  doLast {
    uploadApkToGitHub()
  }
}

dependencies {

  implementation(project(":core:presentation"))
  implementation(project(":core:domain"))
  implementation(project(":core:data"))
  implementation(project(":feature-search:api"))
  implementation(project(":feature-search:implementation"))
  implementation(project(":feature-tags:api"))
  implementation(project(":feature-tags:implementation"))
  implementation(project(":feature-settings:api"))
  implementation(project(":feature-settings:implementation"))
  implementation(project(":feature-auth:api"))
  implementation(project(":feature-auth:implementation"))

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

  testImplementation(libs.junit.jupiter)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.ui.test.junit4)
  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)
}

fun generateNewRelease() {
  incrementCodeVersion()

  val apkFile = file("build/outputs/apk/remote/release/app-remote-release.apk")
  val destinationDir = file("../")

  if (apkFile.exists()) {
    destinationDir.mkdirs()
    val renamedApk = File(destinationDir, getApkName())
    apkFile.copyTo(renamedApk, overwrite = true)
    println("APK copied to: ${renamedApk.absolutePath}")
  } else {
    println("release APK NOT FOUND.")
  }
}

fun uploadApkToGitHub() {
  val githubToken = getGithubToken()
  val repoOwner = "pepitoria"
  val repoName = "BlinkoAndroid"
  val tagName = getVersionNameFromGit()
  val releaseName = "Release $tagName"
  val releaseDescription = getReleaseDescription()
  val apkFilePath = "../${getApkName()}"

  val apkFile = file(apkFilePath)
  if (!apkFile.exists()) {
    throw GradleException("APK not found: $apkFilePath")
  }

  val createReleaseUrl = "https://api.github.com/repos/$repoOwner/$repoName/releases"
  val releaseBody = JsonObject().apply {
    addProperty("tag_name", tagName)
    addProperty("name", releaseName)
    addProperty("body", releaseDescription)
    addProperty("draft", false)
    addProperty("prerelease", false)
  }

  val createReleaseRequest = Request.Builder()
    .url(createReleaseUrl)
    .addHeader("Authorization", "token $githubToken")
    .addHeader("Content-Type", "application/json")
    .post(releaseBody.toString().toRequestBody("application/json".toMediaType()))
    .build()

  val client = OkHttpClient()
  val createReleaseResponse = client.newCall(createReleaseRequest).execute()
  if (!createReleaseResponse.isSuccessful) {
    throw GradleException("Error creating the release: ${createReleaseResponse.body.string()}")
  }

  val releaseResponseJson = JsonParser.parseString(createReleaseResponse.body.string()).asJsonObject
  val encodedFileName = URLEncoder.encode(apkFile.name, StandardCharsets.UTF_8.toString())
  val uploadUrl =   releaseResponseJson["upload_url"].asString.replace("{?name,label}", "?name=$encodedFileName")

  val uploadApkRequest = Request.Builder()
    .url(uploadUrl)
    .addHeader("Authorization", "token $githubToken")
    .addHeader("Content-Type", "application/vnd.android.package-archive")
    .post(apkFile.asRequestBody("application/vnd.android.package-archive".toMediaType()))
    .build()

  val uploadApkResponse = client.newCall(uploadApkRequest).execute()
  if (!uploadApkResponse.isSuccessful) {
    throw GradleException("Error uploading APK: ${uploadApkResponse.body.string()}")
  }

  println("APK uploaded successfully to the release in GitHub.")
}

fun getApkName(): String {
  val versionCodeHash = getShortHashedVersionCode()
  val versionName = getVersionNameFromGit()
  return "BlinkoApp-$versionName-$versionCodeHash.apk"
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

fun getShortHashedVersionCode(): String {
  val versionCode = getCodeVersion().toString()
  val crc = CRC32()
  crc.update(versionCode.toByteArray())
  return crc.value.toString(16)
}

fun getReleaseDescription(): String {
  val lastTagCommand = Runtime.getRuntime().exec("git describe --tags --abbrev=0")
  val lastTag = lastTagCommand.inputStream.bufferedReader().readText().trim()

  val prevTagCommand = Runtime.getRuntime().exec("git describe --tags --abbrev=0 \$(git rev-list --tags --skip=1 --max-count=1)")
  val prevTag = prevTagCommand.inputStream.bufferedReader().readText().trim()

  val commitsBetweenTagsCommand = Runtime.getRuntime().exec("git log $prevTag..$lastTag --oneline")
  val commitsBetweenTags = commitsBetweenTagsCommand.inputStream.bufferedReader().readText().trim()

  return commitsBetweenTags
}

fun getGithubToken(): String {
  val properties = Properties().apply {
    load(File("../blinkoapp.github.properties").reader())
  }
  return properties["GITHUB_TOKEN"].toString().trim()
}