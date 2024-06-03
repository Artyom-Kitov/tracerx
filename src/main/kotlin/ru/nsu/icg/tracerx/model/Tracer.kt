package ru.nsu.icg.tracerx.model

import ru.nsu.icg.tracerx.model.common.Vector3D
import ru.nsu.icg.tracerx.model.primitive.Intersection
import ru.nsu.icg.tracerx.model.primitive.Primitive3D
import ru.nsu.icg.tracerx.model.primitive.Ray
import ru.nsu.icg.tracerx.model.scene.LightSource
import java.awt.Color
import java.awt.Dimension
import java.awt.image.BufferedImage
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

private data class Batch(
    val xFrom: Int,
    val yFrom: Int,
    val xTo: Int,
    val yTo: Int
)

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

    private val dx = screenWidth / screenDimension.width
    private val dy = screenHeight / screenDimension.height

    private val totalRendered = AtomicInteger(0)
    private val prevProgress = AtomicInteger(0)
    private val isInterrupted = AtomicBoolean(false)

    fun render(isParallel: Boolean): BufferedImage? {
        val batches = mutableListOf<Batch>()
        if (isParallel) {
            val di = screenDimension.width / THREAD_GRID_SIZE.first
            val dj = screenDimension.height / THREAD_GRID_SIZE.second
            for (i in 0..<THREAD_GRID_SIZE.first) {
                for (j in 0..<THREAD_GRID_SIZE.second) {
                    var xTo = di * i + di
                    var yTo = dj * j + dj
                    if (i == THREAD_GRID_SIZE.first - 1) {
                        xTo += screenDimension.width % THREAD_GRID_SIZE.first
                    }
                    if (j == THREAD_GRID_SIZE.second - 1) {
                        yTo += screenDimension.height % THREAD_GRID_SIZE.second
                    }
                    batches.add(Batch(
                        di * i, dj * j,
                        xTo, yTo
                    ))
                }
            }
        } else {
            batches.add(Batch(0, 0, screenDimension.width, screenDimension.height))
        }

        totalRendered.set(0)
        val result = BufferedImage(screenDimension.width, screenDimension.height, BufferedImage.TYPE_INT_RGB)
        val threads = mutableListOf<Thread>()
        for (i in 1..batches.lastIndex) {
            val thread = Thread { renderBatch(batches[i], result) }
            threads.add(thread)
            thread.start()
        }
        renderBatch(batches[0], result)
        for (thread in threads) {
            try {
                thread.join()
            } catch (ignore: InterruptedException) {
                // nothing to do
            }
        }
        progressSetter(0)
        return if (isInterrupted.get()) null else result
    }

    private fun renderBatch(batch: Batch, result: BufferedImage) {
        val pixels = screenDimension.width * screenDimension.height
        for (y in batch.yFrom..<batch.yTo) {
            for (x in batch.xFrom..<batch.xTo) {
                if (Thread.currentThread().isInterrupted) {
                    isInterrupted.set(true)
                    return
                }

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

    private fun findIntersections(ray: Ray, result: MutableList<Intersection>, currentDepth: Int) {
        if (currentDepth <= 0) return
        val intersection = findClosestIntersection(ray) ?: return
        result.add(intersection)
        val reflected = intersection.reflect(ray)
        findIntersections(reflected, result, currentDepth - 1)
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

    companion object {
        private val THREAD_GRID_SIZE = 2 to 2
    }
}