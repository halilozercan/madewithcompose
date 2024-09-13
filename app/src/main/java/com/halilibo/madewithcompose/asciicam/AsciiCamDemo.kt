package com.halilibo.madewithcompose.asciicam

import android.graphics.Bitmap
import android.graphics.Typeface
import android.text.TextPaint
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import java.util.concurrent.Executors
import kotlin.math.floor

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AsciiCamDemo() {
    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )

    if (cameraPermissionState.status.isGranted) {
        CameraPreview()
    } else {
        Column {
            val textToShow = if (cameraPermissionState.status.shouldShowRationale) {
                "The camera is kinda the whole deal mate, give me the permission to amaze you!"
            } else {
                "Grant this permission to experience a whole new kind of Camera"
            }
            Text(textToShow)
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text("Request permission")
            }
        }
    }
}

@Composable
fun CameraPreview() {
    var text by remember { mutableStateOf(CameraText("", emptyList())) }
    // width is how many characters can fit in a line, height is how many lines can fit on screen
    var textCanvasSize by remember { mutableStateOf(IntSize.Zero) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Remember a LifecycleCameraController for this composable
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            imageAnalysisOutputImageFormat = ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
            // Default to a very low resolution.
            imageAnalysisTargetSize = CameraController.OutputSize(Size(120, 160))
            imageAnalysisBackpressureStrategy = ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST

            setImageAnalysisAnalyzer(cameraExecutor) {
                // this is highly inefficient but Bitmap API lets me quickly hack my way around
                var bitmap = it.toBitmap()

                if (textCanvasSize == IntSize.Zero) return@setImageAnalysisAnalyzer

                bitmap = Bitmap.createScaledBitmap(
                    bitmap,
                    textCanvasSize.width,
                    textCanvasSize.height,
                    false
                )

                val stringBuilder = StringBuilder()
                val colors = mutableListOf<Int>()

                for (i in 0 until bitmap.width) {
                    for (j in bitmap.height - 1 downTo 0) {
                        val color = bitmap.getColor(i, j)
                        val r = color.red() * 255
                        val g = color.green() * 255
                        val b = color.blue() * 255
                        val value = b * 0.1145 + g * 0.5866 + r * 0.2989
                        val paletteIndex = floor(value / (256.0 / (palette.size))).toInt()
                        stringBuilder.append(palette[paletteIndex])
                        colors.add(color.toArgb())
                    }
                    stringBuilder.append("\n")
                    colors.add(0)
                }

                bitmap.recycle()
                text = CameraText(stringBuilder.toString(), colors)
                it.close()
            }
            bindToLifecycle(lifecycleOwner)
        }
    }

    Box {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_START
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    controller = cameraController
                }
            },
            onRelease = {
                cameraController.unbind()
            }
        )
        MonospaceText(
            text,
            onSizeAvailable = {
                textCanvasSize = it
            },
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            fontSize = 8.sp,
        )
    }
}

@Composable
fun MonospaceText(
    text: CameraText,
    fontSize: TextUnit,
    onSizeAvailable: (IntSize) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val paint = remember {
        TextPaint().apply {
            color = android.graphics.Color.WHITE
            typeface = Typeface.MONOSPACE
        }
    }

    var tw by remember { mutableStateOf(0) }
    var th by remember { mutableStateOf(0) }

    Box(
        modifier
            .onSizeChanged { canvasSize ->
                paint.textSize = with(density) { fontSize.toPx() }
                val rect = android.graphics.Rect()
                palette.fastForEach {
                    paint.getTextBounds("$it", 0, 1, rect)
                    tw = maxOf(tw, rect.width())
                    th = maxOf(th, rect.height())
                }
                onSizeAvailable(
                    IntSize(canvasSize.width / tw, canvasSize.height / th)
                )
            }
            .drawWithContent {
                drawContent()
                drawIntoCanvas { canvas ->
                    var curLeft = 0f
                    var curTop = 0f
                    for (i in text.chars.indices) {
                        val char = text.chars[i]
                        if (char == '\n') {
                            curLeft = 0f
                            curTop += th
                        } else {
                            paint.color = text.colors[i]
                            canvas.nativeCanvas.drawText(char.toString(), 0, 1, curLeft, curTop, paint)
                            curLeft += tw
                        }
                    }
                }
            })
}

data class CameraText(
    val chars: String,
    val colors: List<Int>
)

val palette = listOf(' ', '.', '.', '/', 'c', '(', '@', '#', '8')
