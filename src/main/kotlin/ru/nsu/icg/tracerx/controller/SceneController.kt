package ru.nsu.icg.tracerx.controller

import ru.nsu.icg.tracerx.model.Context
import ru.nsu.icg.tracerx.model.common.Vector3D
import ru.nsu.icg.tracerx.model.scene.LightSource
import ru.nsu.icg.tracerx.model.scene.Render
import ru.nsu.icg.tracerx.model.scene.Scene
import java.awt.Dimension
import java.awt.image.BufferedImage

class SceneController(
    private val context: Context
) {
    var screenWidth by context::screenWidth
    var screenHeight by context::screenHeight
    var backgroundColor by context::backgroundColor
    val lightSources: List<LightSource> by context::projectedLightSources
    var gamma by context::gamma
    var depth by context::depth
    var nThreads by context::nThreads
    var rendererSupplier by context::rendererSupplier

    fun setScene(scene: Scene) = context.setScene(scene)
    fun setRender(render: Render) = context.setRender(render)

    fun calculateProjection(): List<List<Vector3D>> = context.calculateProjection()

    fun move(dx: Float, dy: Float, dz: Float) = context.move(dx, dy, dz)

    fun zoom(isNegative: Boolean) = context.zoom(isNegative)

    fun rotate(aroundVertical: Float, aroundHorizontal: Float) = context.rotate(aroundVertical, aroundHorizontal)

    fun init(screenDimension: Dimension) = context.setInitPosition()

    suspend fun startRender(screenDimension: Dimension, progressSetter: suspend (Int) -> Unit, onDone: (BufferedImage) -> Unit) {
        context.startRender(screenDimension, progressSetter, onDone)
    }

    fun buildRender() = context.buildRender()
}