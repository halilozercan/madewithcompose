package com.halilibo.dotsandlines

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.halilibo.dotsandlines.Dot.Companion.distanceTo
import com.halilibo.dotsandlines.DotsAndLinesState.Companion.next
import com.halilibo.dotsandlines.DotsAndLinesState.Companion.populationControl
import com.halilibo.dotsandlines.DotsAndLinesState.Companion.sizeChanged
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.isActive
import kotlin.math.pow
import kotlin.math.sqrt

fun Modifier.dotsAndLines(
    contentColor: Color = Color.White,
    threshold: Float,
    maxThickness: Float,
    dotRadius: Float,
    speed: Float,
    populationFactor: Float
) = this.composed {
    val dotsAndLinesModel = remember {
        DotsAndLinesModel(
            DotsAndLinesState(
                dotRadius = dotRadius,
                speed = speed
            )
        )
    }

    LaunchedEffect(speed, dotRadius, populationFactor) {
        dotsAndLinesModel.populationControl(speed, dotRadius, populationFactor)
    }

    LaunchedEffect(Unit) {
        var lastFrame = 0L
        while (isActive) {
            val nextFrame = awaitFrame() / 100_000L
            if (lastFrame != 0L) {
                val period = nextFrame - lastFrame
                dotsAndLinesModel.next(period)
            }
            lastFrame = nextFrame
        }
    }

    pointerInput(Unit) {
        detectDragGestures(
            onDragStart = { offset ->
                dotsAndLinesModel.pointerDown(offset)
            },
            onDragEnd = {
                dotsAndLinesModel.pointerUp()
            },
            onDragCancel = {
                dotsAndLinesModel.pointerUp()
            },
            onDrag = { change, dragAmount ->
                dotsAndLinesModel.pointerMove(dragAmount)
                change.consume()
            }
        )
    }
        .onSizeChanged {
            dotsAndLinesModel.sizeChanged(it, populationFactor)
        }
        .drawBehind {
            val allDots = with(dotsAndLinesModel.dotsAndLinesState) { (dots + pointer).filterNotNull() }

            allDots.forEach {
                drawCircle(contentColor, radius = dotRadius, center = it.position)
            }

            val realThreshold = threshold * sqrt(size.width.pow(2) + size.height.pow(2))

            allDots.nestedForEach { first, second ->
                val distance = first distanceTo second

                if (distance <= realThreshold) {
                    drawLine(
                        contentColor,
                        first.position,
                        second.position,
                        0.5f + (realThreshold - distance) * maxThickness / realThreshold
                    )
                }
            }
        }
}

@Immutable
class DotsAndLinesModel(
    initialDotsAndLinesState: DotsAndLinesState
) {
    var dotsAndLinesState by mutableStateOf(initialDotsAndLinesState)

    fun populationControl(
        speed: Float,
        dotRadius: Float,
        populationFactor: Float
    ) {
        dotsAndLinesState = dotsAndLinesState.copy(
            speed = speed,
            dotRadius = dotRadius
        ).populationControl(populationFactor)
    }

    fun next(period: Long) {
        dotsAndLinesState = dotsAndLinesState.next(period)
    }

    fun sizeChanged(size: IntSize, populationFactor: Float) {
        dotsAndLinesState = dotsAndLinesState.sizeChanged(
            size = size,
            populationFactor = populationFactor
        )
    }

    fun pointerDown(offset: Offset) {
        dotsAndLinesState = dotsAndLinesState.copy(
            pointer = Dot(
                position = offset,
                vector = Offset.Zero
            )
        )
    }

    fun pointerMove(offset: Offset) {
        val currentPointer = dotsAndLinesState.pointer ?: return

        dotsAndLinesState = dotsAndLinesState.copy(
            pointer = dotsAndLinesState.pointer?.copy(
                position = currentPointer.position + offset,
                vector = Offset.Zero
            )
        )
    }

    fun pointerUp() {
        dotsAndLinesState = dotsAndLinesState.copy(pointer = null)
    }
}

private fun <T> List<T>.nestedForEach(block: (T, T) -> Unit) {
    for (i in this.indices) {
        for (j in i + 1 until this.size) {
            block(this[i], this[j])
        }
    }
}