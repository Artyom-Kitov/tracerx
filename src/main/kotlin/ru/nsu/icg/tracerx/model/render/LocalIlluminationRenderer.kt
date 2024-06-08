package ru.nsu.icg.tracerx.model.render

import kotlinx.coroutines.yield
import ru.nsu.icg.tracerx.model.common.Vector3D
import ru.nsu.icg.tracerx.model.primitive.Intersection
import ru.nsu.icg.tracerx.model.primitive.Ray
import ru.nsu.icg.tracerx.model.scene.LightSource
import ru.nsu.icg.tracerx.model.scene.Render
import ru.nsu.icg.tracerx.model.scene.Scene
import java.awt.Dimension
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class LocalIlluminationRenderer(
    scene: Scene, render: Render, screenDimension: Dimension, nThreads: Int
) : Renderer(scene, render, screenDimension, nThreads) {

    override fun trace(ray: Ray): Int {
        val intersection = findClosestIntersection(ray) ?: return backgroundColor.rgb
        val optics = intersection.primitive.optics

        var rI = diffusionColor.red / 255f * optics.diffusion.x
        var gI = diffusionColor.green / 255f * optics.diffusion.y
        var bI = diffusionColor.blue / 255f * optics.diffusion.z

        val v = (cameraPosition - intersection.point).normalized()
        for (source in lightSources) {
            val direction = sourceDirection(source, intersection.point) ?: continue
            val r = intersection.reflect(Ray(source.position, (intersection.point - source.position).normalized()))

            val att = attenuation(sqrt(source.position.squaredDistanceBetween(intersection.point)))

            rI += att * source.color.red / 255f * optics.diffusion.x * (intersection.normal scalarTimes direction)
            gI += att * source.color.green / 255f * optics.diffusion.y * (intersection.normal scalarTimes direction)
            bI += att * source.color.blue / 255f * optics.diffusion.z * (intersection.normal scalarTimes direction)

            rI += att * source.color.red / 255f * optics.specularity.x * (r.direction scalarTimes v).pow(optics.specularityPower)
            gI += att * source.color.green / 255f * optics.specularity.y * (r.direction scalarTimes v).pow(optics.specularityPower)
            bI += att * source.color.blue / 255f * optics.specularity.z * (r.direction scalarTimes v).pow(optics.specularityPower)
        }

        val gammaReversed = 1f / gamma
        val red = max(0, min(255, (rI.pow(gammaReversed) * 255f).toInt()))
        val green = max(0, min(255, (gI.pow(gammaReversed) * 255f).toInt()))
        val blue = max(0, min(255, (bI.pow(gammaReversed) * 255f).toInt()))

        return (red shl 16) or (green shl 8) or blue
    }

    private fun sourceDirection(source: LightSource, point: Vector3D): Vector3D? {
        val direction = (source.position - point).normalized()
        val ray = Ray(point, direction)
        for (primitive in primitives) {
            if (primitive.intersects(ray)) {
                return null
            }
        }
        return ray.direction
    }

    private fun findAllIntersections(ray: Ray): List<Intersection> {
        val intersections = mutableListOf<Intersection>()
        for (primitive in primitives) {
            intersections.addAll(primitive.intersectionWith(ray))
        }
        return intersections
    }

    private fun findClosestIntersection(ray: Ray): Intersection? {
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

    private fun attenuation(d: Float) = 1f / (1f + d)
}
