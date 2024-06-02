package ru.nsu.icg.tracerx.model.primitive

import ru.nsu.icg.tracerx.model.common.Vector3D

data class Intersection(
    val primitive: Primitive3D,
    val point: Vector3D,
    var normal: Vector3D
) {
    fun reflect(ray: Ray): Ray {
        val product = ray.direction scalarTimes normal
        if (product > 0) normal *= -1f

        val direction = ray.direction - normal * 2f * (ray.direction scalarTimes normal)
        return Ray(point, direction)
    }
}
