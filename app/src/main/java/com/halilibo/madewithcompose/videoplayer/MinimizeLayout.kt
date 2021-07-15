package com.halilibo.madewithcompose.videoplayer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp

/**
 * Possible values of [MinimizeLayoutState].
 */
enum class MinimizeLayoutValue {
    Expanded,
    Minimized
}

@OptIn(ExperimentalMaterialApi::class)
class MinimizeLayoutState(
    initialValue: MinimizeLayoutValue,
    animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec,
    confirmStateChange: (MinimizeLayoutValue) -> Boolean = { true },
    initialHidden: Boolean = true
) : SwipeableState<MinimizeLayoutValue>(
    initialValue = initialValue,
    animationSpec = animationSpec,
    confirmStateChange = confirmStateChange
) {
    internal val hiddenOffset = Animatable(if (initialHidden) 1f else 0f)

    suspend fun expand() {
        hiddenOffset.snapTo(0f)
        animateTo(MinimizeLayoutValue.Expanded)
    }

    suspend fun minimize() {
        hiddenOffset.snapTo(0f)
        animateTo(MinimizeLayoutValue.Minimized)
    }

    suspend fun hide() {
        hiddenOffset.animateTo(1f)
    }

    val isHidden: Boolean
        get() = hiddenOffset.value == 1f

    // There is a bug in swipeableState.progress in which an exception
    // is thrown when progress reaches completion
    val swipeProgress: SwipeProgress<MinimizeLayoutValue>
        get() = runCatching { progress }
            .getOrElse {
                SwipeProgress(
                    MinimizeLayoutValue.Expanded,
                    MinimizeLayoutValue.Expanded,
                    1f
                )
            }

    companion object {

        fun Saver(
            animationSpec: AnimationSpec<Float>,
            confirmStateChange: (MinimizeLayoutValue) -> Boolean
        ): Saver<MinimizeLayoutState, *> = Saver(
            save = { it.currentValue },
            restore = {
                MinimizeLayoutState(
                    initialValue = it,
                    animationSpec = animationSpec,
                    confirmStateChange = confirmStateChange
                )
            }
        )
    }
}

/**
 * Create a [MinimizeLayoutState] and [remember] it.
 *
 * @param initialValue The initial value of the state.
 * @param animationSpec The default animation that will be used to animate to a new state.
 * @param confirmStateChange Optional callback invoked to confirm or veto a pending state change.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun rememberMinimizeLayoutState(
    initialValue: MinimizeLayoutValue,
    animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec,
    confirmStateChange: (MinimizeLayoutValue) -> Boolean = { true }
): MinimizeLayoutState {
    return rememberSaveable(
        saver = MinimizeLayoutState.Saver(
            animationSpec = animationSpec,
            confirmStateChange = confirmStateChange
        )
    ) {
        MinimizeLayoutState(
            initialValue = initialValue,
            animationSpec = animationSpec,
            confirmStateChange = confirmStateChange
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MinimizeLayout(
    minimizableContent: @Composable (swipeableModifier: Modifier) -> Unit,
    modifier: Modifier = Modifier,
    minimizeLayoutState: MinimizeLayoutState =
        rememberMinimizeLayoutState(MinimizeLayoutValue.Minimized),
    minimizedContentHeight: Density.(Dp) -> Dp = { it / 10 },
    content: @Composable (PaddingValues) -> Unit
) {
    val minimizedContentHeightState by rememberUpdatedState(minimizedContentHeight)
    val density = LocalDensity.current
    MinimizeStack(
        modifier = modifier,
        hideOffsetFraction = minimizeLayoutState.hiddenOffset.value,
        minimizableContent = { constraints ->
            val fullHeight = constraints.maxHeight.toFloat()
            val minimizedHeight by animateFloatAsState(targetValue = with(density) {
                minimizedContentHeightState(fullHeight.toDp()).toPx()
            })
            val anchors = mapOf(
                fullHeight - minimizedHeight to MinimizeLayoutValue.Minimized,
                0f to MinimizeLayoutValue.Expanded
            )
            val swipeable = Modifier.swipeable(
                state = minimizeLayoutState,
                anchors = anchors,
                orientation = Orientation.Vertical
            )

            val swipeOffset = minimizeLayoutState.offset.value
            val currentHeight = if (swipeOffset.isNaN()) {
                0f
            } else {
                (fullHeight - minimizeLayoutState.offset.value).coerceIn(minimizedHeight, fullHeight)
            }

            Box(modifier = Modifier
                .height(with(density) { currentHeight.toDp() })) {
                minimizableContent(swipeable)
            }
        },
        content = { bottomPadding ->
            Box(Modifier.fillMaxSize()) {
                content(PaddingValues(bottom = bottomPadding))
            }
        }
    )
}

@Composable
private fun MinimizeStack(
    modifier: Modifier,
    hideOffsetFraction: Float,
    minimizableContent: @Composable (constraints: Constraints) -> Unit,
    content: @Composable (bottomPadding: Dp) -> Unit
) {
    val density = LocalDensity.current
    SubcomposeLayout(modifier) { constraints ->
        val minimizablePlaceable =
            subcompose(MinimizeStackSlot.Minimizable) {
                minimizableContent(constraints)
            }.first().measure(constraints.copy(minWidth = 0, minHeight = 0))

        val animatedHideOffset = (minimizablePlaceable.height * hideOffsetFraction).toInt()

        val contentPlaceable =
            subcompose(MinimizeStackSlot.Main) {
                content(with(density) {
                    (minimizablePlaceable.height - animatedHideOffset).coerceAtLeast(0).toDp()
                })
            }
                .first().measure(constraints)

        val middleX = (contentPlaceable.width - minimizablePlaceable.width) / 2
        val bottomY = (contentPlaceable.height - minimizablePlaceable.height) + animatedHideOffset

        layout(contentPlaceable.width, contentPlaceable.height) {
            contentPlaceable.placeRelative(0, 0)
            minimizablePlaceable.placeRelative(middleX, bottomY)
        }
    }
}

private enum class MinimizeStackSlot { Minimizable, Main }
