package ru.nsu.icg.tracerx.model.primitive

import ru.nsu.icg.tracerx.model.common.Vector3D

data class Ray(
    val start: Vector3D,
    val direction: Vector3D
)