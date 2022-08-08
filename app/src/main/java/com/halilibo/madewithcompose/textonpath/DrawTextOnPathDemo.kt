@file:OptIn(ExperimentalTextApi::class)

package com.halilibo.madewithcompose.textonpath

import android.graphics.PathMeasure
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Checkbox
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.math.*
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.seconds


@Composable
fun DrawTextOnPathDemo() {
  var amp by remember { mutableStateOf(12f) }
  var wave by remember { mutableStateOf(200f) }
  val density = LocalDensity.current
  val squigglyUnderlineAnimator = rememberSquigglyUnderlineAnimator()
  val squigglyUnderlinePathGenerator = remember {
    SquigglyUnderlinePathGenerator(
      amplitude = amp.sp,
      wavelength = wave.sp,
      animator = squigglyUnderlineAnimator,
      density = density
    )
  }

  SideEffect {
    squigglyUnderlinePathGenerator.amplitude = amp.sp
    squigglyUnderlinePathGenerator.wavelength = wave.sp
  }

  val path = squigglyUnderlinePathGenerator.getDemoPath()

  var drawPath by remember { mutableStateOf(true) }
  Column(Modifier.padding(16.dp)) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .clickable {
          drawPath = !drawPath
        }) {
      Checkbox(checked = drawPath, onCheckedChange = { drawPath = it })
      Text("Draw Path")
    }
    Text(text = "Amplitude")
    Slider(value = amp, onValueChange = { amp = it }, valueRange = 0f..60f)
    Text(text = "Wavelength")
    Slider(value = wave, onValueChange = { wave = it }, valueRange = 0f..400f)
    DrawTextOnPathDemo(path, drawPath)
  }
}

@Composable
fun DrawTextOnPathDemo(
  path: android.graphics.Path,
  drawPath: Boolean
) {
  val textMeasurer = rememberTextMeasurer()
  val pathMeasure = remember { PathMeasure() }

  Box(modifier = Modifier
    .fillMaxSize()
    .drawWithCache {
      // cache expensive layout and box calculations so that each draw call is cheaper.
      val textLayoutResult = textMeasurer.measure(
        AnnotatedString("Jetpack🌊\nCompose👨‍👩‍👧‍👦"),
        style = TextStyle(
          fontSize = 30.sp,
          fontWeight = FontWeight.Bold,
          brush = Brush.horizontalGradient(Gradient),
          letterSpacing = 8.sp,
          textAlign = TextAlign.Center
        ),
        size = IntSize(size.width.roundToInt(), size.height.roundToInt())
      )
      val boxes = textLayoutResult.layoutInput.text.indices.map {
        textLayoutResult.getBoundingBox(it)
      }
      pathMeasure.setPath(path, false)

      onDrawBehind {
        if (drawPath) {
          drawPath(path.asComposePath(), Color.Magenta, style = Stroke(8f))
        }
        var totalLength = 0f
        for (index in textLayoutResult.layoutInput.text.indices) {
          val box = boxes[index]
          val letterDistance = box.center.x // + offset.value
          if (letterDistance <= 0f) continue
          if (letterDistance - totalLength >= pathMeasure.length) {
            totalLength += pathMeasure.length
            if (!pathMeasure.nextContour()) continue
          }
          // https://issuetracker.google.com/197146630
          if (box.left > box.right) continue

          val pos = FloatArray(2)
          val tan = FloatArray(2)
          pathMeasure.getPosTan(letterDistance - totalLength, pos, tan)
          withTransform({
            translate(left = pos[0] - box.center.x, top = pos[1] - textLayoutResult.size.center.y)
            rotate(((atan2(tan[1], tan[0]) / PI) * 180).toFloat(), pivot = box.center)
            clipRect(box.left, box.top, box.right, box.bottom)
          }) {
            drawText(textLayoutResult)
          }
        }
        // if we moved contours, reset the PathMeasure before the next draw.
        if (totalLength > 0) {
          pathMeasure.setPath(path, false)
        }
      }
    })
}

val Gradient = listOf(
  Color(0xff8BDEDA),
  Color(0xff43ADD0),
  Color(0xff998EE0),
  Color(0xffE17DC2),
  Color(0xffEF9393)
)

// Below classes and functions are taken from;
// https://github.com/saket/ExtendedSpans/blob/trunk/extendedspans/src/main/kotlin/me/saket/extendedspans/SquigglyUnderlineSpanPainter.kt
class SquigglyUnderlinePathGenerator(
  internal var wavelength: TextUnit = 9.sp,
  internal var amplitude: TextUnit = 1.sp,
  internal var animator: SquigglyUnderlineAnimator = SquigglyUnderlineAnimator.NoOp,
  private val density: Density
) {
  private val width: TextUnit = 2.sp
  private val bottomOffset: TextUnit = 1.sp

  /**
   * Maths copied from [squigglyspans](https://github.com/samruston/squigglyspans).
   */
  private fun buildSquigglesFor(box: Rect): Path = density.run {
    val path = Path()
    val lineStart = box.left + (width.toPx() / 2)
    val lineEnd = box.right - (width.toPx() / 2)
    val lineBottom = box.bottom + bottomOffset.toPx()

    val segmentWidth = wavelength.toPx() / SEGMENTS_PER_WAVELENGTH
    val numOfPoints = ceil((lineEnd - lineStart) / segmentWidth).toInt() + 1

    var pointX = lineStart
    fastMapRange(0, numOfPoints) { point ->
      val proportionOfWavelength = (pointX - lineStart) / wavelength.toPx()
      val radiansX = proportionOfWavelength * TWO_PI + (TWO_PI * animator.animationProgress.value)
      val offsetY = lineBottom + (sin(radiansX) * amplitude.toPx())

      when (point) {
        0 -> path.moveTo(pointX, offsetY)
        else -> path.lineTo(pointX, offsetY)
      }
      pointX = (pointX + segmentWidth).coerceAtMost(lineEnd)
    }
    path
  }

  fun getDemoPath(): android.graphics.Path {
    return buildSquigglesFor(
      Rect(Offset.Zero, Size(2000f, 200f))
    ).asAndroidPath()
  }

  companion object {
    private const val SEGMENTS_PER_WAVELENGTH = 10
    private const val TWO_PI = 2 * Math.PI.toFloat()
  }
}

@OptIn(ExperimentalTime::class)
@Composable
fun rememberSquigglyUnderlineAnimator(duration: Duration = 1.seconds): SquigglyUnderlineAnimator {
  val animationProgress = rememberInfiniteTransition().animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(duration.inWholeMilliseconds.toInt(), easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    )
  )
  return remember {
    SquigglyUnderlineAnimator(animationProgress)
  }
}

@Stable
class SquigglyUnderlineAnimator internal constructor(internal val animationProgress: State<Float>) {
  companion object {
    val NoOp = SquigglyUnderlineAnimator(animationProgress = mutableStateOf(0f))
  }
}


@OptIn(ExperimentalContracts::class)
internal inline fun <R> fastMapRange(
  start: Int,
  end: Int,
  transform: (Int) -> R
): List<R> {
  contract { callsInPlace(transform) }
  val destination = ArrayList<R>(/* initialCapacity = */ end - start + 1)
  for (i in start..end) {
    destination.add(transform(i))
  }
  return destination
}
