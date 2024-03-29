package com.halilibo.madewithcompose.markdowneditor

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.CodeBlockStyle
import com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.material.MaterialRichText
import com.halilibo.screenshot.ScreenshotController
import com.halilibo.screenshot.screenshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@Composable
fun MarkdownPreview(
  content: CharSequence,
  modifier: Modifier = Modifier,
  onPreviewClick: () -> Unit
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val screenshotController = remember { ScreenshotController() }
  val backgroundColor = MaterialTheme.colors.surface

  Column(modifier) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
      horizontalArrangement = Arrangement.Center
    ) {
      Text(
        text = "PREVIEW",
        letterSpacing = 8.sp,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colors.onPrimary,
        modifier = Modifier
          .background(MaterialTheme.colors.primary, shape = RoundedCornerShape(4.dp))
          .clickable { onPreviewClick() }
          .padding(4.dp)
      )
    }
    Column(
      modifier = Modifier.fillMaxSize()
    ) {
      // Scrollable Markdown
      Column(
        Modifier
          .weight(1f)
          .verticalScroll(rememberScrollState())) {
        MaterialRichText(
          style = RichTextStyle(codeBlockStyle = CodeBlockStyle(wordWrap = false)),
          modifier = Modifier
            .screenshot(screenshotController)
            .padding(8.dp)
        ) {
          Markdown(content = content.toString())
        }
      }

      var isButtonEnabled by remember { mutableStateOf(true) }

      Button(
        enabled = isButtonEnabled,
        modifier = Modifier
          .align(Alignment.End)
          .padding(8.dp), onClick = {
          coroutineScope.launch {
            isButtonEnabled = false

            val bitmap = screenshotController
              .drawWithScreenshot {
                drawRect(backgroundColor)
                it()
              }
              .asAndroidBitmap()

            saveImage(bitmap, context)?.shareImage(context)

            isButtonEnabled = true
          }
        }) {
        Icon(imageVector = Icons.Default.Share, contentDescription = "share")
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Share")
      }
    }
  }
}

/**
 * Saves the image as PNG to the app's cache directory.
 * @param image Bitmap to save.
 * @return Uri of the saved file or null
 */
private suspend fun saveImage(image: Bitmap, context: Context): Uri? = withContext(Dispatchers.IO) {
  val imagesFolder = File(context.cacheDir, "images")
  var uri: Uri? = null
  try {
    imagesFolder.mkdirs()
    val file = File(imagesFolder, "shared_image.png")
    val stream = FileOutputStream(file)
    image.compress(Bitmap.CompressFormat.PNG, 90, stream)
    stream.flush()
    stream.close()
    uri = FileProvider.getUriForFile(context, "com.halilibo.fileprovider", file)
  } catch (e: IOException) {
    Log.d("MarkdownPreview", "IOException while trying to write file for sharing: " + e.message)
  }
  uri
}

/**
 * Shares the PNG image from [this] Uri.
 */
private fun Uri.shareImage(context: Context) {
  val intent = Intent(Intent.ACTION_SEND)
  intent.putExtra(Intent.EXTRA_STREAM, this)
  intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
  intent.type = "image/png"
  context.startActivity(intent)
}
