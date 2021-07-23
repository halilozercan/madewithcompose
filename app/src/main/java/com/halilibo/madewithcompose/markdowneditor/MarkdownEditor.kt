package com.halilibo.madewithcompose.markdowneditor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halilibo.madewithcompose.R

@Composable
fun MarkdownEditor(
    content: TextFieldValue,
    onContentChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    onUndo: () -> Unit = {},
    onRedo: () -> Unit = {},
    undoEnabled: Boolean = true,
    redoEnabled: Boolean = true,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val contentColor = LocalContentColor.current

            IconButton(
                onClick = onUndo,
                enabled = undoEnabled
            ) {
                Icon(
                    imageVector = Icons.Default.Undo,
                    contentDescription = "Undo",
                    tint = contentColor
                )
            }

            IconButton(
                onClick = onRedo,
                enabled = redoEnabled
            ) {
                Icon(
                    imageVector = Icons.Default.Redo,
                    contentDescription = "Redo",
                    tint = contentColor
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .padding(vertical = 4.dp)
                    .background(Color.Gray)
            )

            IconButton(onClick = {
                onContentChange(content.header())
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_header),
                    contentDescription = "Header",
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(onClick = {
                onContentChange(content.bold())
            }) {
                Icon(
                    imageVector = Icons.Default.FormatBold,
                    contentDescription = "Format Bold",
                    tint = contentColor
                )
            }

            IconButton(onClick = {
                onContentChange(content.italic())
            }) {
                Icon(
                    imageVector = Icons.Default.FormatItalic,
                    contentDescription = "FormatItalic",
                    tint = contentColor
                )
            }

            IconButton(onClick = {
                onContentChange(content.strikeThrough())
            }) {
                Icon(
                    imageVector = Icons.Default.FormatStrikethrough,
                    contentDescription = "Format Strikethrough",
                    tint = contentColor
                )
            }


            IconButton(onClick = {
                onContentChange(content.inlineCode())
            }) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = "Format Code",
                    tint = contentColor
                )
            }

            IconButton(onClick = {
                onContentChange(content.quote())
            }) {
                Icon(
                    imageVector = Icons.Default.FormatQuote,
                    contentDescription = "Format Quote",
                    tint = contentColor
                )
            }
        }

        OutlinedTextField(
            value = content,
            onValueChange = onContentChange,
            textStyle = TextStyle(fontSize = 20.sp),
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize()
                .weight(1f)
        )
    }
}