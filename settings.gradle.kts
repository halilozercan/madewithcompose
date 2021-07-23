dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
    mavenLocal()
  }
}
rootProject.name = "MadeWithCompose"
include(":app")
include(":videoplayer")
include(":schedulecalendar")
include(":dotsandlines")
include(":calendar")
include(":weightentry")
include(":screenshot")

include(":colors")
