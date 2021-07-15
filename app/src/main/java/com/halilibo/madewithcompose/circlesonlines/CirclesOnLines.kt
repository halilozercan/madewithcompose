package com.halilibo.madewithcompose.circlesonlines

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CirclesOnLinesDemo() {
    var isStarted by remember { mutableStateOf(false) }
    var linesVisible by remember { mutableStateOf(true) }
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)) {
        Row {
            if (!isStarted) {
                Button(onClick = {
                    isStarted = true
                }, modifier = Modifier.padding(16.dp)) {
                    Text("Start")
                }
            }
            if (isStarted) {
                Button(onClick = {
                    linesVisible = !linesVisible
                }, modifier = Modifier.padding(16.dp)) {
                    Text("${if (linesVisible) "Hide" else "Show"} Lines")
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(CircleShape)
                .background(Color.Red)
        ) {
            repeat(8) { index ->
                val angle = PI.toFloat() / 8 * index
                var isVisible by remember { mutableStateOf(0f) }
                val alpha by animateFloatAsState(isVisible, animationSpec = tween(1000))
                LaunchedEffect(isStarted) {
                    if (isStarted) {
                        val periodMillis = PERIOD.toLong()
                        delay(DELAY_MAP[index]!! * periodMillis)
                        isVisible = 1f
                    }
                }
                LineOscillator(
                    angle = angle,
                    delayMillis = PERIOD + index * PERIOD / 8,
                    lineVisible = linesVisible,
                    modifier = Modifier.alpha(alpha)
                )
            }
        }
    }
}

@Composable
fun LineOscillator(
    angle: Float,
    delayMillis: Int,
    lineVisible: Boolean,
    modifier: Modifier
) {
    val animatedValue = remember { Animatable(-1f) }

    LaunchedEffect(delayMillis, angle) {
        delay(delayMillis.toLong())
        while (isActive) {
            animatedValue.animateTo(1f, tween(PERIOD, easing = EASING))
            animatedValue.animateTo(-1f, tween(PERIOD, easing = EASING))
        }
    }

    Canvas(modifier.fillMaxSize()) {
        val circleRadius = 16.dp.toPx()
        drawCircle(
            Color.White,
            radius = circleRadius,
            center = Offset(
                (1 + animatedValue.value * cos(angle)) * (size.width / 2 - circleRadius) + circleRadius,
                (1 + animatedValue.value * sin(angle)) * (size.width / 2 - circleRadius) + circleRadius
            )
        )
        if (lineVisible) {
            drawLine(
                Color.Black,
                start = Offset(
                    ((1 + cos(angle)) * size.width / 2),
                    (1 + sin(angle)) * size.width / 2
                ),
                end = Offset(
                    ((1 - cos(angle)) * size.width / 2),
                    (1 - sin(angle)) * size.width / 2
                ),
                strokeWidth = 4f,
            )
        }
    }
}

const val PERIOD = 2400
val EASING = CubicBezierEasing(0.375f, 0f, 0.6f, 1f)
val DELAY_MAP = mapOf(
    0 to 0,
    1 to 4,
    2 to 2,
    3 to 5,
    4 to 1,
    5 to 6,
    6 to 3,
    7 to 7
)