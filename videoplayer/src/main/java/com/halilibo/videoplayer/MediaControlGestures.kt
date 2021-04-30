package com.halilibo.videoplayer

import android.os.Parcelable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.halilibo.videoplayer.util.getDurationString
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun MediaControlGestures(
    modifier: Modifier = Modifier
) {
    val controller = LocalVideoPlayerController.current

    val controlsEnabled by controller.collect { controlsEnabled }
    val gesturesEnabled by controller.collect { gesturesEnabled }
    val controlsVisible by controller.collect { controlsVisible }
    val draggingProgress by controller.collect { draggingProgress }

    val quickSeekDirectionState = remember { mutableStateOf(QuickSeekDirection.None) }

    if (controlsEnabled && !controlsVisible && gesturesEnabled) {
        Box(modifier = modifier
            .fillMaxSize()
            .mediaDragAndTapGestures(quickSeekDirectionState)
        ) {
            DraggingProgressOverlay(draggingProgress)
            QuickSeekAnimation(quickSeekDirectionState.value) {
                quickSeekDirectionState.value = QuickSeekDirection.None
            }
        }
    }
}

fun Modifier.mediaDragAndTapGestures(
    quickSeekDirectionState: MutableState<QuickSeekDirection>
) = composed {
    val controller = LocalVideoPlayerController.current

    val coroutineScope = rememberCoroutineScope()

    pointerInput(controller) {
        var wasPlaying = true
        var totalOffset = Offset.Zero
        var diffTime: Float

        var duration: Long = 0
        var currentPosition: Long = 0

        // When this job completes, it seeks to desired position.
        // It gets cancelled if delay does not complete
        var seekJob: Job? = null

        fun resetState() {
            totalOffset = Offset.Zero
            controller.setDraggingProgress(null)
        }

        detectMediaPlayerGesture(
            onDoubleTap = { doubleTapPosition ->
                if (quickSeekDirectionState.value == QuickSeekDirection.None) {
                    when {
                        doubleTapPosition.x < size.width * 0.4f -> {
                            quickSeekDirectionState.value = QuickSeekDirection.Rewind
                            controller.quickSeekRewind()
                        }
                        doubleTapPosition.x > size.width * 0.6f -> {
                            quickSeekDirectionState.value = QuickSeekDirection.Forward
                            controller.quickSeekForward()
                        }
                    }
                }
            },
            onTap = {
                controller.showControls()
            },
            onDragStart = { offset ->
                wasPlaying = controller.currentState { it.isPlaying }
                controller.pause()

                currentPosition = controller.currentState { it.currentPosition }
                duration = controller.currentState { it.duration }

                resetState()
            },
            onDragEnd = {
                if (wasPlaying) controller.play()
                resetState()
            },
            onDrag = { dragAmount: Float ->
                seekJob?.cancel()

                totalOffset += Offset(x = dragAmount, y = 0f)

                val diff = totalOffset.x

                diffTime = if (duration <= 60_000) {
                    duration.toFloat() * diff / size.width.toFloat()
                } else {
                    60_000.toFloat() * diff / size.width.toFloat()
                }

                var finalTime = currentPosition + diffTime
                if (finalTime < 0) {
                    finalTime = 0f
                } else if (finalTime > duration) {
                    finalTime = duration.toFloat()
                }
                diffTime = finalTime - currentPosition

                controller.setDraggingProgress(
                    DraggingProgress(
                        finalTime = finalTime,
                        diffTime = diffTime
                    )
                )

                seekJob = coroutineScope.launch {
                    delay(200)
                    controller.seekTo(finalTime.toLong())
                }
            }
        )
    }
}

suspend fun PointerInputScope.detectMediaPlayerGesture(
    onTap: (Offset) -> Unit,
    onDoubleTap: (Offset) -> Unit,
    onDragStart: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDrag: (Float) -> Unit
) {
    coroutineScope {
        launch {
            detectHorizontalDragGestures(
                onDragStart = onDragStart,
                onDragEnd = onDragEnd,
                onHorizontalDrag = { change, dragAmount ->
                    onDrag(dragAmount)
                    change.consumePositionChange()
                },
            )
        }

        launch {
            detectTapGestures(
                onTap = onTap,
                onDoubleTap = onDoubleTap
            )
        }
    }
}

@Composable
fun BoxScope.QuickSeekAnimation(
    quickSeekDirection: QuickSeekDirection,
    onAnimationEnd: () -> Unit
) {
    val alphaRewind = remember { Animatable(0f) }
    val alphaForward = remember { Animatable(0f) }

    LaunchedEffect(quickSeekDirection) {
        when (quickSeekDirection) {
            QuickSeekDirection.Rewind -> alphaRewind
            QuickSeekDirection.Forward -> alphaForward
            else -> null
        }?.let { animatable ->
            animatable.animateTo(1f)
            animatable.animateTo(0f)
            onAnimationEnd()
        }
    }

    ShadowedIcon(
        Icons.Filled.FastRewind,
        modifier = Modifier
            .fillMaxWidth(.5f)
            .fillMaxHeight()
            .align(Alignment.CenterStart)
            .alpha(alphaRewind.value)
    )

    ShadowedIcon(
        Icons.Filled.FastForward,
        modifier = Modifier
            .fillMaxWidth(.5f)
            .fillMaxHeight()
            .align(Alignment.CenterEnd)
            .alpha(alphaForward.value)
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BoxScope.DraggingProgressOverlay(draggingProgress: DraggingProgress?) {
    AnimatedVisibility(
        visible = draggingProgress != null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.align(Alignment.Center)
    ) {
        Text(
            draggingProgress?.progressText ?: "",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            style = TextStyle(
                shadow = Shadow(
                    blurRadius = 8f,
                    offset = Offset(2f, 2f)
                )
            ),
        )
    }
}

@Parcelize
data class DraggingProgress(
    val finalTime: Float,
    val diffTime: Float
) : Parcelable {
    val progressText: String
        get() = "${getDurationString(finalTime.toLong(), false)} " +
            "[${if (diffTime < 0) "-" else "+"}${
                getDurationString(
                    abs(diffTime.toLong()),
                    false
                )
            }]"
}

enum class QuickSeekDirection {
    None,
    Rewind,
    Forward
}