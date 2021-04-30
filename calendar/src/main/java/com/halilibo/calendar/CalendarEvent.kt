package com.halilibo.calendar

import androidx.compose.ui.graphics.Color
import com.halilibo.colors.G500
import java.util.*

data class CalendarEvent(
    val startDate: Date,
    val endDate: Date,
    val name: String = "",
    val description: String = "",
    val color: Color = G500
)