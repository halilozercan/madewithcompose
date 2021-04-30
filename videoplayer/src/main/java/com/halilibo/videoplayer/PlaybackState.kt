package com.halilibo.videoplayer

import com.google.android.exoplayer2.Player.*

enum class PlaybackState(val value: Int) {

    IDLE(STATE_IDLE),
    BUFFERING(STATE_BUFFERING),
    READY(STATE_READY),
    ENDED(STATE_ENDED);

    companion object {
        fun of(value: Int): PlaybackState {
            return values().first { it.value == value }
        }
    }
}