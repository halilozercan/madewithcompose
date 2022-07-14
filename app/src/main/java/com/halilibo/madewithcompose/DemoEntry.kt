package com.halilibo.madewithcompose

import androidx.compose.runtime.Composable
import com.halilibo.madewithcompose.brush.BrushDemo
import com.halilibo.madewithcompose.calendar.CalendarDemo
import com.halilibo.madewithcompose.circlesonlines.CirclesOnLinesDemo
import com.halilibo.madewithcompose.dotsandlines.DotsAndLinesDemo
import com.halilibo.madewithcompose.schedulecalendar.ScheduleCalendarDemo
import com.halilibo.madewithcompose.videoplayer.VideoPlayerDemo
import com.halilibo.madewithcompose.weightentry.WeightEntryDemo
import com.halilibo.madewithcompose.markdowneditor.MarkdownEditorDemo
import com.halilibo.madewithcompose.pip.PictureInPictureDemo

enum class DemoEntry(
    val title: String,
    val destination: String,
    val composable: @Composable () -> Unit
) {
    ScheduleCalendar(
        "Schedule Calendar",
        "schedulecalendar",
        @Composable {
            ScheduleCalendarDemo()
        }
    ),
    VideoPlayer(
        "Video Player",
        "videoplayer",
        @Composable {
            VideoPlayerDemo()
        }
    ),
    DotsAndLines(
        "Dots and Lines",
        "dotsandlines",
        @Composable {
            DotsAndLinesDemo()
        }
    ),
    Calendar(
        "Calendar",
        "calendar",
        @Composable {
            CalendarDemo()
        }
    ),
    CirclesOnLines(
        "Circles on Lines",
        "circlesonlines",
        @Composable {
            CirclesOnLinesDemo()
        }
    ),
    WeightEntry(
        "Weight Entry",
        "weightentry",
        @Composable {
            WeightEntryDemo()
        }
    ),
    MarkdownEditor(
        "Markdown Editor",
        "markdowneditor",
        @Composable {
            MarkdownEditorDemo()
        }
    ),
    PictureInPicture(
        "Picture in Picture",
        "pictureinpicture",
        @Composable {
            PictureInPictureDemo()
        }
    ),
    TextLighting(
        "Text Lighting",
        "textlighting",
        @Composable {
            BrushDemo()
        }
    )
}
