package com.halilibo.madewithcompose.brush

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.roundToInt

private val BULB_RADIUS = 18.dp
private val SOCKET_HEIGHT = 10.dp
private val SOCKET_WIDTH = 10.dp
private val CABLE_WIDTH = 2.dp

@Composable
fun BrushDemo() {
  BoxWithConstraints(modifier = Modifier.background(Color.Black)) {
    val size = Size(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())
    val lightingTextDemoState = remember(size) { LightingTextDemoState(size) }
    // Text on the background, put first in the box.
    LightingText(state = lightingTextDemoState)
    // Cable attachment to bulb can be drawn freely
    Box(
      modifier = Modifier
        .matchParentSize()
        .bulbAttachments(lightingTextDemoState)
    )
    // Bulb will float freely in the box. Its initial location should be; x = 50%, y = 100dp
    LightBulb(state = lightingTextDemoState)
  }
}

fun Modifier.bulbAttachments(
  state: LightingTextDemoState
): Modifier {
  return drawBehind {
    val bulbPosition = state.bulbOffset

    drawLine(
      Color.DarkGray,
      start = center.copy(y = 0f),
      end = bulbPosition,
      CABLE_WIDTH.toPx()
    )

    val angle = atan((bulbPosition.x - center.x) / (bulbPosition.y))

    val rectHeight = SOCKET_HEIGHT.toPx()
    val rectWidth = SOCKET_WIDTH.toPx()

    withTransform({
      rotate(-(angle / PI * 180).toFloat(), pivot = center.copy(y = 0f))
    }) {
      drawRect(
        Color.LightGray,
        topLeft = Offset(
          x = center.x - rectWidth / 2,
          y = (bulbPosition - center.copy(y = 0f)).getDistance() - (BULB_RADIUS.toPx() + rectHeight)
        ),
        size = Size(rectWidth, rectHeight),
      )
    }
  }
}

@Composable
fun LightBulb(state: LightingTextDemoState) {
  val coroutineScope = rememberCoroutineScope()

  Box(Modifier
    .size(36.dp)
    .offset {
      val position = state.bulbOffset - Offset(BULB_RADIUS.toPx(), BULB_RADIUS.toPx())
      IntOffset(position.x.roundToInt(), position.y.roundToInt())
    }
    .background(
      state.bulbBackground,
      shape = CircleShape
    )
    .shadow(16.dp, shape = CircleShape, clip = false)
    .pointerInput(Unit) {
      detectTapGestures { state.toggleLight() }
    }
    .pointerInput(Unit) {
      detectDragGestures(
        onDragEnd = { coroutineScope.launch { state.onDragEnd() } },
        onDragCancel = { coroutineScope.launch { state.onDragEnd() } },
        onDrag = { change, dragAmount ->
          coroutineScope.launch { state.onDrag(change, dragAmount) }
        }
      )
    }
  )
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun LightingText(state: LightingTextDemoState) {
  Text(
    text = lipsum,
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .verticalScroll(state.scrollState),
    style = LocalTextStyle.current.merge(
      TextStyle(
        fontSize = 18.sp,
        brush = state.lightSourceBrush
      )
    )
  )
}

val lipsum = buildAnnotatedString {
  append("\n\n\n\n\n\n")
  withStyle(SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)) {
    append("Jetpack Compose lets you do awesome things 🎉")
  }
  append(
    """
      
      
  Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam luctus, libero in laoreet viverra, dolor diam dignissim sapien, id lacinia quam metus ut nunc. Sed sit amet leo est. Donec vulputate congue bibendum. Maecenas scelerisque pellentesque nulla eget vulputate. Nam tempor varius finibus. Donec sit amet blandit nibh. Fusce vel ex massa. Pellentesque tincidunt tincidunt lacus sit amet lobortis.

  Quisque malesuada est vel ex iaculis iaculis. Aenean luctus lorem vitae tincidunt placerat. Nullam porta finibus neque. Nullam ipsum justo, feugiat et interdum ut, rhoncus a tellus. Suspendisse urna nibh, pulvinar tempor dolor non, dictum condimentum sem. Nullam molestie, nisl sit amet imperdiet varius, urna justo scelerisque massa, vitae imperdiet magna turpis ac lacus. Integer pulvinar tincidunt nisl a iaculis. Nulla eu lacus a leo posuere rhoncus.

  Donec venenatis viverra metus ac tempor. Etiam iaculis lacinia arcu ut sodales. Phasellus sollicitudin sit amet felis a molestie. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Curabitur vulputate neque in mi feugiat, in rutrum mauris viverra. Aenean vestibulum, justo eu molestie rhoncus, justo dolor faucibus tellus, vitae lobortis libero elit quis velit. Nulla nec efficitur eros. Nunc interdum elit sit amet ipsum egestas condimentum non a massa. Aenean in tortor sollicitudin, malesuada risus in, ultricies purus. Donec nec erat ante. Donec convallis tellus eu lacus placerat, eu feugiat turpis tempus.

  Nulla facilisi. Maecenas quis diam a mi auctor dapibus id quis est. Ut ultricies purus non ultrices posuere. Integer ac diam eu magna dapibus fringilla. Etiam posuere ultrices sem, ut mattis metus facilisis in. Nam viverra id libero vel rutrum. Donec pellentesque tortor sed nisl laoreet bibendum. Vestibulum sagittis tortor vel libero porta sagittis. Etiam odio tortor, vestibulum non auctor vitae, blandit nec justo. Nulla facilisi. Duis ut consequat velit.

  Etiam sed est massa. Sed in metus semper, luctus mauris vel, placerat eros. Nulla interdum leo at diam efficitur, ac dapibus tellus elementum. Nunc ipsum tellus, interdum rutrum interdum vel, ullamcorper eget orci. Vivamus vitae posuere purus, nec facilisis dui. Aenean ornare nisi est, eget faucibus nulla mollis ac. Curabitur auctor feugiat dui, eu hendrerit lorem. Nulla tortor libero, placerat in leo sodales, dictum dictum velit. Nam luctus sollicitudin quam, dapibus iaculis odio sollicitudin at. Aenean aliquet venenatis pellentesque. Nam in rutrum ligula. Vivamus mattis enim quis consectetur viverra. Vivamus placerat orci condimentum aliquet commodo. 
""".trimIndent()
  )
}