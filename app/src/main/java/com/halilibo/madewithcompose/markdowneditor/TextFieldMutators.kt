package com.halilibo.madewithcompose.markdowneditor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.getSelectedText

fun TextFieldValue.inlineWrap(
    startWrappedString: String,
    endWrappedString: String = startWrappedString
): TextFieldValue {
    return copy(
        annotatedString =
        annotatedString.subSequence(0, selection.min) +
                buildAnnotatedString { append(startWrappedString) } +
                getSelectedText() +
                buildAnnotatedString { append(endWrappedString) } +
                annotatedString.subSequence(selection.max, text.length),
        selection = TextRange(
            selection.min + startWrappedString.length,
            selection.max + startWrappedString.length
        )
    )
}

fun TextFieldValue.bold(): TextFieldValue = inlineWrap("**")

fun TextFieldValue.italic(): TextFieldValue = inlineWrap("_")

fun TextFieldValue.inlineCode(): TextFieldValue = inlineWrap("`")

fun TextFieldValue.strikeThrough(): TextFieldValue = inlineWrap("~~")

fun TextFieldValue.header(): TextFieldValue {
    val lineStart = text.take(selection.min)
        .lastIndexOf('\n')
        .takeIf { it != -1 }
        ?.let { it + 1 }
        ?: 0

    val appendedString = if (text[lineStart] == '#') "#" else "# "

    return copy(
        annotatedString =
        annotatedString.subSequence(0, lineStart) +
                buildAnnotatedString { append(appendedString) } +
                annotatedString.subSequence(lineStart, text.length),
        selection = TextRange(
            selection.min + appendedString.length,
            selection.max + appendedString.length
        )
    )
}

fun TextFieldValue.quote(): TextFieldValue {
    val lineStart = text.take(selection.min)
        .lastIndexOf('\n')
        .takeIf { it != -1 }
        ?.let { it + 1 }
        ?: 0

    return copy(
        annotatedString =
        annotatedString.subSequence(0, lineStart) +
                buildAnnotatedString { append("> ") } +
                annotatedString.subSequence(lineStart, text.length),
        selection = TextRange(
            selection.min + 2,
            selection.max + 2
        )
    )
}