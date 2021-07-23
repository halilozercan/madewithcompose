package com.halilibo.madewithcompose.schedulecalendar

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.halilibo.colors.*
import com.halilibo.madewithcompose.ui.theme.MadeWithComposeTheme
import com.halilibo.schedulecalendar.CalendarEvent
import com.halilibo.schedulecalendar.CalendarSection
import com.halilibo.schedulecalendar.ScheduleCalendar
import com.halilibo.schedulecalendar.rememberScheduleCalendarState
import java.time.LocalDateTime
import kotlin.random.Random

@Composable
fun ScheduleCalendarDemo() {
  val viewSpan = remember { mutableStateOf(48 * 3600L) }
  val eventTimesVisible = remember { mutableStateOf(true) }
  Column {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      IconButton(onClick = {
        viewSpan.value = (viewSpan.value * 2).coerceAtMost(96 * 3600)
      }) {
        Icon(imageVector = Icons.Default.ZoomOut, contentDescription = "increase")
      }

      IconButton(onClick = {
        viewSpan.value = (viewSpan.value / 2).coerceAtLeast(3 * 3600)
      }) {
        Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "decrease")
      }

      IconButton(onClick = {
        eventTimesVisible.value = !(eventTimesVisible.value)
      }) {
        Icon(imageVector = Icons.Default.HideImage, contentDescription = "decrease")
      }
    }

    val calendarState = rememberScheduleCalendarState()

    Spacer(modifier = Modifier.height(8.dp))

    val colors = remember {
      listOf(R500, G500, Y500, B500, P500, T500)
    }

    val sections = remember {
      (1..Random.nextInt(3,7)).map { sectionNumber ->
        CalendarSection(
          name = "Schedule #$sectionNumber",
          events = (1..Random.nextInt(1,3)).map { eventNumber ->
            val start = Random.nextLong(-72, 72)
            val end = Random.nextLong(start + 1, 96)
            CalendarEvent(
              startDate = LocalDateTime.now().plusHours(start),
              endDate = LocalDateTime.now().plusHours(end),
              name = "Event #$sectionNumber#$eventNumber",
              description = "",
              color = colors.random()
            )
          }
        )
      }
    }

    ScheduleCalendar(
      state = calendarState,
      now = LocalDateTime.now(),
      eventTimesVisible = eventTimesVisible.value,
      sections = sections,
      viewSpan = viewSpan.value
    )
  }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
  MadeWithComposeTheme {
    ScheduleCalendarDemo()
  }
}
