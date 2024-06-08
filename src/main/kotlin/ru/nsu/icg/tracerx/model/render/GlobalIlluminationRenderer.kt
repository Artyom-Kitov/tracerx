package ru.nsu.icg.tracerx.model.render

import kotlinx.coroutines.*
import ru.nsu.icg.tracerx.model.common.Vector3D
import ru.nsu.icg.tracerx.model.primitive.Intersection
import ru.nsu.icg.tracerx.model.primitive.Ray
import ru.nsu.icg.tracerx.model.scene.LightSource
import ru.nsu.icg.tracerx.model.scene.Render
import ru.nsu.icg.tracerx.model.scene.Scene
import java.awt.Dimension
import java.awt.image.BufferedImage
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class GlobalIlluminationRenderer(
    scene: Scene,
    render: Render,
    screenDimension: Dimension,
    nThreads: Int
) : Renderer(scene, render, screenDimension, nThreads) {

    override suspend fun renderBatch(batch: Batch, result: BufferedImage) = coroutineScope {
        val pixels = screenDimension.width * screenDimension.height
        for (y in batch.yFrom..<batch.yTo) {
            for (x in batch.xFrom..<batch.xTo) {
                yield()

                val ray = rayAt(x, y)
                val pixel = trace(ray)
                synchronized(result) {
                    result.setRGB(x, y, pixel)
                }

                val total = totalRendered.incrementAndGet()
                val progress = (total.toFloat() / pixels * 100f).toInt()
                if (progress > prevProgress.get()) {
                    progressSetter(progress)
                    prevProgress.set(progress)
                }
            }
        }
    }

    private fun trace(ray: Ray): Int {
        val intersections = mutableListOf<Intersection>()
        findReflections(ray, intersections, depth)
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

        val gammaReversed = 1f / gamma
        val red = max(0, min(255, (prevR.pow(gammaReversed) * 255f).toInt()))
        val green = max(0, min(255, (prevG.pow(gammaReversed) * 255f).toInt()))
        val blue = max(0, min(255, (prevB.pow(gammaReversed) * 255f).toInt()))

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
            if (primitive.intersects(ray)) {
                return null
            }
        }
        return ray.direction
    }

    private fun findReflections(ray: Ray, result: MutableList<Intersection>, currentDepth: Int) {
        if (currentDepth <= 0) return
        val intersection = findClosestIntersection(ray) ?: return
        result.add(intersection)
        val reflected = intersection.reflect(ray)
        findReflections(reflected, result, currentDepth - 1)
    }

    private fun rayAt(x: Int, y: Int): Ray {
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