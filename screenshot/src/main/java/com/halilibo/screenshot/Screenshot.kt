package com.halilibo.screenshot

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color.argb
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.viewinterop.AndroidView

interface ScreenshotController {

    suspend fun drawView(
        background: Color = Color.Unspecified
    ): ImageBitmap?

    companion object {
        operator fun invoke(): ScreenshotController {
            return DefaultScreenshotController()
        }
    }
}

@Composable
fun rememberScreenshotController(): ScreenshotController {
    return remember { DefaultScreenshotController() }
}

internal class DefaultScreenshotController: ScreenshotController {
    private var composeView: ComposeView? = null

    @Synchronized
    internal fun attach(newComposeView: ComposeView) {
        if (composeView == null) {
            this.composeView = newComposeView
        } else {
            error("ScreenshotController can only have one-to-one relation with Screenshot composable")
        }
    }

    @Synchronized
    internal fun detach(newComposeView: ComposeView) {
        if (composeView == newComposeView) {
            this.composeView = null
        }
    }

    override suspend fun drawView(background: Color): ImageBitmap? {
        return composeView
            ?.awaitDraw()
            ?.capture(background)
            ?.asImageBitmap()
    }

    private fun View.capture(background: Color): Bitmap {
        val canvas = Canvas()
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            canvas.setBitmap(this)
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
            draw(canvas)
        }
    }
}

fun argb2(alpha: Float, red: Float, green: Float, blue: Float): Int {
    return (alpha * 255.0f + 0.5f).toInt() shl 24 or
            ((red * 255.0f + 0.5f).toInt() shl 16) or
            ((green * 255.0f + 0.5f).toInt() shl 8) or
            (blue * 255.0f + 0.5f).toInt()
}

@Composable
fun Screenshot(
    screenshotController: ScreenshotController,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            ComposeView(context).apply {
                setContent {
                    content()
                }

                addOnAttachStateChangeListener(object: View.OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(v: View?) {
                        (screenshotController as? DefaultScreenshotController)?.attach(v as ComposeView)
                    }

                    override fun onViewDetachedFromWindow(v: View?) {
                        (screenshotController as? DefaultScreenshotController)?.detach(v as ComposeView)
                    }
                })
            }
        }
    )
}