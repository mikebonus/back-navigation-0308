package io.github.sceneview.node

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.ar.sceneform.rendering.RenderableInstance
import io.github.sceneview.R
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale

/**
 * ### Construct a [LoadingNode] with it Position, Rotation and Scale
 *
 * @param position See [Node.position]
 * @param rotation See [Node.rotation]
 * @param scale See [Node.scale]
 */
open class LoadingNode(
    context: Context,
    lifecycle: Lifecycle,
    position: Position = DEFAULT_POSITION,
    rotation: Rotation = DEFAULT_ROTATION,
    scale: Scale = DEFAULT_SCALE,
    layoutResId: Int,
    textNode: String? = ""
) : ViewNode(position, rotation, scale) {
    var textView: TextView? = null
    var textLine: String? = ""
    init {
        isSelectable = false
        textLine = textNode
        loadView(context, lifecycle, layoutResId)
    }

    override fun onViewLoaded(renderableInstance: RenderableInstance, view: View) {
        super.onViewLoaded(renderableInstance, view)

        textView = view.findViewById(R.id.dots)
        textView?.text = textLine
        renderableInstance.apply {
            isShadowCaster = false
            isShadowReceiver = false
            renderPriority = 0
        }
    }

    var text
        get() = textView?.text
        set(value) {
            textView?.text = value
        }

    var with
        get() = textView?.width
        set(value) {
            textView?.width = value!!
        }

}