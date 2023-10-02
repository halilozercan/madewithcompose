plugins {
  id("com.android.application")
  id("kotlin-android")
  id("kotlin-parcelize")
}

android {
  compileSdk = 34
  buildToolsVersion = "33.0.1"
  namespace = "com.halilibo.madewithcompose"

  defaultConfig {
    applicationId = "com.halilibo.madewithcompose"
    minSdk = 26
    targetSdk = 34
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
  packaging {
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

  implementation("com.google.android.material:material:1.9.0")

  implementation(Compose.ui)
  implementation(Compose.material)
  implementation(Compose.uiTooling)
  implementation(Compose.iconsExtended)
  implementation(Compose.activity)
  implementation(Compose.animation)
  implementation(Compose.navigation)
  implementation(Compose.uiUtil)

  implementation(Libraries.coil)
  implementation(Libraries.media)

  implementation(RichText.material)
  implementation(RichText.markdown)
  implementation(RichText.printing)

  implementation("com.google.code.gson:gson:2.9.1")
  implementation("androidx.lifecycle:lifecycle-common-java8:2.6.2")
  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.1.5")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
  androidTestImplementation(Compose.testing)
}