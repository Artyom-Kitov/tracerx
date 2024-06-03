package ru.nsu.icg.tracerx.model.primitive

import ru.nsu.icg.tracerx.model.common.Vector3D
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Sphere(
    val radius: Float,
    val center: Vector3D,
    override val optics: Optics
) : Primitive3D(optics) {
    override val lines: List<List<Vector3D>>
        get() {
            val dPhi = 2 * PI.toFloat() / GENERATRIX_NUM
            val result: MutableList<MutableList<Vector3D>> = mutableListOf()
            for (i in 0..<GENERATRIX_NUM) {
                val line1 = mutableListOf<Vector3D>()
                val line2 = mutableListOf<Vector3D>()
                for (j in 0..<GENERATRIX_NUM) {
                    val point1 = Vector3D(
                        x = radius * sin(dPhi * i) * cos(dPhi * j),
                        y = radius * sin(dPhi * i) * sin(dPhi * j),
                        z = radius * cos(dPhi * i)
                    )
                    val point2 = Vector3D(
                        x = radius * sin(dPhi * j) * cos(dPhi * i),
                        y = radius * sin(dPhi * j) * sin(dPhi * i),
                        z = radius * cos(dPhi * j)
                    )
                    line1.add(center + point1)
                    line2.add(center + point2)
                }
                line1.add(line1[0])
                line2.add(line2[0])
                result.add(line1)
                result.add(line2)
            }
            return result
        }

    override fun intersects(ray: Ray): Boolean {
        val (dx, dy, dz) = ray.direction
        val (p0x, p0y, p0z) = ray.start
        val (cx, cy, cz) = center

        val a = dx * dx + dy * dy + dz * dz
        val b = 2f * (dx * (p0x - cx) + dy * (p0y - cy) + dz * (p0z - cz))
        val c = (p0x - cx) * (p0x - cx) + (p0y - cy) * (p0y - cy) + (p0z - cz) * (p0z - cz) - radius * radius

        val discriminant = b * b - 4 * a * c
        if (discriminant < 0) return false

        val sqrtDiscriminant = sqrt(discriminant)

        val nearest = (-b - sqrtDiscriminant) / (2f * a)
        return nearest >= -eps
    }

    override fun intersectionWith(ray: Ray): List<Intersection> {
        val (dx, dy, dz) = ray.direction
        val (p0x, p0y, p0z) = ray.start
        val (cx, cy, cz) = center

        val a = dx * dx + dy * dy + dz * dz
        val b = 2f * (dx * (p0x - cx) + dy * (p0y - cy) + dz * (p0z - cz))
        val c = (p0x - cx) * (p0x - cx) + (p0y - cy) * (p0y - cy) + (p0z - cz) * (p0z - cz) - radius * radius

        val discriminant = b * b - 4 * a * c
        if (discriminant < 0) return listOf()

        val sqrtDiscriminant = sqrt(discriminant)

        val nearest = (-b - sqrtDiscriminant) / (2f * a)
        if (nearest < -eps) return listOf()

        val point = ray.start + ray.direction * nearest
        val normal = (point - center).normalized()
        return listOf(Intersection(this, point, normal))
    }

    companion object {
        private const val GENERATRIX_NUM = 32
    }
}