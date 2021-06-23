package com.halilibo.madewithcompose.ui.theme

import androidx.compose.runtime.*

val LocalNightMode = staticCompositionLocalOf<NightMode> { error("Night Mode is not provided") }

@Stable
class NightMode(
    initialNightMode: Boolean
) {
    var isNight by mutableStateOf(initialNightMode)

    fun setDay() {
        isNight = false
    }

    fun setNight() {
        isNight = true
    }

    fun toggle() {
        isNight = !isNight
    }
}