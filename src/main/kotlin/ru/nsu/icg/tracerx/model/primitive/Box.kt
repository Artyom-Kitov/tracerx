package ru.nsu.icg.tracerx.model.primitive

import ru.nsu.icg.tracerx.model.common.Vector3D

data class Box(
    val min: Vector3D,
    val max: Vector3D,
    override val optics: Optics
) : Primitive3D(optics) {
    override val lines: List<List<Vector3D>>
        get() {
            val (x0, y0, z0) = min
            val (x1, y1, z1) = max

            val result = mutableListOf<List<Vector3D>>()
            result.add(listOf(
                Vector3D(x0, y0, z0), Vector3D(x0, y1, z0), Vector3D(x1, y1, z0), Vector3D(x1, y0, z0), Vector3D(x0, y0, z0)
            ))
            result.add(listOf(
                Vector3D(x0, y0, z1), Vector3D(x0, y1, z1), Vector3D(x1, y1, z1), Vector3D(x1, y0, z1), Vector3D(x0, y0, z1)
            ))
            result.add(listOf(Vector3D(x0, y0, z0), Vector3D(x0, y0, z1)))
            result.add(listOf(Vector3D(x1, y0, z0), Vector3D(x1, y0, z1)))
            result.add(listOf(Vector3D(x0, y1, z0), Vector3D(x0, y1, z1)))
            result.add(listOf(Vector3D(x1, y1, z0), Vector3D(x1, y1, z1)))

            return result
        }

    override fun intersectionWith(ray: Ray): List<Intersection> {
        TODO("Not yet implemented")
    }
}