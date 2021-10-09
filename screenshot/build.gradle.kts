plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    compileSdk = 31

    defaultConfig {
        minSdk = 26
        targetSdk = 31

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
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Compose.version
        kotlinCompilerVersion = kotlinVersion
    }
}

dependencies {
    implementation(project(":colors"))
    implementation(Compose.ui)
    implementation(Compose.material)
    implementation(Compose.uiTooling)
    implementation(Compose.iconsExtended)
    implementation(Compose.activity)
    implementation(Accompanist.pager)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")
}