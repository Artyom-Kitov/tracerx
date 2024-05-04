package ru.nsu.icg.tracerx.model.common

import kotlin.math.sqrt

data class Vector3D(
    val x: Float,
    val y: Float,
    val z: Float,
    val w: Float = 1f
) {
    operator fun plus(other: Vector3D) = Vector3D(x + other.x, y + other.y, z + other.z, w + other.w)

    operator fun minus(other: Vector3D) = Vector3D(x - other.x, y - other.y, z - other.z, w - other.w)

    operator fun times(scalar: Float) = Vector3D(x * scalar, y * scalar, z * scalar, w * scalar)

    operator fun times(other: Vector3D) = Vector3D(
        x = y * other.z - z * other.y,
        y = -(x * other.z - z * other.x),
        z = x * other.y - y * other.x,
        w = 1f
    )

    operator fun div(scalar: Float) = Vector3D(x / scalar, y / scalar, z / scalar, w / scalar)

    infix fun scalarTimes(other: Vector3D) = x * other.x + y * other.y + z * other.z

    fun norm2() = sqrt(x * x + y * y + z * z)

    fun normalized(): Vector3D {
        val norm = norm2()
        return Vector3D(
            x = x / norm,
            y = y / norm,
            z = z / norm,
            w = w
        )
    }
}
