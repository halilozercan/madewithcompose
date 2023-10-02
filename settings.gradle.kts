@file:Suppress("UnstableApiUsage")

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
    mavenLocal()
    maven { url = uri("https://androidx.dev/snapshots/builds/10890965/artifacts/repository") }
  }
}
rootProject.name = "madewithcompose"
include(":app")
include(":videoplayer")
include(":schedulecalendar")
include(":dotsandlines")
include(":calendar")
include(":weightentry")
include(":screenshot")

include(":colors")
