package io.github.sceneview.node

import android.view.MotionEvent
import io.github.sceneview.renderable.Renderable

interface ITap {
    fun onTap(motionEvent: MotionEvent, renderable: Renderable?)
}