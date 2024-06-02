package ru.nsu.icg.tracerx.model.common

import kotlin.math.sqrt

data class Vector3D(
    val x: Float,
    val y: Float,
    val z: Float,
    var w: Float = 1f
) {
    operator fun plus(other: Vector3D) = Vector3D(x + other.x, y + other.y, z + other.z, 1f)

    operator fun minus(other: Vector3D) = Vector3D(x - other.x, y - other.y, z - other.z, 1f)

    operator fun times(scalar: Float) = Vector3D(x * scalar, y * scalar, z * scalar, 1f)

    operator fun times(other: Vector3D) = Vector3D(
        x = y * other.z - z * other.y,
        y = -(x * other.z - z * other.x),
        z = x * other.y - y * other.x,
        w = 1f
    )

    operator fun div(scalar: Float) = Vector3D(x / scalar, y / scalar, z / scalar, 1f)

    infix fun scalarTimes(other: Vector3D) = x * other.x + y * other.y + z * other.z

    val norm2 get() = sqrt(x * x + y * y + z * z)

    fun normalized(): Vector3D {
        val norm = norm2
        return Vector3D(
            x = x / norm,
            y = y / norm,
            z = z / norm,
            w = w
        )
    }

    fun scaled(): Vector3D = Vector3D(x / w, y / w, z / w, 1f)

    fun squaredDistanceBetween(v: Vector3D) = (x - v.x) * (x - v.x) + (y - v.y) * (y - v.y) + (z - v.z) * (z - v.z)
}
