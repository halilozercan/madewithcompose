package com.halilibo.schedulecalendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halilibo.colors.G500
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*


@Composable
fun ScheduleCalendar(
    state: ScheduleCalendarState,
    modifier: Modifier = Modifier,
    viewSpan: Long = 48 * 3600L, // in seconds
    sections: List<CalendarSection> = emptyList(),
    now: LocalDateTime = LocalDateTime.now(),
    eventTimesVisible: Boolean = true
) = BoxWithConstraints(
    modifier
        .fillMaxWidth()
        .scrollable(
            state.scrollableState,
            Orientation.Horizontal,
            flingBehavior = state.scrollFlingBehavior
        )
) {
    state.updateView(viewSpan, constraints.maxWidth)

    Column {
        DaysRow(
            state = state,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        HoursRow(state)

        Box(modifier = Modifier.fillMaxWidth()) {
            Column {
                sections.forEach {
                    CalendarSectionRow(
                        section = it,
                        state = state,
                        eventTimesVisible = eventTimesVisible
                    )
                }
            }

            // hour dividers
            Canvas(modifier = Modifier.matchParentSize()) {
                state.visibleHours.forEach { localDateTime ->
                    val offsetPercent = state.offsetFraction(localDateTime)
                    drawLine(
                        color = Color.Gray,
                        strokeWidth = 2f,
                        start = Offset(offsetPercent * size.width, 0f),
                        end = Offset(offsetPercent * size.width, size.height),
                        pathEffect = PathEffect.dashPathEffect(
                            intervals = floatArrayOf(10f, 20f),
                            phase = 5f
                        )
                    )
                }
            }
        }
    }

    DayDividers(
        state = state,
        modifier = Modifier.matchParentSize()
    )

    // "now" indicator
    Canvas(modifier = Modifier.matchParentSize()) {
        val offsetPercent = state.offsetFraction(now)
        drawLine(
            color = Color.Magenta,
            strokeWidth = 6f,
            start = Offset(offsetPercent * size.width, 0f),
            end = Offset(offsetPercent * size.width, size.height)
        )
        drawCircle(
            Color.Magenta,
            center = Offset(offsetPercent * size.width, 12f),
            radius = 12f
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CalendarSectionRow(
    section: CalendarSection,
    state: ScheduleCalendarState,
    eventTimesVisible: Boolean
) {
    Column(Modifier.animateContentSize()) {
        val eventMap = section.events.map { event ->
            Triple(
                event,
                event.startDate.isAfter(state.startDateTime) && event.startDate.isBefore(state.endDateTime),
                event.endDate.isAfter(state.startDateTime) && event.endDate.isBefore(state.endDateTime),
            )
        }.filter { (event, startHit, endHit) ->
            startHit || endHit || (event.startDate.isBefore(state.startDateTime) && event.endDate.isAfter(state.endDateTime))
        }

        Text(
            text = section.name,
            fontSize = 12.sp,
            fontWeight = W500,
            modifier = Modifier.padding(4.dp)
        )
        if (eventMap.isNotEmpty()) {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                eventMap.forEach { (event, startHit, endHit) ->
                    val (width, offsetX) = state.widthAndOffsetForEvent(
                        start = event.startDate,
                        end = event.endDate,
                        totalWidth = constraints.maxWidth
                    )

                    val shape = when {
                        startHit && endHit -> RoundedCornerShape(4.dp)
                        startHit -> RoundedCornerShape(
                            topStart = 4.dp,
                            bottomStart = 4.dp
                        )
                        endHit -> RoundedCornerShape(
                            topEnd = 4.dp,
                            bottomEnd = 4.dp
                        )
                        else -> RoundedCornerShape(4.dp)
                    }

                    Column(modifier = Modifier
                        .padding(vertical = 8.dp)
                        .width(with(LocalDensity.current) { width.toDp() })
                        .offset { IntOffset(offsetX, 0) }
                        .background(event.color, shape = shape)
                        .clip(shape)
                        .clickable { }
                        .padding(4.dp)
                    ) {
                        Text(
                            text = event.name,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        AnimatedVisibility(visible = eventTimesVisible) {
                            Text(
                                text = event.startDate.format(DateTimeFormatter.ofPattern("HH:mm")) + " - " +
                                    event.endDate.format(DateTimeFormatter.ofPattern("HH:mm")),
                                fontSize = 12.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun DaysRow(
    state: ScheduleCalendarState,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        (state.startDateTime daysBetween state.endDateTime).forEach { localDateTime ->
            val (width, offsetX) = state.widthAndOffsetForEvent(
                start = localDateTime,
                end = localDateTime.plusDays(1),
                totalWidth = constraints.maxWidth
            )
            Column(modifier = Modifier
                .width(with(LocalDensity.current) { width.toDp() })
                .offset { IntOffset(offsetX, 0) }
                .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = localDateTime.format(DateTimeFormatter.ofPattern("MMM, dd")),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun HoursRow(
    state: ScheduleCalendarState
) {
    AnimatedVisibility(visible = state.visibleHours.isNotEmpty()) {
        Layout(
            content = {
                state.visibleHours.forEach { localDateTime ->
                    Text(
                        localDateTime.format(DateTimeFormatter.ofPattern("hh a")),
                        fontSize = 12.sp,
                        modifier = Modifier.then(
                            LocalDateTimeData(localDateTime)
                        )
                    )
                }
            }
        ) { measurables, constraints ->
            val placeables = measurables.map { it.measure(constraints) to it.localDateTime }

            val width = constraints.maxWidth
            val height = if (placeables.isNotEmpty()) { placeables.maxOf { it.first.height } } else { 0 }
            layout(width, height) {
                placeables.forEach { (placeable, localDateTime) ->
                    val origin = state.offsetFraction(localDateTime) * width
                    val x = origin.toInt() - placeable.width / 2
                    placeable.placeRelative(x.coerceAtLeast(0), 0)
                }
            }
        }
    }
}

@Composable
fun DayDividers(
    state: ScheduleCalendarState,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        (state.startDateTime daysBetween state.endDateTime).forEach { localDateTime ->
            val offsetPercent = state.offsetFraction(localDateTime)
            drawLine(
                color = Color.Gray,
                strokeWidth = 4f,
                start = Offset(offsetPercent * size.width, 0f),
                end = Offset(offsetPercent * size.width, size.height),
                pathEffect = PathEffect.dashPathEffect(
                    intervals = floatArrayOf(10f, 20f),
                    phase = 5f
                )
            )
        }
    }
}

data class CalendarSection(
    val name: String,
    val events: List<CalendarEvent>
)

data class CalendarEvent(
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val name: String = "",
    val description: String = "",
    val color: Color = G500
)

private data class LocalDateTimeData(
    val localDateTime: LocalDateTime,
) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?) = this@LocalDateTimeData
}

private val Measurable.localDateTime: LocalDateTime
    get() = (parentData as? LocalDateTimeData)?.localDateTime ?: error("No LocalDateTime for measurable $this")

fun LocalDateTime.between(
    target: LocalDateTime,
    increment: LocalDateTime.() -> LocalDateTime
): Sequence<LocalDateTime> {
    return generateSequence(
        seed = this,
        nextFunction = {
            val next = it.increment()
            if (next.isBefore(target)) next else null
        }
    )
}

infix fun LocalDateTime.daysBetween(target: LocalDateTime): Sequence<LocalDateTime> {
    val start = truncatedTo(ChronoUnit.DAYS)
    return start.between(target.truncatedTo(ChronoUnit.DAYS).plusDays(1)) {
        plusDays(1)
    }
}