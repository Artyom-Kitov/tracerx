package ru.nsu.icg.tracerx.model.structure

import ru.nsu.icg.tracerx.model.common.Vector3D
import ru.nsu.icg.tracerx.model.findBoundingBox
import ru.nsu.icg.tracerx.model.primitive.*
import kotlin.math.max

private data class BoundingCube(
    val point: Vector3D,
    val length: Float
) {
    val upper = Vector3D(point.x + length, point.y + length, point.z + length)

    val points: List<Vector3D>
        get() {
            val result = mutableListOf<Vector3D>()
            for (x in 0..1) {
                for (y in 0..1) {
                    for (z in 0..1) {
                        val p = point + Vector3D(x * length, y * length, z * length)
                        result.add(p)
                    }
                }
            }
            return result
        }

    companion object {
        fun of(primitives: List<Primitive3D>): BoundingCube {
            val (min, max) = primitives.findBoundingBox()
            val dx = max.x - min.x
            val dy = max.y - min.y
            val dz = max.z - min.z
            return BoundingCube(min, max(dx, max(dy, dz)))
        }

        private fun Box.containsPoint(p: Vector3D): Boolean {
            return p.x >= min.x && p.x <= max.x
                    && p.y >= min.y && p.y <= max.y
                    && p.z >= min.z && p.z <= max.z
        }
    }

    fun intersectsRay(ray: Ray): Boolean {
        var tNear = Float.MIN_VALUE
        var tFar = Float.MAX_VALUE

        val (x0, y0, z0) = ray.start
        val (xd, yd, zd) = ray.direction
        val (xl, yl, zl) = point
        val (xh, yh, zh) = upper

        if (xd == 0f && (x0 < xl || x0 > xh)) return false
        var t1 = (xl - x0) / xd
        var t2 = (xh - x0) / xd
        if (t2 < t1) {
            val tmp = t1
            t1 = t2
            t2 = tmp
        }
        if (t1 > tNear) tNear = t1
        if (t2 < tFar) tFar = t2
        if (tNear > tFar || tFar < 0) return false

        if (yd == 0f && (y0 < yl || y0 > yh)) return false
        t1 = (yl - y0) / yd
        t2 = (yh - y0) / yd
        if (t2 < t1) {
            val tmp = t1
            t1 = t2
            t2 = tmp
        }
        if (t1 > tNear) tNear = t1
        if (t2 < tFar) tFar = t2
        if (tNear > tFar || tFar < 0) return false

        if (zd == 0f && (z0 < zl || z0 > zh)) return false
        t1 = (zl - z0) / zd
        t2 = (zh - z0) / zd
        if (t2 < t1) {
            val tmp = t1
            t1 = t2
            t2 = tmp
        }
        if (t1 > tNear) tNear = t1
        if (t2 < tFar) tFar = t2
        if (tNear > tFar || tFar < 0) return false

        return true
    }

    fun split(): List<BoundingCube> {
        val result = mutableListOf<BoundingCube>()
        val delta = length / 2f
        for (x in 0..1) {
            for (y in 0..1) {
                for (z in 0..1) {
                    val p = point + Vector3D(x * delta, y * delta, z * delta)
                    result.add(BoundingCube(p, delta))
                }
            }
        }
        return result
    }

    fun intersects(primitive: Primitive3D): Boolean {
        return when (primitive) {
            is Triangle -> intersectsTriangle(primitive)
            is Box -> intersectsBox(primitive)
            is Quadrangle -> intersectsQuadrangle(primitive)
            is Sphere -> intersectsSphere(primitive)
        }
    }

    fun containsPoint(p: Vector3D): Boolean {
        return p.x >= point.x && p.x <= point.x + length
                && p.y >= point.y && p.y <= point.y + length
                && p.z >= point.z && p.z <= point.z + length
    }

    private fun intersectsTriangle(triangle: Triangle): Boolean {
        // Check if any of the triangle vertices are inside the box
        if (containsPoint(triangle.a) || containsPoint(triangle.b) || containsPoint(triangle.c)) return true

        val axes = mutableListOf(
            Vector3D(1f, 0f, 0f), Vector3D(0f, 1f, 0f), Vector3D(0f, 0f, 1f),
            triangle.normal
        )

        val cubeEdges = listOf(
            Vector3D(1f, 0f, 0f), Vector3D(0f, 1f, 0f), Vector3D(0f, 0f, 1f)
        )
        val triangleEdges = listOf(
            triangle.b - triangle.a,
            triangle.c - triangle.b,
            triangle.a - triangle.c
        )

        for (edge1 in cubeEdges) {
            for (edge2 in triangleEdges) {
                axes.add(edge1 * edge2)
            }
        }

        fun isSeparatingAxis(axis: Vector3D, triangle: Triangle): Boolean {
            val triangleProjections = listOf(
                triangle.a scalarTimes axis,
                triangle.b scalarTimes axis,
                triangle.c scalarTimes axis
            )
            val minTriProj = triangleProjections.minOrNull() ?: 0f
            val maxTriProj = triangleProjections.maxOrNull() ?: 0f

            val cubePoints = points

            val cubeProjections = cubePoints.map { it scalarTimes axis }

            val minBoxProj = cubeProjections.minOrNull() ?: 0f
            val maxBoxProj = cubeProjections.maxOrNull() ?: 0f

            return maxTriProj < minBoxProj || minTriProj > maxBoxProj
        }

        for (axis in axes) {
            if (isSeparatingAxis(axis, triangle)) {
                return false
            }
        }
        return true
    }

    private fun intersectsQuadrangle(quadrangle: Quadrangle): Boolean {
        return intersectsTriangle(quadrangle.triangle1) || intersectsTriangle(quadrangle.triangle2)
    }

    private fun intersectsBox(box: Box): Boolean {
        return containsPoint(box.min) || containsPoint(box.max)
                || box.containsPoint(point) || box.containsPoint(upper)
    }

    private fun intersectsSphere(sphere: Sphere): Boolean {
        val center = point + Vector3D(length / 2f, length / 2f, length / 2f)
        val fromSphere = (center - sphere.center).normalized()
        val p = sphere.center + fromSphere * sphere.radius
        return containsPoint(p)
    }
}

private class OcTreeNode(
    val bounds: BoundingCube,
    val primitives: List<Primitive3D>,
    val children: List<OcTreeNode>,
) {
    val isLeaf get() = children.isEmpty()
}

class OcTree private constructor(private val root: OcTreeNode) : TracingStructure {

    companion object {
        fun buildFrom(primitives: List<Primitive3D>, maxDepth: Int): OcTree {
            val bounds = BoundingCube.of(primitives)
            val root = buildImpl(bounds, primitives, maxDepth)
            return OcTree(root)
        }

        private fun buildImpl(bounds: BoundingCube, primitives: List<Primitive3D>, depth: Int): OcTreeNode {
            if (depth == 0 || primitives.isEmpty()) {
                return OcTreeNode(bounds, primitives, listOf())
            }
            val cubes = bounds.split()
            val children = mutableListOf<OcTreeNode>()
            for (cube in cubes) {
                val intersectingPrimitives = primitives.filter { cube.intersects(it) }
                children.add(buildImpl(cube, intersectingPrimitives, depth - 1))
            }
            return OcTreeNode(bounds, listOf(), children)
        }
    }

    override fun findAllIntersections(ray: Ray): List<Intersection> {
        val result = mutableListOf<Intersection>()
        findIntersectionsImpl(root, result, ray)
        return result
    }

    private fun findIntersectionsImpl(node: OcTreeNode, intersections: MutableList<Intersection>, ray: Ray) {
        if (node.isLeaf) {
            for (primitive in node.primitives) {
                intersections.addAll(primitive.intersectionWith(ray))
            }
            return
        }
        for (child in node.children) {
            if (child.bounds.intersectsRay(ray))
                findIntersectionsImpl(child, intersections, ray)
        }
    }

    override fun hasIntersection(ray: Ray): Boolean {
        return hasIntersectionImpl(root, ray)
    }

    private fun hasIntersectionImpl(node: OcTreeNode, ray: Ray): Boolean {
        if (node.isLeaf) {
            for (primitive in node.primitives) {
                if (primitive.intersects(ray)) return true
            }
            return false
        }
        for (child in node.children) {
            if (child.bounds.intersectsRay(ray) && hasIntersectionImpl(child, ray)) return true
        }
        return false
    }
}