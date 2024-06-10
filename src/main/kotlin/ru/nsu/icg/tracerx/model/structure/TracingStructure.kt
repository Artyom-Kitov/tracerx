package ru.nsu.icg.tracerx.model.structure

import ru.nsu.icg.tracerx.model.primitive.Intersection
import ru.nsu.icg.tracerx.model.primitive.Ray

interface TracingStructure {

    fun findClosestIntersection(ray: Ray): Intersection? {
        val intersections = findAllIntersections(ray)
        if (intersections.isEmpty()) return null

        var minDistance = Float.MAX_VALUE
        var result = intersections[0]
        for (intersection in intersections) {
            val sd = intersection.point.squaredDistanceBetween(ray.start)
            if (sd < minDistance) {
                minDistance = sd
                result = intersection
            }
        }
        return result
    }

    fun findAllIntersections(ray: Ray): List<Intersection>

    fun hasIntersection(ray: Ray): Boolean
}