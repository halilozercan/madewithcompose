package com.halilibo.madewithcompose.videoplayer

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun MinimizedTitleAndControls(
    videoTitle: String,
    isPlaying: Boolean,
    onPlayPauseToggle: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val onPlayPauseToggleState by rememberUpdatedState(newValue = onPlayPauseToggle)
    val onDismissState by rememberUpdatedState(newValue = onDismiss)
    Layout(
        content = {
            Text(
                videoTitle,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            IconButton(onClick = {
                onPlayPauseToggleState()
            }) {
                if (isPlaying) {
                    Icon(Icons.Default.Pause, contentDescription = null)
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                }
            }

            IconButton(onClick = {
                onDismissState()
            }) {
                Icon(Icons.Default.Close, contentDescription = null)
            }
        },
        modifier = modifier
    ) { measurables, constraints ->
        val playPausePlaceable = measurables[1].measure(constraints)
        val dismissPlaceable = measurables[2].measure(constraints)

        val remainingWidth =
            (constraints.maxWidth - playPausePlaceable.width - dismissPlaceable.width).coerceAtLeast(
                0
            )
        val titlePlaceable = measurables[0].measure(constraints.copy(maxWidth = remainingWidth))

        layout(constraints.maxWidth, constraints.maxHeight) {
            titlePlaceable.placeRelative(0, (constraints.maxHeight - titlePlaceable.height) / 2)
            playPausePlaceable.placeRelative(
                titlePlaceable.width,
                (constraints.maxHeight - playPausePlaceable.height) / 2
            )
            dismissPlaceable.placeRelative(
                titlePlaceable.width + dismissPlaceable.width,
                (constraints.maxHeight - dismissPlaceable.height) / 2
            )
        }
    }
}