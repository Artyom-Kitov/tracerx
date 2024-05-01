package ru.nsu.icg.tracerx.model.primitive

import ru.nsu.icg.tracerx.model.common.Point3D

data class Optics(
    val diffusion: Point3D,
    val specularity: Point3D,
    val specularityPower: Float
)
