package ru.nsu.icg.tracerx.model.primitive

import ru.nsu.icg.tracerx.model.common.Vector3D

data class Quadrangle(
    val a: Vector3D,
    val b: Vector3D,
    val c: Vector3D,
    val d: Vector3D,
    override val optics: Optics
) : Primitive3D(optics) {
    override val lines: List<List<Vector3D>>
        get() = listOf(listOf(a, b, c, d, a))

    private val triangle1 = Triangle(a, b, c, optics)
    private val triangle2 = Triangle(a, c, d, optics)

    override fun intersects(ray: Ray): Boolean {
        return triangle1.intersects(ray) || triangle2.intersects(ray)
    }

    override fun intersectionWith(ray: Ray): List<Intersection> {
        return triangle1.intersectionWith(ray) + triangle2.intersectionWith(ray)
    }
}