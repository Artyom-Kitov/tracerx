package ru.nsu.icg.tracerx.model.common

import kotlin.math.sqrt

data class Vector3D(
    val x: Float,
    val y: Float,
    val z: Float
) {
    operator fun plus(other: Vector3D) = Vector3D(x + other.x, y + other.y, z + other.z)

    operator fun minus(other: Vector3D) = Vector3D(x - other.x, y - other.y, z - other.z)

    operator fun times(scalar: Float) = Vector3D(x * scalar, y * scalar, z * scalar)

    operator fun times(other: Vector3D) = Vector3D(
        x = y * other.z - z * other.y,
        y = -(x * other.z - z * other.x),
        z = x * other.y - y * other.x
    )

    operator fun div(scalar: Float) = Vector3D(x / scalar, y / scalar, z / scalar)

    infix fun scalarTimes(other: Vector3D) = x * other.x + y * other.y + z * other.z

    fun norm2() = sqrt(x * x + y * y + z * z)

    fun normalized() = this / norm2()
}
