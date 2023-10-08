package com.halilibo.madewithcompose.varimoji

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halilibo.madewithcompose.R

@OptIn(ExperimentalTextApi::class)
@Composable
fun VarimojiDemo() {
  var actv by remember { mutableFloatStateOf(50f) }
  var vlnc by remember { mutableFloatStateOf(50f) }
  Column(
    modifier = Modifier.padding(32.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = "A",
      fontFamily = FontFamily(
        Font(
          resId = R.font.varimoji,
          variationSettings = FontVariation.Settings(
            FontVariation.Setting("ACTV", actv),
            FontVariation.Setting("VLNC", vlnc),
          )
        )
      ),
      fontSize = 140.sp
    )
    Spacer(modifier = Modifier.height(32.dp))
    Box(modifier = Modifier
      .fillMaxSize()
      .pointerInput(Unit) {
        awaitEachGesture {
          do {
            val event = awaitPointerEvent()
            event.changes.filter { it.pressed }.forEach {
              val coercedPosition = Offset(
                it.position.x.coerceIn(0f, size.width.toFloat()),
                it.position.y.coerceIn(0f, size.height.toFloat()),
              )
              actv = coercedPosition.x / size.width * 100f
              vlnc = coercedPosition.y / size.height * 100f
              it.consume()
            }
          } while (event.changes.all { it.pressed })
        }
      }
      .drawBehind {
        drawRect(
          Brush.linearGradient(
            listOf(Color(0xFFE5E5E5), Color.White),
            start = Offset(size.width / 2f, size.height / 2f),
            end = Offset(size.width, 0f)
          ),
          topLeft = Offset(size.width / 2f, 0f),
          size = size / 2f
        )

        drawRect(
          Brush.linearGradient(
            listOf(Color(0xFFE5E5E5), Color.White),
            start = Offset(size.width / 2f, size.height / 2f),
            end = Offset(0f, size.height)
          ),
          topLeft = Offset(0f, size.height / 2f),
          size = size / 2f
        )

        val point = Offset(actv / 100f * size.width, vlnc / 100f * size.height)
        drawCircle(
          Brush.radialGradient(
            listOf(Color.Red.copy(alpha = 0.5f), Color.Transparent),
            radius = 36.dp.toPx(),
            center = point
          ),
          radius = 36.dp.toPx(),
          center = point
        )
      }) {
    }
  }
}