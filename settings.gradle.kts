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

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "My Application"

// تطبيق إدارة التراخيص (License Manager) هو الآن التطبيق الرئيسي (:app) ليتم بناؤه وتثبيته تلقائياً على المحاكي.
// تم نقل تطبيق كروتك إلى المجلد (:kurotek) لتسهيل التبديل لاحقاً.
include(":app")

include(":kurotek")
project(":kurotek").projectDir = file("kurotek")

