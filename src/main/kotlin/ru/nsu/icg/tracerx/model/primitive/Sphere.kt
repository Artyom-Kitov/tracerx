package ru.nsu.icg.tracerx.model.primitive

import ru.nsu.icg.tracerx.model.common.Vector3D

data class Sphere(
    val radius: Float,
    val center: Vector3D,
    override val optics: Optics
) : Primitive3D(optics) {
    override val lines: List<List<Vector3D>>
        get() = TODO("Not yet implemented")
}