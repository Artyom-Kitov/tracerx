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

    private val edge1 = b - a
    private val edge2 = c - a
    val normal = (edge1 * edge2).normalized()

    override fun intersects(ray: Ray): Boolean {
        val h = ray.direction * edge2
        val aDivisor = edge1 scalarTimes h

        if (abs(aDivisor) < eps) return false

        val f = 1.0f / aDivisor
        val s = ray.start - a
        val u = f * (s scalarTimes (h))

        if (u < 0.0 || u > 1.0) return false

        val q = s * edge1
        val v = f * (ray.direction scalarTimes q)

        if (v < 0.0 || u + v > 1.0) return false

        val t = f * (edge2 scalarTimes q)
        return t > eps
    }

    override fun intersectionWith(ray: Ray): List<Intersection> {
        val h = ray.direction * edge2
        val aDivisor = edge1 scalarTimes h

        if (abs(aDivisor) < eps) return listOf() // Луч параллелен плоскости треугольника

        val f = 1.0f / aDivisor
        val s = ray.start - a
        val u = f * (s scalarTimes (h))

        if (u < 0.0 || u > 1.0) return listOf() // Точка пересечения вне треугольника

        val q = s * edge1
        val v = f * (ray.direction scalarTimes q)

        if (v < 0.0 || u + v > 1.0) return listOf() // Точка пересечения вне треугольника

        val t = f * (edge2 scalarTimes q)
        return if (t > eps) {
            val intersection = ray.start + ray.direction * t
            listOf(Intersection(this, intersection, normal))
        } else {
            listOf() // Пересечение за пределами треугольника или луч указывает в обратную сторону
        }
    }
}