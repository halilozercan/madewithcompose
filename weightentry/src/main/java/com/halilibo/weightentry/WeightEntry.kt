package com.halilibo.weightentry

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

@Composable
fun rememberWeightEntryState(
    initialValue: Int,
    valueRange: ClosedRange<Int>,
): WeightEntryState {
    val coroutineScope = rememberCoroutineScope()
    val state = remember(coroutineScope) {
        WeightEntryState(
            initialValue = initialValue.toFloat(),
            valueRange = valueRange.start.toFloat()..valueRange.endInclusive.toFloat(),
            coroutineScope = coroutineScope
        )
    }

    LaunchedEffect(initialValue, valueRange) {
        state.valueRange = valueRange.start.toFloat()..valueRange.endInclusive.toFloat()
        state.snapToValue(initialValue.toFloat())
    }
    return state
}

class WeightEntryState(
    initialValue: Float,
    var valueRange: ClosedFloatingPointRange<Float>,
    private val coroutineScope: CoroutineScope
) {
    private val animatedValue = Animatable(initialValue)

    val value: Float
        get() = animatedValue.value

    fun changeValueBy(number: Float) {
        coroutineScope.launch {
            animatedValue.snapTo((animatedValue.value - number).coerceIn(valueRange))
        }
    }

    fun roundValue() {
        coroutineScope.launch {
            animatedValue.animateTo(animatedValue.value.roundToInt().toFloat())
        }
    }

    fun snapToValue(newValue: Float) {
        coroutineScope.launch {
            animatedValue.snapTo(newValue)
        }
    }
}

@Composable
fun WeightEntry(
    state: WeightEntryState,
    modifier: Modifier
) {
    Column(modifier = modifier) {
        WeightNumbersRow(
            state = state,
            modifier = Modifier.pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        state.roundValue()
                    }
                ) { change, dragAmount ->
                    val scrolledValue = (dragAmount / size.width) * 5
                    state.changeValueBy(scrolledValue)
                    change.consumePositionChange()
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        WeightIndicatorsRow(
            state = state,
            modifier = Modifier.pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        state.roundValue()
                    }
                ) { change, dragAmount ->
                    val scrolledValue = (dragAmount / size.width) * 45
                    state.changeValueBy(scrolledValue)
                    change.consumePositionChange()
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        WeightSliderRow(
            state = state,
            onValueChange = { newValue ->
                state.snapToValue(newValue)
            },
            onValueChangeFinished = {
                state.roundValue()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun WeightNumbersRow(
    state: WeightEntryState,
    modifier: Modifier = Modifier
) {
    // Current weight always in the center
    // If current weight and the next weight is not sticky, they should be animated within
    // If current weight or the next weight is sticky, whole row should be animated
    // Each sticky text decides on its position according to current value

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
    ) {
        val itemWidthPx = constraints.maxWidth / 5f
        val currentValue = state.value

        ((currentValue.toInt() - 3)..(currentValue.toInt() + 3)).forEach { number ->
            val color = lerp(
                start = ScaleAccent,
                stop = Color.LightGray,
                fraction = abs(number - currentValue).coerceAtMost(1f)
            )
            val scale = ((2f - abs(number - currentValue).coerceAtMost(2f)).pow(2f) + 2f) / 6f
            Text(
                text = "$number",
                fontSize = 48.sp,
                color = color,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .alpha(if (number.toFloat() in state.valueRange) 1f else 0f)
                    .padding(4.dp)
                    .align(Alignment.Center)
                    .offset {
                        IntOffset(x = (itemWidthPx * (number - currentValue)).roundToInt(), y = 0)
                    }
                    .scale(scale)
            )
        }
    }
}

@Composable
private fun WeightIndicatorsRow(
    state: WeightEntryState,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        val itemWidth = maxWidth / 9f
        val itemWidthPx = (constraints.maxWidth / 9f) / 5
        val currentValue = state.value

        ((currentValue.toInt() - 30)..(currentValue.toInt() + 30))
            .filter { it % 5 == 0 }
            .forEach { number ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(itemWidth)
                        .alpha(if (number.toFloat() in state.valueRange) 1f else 0f)
                        .offset {
                            IntOffset(
                                x = (itemWidthPx * (number - currentValue)).roundToInt(),
                                y = 0
                            )
                        }
                ) {
                    val width = (30f - (abs(number - currentValue).coerceAtMost(30f))) / 10f
                    if (number % 10 == 0) {
                        Box(
                            modifier = Modifier
                                .width(width.dp)
                                .background(Color.Gray)
                                .height(24.dp)
                        )
                        Text(text = "$number")
                    } else {
                        Box(
                            modifier = Modifier
                                .width(width.dp)
                                .background(Color.Gray)
                                .height(12.dp)
                        )
                    }
                }
            }
    }
}

@Composable
private fun WeightSliderRow(
    state: WeightEntryState,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Slider(
        value = state.value,
        onValueChange = onValueChange,
        valueRange = state.valueRange,
        onValueChangeFinished = onValueChangeFinished,
        modifier = modifier
    )
}

@Preview
@Composable
fun WeightEntryPreview() {
    val state = rememberWeightEntryState(
        initialValue = 64,
        valueRange = 20..180
    )
    WeightEntry(
        state = state,
        modifier = Modifier.fillMaxWidth()
    )
}
