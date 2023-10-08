@file:OptIn(ExperimentalFoundationApi::class)

package com.halilibo.madewithcompose.markdowneditor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text2.input.TextFieldBuffer
import androidx.compose.ui.text.TextRange

fun TextFieldBuffer.inlineWrap(
  startWrappedString: String,
  endWrappedString: String = startWrappedString
) {
  val initialSelection = selectionInChars
  replace(initialSelection.min, initialSelection.min, startWrappedString)
  replace(
    initialSelection.max + startWrappedString.length,
    initialSelection.max + startWrappedString.length,
    endWrappedString
  )
  selectCharsIn(
    TextRange(
      initialSelection.min,
      initialSelection.max + startWrappedString.length + endWrappedString.length
    )
  )
}

fun TextFieldBuffer.bold() = inlineWrap("**")

fun TextFieldBuffer.italic() = inlineWrap("_")

fun TextFieldBuffer.inlineCode() = inlineWrap("`")

fun TextFieldBuffer.strikeThrough() = inlineWrap("~~")

fun TextFieldBuffer.header() {
  val text = toString()
  val lineStart = text.take(selectionInChars.min)
    .lastIndexOf('\n')
    .takeIf { it != -1 }
    ?.let { it + 1 }
    ?: 0

  val appendedString = if (text[lineStart] == '#') "#" else "# "

  val initialSelection = selectionInChars

  replace(lineStart, lineStart, appendedString)
  selectCharsIn(
    TextRange(
      initialSelection.min + appendedString.length,
      initialSelection.max + appendedString.length
    )
  )
}

fun TextFieldBuffer.quote() {
  val text = toString()
  val lineStart = text.take(selectionInChars.min)
    .lastIndexOf('\n')
    .takeIf { it != -1 }
    ?.let { it + 1 }
    ?: 0

  val initialSelection = selectionInChars

  replace(lineStart, lineStart, "> ")
  selectCharsIn(
    TextRange(
      initialSelection.min + 2,
      initialSelection.max + 2
    )
  )
}