package com.halilibo.madewithcompose.markdowneditor

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce

@Composable
fun rememberTextFieldHistory(
    initialTextFieldValue: TextFieldValue
): TextFieldHistory {
    val textFieldHistory = rememberSaveable(saver = TextFieldHistory.SAVER) {
        TextFieldHistory(HistoryState(listOf(initialTextFieldValue), 0))
    }

    LaunchedEffect(Unit) {
        textFieldHistory.start()
    }

    return textFieldHistory
}

/**
 * Extends TextFieldValue state to have a history stack that provides undo/redo actions
 */
class TextFieldHistory internal constructor(
    initialHistoryState: HistoryState
) {
    private val textFieldValueChannel = Channel<TextFieldValue>(capacity = 16)

    private var historyState by mutableStateOf(initialHistoryState)

    private fun updateHistoryState(block: HistoryState.() -> HistoryState) {
        historyState = historyState.block().apply {
            stack.dropWhile { stack.size > 100 }
        }
    }

    var textFieldValue by mutableStateOf(initialHistoryState.currentValue)
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

    suspend fun start() {
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

    companion object {
        internal val SAVER = Saver<TextFieldHistory, Any>(
            restore = { value ->
                with(TextFieldValue.Saver) {
                    @Suppress("UNCHECKED_CAST")
                    val list = value as List<Any>
                    TextFieldHistory(
                        initialHistoryState = HistoryState(
                            marker = list[0] as Int,
                            stack = list.drop(1).map { savedTextFieldValue ->
                                restore(savedTextFieldValue)!!
                            }
                        )
                    )
                }
            },
            save = { value ->
                with(TextFieldValue.Saver) {
                    arrayListOf<Any>().apply {
                        add(value.historyState.marker)
                        addAll(
                            value.historyState.stack.map { textFieldValue ->
                                save(textFieldValue)!!
                            }
                        )
                    }
                }
            }
        )
    }
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