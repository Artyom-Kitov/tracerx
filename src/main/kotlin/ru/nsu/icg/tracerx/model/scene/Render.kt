package ru.nsu.icg.tracerx.model.scene

import ru.nsu.icg.tracerx.model.common.Vector3D
import java.awt.Color

data class Render(
    val backgroundColor: Color,
    val gamma: Float,
    val renderDepth: Int,
    val quality: RenderQuality,
    val cameraPosition: Vector3D,
    val observationPosition: Vector3D,
    val up: Vector3D,
    val zNear: Float, val zFar: Float,
    val screenWidth: Float, val screenHeight: Float
)