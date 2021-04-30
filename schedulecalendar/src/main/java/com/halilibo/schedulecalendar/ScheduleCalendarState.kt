package com.halilibo.schedulecalendar

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.roundToLong

@Composable
fun rememberScheduleCalendarState(
    referenceDateTime: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS),
    onDateTimeSelected: (LocalDateTime) -> Unit = {}
): ScheduleCalendarState {
    val coroutineScope = rememberCoroutineScope()
    return remember(coroutineScope) {
        ScheduleCalendarState(
            referenceDateTime = referenceDateTime,
            onDateTimeSelected = onDateTimeSelected,
            coroutineScope = coroutineScope,
        )
    }
}

class ScheduleCalendarState(
    referenceDateTime: LocalDateTime,
    private val onDateTimeSelected: (LocalDateTime) -> Unit,
    private val coroutineScope: CoroutineScope
) {
    val startDateTime: LocalDateTime by derivedStateOf {
        referenceDateTime.plusSeconds(secondsOffset.value)
    }

    val endDateTime: LocalDateTime by derivedStateOf {
        startDateTime.plusSeconds(this.viewSpanSeconds.value)
    }

    private val viewSpanSeconds = Animatable(ChronoUnit.DAYS.duration.seconds, LongToVector)
    private val secondsOffset = Animatable(0L, LongToVector)
    private val width = mutableStateOf(1)

    internal fun updateView(newViewSpanSeconds: Long, newWidth: Int) {
        this.width.value = newWidth
        val currentViewSpanSeconds = viewSpanSeconds.value
        coroutineScope.launch {
            viewSpanSeconds.animateTo(newViewSpanSeconds)
        }
        coroutineScope.launch {
            if (newViewSpanSeconds != currentViewSpanSeconds) {
                updateAnchors(newViewSpanSeconds)
            }
        }
    }

    internal val scrollableState = ScrollableState {
        coroutineScope.launch {
            secondsOffset.snapTo(secondsOffset.value - it.toSeconds())
        }
        it
    }

    private val secondsInPx by derivedStateOf {
        this.viewSpanSeconds.value.toFloat() / width.value.toFloat()
    }

    private fun Float.toSeconds(): Long {
        return (this * secondsInPx).roundToLong()
    }

    private fun Long.toPx(): Float {
        return this / secondsInPx
    }

    internal val scrollFlingBehavior = object : FlingBehavior {
        val decay = exponentialDecay<Float>(frictionMultiplier = 2f)
        override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
            val endPosition = decay.calculateTargetValue(0f, -initialVelocity)
            flingToNearestAnchor(secondsOffset.value.toPx() + endPosition)
            return 0f
        }
    }

    internal val visibleHours by derivedStateOf {
        val startHour = startDateTime.truncatedTo(ChronoUnit.HOURS)
        val endHour = endDateTime.truncatedTo(ChronoUnit.HOURS).plusHours(1)

        startHour.between(endHour) { plusHours(1L) }.filter {
            it.hour % (anchorRangeSeconds / 3600) == 0L && it.hour != 0
        }.toList()
    }

    private var anchorRangeSeconds by mutableStateOf(Long.MAX_VALUE)
    private var anchorRangePx by mutableStateOf(Float.MAX_VALUE)
    private suspend fun updateAnchors(viewSpanInSeconds: Long) {
        anchorRangeSeconds = when(viewSpanInSeconds) {
            in (24*3600L+1..Long.MAX_VALUE) -> 24 * 3600L
            in (12*3600L+1..24*3600L) -> 6 * 3600L
            in (6*3600L+1..12*3600L) -> 3 * 3600L
            in (3*3600L+1..6*3600L) -> 2 * 3600L
            else -> 3600L
        }
        anchorRangePx = anchorRangeSeconds.toPx()
        flingToNearestAnchor(secondsOffset.value.toPx())
    }

    private suspend fun flingToNearestAnchor(target: Float) {
        val nearestAnchor = target - (target.absRem(anchorRangePx))
        val nearestAnchor2 = nearestAnchor + anchorRangePx

        val newAnchoredPosition = (abs(target - nearestAnchor) to abs(target - nearestAnchor2)).let {
            if (it.first > it.second) nearestAnchor2 else nearestAnchor
        }
        secondsOffset.animateTo(newAnchoredPosition.toSeconds())
        onDateTimeSelected(startDateTime)
    }

    internal fun offsetFraction(localDateTime: LocalDateTime): Float {
        return ChronoUnit.SECONDS.between(startDateTime, localDateTime).toFloat() / (viewSpanSeconds.value).toFloat()
    }

    internal fun widthAndOffsetForEvent(
        start: LocalDateTime, end: LocalDateTime, totalWidth: Int
    ): Pair<Int, Int> {
        val startOffsetPercent = offsetFraction(start).coerceIn(0f, 1f)
        val endOffsetPercent = offsetFraction(end).coerceIn(0f, 1f)

        val width = ((endOffsetPercent - startOffsetPercent) * totalWidth).toInt() + 1
        val offsetX = (startOffsetPercent * totalWidth).toInt()
        return width to offsetX
    }
}

private val LongToVector: TwoWayConverter<Long, AnimationVector1D> =
    TwoWayConverter({ AnimationVector1D(it.toFloat()) }, { it.value.toLong() })

private fun Float.absRem(modular: Float): Float {
    return ((this % modular) + modular) % modular
}