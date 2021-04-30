package com.halilibo.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.halilibo.colors.N100
import kotlinx.coroutines.flow.collect
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalPagerApi::class)
@Composable
fun Calendar(
    modifier: Modifier = Modifier,
    selectedDay: CalendarDay = CalendarDay.create(),
    onSelectedDayChange: (CalendarDay) -> Unit = {},
    visibleMonth: CalendarDay = CalendarDay.create(),
    onVisibleMonthChange: (CalendarDay) -> Unit = {},
    today: CalendarDay = CalendarDay.create(),
    events: Set<CalendarEvent> = emptySet(),
    firstDayOfWeek: Int = Calendar.SUNDAY
) {
    val pagerState = rememberPagerState(
        pageCount = Int.MAX_VALUE,
        initialPage = visibleMonth.monthIndex
    )

    val calendarScope = remember(firstDayOfWeek) { CalendarScope(firstDayOfWeek) }

    LaunchedEffect(pagerState, visibleMonth) {
        pagerState.animateScrollToPage(visibleMonth.monthIndex)
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect {
            onVisibleMonthChange(
                CalendarDay(
                    day = 1,
                    month = it % 12,
                    year = it / 12
                )
            )
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier.background(MaterialTheme.colors.surface)
    ) { index ->
        with (calendarScope) {
            CalendarPage(
                monthIndex = index,
                onDayClick = {
                    onVisibleMonthChange(CalendarDay(day = 1, month = it.month, year = it.year))
                    onSelectedDayChange(it)
                },
                selectedDay = selectedDay,
                today = today,
                events = events
            )
        }
    }
}

@Composable
private fun CalendarScope.CalendarPage(
    monthIndex: Int,
    onDayClick: (CalendarDay) -> Unit,
    selectedDay: CalendarDay,
    today: CalendarDay,
    events: Set<CalendarEvent>
) {
    val daysInMonth = remember(monthIndex) { getDaysInMonth(monthIndex) }
    val daysOfWeek = remember { daysOfWeek("EEE") }

    Layout(content = {
        daysOfWeek.forEach { dayOfWeek ->
            Text(
                text = dayOfWeek.take(1).toUpperCase(),
                fontSize = 12.sp,
                color = N100,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        daysInMonth.forEach { (calendarDay, dayOfWeek) ->
            val isInCurrentMonth = monthIndex == calendarDay.monthIndex
            CalendarDayView(
                number = calendarDay.day,
                isSelected = calendarDay == selectedDay,
                isToday = calendarDay == today,
                events = events.filterByDay(calendarDay).toSet(),
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .alpha(if (isInCurrentMonth) 1f else 0f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        onDayClick(calendarDay)
                    }
            )
        }
    }) { measurables, constraints ->
        val measurablesByWeek = measurables.chunked(7)
        val placeables = measurablesByWeek.map { weekOfMeasurables ->
            weekOfMeasurables.map { measurable ->
                measurable.measure(Constraints.fixedWidth(constraints.maxWidth / 7))
            }
        }

        val heightOfWeeks = placeables.sumOf { weekOfPlaceables ->
            weekOfPlaceables.maxOf { it.height }
        }

        layout(constraints.maxWidth, heightOfWeeks) {
            var accHeight = 0
            placeables.forEach { weekOfPlaceables ->
                var accWidth = 0
                weekOfPlaceables.forEach {
                    it.placeRelative(accWidth, accHeight)
                    accWidth += it.width
                }
                accHeight += weekOfPlaceables.maxOf { it.height }
            }
        }
    }
}

@Composable
private fun CalendarScope.CalendarDayView(
    number: Int,
    isSelected: Boolean,
    isToday: Boolean,
    events: Set<CalendarEvent>,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isToday -> {
            MaterialTheme.colors.primary
        }
        isSelected -> {
            MaterialTheme.colors.primary.copy(alpha = 0.2f)
        }
        else -> {
            MaterialTheme.colors.surface
        }
    }
    Column(
        modifier = modifier
            .padding(horizontal = 2.dp)
            .size(36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColorFor(backgroundColor)) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(backgroundColor, CircleShape)
            ) {
                Text(
                    "$number",
                    fontSize = 14.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.W400,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            if (!isSelected && !isToday) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(
                        2.dp,
                        Alignment.CenterHorizontally
                    ),
                    modifier = Modifier
                        .height(6.dp)
                        .alpha(if (events.isNotEmpty()) 1f else 0f)
                ) {
                    events.take(3).forEach { event ->
                        Box(
                            Modifier
                                .size(4.dp)
                                .background(event.color, CircleShape)
                        )
                    }
                }
            }
        }
    }
}

private data class CalendarScope(
    val firstDayOfWeek: Int = Calendar.SUNDAY
) {
    fun CalendarDay.toCalendar(): Calendar = toCalendar(this@CalendarScope.firstDayOfWeek)
}

fun CalendarDay.toCalendar(
    firstDayOfWeek: Int
): Calendar = Calendar.getInstance().apply {
    set(year, month, day)
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
    setFirstDayOfWeek(firstDayOfWeek)
}

// returns list of calendardays in given month.
// Second item in the pair is the day of the week from Calendar.
@OptIn(ExperimentalStdlibApi::class)
private fun CalendarScope.getDaysInMonth(monthIndex: Int): List<Pair<CalendarDay, Int>> {
    val calendar =
        CalendarDay(day = 1, month = monthIndex % 12, year = monthIndex / 12).toCalendar()
    calendar.goBackToFirstFirstDayOfWeek()
    return buildList {
        while(calendar.get(Calendar.MONTH) <= monthIndex % 12) {
            repeat(7) {
                add(calendar.toCalendarDay() to calendar.get(Calendar.DAY_OF_WEEK))
                calendar.add(Calendar.DATE, 1)
            }
        }
    }
}

fun Calendar.goBackToFirstFirstDayOfWeek() = apply {
    while (get(Calendar.DAY_OF_WEEK) != firstDayOfWeek) {
        add(Calendar.DATE, -1)
    }
}

@OptIn(ExperimentalStdlibApi::class)
private fun CalendarScope.daysOfWeek(format: String): List<String> {
    val calendar = Calendar.getInstance().apply {
        firstDayOfWeek = this@daysOfWeek.firstDayOfWeek
    }
    val simpleDateFormat = SimpleDateFormat(format, Locale.getDefault())
    calendar.set(Calendar.DAY_OF_WEEK, 1)
    return buildList {
        repeat(7) {
            add(simpleDateFormat.format(calendar.asDate))
            calendar.add(Calendar.DATE, 1)
        }
    }
}

data class CalendarDay(
    val day: Int,
    val month: Int,
    val year: Int
) {
    companion object {
        fun create(): CalendarDay = with(Calendar.getInstance()) {
            CalendarDay(
                day = get(Calendar.DAY_OF_MONTH),
                month = get(Calendar.MONTH),
                year = get(Calendar.YEAR)
            )
        }
    }
}

val CalendarDay.monthIndex: Int
    get() = month + year * 12

fun Calendar.toCalendarDay(): CalendarDay = CalendarDay(
    day = get(Calendar.DAY_OF_MONTH),
    month = get(Calendar.MONTH),
    year = get(Calendar.YEAR),
)

val Calendar.asDate
    get() = Date(timeInMillis)

fun Set<CalendarEvent>.filterByDay(calendarDay: CalendarDay) = filterNot {
    it.startDate.after(calendarDay.asCalendar.apply { add(Calendar.DATE, 1) }.time) ||
        it.endDate.before(calendarDay.asCalendar.time)
}

val CalendarDay.asCalendar
    get() = toCalendar(Calendar.SUNDAY)