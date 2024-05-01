package ru.nsu.icg.tracerx.model.primitive

import ru.nsu.icg.tracerx.model.common.Point3D

data class Triangle(
    val a: Point3D,
    val b: Point3D,
    val c: Point3D,
    override val optics: Optics
) : Primitive3D(optics) {
}