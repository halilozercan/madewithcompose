package com.halilibo.madewithcompose.circlesonlines

import androidx.compose.animation.core.*
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
    val revealOrderAnimation = remember { Animatable(initialValue = 0, Int.VectorConverter) }
    LaunchedEffect(isStarted) {
        revealOrderAnimation.animateTo(
            targetValue = if (isStarted) 8 else 0,
            animationSpec = tween(16000, easing = LinearEasing)
        )
    }

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
                val alpha by animateFloatAsState(
                    if (DELAY_ORDER.take(revealOrderAnimation.value).contains(index)) 1f else 0f,
                    animationSpec = tween(1000)
                )
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
    val animatedValue = remember { Animatable(initialValue = -1f) }
    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        animatedValue.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = PERIOD, easing = EASING),
                repeatMode = RepeatMode.Reverse
            )
        )
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
val DELAY_ORDER = listOf(0, 4, 2, 6, 1, 5, 3, 7)