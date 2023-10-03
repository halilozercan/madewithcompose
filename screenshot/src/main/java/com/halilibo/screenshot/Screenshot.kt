package com.halilibo.screenshot

import android.graphics.Bitmap
import android.graphics.Picture
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.math.abs

/**
 * A modifier that can delegate its draw operations to a [screenshotController] to obtain an
 * [ImageBitmap] of its contents.
 *
 * @see com.halilibo.screenshot.ScreenshotController
 */
fun Modifier.screenshot(
  screenshotController: ScreenshotController
): Modifier = this.drawWithContent {
  if (screenshotController.recordingRequested > 0) {
    val pictureCanvas =
      Canvas(
        screenshotController.picture.beginRecording(
          this.size.width.toInt(),
          this.size.height.toInt()
        )
      )
    screenshotController.density = this
    screenshotController.layoutDirection = this.layoutDirection
    draw(this, this.layoutDirection, pictureCanvas, this.size) {
      this@drawWithContent.drawContent()
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

/**
 * Controller class to request a screenshot of a Composable.
 */
class ScreenshotController {

  internal val picture: Picture = Picture()

  // These initial values are just placeholders and will be filled with actual values by the
  // screenshot modifier.
  internal var density: Density = Density(1f)
  internal var layoutDirection = LayoutDirection.Ltr

  internal var recordingRequested by mutableIntStateOf(-1)

  /**
   * Provides a [DrawScope] to draw onto a canvas that has the same size as the composable
   * which is managed by this [ScreenshotController]. Do not forget to call the provided
   * `drawScreenshot` lambda to actually draw the contents of the managed composable.
   */
  suspend fun drawWithScreenshot(block: DrawScope.(drawScreenshot: () -> Unit) -> Unit): ImageBitmap {
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
    val composeCanvas = Canvas(canvas)
    val backingDrawScope: DrawScope = CanvasDrawScope()
    backingDrawScope.draw(
      density,
      layoutDirection,
      composeCanvas,
      Size(picture.width.toFloat(), picture.height.toFloat()),
    ) {
      block {
        canvas.drawPicture(picture)
      }
    }

    return bitmap.asImageBitmap()
  }

  /**
   * Simply returns the contents of the composable that's managed by this controller on an
   * [ImageBitmap].
   */
  suspend fun draw(): ImageBitmap {
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
