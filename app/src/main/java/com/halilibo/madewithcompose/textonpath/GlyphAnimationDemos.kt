package com.halilibo.madewithcompose.textonpath

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun DemoBouncy(text: String) {
  val textMeasurer = rememberTextMeasurer()
  val animatables = remember { List(text.length) { Animatable(0f) } }
  val coroutineScope = rememberCoroutineScope()

  Button(onClick = {
    coroutineScope.launch {
      animatables.forEach { it.snapTo(0f) }
      animatables.forEachIndexed { index, animatable ->
        launch {
          delay(Random.nextLong(300) + index * 100)
          animatable.animateTo(1f, spring(dampingRatio = 0.3f, stiffness = 150f))
        }
      }
    }
  }) {
    Text("Bounce")
  }
  Box(modifier = Modifier
    .fillMaxWidth()
    .height(300.dp)
    .drawWithCache {
      val textLayoutResult = textMeasurer.measure(
        text = AnnotatedString(text),
        style = TextStyle(
          fontSize = 40.sp,
          brush = Brush.horizontalGradient(colors = listOf(Color.Blue, Color.Red))
        ),
        constraints = Constraints.fixed(size.width.roundToInt(), size.height.roundToInt())
      )
      val glyphs = textLayoutResult.toGlyphs()
      onDrawBehind {
        drawGlyphs(glyphs) {
          val value = animatables[offset].value
          if (value > 0f) {
            translate(top = -(1 - value) * 200f) {
              this@drawGlyphs.drawGlyph(alpha = value)
            }
          }
        }
      }
    })
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun DemoTyping(text: String) {
  val textMeasurer = rememberTextMeasurer()
  val animatables = remember { List(text.length) { Animatable(0f) } }
  val coroutineScope = rememberCoroutineScope()

  Button(onClick = {
    coroutineScope.launch {
      animatables.forEach { it.snapTo(0f) }
      animatables.forEachIndexed { index, animatable ->
        launch {
          delay(index * 100L)
          animatable.animateTo(1f, tween(100))
        }
      }
    }
  }) {
    Text("Type")
  }
  Box(modifier = Modifier
    .fillMaxWidth()
    .height(300.dp)
    .drawWithCache {
      val textLayoutResult = textMeasurer.measure(
        text = AnnotatedString(text),
        style = TextStyle(
          fontSize = 40.sp,
          brush = Brush.horizontalGradient(colors = listOf(Color.Blue, Color.Red))
        ),
        constraints = Constraints.fixed(size.width.roundToInt(), size.height.roundToInt())
      )
      val glyphs = textLayoutResult.toGlyphs()
      onDrawBehind {
        drawGlyphs(glyphs) {
          val value = animatables[offset].value
          if (value > 0f) {
            translate(left = (1f - value) * 40f) {
              this@drawGlyphs.drawGlyph(alpha = value)
            }
          }
        }
      }
    })
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun DemoRotating(text: String) {
  val textMeasurer = rememberTextMeasurer()
  val animatables = remember { List(text.length) { Animatable(0f) } }
  val coroutineScope = rememberCoroutineScope()

  Button(onClick = {
    coroutineScope.launch {
      animatables.forEach { it.snapTo(0f) }
      animatables.forEachIndexed { index, animatable ->
        launch {
          delay(index * 200L)
          animatable.animateTo(1f, spring(dampingRatio = 0.3f, stiffness = 150f))
        }
      }
    }
  }) {
    Text("Whirl")
  }
  Box(modifier = Modifier
    .fillMaxWidth()
    .height(300.dp)
    .drawWithCache {
      val textLayoutResult = textMeasurer.measure(
        text = AnnotatedString(text),
        style = TextStyle(
          fontSize = 40.sp,
          brush = Brush.horizontalGradient(colors = listOf(Color.Blue, Color.Red))
        ),
        constraints = Constraints.fixed(size.width.roundToInt(), size.height.roundToInt())
      )
      val glyphs = textLayoutResult.toGlyphs()
      onDrawBehind {
        drawGlyphs(glyphs) {
          val value = animatables[offset].value
          if (value > 0f) {
            rotate(value * 360f) {
              scale(value) {
                this@drawGlyphs.drawGlyph(alpha = value)
              }
            }
          }
        }
      }
    })
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun DemoDropping(text: String) {
  val textMeasurer = rememberTextMeasurer()
  val animatables = remember { List(text.length) { Animatable(0f) } }
  val coroutineScope = rememberCoroutineScope()

  Button(onClick = {
    coroutineScope.launch {
      animatables.forEach { it.snapTo(0f) }
      animatables.indices.toList().shuffled().forEachIndexed { index, animatableIndex ->
        launch {
          delay(index * Random.nextLong(75L, 100L))
          animatables[animatableIndex].snapTo(20f)
          animatables[animatableIndex].animateTo(1f, tween(500))
        }
      }
    }
  }) {
    Text("Drop")
  }
  Box(modifier = Modifier
    .fillMaxWidth()
    .height(300.dp)
    .drawWithCache {
      val textLayoutResult = textMeasurer.measure(
        text = AnnotatedString(text),
        style = TextStyle(
          fontSize = 40.sp,
          brush = Brush.horizontalGradient(colors = listOf(Color.Blue, Color.Red))
        ),
        constraints = Constraints.fixed(size.width.roundToInt(), size.height.roundToInt())
      )
      val glyphs = textLayoutResult.toGlyphs()
      onDrawBehind {
        drawGlyphs(glyphs) {
          val value = animatables[offset].value
          if (value > 0f) {
            scale(value) {
              this@drawGlyphs.drawGlyph(alpha = (20f - value) / 19f)
            }
          }
        }
      }
    })
}
