pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "BlinkoApp"
include(":app")
include(":core:presentation")
include(":core:domain")
include(":core:data")
include(":feature-search:api")
include(":feature-search:implementation")
include(":feature-tags:api")
include(":feature-tags:implementation")
include(":feature-settings:api")
include(":feature-settings:implementation")