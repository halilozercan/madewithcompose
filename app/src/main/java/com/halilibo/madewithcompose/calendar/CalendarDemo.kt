package com.halilibo.madewithcompose.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halilibo.calendar.*
import com.halilibo.colors.B500
import com.halilibo.colors.R500
import com.halilibo.colors.Y500
import com.halilibo.madewithcompose.ui.theme.MadeWithComposeTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarDemo() {
  val events = remember {
    setOf(
      CalendarEvent(
        startDate = Date().apply { time -= 6 * 60 * 60 * 1000 },
        endDate = Date().apply { time += 12 * 60 * 60 * 1000 },
        name = "Event 1",
        description = "",
        color = R500
      ),
      CalendarEvent(
        startDate = Date().apply { time += 24 * 60 * 60 * 1000 },
        endDate = Date().apply { time += 48 * 60 * 60 * 1000 },
        name = "Event 2",
        description = "",
        color = B500
      ),
      CalendarEvent(
        startDate = Date(),
        endDate = Date().apply { time += 36 * 60 * 60 * 1000 },
        name = "Event 3",
        description = "",
        color = Y500,
      )
    )
  }

  val selectedDay = remember { mutableStateOf(CalendarDay.create()) }
  val visibleMonth = remember { mutableStateOf(CalendarDay.create()) }

  Column(modifier = Modifier.padding(16.dp)) {
    Text(
      text = SimpleDateFormat("MMM").format(visibleMonth.value.asCalendar.asDate),
      fontSize = 20.sp
    )
    Spacer(modifier = Modifier.height(16.dp))
    Calendar(
      selectedDay = selectedDay.value,
      onSelectedDayChange = {
        selectedDay.value = it
      },
      visibleMonth = visibleMonth.value,
      onVisibleMonthChange = {
        visibleMonth.value = it
      },
      events = events
    )
  }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
  MadeWithComposeTheme {
    CalendarDemo()
  }
}