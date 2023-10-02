package com.halilibo.madewithcompose.markdowneditor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text2.input.TextFieldState
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun MarkdownEditorDemo() {
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()

    val state = rememberSaveable(saver = TextFieldState.Saver) {
        TextFieldState(
            """
            Hello World
            
            This huuuge tweet 😛 was written/rendered by a #JetpackCompose app using Markdown.
             
            RichText Commonmark module enables us to easily render any raw Markdown content into
            a Composable. Later, we can simply Screenshot the whole view and share the image.
            
            Hugetwit was one of my first Android projects that made me decide to take #AndroidDev as my career.
            
            This is just a reincarnation of that project in Compose :)
            """.trimIndent()
        )
    }

    BottomSheetScaffold(
        sheetContent = {
            MarkdownPreview(
                content = state.text,
                onPreviewClick = {
                    coroutineScope.launch {
                        if (bottomSheetScaffoldState.bottomSheetState.isExpanded) {
                            bottomSheetScaffoldState.bottomSheetState.collapse()
                        } else {
                            bottomSheetScaffoldState.bottomSheetState.expand()
                        }
                    }
                }
            )
        },
        scaffoldState = bottomSheetScaffoldState,
        sheetPeekHeight = 72.dp,
        sheetElevation = 12.dp,
        sheetShape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Card {
            MarkdownEditor(
                state = state,
                onUndo = { state.undoState.undo() },
                onRedo = { state.undoState.redo() },
                undoEnabled = state.undoState.canUndo,
                redoEnabled = state.undoState.canRedo,
                modifier = Modifier.padding(bottom = 72.dp)
            )
        }
    }
}
