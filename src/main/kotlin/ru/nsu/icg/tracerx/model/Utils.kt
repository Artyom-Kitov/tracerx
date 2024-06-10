package ru.nsu.icg.tracerx.model

import ru.nsu.icg.tracerx.model.common.Vector3D
import ru.nsu.icg.tracerx.model.primitive.Primitive3D

fun List<Primitive3D>.findBoundingBox(): Pair<Vector3D, Vector3D> {
    val first = this[0].lines[0][0]
    var (minX, minY, minZ) = first
    var (maxX, maxY, maxZ) = first
    for (primitive in this) {
        for (line in primitive.lines) {
            for (point in line) {
                if (point.x > maxX) maxX = point.x
                if (point.x < minX) minX = point.x

                if (point.y > maxY) maxY = point.y
                if (point.y < minY) minY = point.y

                if (point.z > maxZ) maxZ = point.z
                if (point.z < minZ) minZ = point.z
            }
        }
    }
    val min = Vector3D(minX, minY, minZ)
    val max = Vector3D(maxX, maxY, maxZ)
    return min to max
}
