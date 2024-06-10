package ru.nsu.icg.tracerx.model.render

import kotlinx.coroutines.*
import ru.nsu.icg.tracerx.model.common.Vector3D
import ru.nsu.icg.tracerx.model.primitive.Intersection
import ru.nsu.icg.tracerx.model.primitive.Ray
import ru.nsu.icg.tracerx.model.scene.LightSource
import ru.nsu.icg.tracerx.model.scene.Render
import ru.nsu.icg.tracerx.model.scene.RenderQuality
import ru.nsu.icg.tracerx.model.structure.TracingStructure
import java.awt.Color
import java.awt.Dimension
import java.awt.image.BufferedImage
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.pow
import kotlin.math.sqrt

class GlobalIlluminationRenderer(
    structure: TracingStructure,
    lightSources: List<LightSource>,
    diffusionColor: Color,
    render: Render,
    screenDimension: Dimension,
    nThreads: Int
) : Renderer(structure, lightSources, diffusionColor, render, screenDimension, nThreads) {

    private val totalRendered = AtomicInteger(0)
    private val prevProgress = AtomicInteger(0)

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun render(): BufferedImage = coroutineScope {
        totalRendered.set(0)

        val threadPool = newFixedThreadPoolContext(nThreads, "renderer")
        val batches = findBatches()
        val result = BufferedImage(screenDimension.width, screenDimension.height, BufferedImage.TYPE_INT_RGB)
        val tasks = List(batches.size) { index ->
            launch(threadPool) {
                renderBatch(batches[index], result)
            }
        }
        tasks.joinAll()

        progressSetter(0)
        result
    }

    private suspend fun renderBatch(batch: Batch, result: BufferedImage) = coroutineScope {
        val nPixels = screenDimension.width * screenDimension.height
        val s = if (quality == RenderQuality.ROUGH) 2 else 1
        for (y in batch.yFrom..<batch.yTo step s) {
            for (x in batch.xFrom..<batch.xTo step s) {
                yield()

                traceWithQuality(x, y, result)

                val total = if (quality == RenderQuality.ROUGH)
                    totalRendered.addAndGet(4)
                else
                    totalRendered.incrementAndGet()
                val progress = (total.toFloat() / nPixels * 100f).toInt()
                if (progress > prevProgress.get()) {
                    progressSetter(progress)
                    prevProgress.set(progress)
                }
            }
        }
    }

    private fun traceWithQuality(x: Int, y: Int, result: BufferedImage) {
        when (quality) {
            RenderQuality.FINE -> {
                val pixels = raysAt(x, y).map { trace(it) }
                var midR = 0f
                var midG = 0f
                var midB = 0f
                for (p in pixels) {
                    midR += p.r
                    midG += p.g
                    midB += p.b
                }
                midR /= pixels.size
                midG /= pixels.size
                midB /= pixels.size
                val intensity = Intensity(midR, midG, midB)
                synchronized(result) {
                    result.setRGB(x, y, gammaCorrection(intensity))
                }
            }
            RenderQuality.NORMAL -> {
                val ray = rayAt(x, y)
                val intensity = trace(ray)
                synchronized(result) {
                    result.setRGB(x, y, gammaCorrection(intensity))
                }
            }
            RenderQuality.ROUGH -> {
                val ray = rayAt(x, y)
                val intensity = trace(ray)
                val corrected = gammaCorrection(intensity)
                synchronized(result) {
                    result.setRGB(x, y, corrected)
                    if (x + 1 < result.width)
                        result.setRGB(x + 1, y, corrected)
                    if (y + 1 < result.height)
                        result.setRGB(x, y + 1, corrected)
                    if (x + 1 < result.width && y + 1 < result.height)
                    result.setRGB(x + 1, y + 1, corrected)
                }
            }
        }
    }

    private fun trace(ray: Ray): Intensity {
        val reflections = mutableListOf<Intersection>()
        findReflections(ray, reflections, depth)
        if (reflections.isEmpty()) return Intensity(
            backgroundColor.red / 255f,
            backgroundColor.green / 255f,
            backgroundColor.blue / 255f
        )

        var prevPoint = reflections.last().point

        val intensity = Intensity(
            r = diffusionColor.red.toFloat() / 255f,
            g = diffusionColor.green.toFloat() / 255f,
            b = diffusionColor.blue.toFloat() / 255f
        )
        for (i in reflections.size - 1 downTo 0) {
            val optics = reflections[i].primitive.optics

            val v = if (i != 0) (reflections[i - 1].point - reflections[i].point).normalized()
                else (cameraPosition - reflections[0].point).normalized()

            var (rI, gI, bI) = findSourcesIntensities(v, reflections[i])

            rI += diffusionColor.red.toFloat() / 255f * optics.diffusion.x
            gI += diffusionColor.green.toFloat() / 255f * optics.diffusion.y
            bI += diffusionColor.blue.toFloat() / 255f * optics.diffusion.z

            if (i != reflections.size - 1) {
                val d = sqrt(prevPoint.squaredDistanceBetween(reflections[i].point))
                val att = attenuation(d)
                rI += optics.specularity.x * att * intensity.r
                gI += optics.specularity.y * att * intensity.g
                bI += optics.specularity.z * att * intensity.b
            }
            prevPoint = reflections[i].point
            intensity.r = rI
            intensity.g = gI
            intensity.b = bI
        }
        return intensity
    }

    private fun findSourcesIntensities(v: Vector3D, intersection: Intersection): Intensity {
        val result = Intensity()

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

                result.r += ir
                result.g += ig
                result.b += ib
            }
        }
        return result
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
        if (structure.hasIntersection(ray)) return null
        return ray.direction
    }

    private fun findReflections(ray: Ray, result: MutableList<Intersection>, currentDepth: Int) {
        if (currentDepth <= 0) return
        val intersection = structure.findClosestIntersection(ray) ?: return
        result.add(intersection)
        val reflected = intersection.reflect(ray)
        findReflections(reflected, result, currentDepth - 1)
    }
}