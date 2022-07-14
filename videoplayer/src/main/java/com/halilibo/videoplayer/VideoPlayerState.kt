package com.halilibo.videoplayer

import android.os.Parcelable
import androidx.compose.ui.geometry.Size
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class VideoPlayerState(
    val isPlaying: Boolean = true,
    val controlsVisible: Boolean = true,
    val controlsEnabled: Boolean = true,
    val gesturesEnabled: Boolean = true,
    val duration: Long = 1L,
    val currentPosition: Long = 1L,
    val secondaryProgress: Long = 1L,
    val videoWidth: Float = 1920f,
    val videoHeight: Float = 1080f,
    val draggingProgress: DraggingProgress? = null,
    val playbackState: PlaybackState = PlaybackState.IDLE
): Parcelable {
    @IgnoredOnParcel
    val videoSize: Size = Size(videoWidth, videoHeight)
}