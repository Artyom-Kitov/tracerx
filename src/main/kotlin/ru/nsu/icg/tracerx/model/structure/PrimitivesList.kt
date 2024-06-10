package ru.nsu.icg.tracerx.model.structure

import ru.nsu.icg.tracerx.model.primitive.Intersection
import ru.nsu.icg.tracerx.model.primitive.Primitive3D
import ru.nsu.icg.tracerx.model.primitive.Ray

class PrimitivesList(private val primitives: List<Primitive3D>) : TracingStructure {
    override fun findAllIntersections(ray: Ray): List<Intersection> {
        val intersections = mutableListOf<Intersection>()
        for (primitive in primitives) {
            intersections.addAll(primitive.intersectionWith(ray))
        }
        return intersections
    }

    override fun hasIntersection(ray: Ray): Boolean {
        for (primitive in primitives) {
            if (primitive.intersects(ray)) return true
        }
        return false
    }
}