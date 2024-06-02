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
//        val normal = ((b - a) * (c - a)).normalized()
//
//        val divisor = normal scalarTimes ray.direction
//        if (abs(divisor) < eps) return listOf()
//
//        val delta = normal scalarTimes (a - ray.start) / divisor
//        if (delta < 0f) return listOf()
//
//        val intersection = ray.start + ray.direction * delta
//
//        val totalArea = triangleArea(a, b, c)
//        val p1 = triangleArea(a, b, intersection)
//        val p2 = triangleArea(b, c, intersection)
//        val p3 = triangleArea(a, c, intersection)
//        if (abs(p1 + p2 + p3 - totalArea) > eps) {
//            return listOf()
//        }
//        return listOf(Intersection(this, intersection, normal))
        val edge1 = b - a
        val edge2 = c - a

        val normal = (edge1 * edge2).normalized()

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