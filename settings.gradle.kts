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
include(":shared-domain")
include(":shared-networking")
include(":shared-storage")
include(":shared-theme")
include(":shared-navigation:api")
include(":shared-navigation:implementation")
include(":shared-ui")
include(":feature-search:api")
include(":feature-search:implementation")
include(":feature-tags:api")
include(":feature-tags:implementation")
include(":feature-settings:api")
include(":feature-settings:implementation")
include(":feature-notes:api")
include(":feature-notes:implementation")
include(":feature-auth:api")
include(":feature-auth:implementation")