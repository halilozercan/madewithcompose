package com.halilibo.madewithcompose.markdowneditor

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterialApi::class)
@Composable
fun MarkdownEditorDemo() {
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()

    val textFieldHistory = rememberTextFieldHistory(
        initialTextFieldValue = TextFieldValue(
            """
            Hello World
            
            This huuuge tweet 😛 was written/rendered by a #JetpackCompose app using Markdown.
             
            RichText Commonmark module enables us to easily render any raw Markdown content into
            a Composable. Later, we can simply Screenshot the whole view and share the image.
            
            Hugetwit was one of my first Android projects that made me decide to take #AndroidDev as my career.
            
            This is just a reincarnation of that project in Compose :)
            """.trimIndent()
        )
    )

    BottomSheetScaffold(
        sheetContent = {
            MarkdownPreview(
                content = textFieldHistory.textFieldValue,
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
            .navigationBarsWithImePadding()
    ) {
        Card {
            MarkdownEditor(
                content = textFieldHistory.textFieldValue,
                onContentChange = { textFieldHistory.onValueChange(it) },
                onUndo = { textFieldHistory.undo() },
                onRedo = { textFieldHistory.redo() },
                undoEnabled = textFieldHistory.isBackEnabled,
                redoEnabled = textFieldHistory.isForwardEnabled,
                modifier = Modifier.padding(bottom = 72.dp)
            )
        }
    }
}
