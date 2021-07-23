package com.halilibo.madewithcompose.markdowneditor

import androidx.compose.runtime.*
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@Composable
fun rememberTextFieldHistory(
    initialTextFieldValue: TextFieldValue
): TextFieldHistory {
    val coroutineScope = rememberCoroutineScope()
    return remember { TextFieldHistory(initialTextFieldValue, coroutineScope) }
}

/**
 * Extends TextFieldValue state to have a history stack that provides undo/redo actions
 */
class TextFieldHistory(
    initialTextFieldValue: TextFieldValue,
    coroutineScope: CoroutineScope
) {
    private val textFieldValueChannel = Channel<TextFieldValue>(capacity = 16)

    init {
        coroutineScope.launch {
            textFieldValueChannel.consumeAsFlow()
                .debounce(500L)
                .collect { newTextFieldValue ->
                    if (historyState.currentValue.text != newTextFieldValue.text) {
                        updateHistoryState {
                            copy(
                                stack = stack + newTextFieldValue,
                                marker = stack.size
                            )
                        }
                    }
                }
        }
    }

    private fun updateHistoryState(block: HistoryState.() -> HistoryState) {
        historyState = historyState.block()
    }

    var textFieldValue by mutableStateOf(initialTextFieldValue)
        private set

    val isBackEnabled by derivedStateOf {
        historyState.stack.size > 1 && historyState.marker > 0
    }

    val isForwardEnabled by derivedStateOf {
        historyState.marker < historyState.stack.size - 1
    }

    fun onValueChange(textFieldValue: TextFieldValue) {
        this.textFieldValue = textFieldValue

        if (historyState.currentValue.text != textFieldValue.text) {
            updateHistoryState {
                copy(stack = stack.take(marker + 1))
            }
            textFieldValueChannel.trySend(textFieldValue)
        }
    }

    fun undo() {
        updateHistoryState { copy(marker = marker - 1) }
        textFieldValue = historyState.currentValue
    }

    fun redo() {
        updateHistoryState { copy(marker = marker + 1) }
        textFieldValue = historyState.currentValue
    }

    private var historyState by mutableStateOf(HistoryState(
        stack = listOf(initialTextFieldValue),
        marker = 0
    ))
}

data class HistoryState(
    val stack: List<TextFieldValue>,
    val marker: Int
) {
    init {
        require(marker >= 0 && marker < stack.size) {
            "History marker cannot be lower than 0 or higher than stack size: ${stack.size}"
        }
    }

    val currentValue: TextFieldValue
        get() = stack[marker]
}