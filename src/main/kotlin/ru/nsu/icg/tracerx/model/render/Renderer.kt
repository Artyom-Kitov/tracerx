package ru.nsu.icg.tracerx.model.render

import ru.nsu.icg.tracerx.model.primitive.Ray
import ru.nsu.icg.tracerx.model.scene.LightSource
import ru.nsu.icg.tracerx.model.scene.Render
import ru.nsu.icg.tracerx.model.structure.TracingStructure
import java.awt.Color
import java.awt.Dimension
import java.awt.image.BufferedImage
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

abstract class Renderer(
    protected val structure: TracingStructure,
    protected val lightSources: List<LightSource>,
    protected val diffusionColor: Color,
    render: Render,
    protected val screenDimension: Dimension,
    protected val nThreads: Int
) {
    private val viewDirection = (render.observationPosition - render.cameraPosition).normalized()
    protected val cameraPosition = render.cameraPosition
    private val up = render.up
    private val screenDistance = render.zNear
    protected val viewDistance = render.zFar
    private val screenWidth = render.screenWidth
    private val screenHeight = render.screenHeight
    protected val quality = render.quality

    private val rightDirection = (viewDirection * up).normalized()
    private val down = up * -1f
    private val screenCenter = cameraPosition + viewDirection * screenDistance
    private val upperLeft = screenCenter + (up * (screenHeight / 2f)) - (rightDirection * (screenWidth / 2))

    private val dx = screenWidth / screenDimension.width
    private val dy = screenHeight / screenDimension.height

    protected val depth = render.renderDepth
    protected val backgroundColor = render.backgroundColor
    private val gamma = render.gamma

    var progressSetter: suspend (Int) -> Unit = {}

    protected data class Batch(
        val xFrom: Int,
        val yFrom: Int,
        val xTo: Int,
        val yTo: Int
    )

    protected fun findBatches(): List<Batch> {
        if (nThreads == 1) return listOf(Batch(0, 0, screenDimension.width, screenDimension.height))

        val batches = mutableListOf<Batch>()
        val threadsWidth = nThreads / 2
        val threadsHeight = 2

        val di = screenDimension.width / threadsWidth
        val dj = screenDimension.height / threadsHeight
        for (i in 0..<threadsWidth) {
            for (j in 0..<threadsHeight) {
                var xTo = di * i + di
                var yTo = dj * j + dj
                if (i == threadsWidth - 1) {
                    xTo += screenDimension.width % threadsWidth
                }
                if (j == threadsHeight - 1) {
                    yTo += screenDimension.height % threadsHeight
                }
                batches.add(
                    Batch(
                        di * i, dj * j,
                        xTo, yTo
                    )
                )
            }
        }
        return batches
    }

    abstract suspend fun render(): BufferedImage

    protected fun rayAt(x: Int, y: Int): Ray {
        val direction = ((upperLeft + down * (y * dy + dy / 2f) + rightDirection * (x * dx + dx / 2f)) - cameraPosition)
            .normalized()
        return Ray(cameraPosition, direction)
    }

    // for FINE quality
    protected fun raysAt(x: Int, y: Int): List<Ray> {
        val result = mutableListOf<Ray>()
        for (i in 0..1) {
            for (j in 0..1) {
                result.add(
                    Ray(cameraPosition,
                        ((upperLeft + down * (y * dy + dy * j) + rightDirection * (x * dx + dx * i)) - cameraPosition)
                        .normalized()
                    )
                )
            }
        }
        return result
    }

    protected data class Intensity(
        var r: Float = 0f,
        var g: Float = 0f,
        var b: Float = 0f
    )

    protected fun gammaCorrection(intensity: Intensity): Int {
        val gammaReversed = 1f / gamma
        val red = max(0, min(255, (intensity.r.pow(gammaReversed) * 255f).toInt()))
        val green = max(0, min(255, (intensity.g.pow(gammaReversed) * 255f).toInt()))
        val blue = max(0, min(255, (intensity.b.pow(gammaReversed) * 255f).toInt()))
        return (red shl 16) or (green shl 8) or blue
    }
}
