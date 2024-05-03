package ru.nsu.icg.tracerx.model.primitive

import ru.nsu.icg.tracerx.model.common.Vector3D

data class Quadrangle(
    val a: Vector3D,
    val b: Vector3D,
    val c: Vector3D,
    val d: Vector3D,
    override val optics: Optics
) : Primitive3D(optics) {
}