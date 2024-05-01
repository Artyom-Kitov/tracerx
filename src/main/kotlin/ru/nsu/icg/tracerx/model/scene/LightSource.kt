package ru.nsu.icg.tracerx.model.scene

import ru.nsu.icg.tracerx.model.common.Point3D
import java.awt.Color

data class LightSource(
    val position: Point3D,
    val color: Color
)