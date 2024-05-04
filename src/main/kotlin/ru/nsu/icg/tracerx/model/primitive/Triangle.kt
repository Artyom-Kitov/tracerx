package ru.nsu.icg.tracerx.model.primitive

import ru.nsu.icg.tracerx.model.common.Vector3D

data class Triangle(
    val a: Vector3D,
    val b: Vector3D,
    val c: Vector3D,
    override val optics: Optics
) : Primitive3D(optics) {
    override val lines: List<List<Vector3D>>
        get() = listOf(listOf(a, b, c, a))
}