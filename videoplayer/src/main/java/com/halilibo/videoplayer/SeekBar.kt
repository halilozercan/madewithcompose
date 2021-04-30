package com.halilibo.videoplayer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.halilibo.videoplayer.util.getDurationString

@Composable
fun SeekBar(
    progress: Long,
    max: Long,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    secondaryProgress: Long? = null,
    onSeek: (progress: Long) -> Unit = {},
    onSeekStarted: (startedProgress: Long) -> Unit = {},
    onSeekStopped: (stoppedProgress: Long) -> Unit = {},
    seekerPopup: @Composable () -> Unit = {},
    showSeekerDuration: Boolean = true,
    color: Color = MaterialTheme.colors.primary,
    secondaryColor: Color = Color.White.copy(alpha = 0.6f)
) {
    // if there is an ongoing drag, only dragging progress is evaluated.
    // when dragging finishes, given [progress] continues to be used.
    var onGoingDrag by remember { mutableStateOf(false) }
    val indicatorSize = if (onGoingDrag) 24.dp else 16.dp

    BoxWithConstraints(modifier = modifier.offset(y = indicatorSize / 2)) {
        if (progress >= max) return@BoxWithConstraints

        val boxWidth = constraints.maxWidth.toFloat()

        val percentage = remember(progress, max) {
            progress.coerceAtMost(max).toFloat() / max.toFloat()
        }

        val indicatorOffsetByPercentage = remember(percentage) {
            Offset(percentage * boxWidth, 0f)
        }

        // Indicator should be at "percentage" but dragging can change that.
        // This state keeps track of current dragging position.
        var indicatorOffsetByDragState by remember { mutableStateOf(Offset.Zero) }

        val finalIndicatorOffset = remember(
            indicatorOffsetByDragState,
            indicatorOffsetByPercentage,
            onGoingDrag
        ) {
            val finalIndicatorPosition = if (onGoingDrag) {
                indicatorOffsetByDragState
            } else {
                indicatorOffsetByPercentage
            }
            finalIndicatorPosition.copy(x = finalIndicatorPosition.x.coerceIn(0f, boxWidth))
        }

        Column {

            // SEEK POPUP
            if (onGoingDrag) {
                var popupSize by remember { mutableStateOf(IntSize(0, 0)) }

                // popup seeker must center the actual seeker position. Therefore, we offset
                // it negatively to the left.
                val popupSeekerOffsetXDp = with(LocalDensity.current) {
                    (finalIndicatorOffset.x - popupSize.width / 2)
                        .coerceIn(0f, (boxWidth - popupSize.width))
                        .toDp()
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .offset(x = popupSeekerOffsetXDp)
                        .alpha(if (popupSize == IntSize.Zero) 0f else 1f)
                        .onGloballyPositioned {
                            if (popupSize != it.size) {
                                popupSize = it.size
                            }
                        }
                ) {
                    val indicatorProgressDurationString = getDurationString(
                        ((finalIndicatorOffset.x / boxWidth) * max).toLong(),
                        false
                    )

                    Box(modifier = Modifier.shadow(4.dp)) {
                        seekerPopup()
                    }

                    if(showSeekerDuration) {
                        Text(
                            text = indicatorProgressDurationString,
                            style = TextStyle(shadow = Shadow(
                                blurRadius = 8f,
                                offset = Offset(2f, 2f))
                            )
                        )
                    }
                }
            }

            Box(modifier = Modifier.height(indicatorSize)) {
                // SECONDARY PROGRESS
                if (secondaryProgress != null) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().align(Alignment.Center),
                        progress = secondaryProgress.coerceAtMost(max).toFloat() / max.coerceAtLeast(1L).toFloat(),
                        color = secondaryColor
                    )
                }

                // SEEK INDICATOR
                if (enabled) {
                    val (offsetDpX, offsetDpY) = with(LocalDensity.current) {
                        (finalIndicatorOffset.x).toDp() - indicatorSize / 2 to (finalIndicatorOffset.y).toDp()
                    }

                    val draggableState = rememberDraggableState(onDelta = { dx ->
                        indicatorOffsetByDragState = Offset(
                            x = (indicatorOffsetByDragState.x + dx),
                            y = indicatorOffsetByDragState.y
                        )

                        val currentProgress =
                            (indicatorOffsetByDragState.x / boxWidth) * max
                        onSeek(currentProgress.toLong())
                    })

                    Row(modifier = Modifier
                        .matchParentSize()
                        .draggable(
                            state = draggableState,
                            orientation = Orientation.Horizontal,
                            startDragImmediately = true,
                            onDragStarted = { downPosition ->
                                onGoingDrag = true
                                indicatorOffsetByDragState =
                                    indicatorOffsetByDragState.copy(x = downPosition.x)
                                val newProgress =
                                    (indicatorOffsetByDragState.x / boxWidth) * max
                                onSeekStarted(newProgress.toLong())
                            },
                            onDragStopped = {
                                val newProgress =
                                    (indicatorOffsetByDragState.x / boxWidth) * max
                                onSeekStopped(newProgress.toLong())
                                indicatorOffsetByDragState = Offset.Zero
                                onGoingDrag = false
                            }
                        )
                    ) {

                        Indicator(
                            modifier = Modifier
                                .offset(x = offsetDpX, y = offsetDpY)
                                .size(indicatorSize)
                        )
                    }
                }

                // MAIN PROGRESS
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().align(Alignment.Center),
                    progress = percentage,
                    color = color
                )
            }
        }
    }
}

@Composable
fun Indicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.primary,
) {
    Canvas(modifier = modifier) {
        val radius = size.height / 2
        drawCircle(color, radius)
    }
}