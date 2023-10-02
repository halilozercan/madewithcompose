package com.halilibo.madewithcompose.brush

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.PointerInputChange

private val BULB_START_X = 0.5f
private val BULB_START_Y = 0.1f

class LightingTextDemoState(private val size: Size) {
  var isLightOn by mutableStateOf(true)

  fun toggleLight() {
    isLightOn = !isLightOn
  }

  // bulb dragging animatable
  val dragAnimatable = Animatable(Offset(0f, 0f), Offset.VectorConverter)

  // text scroll
  val scrollState = ScrollState(0)

  private val scrollOffset by derivedStateOf {
    Offset(0f, scrollState.value.toFloat())
  }

  val bulbOffset by derivedStateOf {
    dragAnimatable.value.exactPositionIn()
  }

  suspend fun onDragEnd() {
    dragAnimatable.animateTo(
      Offset.Zero,
      spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow)
    )
  }

  suspend fun onDrag(change: PointerInputChange, dragAmount: Offset) {
    change.consume()
    dragAnimatable.snapTo(dragAnimatable.value + dragAmount)
  }

  val bulbBackground by derivedStateOf {
    if (isLightOn) {
      Brush.radialGradient(listOf(Color.Yellow, Color(0xff85733a)))
    } else {
      SolidColor(Color.Gray)
    }
  }

  val lightSourceBrush by derivedStateOf {
    if (isLightOn) {
      Brush.radialGradient(
        center = bulbOffset + scrollOffset,
        radius = size.minDimension,
        colors = listOf(Color.Yellow, Color(0xff85733a), Color.DarkGray)
      )
    } else {
      SolidColor(Color.DarkGray)
    }
  }

  private fun Offset.exactPositionIn(): Offset {
    return this + Offset(size.width * BULB_START_X, size.height * BULB_START_Y)
  }
}