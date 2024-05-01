package ru.nsu.icg.tracerx.model.primitive

import ru.nsu.icg.tracerx.model.common.Point3D

data class Box(
    val min: Point3D,
    val max: Point3D,
    override val optics: Optics
) : Primitive3D(optics) {
}