package com.halilibo.dotsandlines

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
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
) = composed {
    var dotsAndLinesState by rememberSaveable {
        mutableStateOf(
            DotsAndLinesState(
                dotRadius = dotRadius,
                speed = speed
            )
        )
    }

    LaunchedEffect(speed, dotRadius, populationFactor) {
        dotsAndLinesState = dotsAndLinesState.copy(
            speed = speed,
            dotRadius = dotRadius
        ).populationControl(populationFactor)
    }

    LaunchedEffect(Unit) {
        var lastFrame = 0L
        while (isActive) {
            val nextFrame = awaitFrame() / 100_000L
            if (lastFrame != 0L) {
                val period = nextFrame - lastFrame
                dotsAndLinesState = dotsAndLinesState.next(period)
            }
            lastFrame = nextFrame
        }
    }

    onSizeChanged {
        dotsAndLinesState = dotsAndLinesState.sizeChanged(
            size = it,
            populationFactor = populationFactor
        )
    }.drawBehind {
        dotsAndLinesState.dots.forEach {
            drawCircle(contentColor, radius = dotRadius, center = it.position)
        }

        val realThreshold = threshold * sqrt(size.width.pow(2) + size.height.pow(2))

        dotsAndLinesState.dots.nestedForEach { first, second ->
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

private fun <T> List<T>.nestedForEach(block: (T, T) -> Unit) {
    for (i in this.indices) {
        for (j in i + 1 until this.size) {
            block(this[i], this[j])
        }
    }
}