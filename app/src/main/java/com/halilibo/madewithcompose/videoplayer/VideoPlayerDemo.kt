package com.halilibo.madewithcompose.videoplayer

import android.graphics.Rect
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.gson.Gson
import com.halilibo.madewithcompose.R
import com.halilibo.madewithcompose.pip.LocalPipState
import com.halilibo.videoplayer.VideoPlayer
import com.halilibo.videoplayer.VideoPlayerController
import com.halilibo.videoplayer.VideoPlayerSource
import com.halilibo.videoplayer.rememberVideoPlayerController
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun VideoPlayerDemo() {
    var selectedVideo by rememberSaveable { mutableStateOf<Video?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val videoPlayerController = rememberVideoPlayerController()
    val videoPlayerUiState by videoPlayerController.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val pipState = LocalPipState.current

    DisposableEffect(videoPlayerController, lifecycleOwner) {
        val observer = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                videoPlayerController.startMediaSession()
            }

            override fun onStop(owner: LifecycleOwner) {
                videoPlayerController.pause()
                videoPlayerController.stopMediaSession()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                videoPlayerController.reset()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    with(pipState) {
        PipEnabled(isEnabled = selectedVideo != null && videoPlayerUiState.isPlaying)
        setAspectRatio(videoPlayerUiState.videoSize.width, videoPlayerUiState.videoSize.height)
    }

    val minimizeLayoutState = rememberMinimizeLayoutState(MinimizeLayoutValue.Expanded)

    val selectedVideoFlow = remember { snapshotFlow { selectedVideo } }

    LaunchedEffect(Unit) {
        selectedVideoFlow.onEach { video ->
            if (video != null) {
                videoPlayerController.setSource(VideoPlayerSource.Network(video.sources.first()))
                minimizeLayoutState.expand()
            } else {
                minimizeLayoutState.hide()
                videoPlayerController.reset()
            }
        }.launchIn(this)
    }

    val isFullyMaximized = minimizeLayoutState.currentValue == MinimizeLayoutValue.Expanded &&
            minimizeLayoutState.targetValue != MinimizeLayoutValue.Minimized &&
            !minimizeLayoutState.isHidden

    BackHandler(
        onBack = {
            coroutineScope.launch { minimizeLayoutState.minimize() }
        },
        enabled = isFullyMaximized
    )

    if (pipState.isInPictureInPicture.value) {
        PictureInPictureVideoPlayerDemo(videoPlayerController)
    } else {
        MinimizeLayout(
            minimizeLayoutState = minimizeLayoutState,
            minimizedContentHeight = { 60.dp },
            minimizableContent = { swipeable ->
                val videoTitle = selectedVideo?.title ?: ""
                val videoDescription = selectedVideo?.description ?: ""

                VideoPlayerPage(
                    videoPlayer = {
                        VideoPlayer(
                            videoPlayerController = videoPlayerController,
                            backgroundColor = Color.Transparent,
                            modifier = Modifier
                                .then(swipeable)
                                .onGloballyPositioned {
                                    val size = it.size
                                    val positionInWindow = it.localToWindow(Offset.Zero)
                                    pipState.setSourceRectHint(Rect(
                                        positionInWindow.x.toInt(),
                                        positionInWindow.y.toInt(),
                                        (positionInWindow.x + size.width).toInt(),
                                        (positionInWindow.y + size.height).toInt()
                                    ))
                                },
                            controlsEnabled = isFullyMaximized
                        )
                    },
                    content = {
                        Text(
                            videoTitle,
                            style = TextStyle(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                .padding(16.dp)
                                .clickable {
                                    selectedVideo = null
                                }
                        )
                        Text(
                            videoDescription,
                            style = TextStyle(
                                fontSize = 14.sp
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    },
                    minimizedContent = {
                        MinimizedTitleAndControls(
                            videoTitle = videoTitle,
                            isPlaying = videoPlayerUiState.isPlaying,
                            onPlayPauseToggle = { videoPlayerController.playPauseToggle() },
                            onDismiss = { selectedVideo = null },
                            modifier = Modifier.then(swipeable)
                        )
                    },
                    swipeProgress = minimizeLayoutState.swipeProgress,
                    modifier = Modifier
                )
            }
        ) { paddingValues ->
            Surface {
                VideoListPage(
                    onVideoSelected = { video ->
                        selectedVideo = video
                    },
                    contentPadding = paddingValues
                )
            }
        }
    }
}

@Composable
fun PictureInPictureVideoPlayerDemo(
    videoPlayerController: VideoPlayerController
) {
    VideoPlayer(
        videoPlayerController = videoPlayerController,
        backgroundColor = Color.Transparent,
        controlsEnabled = false,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun getVideoList(): List<Video> {
    val context = LocalContext.current

    return remember(context) {
        val content = context.resources.openRawResource(R.raw.videos)
            .bufferedReader()
            .use { it.readText() }
        Gson().fromJson(content, VideoList::class.java).videos
    }
}
