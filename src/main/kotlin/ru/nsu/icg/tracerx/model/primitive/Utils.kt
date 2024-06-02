package ru.nsu.icg.tracerx.model.primitive

import ru.nsu.icg.tracerx.model.common.Vector3D
import kotlin.math.sqrt

fun triangleArea(a: Vector3D, b: Vector3D, c: Vector3D): Float {
    val l1 = (b - a).norm2()
    val l2 = (c - a).norm2()
    val l3 = (b - c).norm2()
    val p = (l1 + l2 + l3) / 2f
    return sqrt(p * (p - l1) * (p - l2) * (p - l3))
}
