@file:OptIn(ExperimentalFoundationApi::class, ExperimentalFoundationApi::class)

package com.halilibo.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.halilibo.colors.N100
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import kotlin.math.absoluteValue

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
    val pagerState = rememberPagerState(initialPage = visibleMonth.monthIndex) {
        Int.MAX_VALUE
    }

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
        modifier = modifier.background(MaterialTheme.colors.surface),
        verticalAlignment = Alignment.Top
    ) { index ->
        with(calendarScope) {
            CalendarPage(
                monthIndex = index,
                onDayClick = {
                    onVisibleMonthChange(CalendarDay(day = 1, month = it.month, year = it.year))
                    onSelectedDayChange(it)
                },
                selectedDay = selectedDay,
                today = today,
                events = events,
                modifier = Modifier.graphicsLayer {
                    // Calculate the absolute offset for the current page from the
                    // scroll position. We use the absolute value which allows us to mirror
                    // any effects for both directions
                    val currentOffsetForPage = (pagerState.currentPage - index) +
                        pagerState.currentPageOffsetFraction
                    val pageOffset = currentOffsetForPage.absoluteValue

                    // We animate the scaleX + scaleY, between 85% and 100%
                    lerp(
                        start = 0.85f,
                        stop = 1f,
                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                    ).also { scale ->
                        scaleX = scale
                        scaleY = scale
                    }

                    // We animate the alpha, between 50% and 100%
                    alpha = lerp(
                        start = 0.5f,
                        stop = 1f,
                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                    )
                }
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
    events: Set<CalendarEvent>,
    modifier: Modifier = Modifier
) {
    val daysInMonth = remember(monthIndex) { getDaysInMonth(monthIndex) }
    val daysOfWeek = remember { daysOfWeek("EEE") }

    Layout(
        modifier = modifier,
        content = {
            daysOfWeek.forEach { dayOfWeek ->
                Text(
                    text = dayOfWeek.take(1).toUpperCase(Locale.current),
                    fontSize = 12.sp,
                    color = N100,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            daysInMonth.forEach { (calendarDay, _) ->
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
        }
    ) { measurables, constraints ->
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
private fun CalendarDayView(
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
        repeat(6) { // 6 weeks at least to show a month
            repeat(7) { // 7 days every week
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
    val simpleDateFormat = SimpleDateFormat(format, java.util.Locale.getDefault())
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