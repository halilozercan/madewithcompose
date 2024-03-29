const val kotlinVersion = "1.9.10"

object Compose {
    const val version = "1.6.0-alpha07"
    const val compilerVersion = "1.5.3"
    const val animation = "androidx.compose.animation:animation:$version"
    const val compiler = "androidx.compose.compiler:compiler:$compilerVersion"
    const val foundation = "androidx.compose.foundation:foundation:$version"
    const val material = "androidx.compose.material:material:$version"
    const val runtime = "androidx.compose.runtime:runtime:$version"
    const val ui = "androidx.compose.ui:ui:$version"
    const val uiUtil = "androidx.compose.ui:ui-util:$version"

    const val uiGraphics = "androidx.compose.ui:ui-graphics:$version"
    const val uiTooling = "androidx.compose.ui:ui-tooling:$version"
    const val icons = "androidx.compose.material:material-icons-core:$version"
    const val iconsExtended = "androidx.compose.material:material-icons-extended:$version"
    const val navigation = "androidx.navigation:navigation-compose:2.4.0-alpha05"
    const val activity = "androidx.activity:activity-compose:1.3.0-beta02"
    const val viewModel = "androidx.lifecycle:lifecycle-viewmodel-compose:1.0.0-alpha07"

    const val testing = "androidx.compose.ui:ui-test-junit4:$version"
}

object Libraries {
    const val coil = "io.coil-kt:coil-compose:2.4.0"
    const val media = "androidx.media:media:1.4.2"
}

object BuildPlugins {
    const val androidGradlePlugin = "com.android.tools.build:gradle:8.1.2"
    const val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${kotlinVersion}"
}

object RichText {
    private const val version = "0.16.0"
    const val material = "com.halilibo.compose-richtext:richtext-ui-material:$version"
    const val markdown = "com.halilibo.compose-richtext:richtext-commonmark:$version"
    const val printing = "com.halilibo.compose-richtext:printing:$version"
}