dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}
rootProject.name = "MadeWithCompose"
include(":app")
include(":videoplayer")
include(":schedulecalendar")
include(":dotsandlines")
include(":calendar")
include(":weightentry")

include(":colors")
