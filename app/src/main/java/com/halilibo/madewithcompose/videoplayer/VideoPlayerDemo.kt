package com.halilibo.madewithcompose.videoplayer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.halilibo.videoplayer.VideoPlayer
import com.halilibo.videoplayer.VideoPlayerSource
import com.halilibo.videoplayer.rememberVideoPlayerController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun VideoPlayerDemo() {
    var selectedVideoState by rememberSaveable { mutableStateOf<Video?>(null) }

    val videoPlayerController = rememberVideoPlayerController()
    val videoPlayerUiState by videoPlayerController.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(videoPlayerController, lifecycleOwner) {
        val observer = object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) {
                videoPlayerController.pause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val minimizeLayoutState = rememberMinimizeLayoutState(MinimizeLayoutValue.Expanded)
    LaunchedEffect(selectedVideoState) {
        val selectedVideo = selectedVideoState
        if (selectedVideo != null) {
            videoPlayerController.setSource(VideoPlayerSource.Network(selectedVideo.sources.first()))
            minimizeLayoutState.expand()
        } else {
            minimizeLayoutState.hide()
            videoPlayerController.reset()
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val isFullyMaximized = minimizeLayoutState.currentValue == MinimizeLayoutValue.Expanded &&
        minimizeLayoutState.targetValue != MinimizeLayoutValue.Minimized

    BackHandler(
        onBack = {
            coroutineScope.launch { minimizeLayoutState.minimize() }
        },
        enabled = isFullyMaximized
    )

    MinimizeLayout(
        minimizeLayoutState = minimizeLayoutState,
        minimizedContentHeight = { 60.dp },
        minimizableContent = { swipeable ->
            val videoTitle = selectedVideoState?.title ?: ""
            val videoDescription = selectedVideoState?.description ?: ""

            VideoPlayerPage(
                videoPlayer = {
                    VideoPlayer(
                        videoPlayerController = videoPlayerController,
                        backgroundColor = Color.Transparent,
                        modifier = Modifier.then(swipeable),
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
                                selectedVideoState = null
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
                        onDismiss = { selectedVideoState = null },
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
                    selectedVideoState = video
                },
                contentPadding = paddingValues
            )
        }
    }
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
