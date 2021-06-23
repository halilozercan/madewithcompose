package com.halilibo.madewithcompose.weightentry

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.halilibo.madewithcompose.ui.theme.MadeWithComposeTheme
import com.halilibo.weightentry.WeightEntry
import com.halilibo.weightentry.WeightScale
import com.halilibo.weightentry.rememberWeightEntryState
import kotlin.math.roundToInt

@Composable
fun WeightEntryDemo() {
    Column(modifier = Modifier.padding(16.dp)) {
        val state = rememberWeightEntryState(
            initialValue = 78,
            valueRange = 20..180
        )

        WeightEntry(
            state = state,
            modifier = Modifier
        )

        WeightScale(
            weight = state.value.roundToInt(),
            valueRange = 20..180,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MadeWithComposeTheme {
        WeightEntryDemo()
    }
}