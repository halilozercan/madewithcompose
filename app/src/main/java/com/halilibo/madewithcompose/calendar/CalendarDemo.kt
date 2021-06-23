package com.halilibo.madewithcompose.calendar

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
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
    MonthRow(
      month = visibleMonth.value,
      modifier = Modifier.fillMaxWidth()
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MonthRow(
  month: CalendarDay,
  modifier: Modifier = Modifier
) {
  val formatter = remember {
    SimpleDateFormat("MMM", Locale.getDefault())
  }
  AnimatedContent(
    targetState = month.asCalendar.asDate,
    transitionSpec = {
      if (initialState < targetState) {
        slideInHorizontally({ it }) with slideOutHorizontally({ -it })
      } else {
        slideInHorizontally({ -it }) with slideOutHorizontally({ it })
      }
    },
    modifier = modifier
  ) { targetState ->
    Text(
      text = formatter.format(targetState),
      fontSize = 20.sp,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth()
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