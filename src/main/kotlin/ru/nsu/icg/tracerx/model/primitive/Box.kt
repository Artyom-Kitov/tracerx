package ru.nsu.icg.tracerx.model.primitive

import ru.nsu.icg.tracerx.model.common.Vector3D

data class Box(
    val min: Vector3D,
    val max: Vector3D,
    override val optics: Optics
) : Primitive3D(optics) {
}