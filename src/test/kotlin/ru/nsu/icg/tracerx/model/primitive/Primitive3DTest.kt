package ru.nsu.icg.tracerx.model.primitive

import ru.nsu.icg.tracerx.model.common.Vector3D
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class Primitive3DTest {
    private val optics = Optics(Vector3D(0.9f, 0.9f, 0.9f), Vector3D(0f, 0f, 0f), 2000f)

    @Test
    fun `triangle intersection`() {
        val a = Vector3D(0f, 2f, 0f)
        val b = Vector3D(2f, -1f, 0f)
        val c = Vector3D(-2f, -1f, 0f)
        val ray = Ray(Vector3D(0f, 0f, 1f), Vector3D(0f, 0f, -1f))
        val triangle = Triangle(a, b, c, optics)
        val intersection = triangle.intersectionWith(ray)[0].point
        assertEquals(intersection, Vector3D(0f, 0f, 0f))

        val ray2 = ray.copy(start = Vector3D(0f, 0f, -1f, 0f))
        assertTrue(triangle.intersectionWith(ray2).isEmpty())

        val ray3 = ray.copy(start = Vector3D(10f, 0f, 1f))
        assertTrue(triangle.intersectionWith(ray3).isEmpty())

        val ray4 = ray.copy(start = Vector3D(1f, 0f, 1f, 1f))
        val intersection2 = triangle.intersectionWith(ray4)[0].point
        assertEquals(intersection2, Vector3D(1f, 0f, 0f))

        val ray5 = ray.copy(start = Vector3D(0f, 2.01f, 1f))
        assertTrue(triangle.intersectionWith(ray5).isEmpty())
    }

    @Test
    fun `quadrangle intersection`() {
        val a = Vector3D(1f, 1f, 0f)
        val b = Vector3D(-1f, 1f, 0f)
        val c = Vector3D(-1f, -1f, 0f)
        val d = Vector3D(1f, -1f, 0f)
        val quadrangle = Quadrangle(a, c, b, d, optics)

        val ray = Ray(Vector3D(0.1f, 0f, 1f), Vector3D(0f, 0f, -1f))
        val intersection = quadrangle.intersectionWith(ray)[0].point
        assertEquals(intersection, Vector3D(0.1f, 0f, 0f))
    }

    @Test
    fun `sphere intersection`() {
        val sphere = Sphere(1f, Vector3D(0f, 0f, 0f), optics)
        var ray = Ray(Vector3D(2f, 0f, 0f), Vector3D(-1f, 0f, 0f))
        var intersections = sphere.intersectionWith(ray).map { it.point }
        assertTrue(intersections.contains(Vector3D(1f, 0f, 0f)) && intersections.contains(Vector3D(-1f, 0f, 0f)))

        ray = ray.copy(start = Vector3D(2f, 0f, 1.0f))
        intersections = sphere.intersectionWith(ray).map { it.point }
        assertTrue(intersections.size == 1 && intersections[0] == Vector3D(0f, 0f, 1f))

        ray = ray.copy(start = Vector3D(10f, 0f, 1.1f))
        intersections = sphere.intersectionWith(ray).map { it.point }
        assertTrue(intersections.isEmpty())
    }
}