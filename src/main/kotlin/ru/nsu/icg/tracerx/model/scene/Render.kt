package ru.nsu.icg.tracerx.model.scene

import ru.nsu.icg.tracerx.model.common.Point3D
import java.awt.Color

data class Render(
    val backgroundColor: Color,
    val gamma: Float,
    val renderDepth: Int,
    val quality: RenderQuality,
    val cameraPosition: Point3D,
    val observationPosition: Point3D,
    val up: Point3D,
    val zNear: Float, val zFar: Float,
    val screenWidth: Float, val screenHeight: Float
)