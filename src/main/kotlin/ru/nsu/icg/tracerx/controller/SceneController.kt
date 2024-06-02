package ru.nsu.icg.tracerx.controller

import ru.nsu.icg.tracerx.model.Context
import ru.nsu.icg.tracerx.model.common.Vector3D
import ru.nsu.icg.tracerx.model.scene.LightSource
import ru.nsu.icg.tracerx.model.scene.Render
import ru.nsu.icg.tracerx.model.scene.Scene
import java.awt.Color
import java.awt.Dimension
import java.awt.image.BufferedImage

class SceneController(
    private val context: Context
) {
    var screenWidth by context::screenWidth
    var screenHeight by context::screenHeight
    val backgroundColor: Color by context::backgroundColor
    val lightSources: List<LightSource> by context::projectedLightSources

    fun setScene(scene: Scene) = context.setScene(scene)
    fun setRender(render: Render) = context.setRender(render)

    fun calculateProjection(): List<List<Vector3D>> = context.calculateProjection()

    fun move(dx: Float, dy: Float, dz: Float) = context.move(dx, dy, dz)

    fun zoom(isNegative: Boolean) = context.zoom(isNegative)

    fun rotate(aroundVertical: Float, aroundHorizontal: Float) = context.rotate(aroundVertical, aroundHorizontal)

    fun init() = context.setInitPosition()

    fun startRender(screenDimension: Dimension, progressSetter: (Int) -> Unit, onDone: (BufferedImage) -> Unit) {
        context.startRender(screenDimension, progressSetter, onDone)
    }
}