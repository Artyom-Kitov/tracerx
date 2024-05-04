package ru.nsu.icg.tracerx.controller

import ru.nsu.icg.tracerx.model.Context
import ru.nsu.icg.tracerx.model.common.Vector3D
import java.awt.Color

class SceneController(
    private val context: Context
) {
    var screenWidth by context::screenWidth
    var screenHeight by context::screenHeight
    val backgroundColor: Color by context::backgroundColor

    fun calculateProjection(): List<List<Vector3D>> = context.calculateProjection()

    fun move(dx: Float, dy: Float, dz: Float) = context.move(dx, dy, dz)

    fun zoom(isNegative: Boolean) = context.zoom(isNegative)

    fun rotate(aroundVertical: Float, aroundHorizontal: Float) = context.rotate(aroundVertical, aroundHorizontal)
}