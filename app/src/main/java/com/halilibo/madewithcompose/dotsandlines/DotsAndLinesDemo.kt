package com.halilibo.madewithcompose.dotsandlines


import android.os.Parcelable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.halilibo.dotsandlines.dotsAndLines
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DotsAndLinesDemo() {
    var dotsAndLinesConfig by rememberSaveable { mutableStateOf(DotsAndLinesConfig()) }
    val scaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetBackgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.5f),
        sheetContentColor = MaterialTheme.colors.onSurface,
        sheetElevation = 0.dp,
        sheetGesturesEnabled = false,
        sheetContent = {
            DotsAndLinesSliderRow(
                title = "Connectivity",
                value = dotsAndLinesConfig.threshold,
                valueRange = 0f..0.2f,
                onValueChanged = {
                    dotsAndLinesConfig = dotsAndLinesConfig.copy(threshold = it)
                }
            )
            DotsAndLinesSliderRow(
                title = "Line Thickness",
                value = dotsAndLinesConfig.maxThickness,
                valueRange = 2f..20f,
                onValueChanged = {
                    dotsAndLinesConfig = dotsAndLinesConfig.copy(maxThickness = it)
                }
            )
            DotsAndLinesSliderRow(
                title = "Dot Size",
                value = dotsAndLinesConfig.dotRadius,
                valueRange = 2f..20f,
                onValueChanged = {
                    dotsAndLinesConfig = dotsAndLinesConfig.copy(dotRadius = it)
                }
            )
            DotsAndLinesSliderRow(
                title = "Speed",
                value = dotsAndLinesConfig.speedCoefficient,
                valueRange = 0.001f..0.1f,
                onValueChanged = {
                    dotsAndLinesConfig = dotsAndLinesConfig.copy(speedCoefficient = it)
                }
            )
            DotsAndLinesSliderRow(
                title = "Density",
                value = dotsAndLinesConfig.population,
                valueRange = 0.1f..2f,
                onValueChanged = {
                    dotsAndLinesConfig = dotsAndLinesConfig.copy(population = it)
                }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { coroutineScope.launch { scaffoldState.bottomSheetState.collapse() } },
                    modifier = Modifier
                        .padding(8.dp)
                ) {
                    Text("Done")
                }
            }
        }
    ) {
        with(dotsAndLinesConfig) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.surface)
                    .dotsAndLines(
                        contentColor = MaterialTheme.colors.onSurface,
                        threshold = threshold,
                        maxThickness = maxThickness,
                        dotRadius = dotRadius,
                        speed = speedCoefficient,
                        populationFactor = population
                    )
            ) {
                if (scaffoldState.bottomSheetState.isCollapsed && !scaffoldState.bottomSheetState.isAnimationRunning) {
                    Button(
                        onClick = { coroutineScope.launch { scaffoldState.bottomSheetState.expand() } },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                    ) {
                        Text("Options")
                    }
                }
            }
        }
    }
}

@Composable
fun DotsAndLinesSliderRow(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChanged: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        Text("$title: $value")
        Slider(
            value = value,
            valueRange = valueRange,
            onValueChange = onValueChanged
        )
    }
}

@Parcelize
data class DotsAndLinesConfig(
    val threshold: Float = 0.06f,
    val maxThickness: Float = 6f,
    val dotRadius: Float = 4f,
    val speedCoefficient: Float = 0.05f,
    val population: Float = 0.3f // per 100^2 pixels
) : Parcelable