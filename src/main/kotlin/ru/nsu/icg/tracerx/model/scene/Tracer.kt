package ru.nsu.icg.tracerx.model.scene

import ru.nsu.icg.tracerx.model.common.Vector3D
import ru.nsu.icg.tracerx.model.primitive.Intersection
import ru.nsu.icg.tracerx.model.primitive.Primitive3D
import ru.nsu.icg.tracerx.model.primitive.Ray
import java.awt.Color
import java.awt.Dimension
import java.awt.image.BufferedImage
import kotlin.math.pow
import kotlin.math.sqrt

data class Tracer(
    val primitives: List<Primitive3D>,
    val lightSources: List<LightSource>,
    val depth: Int,
    val cameraPosition: Vector3D,
    val viewDirection: Vector3D,
    val screenDistance: Float,
    val up: Vector3D,
    val screenWidth: Float, val screenHeight: Float,
    val screenDimension: Dimension,
    val gamma: Float,
    val backgroundColor: Color,
    val diffusionColor: Color
) {
    var progressSetter: (Int) -> Unit = {}

    private val rightDirection = (viewDirection * up).normalized()
    private val down = up * -1f
    private val screenCenter = cameraPosition + viewDirection * screenDistance
    private val upperLeft = screenCenter + (up * (screenHeight / 2f)) - (rightDirection * (screenWidth / 2))

    fun render(): BufferedImage? {
        val result = BufferedImage(screenDimension.width, screenDimension.height, BufferedImage.TYPE_INT_RGB)

        val dx = screenWidth / screenDimension.width
        val dy = screenHeight / screenDimension.height

        val pixels = screenDimension.width * screenDimension.height
        var totalRendered = 0
        var prevPercent = 0
        for (y in 0..<screenDimension.height) {
            for (x in 0..<screenDimension.width) {
                if (Thread.currentThread().isInterrupted) return null

                val ray = rayAt(x, y, dx, dy)

                result.setRGB(x, y, trace(ray))

                totalRendered++
                val progress = (totalRendered.toFloat() / pixels * 100f).toInt()
                if (prevPercent != progress) {
                    prevPercent = progress
                    progressSetter(progress)
                }
            }
        }

        return result
    }

    private fun trace(ray: Ray): Int {
        val intersections = mutableListOf<Intersection>()
        findIntersections(ray, intersections, depth)
        if (intersections.isEmpty()) return backgroundColor.rgb

        var prevPoint = intersections.last().point
        var prevR = diffusionColor.red.toFloat() / 255f
        var prevG = diffusionColor.green.toFloat() / 255f
        var prevB = diffusionColor.blue.toFloat() / 255f
        for (i in intersections.size - 1 downTo 0) {
            val optics = intersections[i].primitive.optics

            val v = if (i != 0) (intersections[i - 1].point - intersections[i].point).normalized()
                else (cameraPosition - intersections[0].point).normalized()

            var (rI, gI, bI) = findSourcesIntensities(v, intersections[i])

            rI += diffusionColor.red.toFloat() / 255f
            gI += diffusionColor.green.toFloat() / 255f
            bI += diffusionColor.blue.toFloat() / 255f

            if (i != intersections.size - 1) {
                val d = sqrt(prevPoint.squaredDistanceBetween(intersections[i].point))
                val att = if (i != 0) attenuation(d) else 1f
                rI += optics.specularity.x * att * prevR
                gI += optics.specularity.y * att * prevG
                bI += optics.specularity.z * att * prevB
            }
            prevPoint = intersections[i].point
            prevR = rI
            prevG = gI
            prevB = bI
        }

        val red = (prevR * 255f).toInt()
        val green = (prevG * 255f).toInt()
        val blue = (prevB * 255f).toInt()

        return (red shl 16) or (green shl 8) or blue
    }

    private fun findSourcesIntensities(v: Vector3D, intersection: Intersection): Triple<Float, Float, Float> {
        var rI = 0f
        var gI = 0f
        var bI = 0f

        for (i in lightSources.indices) {
            val source = lightSources[i]
            val optics = intersection.primitive.optics
            val direction = sourceDirection(source, intersection.point)

            if (direction != null) {
                val r = intersection.reflect(Ray(source.position, (intersection.point - source.position).normalized()))
                val att = attenuation(sqrt(intersection.point.squaredDistanceBetween(source.position)))

                val ir = globalLightIntensity(source.color.red, att, optics.diffusion.x, optics.specularity.x,
                    intersection.normal, direction, r.direction, v, optics.specularityPower)
                val ig = globalLightIntensity(source.color.green, att, optics.diffusion.y, optics.specularity.y,
                    intersection.normal, direction, r.direction, v, optics.specularityPower)
                val ib = globalLightIntensity(source.color.blue, att, optics.diffusion.z, optics.specularity.z,
                    intersection.normal, direction, r.direction, v, optics.specularityPower)

                rI += ir
                gI += ig
                bI += ib
            }
        }

        return Triple(rI, gI, bI)
    }

    private fun globalLightIntensity(i: Int, att: Float, kd: Float, ks: Float,
                                     n: Vector3D, l: Vector3D, r: Vector3D, v: Vector3D, power: Float): Float {
        var res = i.toFloat() / 255f * att
        res *= kd * (n scalarTimes l) + ks * (r scalarTimes v).pow(power)
        return res
    }

    private fun attenuation(d: Float) = 1f / (1f + d)

    private fun sourceDirection(source: LightSource, point: Vector3D): Vector3D? {
        val direction = (source.position - point).normalized()
        val ray = Ray(point, direction)
        for (primitive in primitives) {
            if (primitive.intersectionWith(ray).isNotEmpty()) {
                return null
            }
        }
        return ray.direction
    }

    private fun findIntersections(ray: Ray, result: MutableList<Intersection>, currentDepth: Int) {
        if (currentDepth <= 0) return
        val intersection = findClosestIntersection(ray) ?: return
        result.add(intersection)
        val reflected = intersection.reflect(ray)
        findIntersections(reflected, result, currentDepth - 1)
    }

    private fun rayAt(x: Int, y: Int, dx: Float, dy: Float): Ray {
        val direction = ((upperLeft + down * (y * dy) + rightDirection * (x * dx)) - cameraPosition).normalized()
        return Ray(cameraPosition, direction)
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

        var minDistance = 1000000000f
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
}