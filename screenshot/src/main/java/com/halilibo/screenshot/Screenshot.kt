package com.halilibo.screenshot

import android.graphics.Bitmap
import android.graphics.Picture
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.nativeCanvas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.math.abs

/**
 * A composable that can draw its content onto an [ImageBitmap] via calling the `draw` method on the
 * specified [screenshotController].
 */
@Composable
fun Screenshot(
  screenshotController: ScreenshotController,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  Box(modifier = modifier.drawWithCache {
    val width = this.size.width.toInt()
    val height = this.size.height.toInt()

    onDrawWithContent {
      if (screenshotController.recordingRequested > 0) {
        val pictureCanvas =
          Canvas(
            screenshotController.picture.beginRecording(
              width,
              height
            )
          )
        draw(this, this.layoutDirection, pictureCanvas, this.size) {
          this@onDrawWithContent.drawContent()
        }
        screenshotController.picture.endRecording()

        drawIntoCanvas { canvas ->
          canvas.nativeCanvas.drawPicture(screenshotController.picture)
        }

        screenshotController.recordingRequested = -abs(screenshotController.recordingRequested)
      } else {
        drawContent()
      }
    }
  }) {
    content()
  }
}

/**
 * Controller class to request a screenshot of a Composable.
 */
class ScreenshotController {

  internal val picture: Picture = Picture()

  internal var recordingRequested by mutableIntStateOf(-1)

  suspend fun draw(background: Color): ImageBitmap {
    withContext(Dispatchers.Unconfined) {
      try {
        waitForDrawCycle()
      } catch (e: TimeoutCancellationException) {
        throw IllegalStateException("Composable could not be drawn into a Bitmap")
      }
    }
    val bitmap = Bitmap.createBitmap(
      picture.width,
      picture.height,
      Bitmap.Config.ARGB_8888
    )
    val canvas = android.graphics.Canvas(bitmap)
    if (background.isSpecified) {
      canvas.drawColor(
        argb2(
          background.alpha,
          background.red,
          background.green,
          background.blue
        )
      )
    }
    canvas.drawPicture(picture)
    return bitmap.asImageBitmap()
  }

  private suspend fun waitForDrawCycle() {
    recordingRequested = abs(recordingRequested) + 1
    withTimeout(1000) {
      while (recordingRequested > 0) {
        delay(8)
      }
    }
  }
}

fun argb2(alpha: Float, red: Float, green: Float, blue: Float): Int {
  return (alpha * 255.0f + 0.5f).toInt() shl 24 or
      ((red * 255.0f + 0.5f).toInt() shl 16) or
      ((green * 255.0f + 0.5f).toInt() shl 8) or
      (blue * 255.0f + 0.5f).toInt()
}
