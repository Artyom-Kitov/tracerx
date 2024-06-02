package ru.nsu.icg.tracerx.model.primitive

import ru.nsu.icg.tracerx.model.common.Vector3D
import kotlin.math.abs

data class Triangle(
    val a: Vector3D,
    val b: Vector3D,
    val c: Vector3D,
    override val optics: Optics
) : Primitive3D(optics) {
    override val lines: List<List<Vector3D>>
        get() = listOf(listOf(a, b, c, a))

    override fun intersectionWith(ray: Ray): List<Intersection> {
        val normal = ((b - a) * (c - a)).normalized()

        val divisor = normal scalarTimes ray.direction
        if (abs(divisor) < eps) return listOf()

        val delta = normal scalarTimes (a - ray.start) / divisor
        if (delta < 0f) return listOf()

        val intersection = ray.start + ray.direction * delta

        val totalArea = triangleArea(a, b, c)
        val p1 = triangleArea(a, b, intersection) / totalArea
        val p2 = triangleArea(b, c, intersection) / totalArea
        val p3 = triangleArea(a, c, intersection) / totalArea
        if (abs(p1 + p2 + p3 - 1f) > eps) {
            return listOf()
        }
        return listOf(Intersection(this, intersection, normal))
    }
}