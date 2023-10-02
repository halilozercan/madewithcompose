package com.halilibo.weightentry

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

val ScaleGray = Color(239, 239, 239)
val ScaleAccent = Color(119, 49, 255)

@Composable
fun WeightScale(
    weight: Int,
    valueRange: ClosedRange<Int>,
    modifier: Modifier
) {
    val animatedValue = remember { Animatable(weight, Int.VectorConverter) }
    LaunchedEffect(weight) {
        animatedValue.snapTo(weight)
    }

    Box(
        modifier = modifier
            .padding(16.dp)
            .weightScaleBackground(animatedValue.value, valueRange),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = buildAnnotatedString {
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                append("$weight")
                pop()
                append(" kg")
            },
            fontSize = 40.sp
        )
    }
}

private fun Modifier.weightScaleBackground(
    weight: Int,
    valueRange: ClosedRange<Int>,
) = drawBehind {
    val radius = size.width / 2
    val squareSize = Size(radius * 2, radius * 2)
    drawArc(
        color = ScaleGray,
        startAngle = 165f,
        sweepAngle = 210f,
        useCenter = false,
        size = squareSize,
        style = Stroke(width = 12f, cap = StrokeCap.Round)
    )

    drawArc(
        color = ScaleAccent,
        startAngle = -105f,
        sweepAngle = 30f,
        useCenter = true,
        topLeft = Offset(
            squareSize.width / 2 - squareSize.div(8f).width,
            (squareSize.height - squareSize.height / 1.1f) / 2
        ),
        size = squareSize.div(4f),
        style = Fill
    )

    drawArc(
        color = ScaleGray,
        startAngle = 165f,
        sweepAngle = 210f,
        useCenter = false,
        topLeft = Offset(
            (squareSize.width - squareSize.width / 1.1f) / 2,
            (squareSize.height - squareSize.height / 1.1f) / 2
        ),
        size = squareSize.div(1.1f),
        style = Stroke(width = 20f, cap = StrokeCap.Round)
    )

    ((0..14) - 7).forEach { index ->
        val alpha = (165f + index * 15f) / 180f * PI.toFloat()
        drawLine(
            color = ScaleGray,
            start = Offset(
                x = radius + (radius / 1.12f) * cos(alpha),
                y = radius + (radius / 1.12f) * sin(alpha),
            ),
            end = Offset(
                x = radius + (radius / 1.3f) * cos(alpha),
                y = radius + (radius / 1.3f) * sin(alpha),
            ),
            strokeWidth = 20f,
            cap = StrokeCap.Round
        )
    }

    drawArc(
        color = ScaleAccent,
        startAngle = 165f,
        sweepAngle = (210f / (valueRange.endInclusive - valueRange.start)) * (weight - valueRange.start),
        useCenter = false,
        topLeft = Offset(
            (squareSize.width - squareSize.width / 1.1f) / 2,
            (squareSize.height - squareSize.height / 1.1f) / 2
        ),
        size = squareSize.div(1.1f),
        style = Stroke(width = 30f, cap = StrokeCap.Round)
    )
}