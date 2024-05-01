package ru.nsu.icg.tracerx.model.primitive

import ru.nsu.icg.tracerx.model.common.Point3D

data class Sphere(
    val radius: Float,
    val center: Point3D,
    override val optics: Optics
) : Primitive3D(optics) {
}