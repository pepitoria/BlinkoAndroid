// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.androidApplication) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.androidLibrary) apply false
  alias(libs.plugins.kotlin.compose.compiler)
  alias(libs.plugins.hilt.android) apply false
  alias(libs.plugins.ksp) apply false
  alias(libs.plugins.ktlint)
}

// Task to run all unit tests across all modules
tasks.register("allUnitTests") {
  description = "Runs all unit tests in all modules"
  group = "verification"

  dependsOn(
    ":feature-auth:implementation:testRemoteDebugUnitTest",
    ":feature-notes:implementation:testRemoteDebugUnitTest",
    ":feature-search:implementation:testRemoteDebugUnitTest",
    ":feature-settings:implementation:testRemoteDebugUnitTest",
    ":feature-tags:implementation:testRemoteDebugUnitTest",
    ":shared-networking:testRemoteDebugUnitTest",
    ":shared-offline:testRemoteDebugUnitTest",
  )
}

// Task to run all instrumentation tests (requires connected device/emulator)
tasks.register("allInstrumentationTests") {
  description = "Runs all instrumentation tests (requires device/emulator)"
  group = "verification"

  dependsOn(
    ":shared-storage:connectedRemoteDebugAndroidTest",
  )
}

// Task to run all tests (unit + instrumentation)
tasks.register("allTests") {
  description = "Runs all unit and instrumentation tests"
  group = "verification"

  dependsOn("allUnitTests", "allInstrumentationTests")
}
