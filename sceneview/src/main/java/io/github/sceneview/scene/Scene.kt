package io.github.sceneview.scene

import com.google.android.filament.Scene
import io.github.sceneview.Filament

fun Scene.destroy() = Filament.engine.destroyScene(this)