@file:Suppress("UnstableApiUsage")

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
    mavenLocal()
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
