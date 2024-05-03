package ru.nsu.icg.tracerx.model.scene

import ru.nsu.icg.tracerx.model.common.Vector3D
import java.awt.Color

data class LightSource(
    val position: Vector3D,
    val color: Color
)