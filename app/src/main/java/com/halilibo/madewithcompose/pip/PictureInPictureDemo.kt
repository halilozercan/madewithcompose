package com.halilibo.madewithcompose.pip

import android.app.PictureInPictureParams
import android.graphics.Rect
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.UUID
import kotlin.math.roundToInt

@Composable
fun PictureInPictureDemo() {
    val pipState = LocalPipState.current
    var pipEnabledCheckboxState by remember { mutableStateOf(true) }

    pipState.PipEnabled(isEnabled = pipEnabledCheckboxState)

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column {
            if (pipState.isInPictureInPicture.value) {
                Text("Currently in PiP")
            } else {
                Text("This is not PiP")

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = pipEnabledCheckboxState,
                        onCheckedChange = { isChecked ->
                            pipEnabledCheckboxState = isChecked
                        }
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(text = "Should enter PiP?")
                }
            }
        }
    }
}

private class DefaultPipState : PipState {
    private val _isInPictureInPicture = mutableStateOf(false)
    override val isInPictureInPicture: State<Boolean> = _isInPictureInPicture

    override fun pictureInPictureModeChanged(newValue: Boolean) {
        _isInPictureInPicture.value = newValue
    }

    override fun onUserLeaveHint(activity: ComponentActivity) {
        if (shouldEnterPip.value) {
            activity.enterPictureInPictureMode(pipParams)
        }
    }

    override val pipParams
        get() = PictureInPictureParams.Builder()
            .setAspectRatio(aspectRatio)
            .apply { sourceRectHint?.let(::setSourceRectHint) }
            .build()

    private var aspectRatio = Rational(16, 9)
    override fun setAspectRatio(width: Float, height: Float) {
        aspectRatio = Rational(width.roundToInt(), height.roundToInt())
    }

    private var sourceRectHint: Rect? = null
    override fun setSourceRectHint(rect: Rect) {
        sourceRectHint = rect
    }

    private val shouldEnterPipValues = mutableStateMapOf<String, Boolean>()
    override val shouldEnterPip: State<Boolean> = derivedStateOf {
        if (shouldEnterPipValues.isEmpty()) {
            false
        } else {
            shouldEnterPipValues.values.reduce { acc, b -> acc && b }
        }
    }

    @Composable
    override fun PipEnabled(isEnabled: Boolean) {
        val uuid = remember { UUID.randomUUID().toString() }
        DisposableEffect(isEnabled) {
            shouldEnterPipValues[uuid] = isEnabled
            onDispose {
                shouldEnterPipValues.remove(uuid)
            }
        }
    }
}

interface PipState {
    val isInPictureInPicture: State<Boolean>
    val shouldEnterPip: State<Boolean>

    val pipParams: PictureInPictureParams

    fun setAspectRatio(width: Float, height: Float)
    fun setSourceRectHint(rect: Rect)

    fun pictureInPictureModeChanged(newValue: Boolean)
    fun onUserLeaveHint(activity: ComponentActivity)

    @Composable fun PipEnabled(isEnabled: Boolean)
}

val LocalPipState = compositionLocalOf<PipState> { error("Not defined") }

@Stable
data class ComposePip(
    val ProvidePip: @Composable (content: @Composable () -> Unit) -> Unit,
    val pipState: PipState
) : PipState by pipState

fun composePip(): ComposePip {
    val controller = DefaultPipState()
    val provider: @Composable (content: @Composable () -> Unit) -> Unit = @Composable { content ->
        CompositionLocalProvider(LocalPipState provides controller) {
            content()
        }
    }

    return ComposePip(provider, controller)
}
