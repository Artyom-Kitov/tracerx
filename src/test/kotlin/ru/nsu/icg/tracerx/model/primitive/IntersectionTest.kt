package ru.nsu.icg.tracerx.model.primitive

import org.junit.jupiter.api.Assertions.*
import ru.nsu.icg.tracerx.model.common.Vector3D
import kotlin.test.Test

internal class IntersectionTest {
    private val optics = Optics(Vector3D(0.9f, 0.9f, 0.9f), Vector3D(0f, 0f, 0f), 2000f)

    @Test
    fun reflection() {
        val a = Vector3D(0f, 2f, 0f)
        val b = Vector3D(2f, -1f, 0f)
        val c = Vector3D(-2f, -1f, 0f)
        var ray = Ray(Vector3D(0f, 0f, 1f), Vector3D(0f, 0f, -1f))
        val triangle = Triangle(a, c, b, optics)
        var intersection = triangle.intersectionWith(ray)[0]

        val reflected = intersection.reflect(ray)
        assertEquals(reflected.start, intersection.point)

        ray = Ray(Vector3D(-2f, 0f, 2f), Vector3D(2f, 0f, -2f).normalized())
        intersection = triangle.intersectionWith(ray)[0]

        // IDK how to test such things :D
        // Yet everything is OK here
        println(intersection.reflect(ray))

        val sphere = Sphere(1f, Vector3D(1f, 0f, 0f), optics)
        intersection = sphere.intersectionWith(ray)[1]
        println(intersection.reflect(ray))
    }
}