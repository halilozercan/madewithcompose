plugins {
  id("com.android.application")
  id("kotlin-android")
  id("kotlin-parcelize")
}

android {
  compileSdk = 32
  buildToolsVersion = "30.0.3"

  defaultConfig {
    applicationId = "com.halilibo.madewithcompose"
    minSdk = 26
    targetSdk = 32
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables {
      useSupportLibrary = true
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  kotlinOptions {
    jvmTarget = "11"
  }
  buildFeatures {
    compose = true
  }
  composeOptions {
    kotlinCompilerExtensionVersion = Compose.compilerVersion
  }
  packagingOptions {
    resources.excludes.add("META-INF/DEPENDENCIES")
    resources.excludes.add("META-INF/LICENSE")
    resources.excludes.add("META-INF/LICENSE.txt")
    resources.excludes.add("META-INF/license.txt")
    resources.excludes.add("META-INF/NOTICE")
    resources.excludes.add("META-INF/NOTICE.txt")
    resources.excludes.add("META-INF/notice.txt")
    resources.excludes.add("META-INF/ASL2.0")
    resources.excludes.add("META-INF/*.kotlin_module")
  }
}

configurations.all {
  resolutionStrategy.eachDependency {
    if (requested.group.contains("org.jetbrains.compose")) {
      val groupName = requested.group.replace("org.jetbrains.compose", "androidx.compose")
      useTarget("$groupName:${requested.name}:${Compose.version}")
    }
  }
}

dependencies {
  implementation(project(":videoplayer"))
  implementation(project(":schedulecalendar"))
  implementation(project(":dotsandlines"))
  implementation(project(":calendar"))
  implementation(project(":weightentry"))
  implementation(project(":screenshot"))
  implementation(project(":colors"))

  implementation("com.google.android.material:material:1.4.0")

  implementation(Compose.ui)
  implementation(Compose.material)
  implementation(Compose.uiTooling)
  implementation(Compose.iconsExtended)
  implementation(Compose.activity)
  implementation(Compose.animation)
  implementation(Compose.navigation)
  implementation(Compose.uiUtil)
  implementation(Accompanist.pager)
  implementation(Accompanist.insets)

  implementation(Libraries.coil)
  implementation(Libraries.media)

  implementation(RichText.material)
  implementation(RichText.markdown)
  implementation(RichText.printing)

  implementation("com.google.code.gson:gson:2.8.6")
  implementation("androidx.lifecycle:lifecycle-common-java8:2.3.1")
  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.1.3")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
  androidTestImplementation(Compose.testing)
}