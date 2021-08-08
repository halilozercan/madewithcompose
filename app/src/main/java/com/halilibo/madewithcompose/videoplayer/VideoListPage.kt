package com.halilibo.madewithcompose.videoplayer

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.LocalImageLoader
import coil.compose.rememberImagePainter

@Composable
fun VideoListPage(
    onVideoSelected: (Video) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val videoList = getVideoList()

    LazyColumn(
        modifier = modifier.fillMaxHeight(),
        contentPadding = contentPadding
    ) {
        items(videoList) { video ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onVideoSelected(video)
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Image(
                    painter = rememberImagePainter(
                        data = video.thumb,
                        imageLoader = LocalImageLoader.current
                    ),
                    contentDescription = video.title,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    video.title,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}