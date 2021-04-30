package com.halilibo.madewithcompose.videoplayer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun VideoPlayerPage(
    videoPlayer: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
    minimizedContent: @Composable () -> Unit,
    swipeProgress: SwipeProgress<MinimizeLayoutValue>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = 16.dp,
        color = MaterialTheme.colors.surface,
        contentColor = contentColorFor(MaterialTheme.colors.surface)
    ) {
        Layout(
            content = {
                videoPlayer()
                Column {
                    content()
                }
                minimizedContent()
            },
            measurePolicy = { measurables, constraints ->
                val minimizableHeight = constraints.maxHeight

                val videoPlayerPlaceable =
                    measurables[0].measure(constraints.copy(maxHeight = Constraints.Infinity))
                val fullPagePlaceable = measurables[1].measure(constraints)

                val videoPlayerScale =
                    (minimizableHeight.toFloat() / videoPlayerPlaceable.height.toFloat()).coerceAtMost(1f)
                val videoPlayerScaledWidth =
                    (videoPlayerPlaceable.width * videoPlayerScale).toInt()

                val minimizedPlaceable = measurables[2].measure(
                    constraints.copy(
                        maxHeight = minimizableHeight,
                        maxWidth = constraints.maxWidth - videoPlayerScaledWidth,
                        minWidth = 0
                    )
                )

                layout(constraints.maxWidth, constraints.maxHeight) {
                    videoPlayerPlaceable.placeRelativeWithLayer(0, 0) {
                        if (videoPlayerPlaceable.height > minimizableHeight) {
                            scaleX = videoPlayerScale
                            scaleY = videoPlayerScale
                            transformOrigin = TransformOrigin(0f, 0f)
                        }
                    }
                    if (videoPlayerPlaceable.height <= minimizableHeight) {
                        fullPagePlaceable.placeRelativeWithLayer(0, videoPlayerPlaceable.height) {
                            alpha = when {
                                swipeProgress.from == swipeProgress.to &&
                                    swipeProgress.to == MinimizeLayoutValue.Expanded -> 1f
                                swipeProgress.to == MinimizeLayoutValue.Expanded -> swipeProgress.fraction
                                swipeProgress.from == MinimizeLayoutValue.Expanded -> 1 - swipeProgress.fraction
                                else -> 0f
                            }
                        }
                    } else {
                        minimizedPlaceable.placeRelative(videoPlayerScaledWidth, 0)
                    }
                }
            }
        )
    }
}