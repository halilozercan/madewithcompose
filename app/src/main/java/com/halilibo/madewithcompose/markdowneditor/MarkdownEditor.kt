package com.halilibo.madewithcompose.markdowneditor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text2.BasicTextField2
import androidx.compose.foundation.text2.input.TextFieldState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halilibo.madewithcompose.R

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun MarkdownEditor(
  state: TextFieldState,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
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
          imageVector = Icons.AutoMirrored.Filled.Undo,
          contentDescription = "Undo",
          tint = contentColor
        )
      }

      IconButton(
        onClick = onRedo,
        enabled = redoEnabled
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.Redo,
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
        state.edit { header() }
      }) {
        Icon(
          painter = painterResource(id = R.drawable.ic_header),
          contentDescription = "Header",
          tint = contentColor,
          modifier = Modifier.size(16.dp)
        )
      }

      IconButton(onClick = {
        state.edit { bold() }
      }) {
        Icon(
          imageVector = Icons.Default.FormatBold,
          contentDescription = "Format Bold",
          tint = contentColor
        )
      }

      IconButton(onClick = {
        state.edit { italic() }
      }) {
        Icon(
          imageVector = Icons.Default.FormatItalic,
          contentDescription = "FormatItalic",
          tint = contentColor
        )
      }

      IconButton(onClick = {
        state.edit { strikeThrough() }
      }) {
        Icon(
          imageVector = Icons.Default.FormatStrikethrough,
          contentDescription = "Format Strikethrough",
          tint = contentColor
        )
      }


      IconButton(onClick = {
        state.edit { inlineCode() }
      }) {
        Icon(
          imageVector = Icons.Default.Code,
          contentDescription = "Format Code",
          tint = contentColor
        )
      }

      IconButton(onClick = {
        state.edit { quote() }
      }) {
        Icon(
          imageVector = Icons.Default.FormatQuote,
          contentDescription = "Format Quote",
          tint = contentColor
        )
      }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val colors = TextFieldDefaults.outlinedTextFieldColors()
    BasicTextField2(
      state = state,
      textStyle = LocalTextStyle.current.copy(fontSize = 20.sp, color = LocalContentColor.current),
      interactionSource = interactionSource,
      enabled = enabled,
      cursorBrush = SolidColor(colors.cursorColor(false).value),
      decorationBox = {
        TextFieldDefaults.OutlinedTextFieldDecorationBox(
          value = state.text.toString(),
          innerTextField = it,
          enabled = true,
          singleLine = false,
          visualTransformation = VisualTransformation.None,
          interactionSource = interactionSource
        )
      },
      modifier = Modifier
        .padding(8.dp)
        .fillMaxSize()
        .weight(1f)
    )
  }
}