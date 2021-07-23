package com.halilibo.screenshot

import android.view.View
import android.view.ViewTreeObserver
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

suspend fun View.awaitDraw(): View {
    suspendCancellableCoroutine<Unit> { continuation ->
        var handled = false
        val onDrawListener = object : ViewTreeObserver.OnDrawListener {
            override fun onDraw() {
                if (handled) return
                handled = true

                // Wait for the next frame.
                post {
                    viewTreeObserver.removeOnDrawListener(this)
                    continuation.resume(Unit)
                }
            }
        }
        viewTreeObserver.addOnDrawListener(onDrawListener)
        continuation.invokeOnCancellation { viewTreeObserver.removeOnDrawListener(onDrawListener) }
    }
    return this
}
