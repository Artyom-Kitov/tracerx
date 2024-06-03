package ru.nsu.icg.tracerx.model.primitive

import ru.nsu.icg.tracerx.model.common.Vector3D

data class Box(
    val min: Vector3D,
    val max: Vector3D,
    override val optics: Optics
) : Primitive3D(optics) {

    private val x0 = min.x
    private val y0 = min.y
    private val z0 = min.z

    private val x1 = max.x
    private val y1 = max.y
    private val z1 = max.z

    private val a = min
    private val b = Vector3D(x1, y0, z0)
    private val c = Vector3D(x0, y1, z0)
    private val d = Vector3D(x1, y1, z0)
    private val e = Vector3D(x0, y0, z1)
    private val f = Vector3D(x1, y0, z1)
    private val g = Vector3D(x0, y1, z1)
    private val h = max

    private val edges = listOf(
        Quadrangle(a, b, c, d, optics),
        Quadrangle(e, f, h, g, optics),
        Quadrangle(a, b, f, e, optics),
        Quadrangle(c, d, h, g, optics),
        Quadrangle(a, c, g, e, optics),
        Quadrangle(b, d, h, f, optics)
    )

    override val lines: List<List<Vector3D>>
        get() {
            val result = mutableListOf<List<Vector3D>>()
            result.add(listOf(
                a, c, d, b, a
            ))
            result.add(listOf(
                e, g, h, f, e
            ))
            result.add(listOf(a, e))
            result.add(listOf(b, f))
            result.add(listOf(c, g))
            result.add(listOf(d, h))

            return result
        }

    override fun intersects(ray: Ray): Boolean {
        for (edge in edges) {
            if (edge.intersects(ray)) return true
        }
        return false
    }

    override fun intersectionWith(ray: Ray): List<Intersection> {
        val result = mutableListOf<Intersection>()
        for (edge in edges) {
            result.addAll(edge.intersectionWith(ray))
        }
        return result
    }
}