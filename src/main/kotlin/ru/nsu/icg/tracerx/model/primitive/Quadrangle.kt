package ru.nsu.icg.tracerx.model.primitive

import ru.nsu.icg.tracerx.model.common.Point3D

data class Quadrangle(
    val a: Point3D,
    val b: Point3D,
    val c: Point3D,
    val d: Point3D,
    override val optics: Optics
) : Primitive3D(optics) {
}