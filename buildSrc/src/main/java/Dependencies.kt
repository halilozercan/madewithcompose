const val kotlinVersion = "1.4.32"

object Compose {
    const val version = "1.0.0-beta07"
    const val ui = "androidx.compose.ui:ui:$version"
    const val uiGraphics = "androidx.compose.ui:ui-graphics:$version"
    const val uiTooling = "androidx.compose.ui:ui-tooling:$version"
    const val foundationLayout = "androidx.compose.foundation:foundation-layout:$version"
    const val material = "androidx.compose.material:material:$version"
    const val icons = "androidx.compose.material:material-icons-core:$version"
    const val iconsExtended = "androidx.compose.material:material-icons-extended:$version"
    const val runtimeLiveData = "androidx.compose.runtime:runtime-livedata:$version"
    const val navigation = "androidx.navigation:navigation-compose:2.4.0-alpha01"
    const val activity = "androidx.activity:activity-compose:1.3.0-alpha08"
    const val viewModel = "androidx.lifecycle:lifecycle-viewmodel-compose:1.0.0-alpha05"
    const val testing = "androidx.compose.ui:ui-test-junit4:$version"
}

object Accompanist {
    const val version = "0.10.0"
    const val pager = "com.google.accompanist:accompanist-pager:$version"
    const val coil = "com.google.accompanist:accompanist-coil:$version"
}

object BuildPlugins {
    const val androidGradlePlugin = "com.android.tools.build:gradle:7.1.0-alpha01"
    const val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${kotlinVersion}"
}