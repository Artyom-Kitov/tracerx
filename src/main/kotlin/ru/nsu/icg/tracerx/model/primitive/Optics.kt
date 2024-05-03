package ru.nsu.icg.tracerx.model.primitive

import ru.nsu.icg.tracerx.model.common.Vector3D

data class Optics(
    val diffusion: Vector3D,
    val specularity: Vector3D,
    val specularityPower: Float
)
