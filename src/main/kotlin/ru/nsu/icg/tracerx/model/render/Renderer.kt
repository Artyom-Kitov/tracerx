package ru.nsu.icg.tracerx.model.render

import kotlinx.coroutines.*
import ru.nsu.icg.tracerx.model.primitive.Ray
import ru.nsu.icg.tracerx.model.scene.Render
import ru.nsu.icg.tracerx.model.scene.Scene
import java.awt.Dimension
import java.awt.image.BufferedImage
import java.util.concurrent.atomic.AtomicInteger

abstract class Renderer(
    scene: Scene,
    render: Render,
    protected val screenDimension: Dimension,
    private val nThreads: Int
) {
    private val viewDirection = (render.observationPosition - render.cameraPosition).normalized()
    protected val cameraPosition = render.cameraPosition
    private val up = render.up
    private val screenDistance = render.zNear
    protected val viewDistance = render.zFar
    private val screenWidth = render.screenWidth
    private val screenHeight = render.screenHeight

    protected val rightDirection = (viewDirection * up).normalized()
    protected val down = up * -1f
    private val screenCenter = cameraPosition + viewDirection * screenDistance
    protected val upperLeft = screenCenter + (up * (screenHeight / 2f)) - (rightDirection * (screenWidth / 2))

    protected val dx = screenWidth / screenDimension.width
    protected val dy = screenHeight / screenDimension.height

    protected val depth = render.renderDepth
    protected val diffusionColor = scene.diffusionColor
    protected val backgroundColor = render.backgroundColor
    protected val gamma = render.gamma

    protected val lightSources = scene.lightSources
    protected val primitives = scene.primitives

    var progressSetter: suspend (Int) -> Unit = {}

    data class Batch(
        val xFrom: Int,
        val yFrom: Int,
        val xTo: Int,
        val yTo: Int
    )

    private fun findBatches(): List<Batch> {
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

    private val totalRendered = AtomicInteger(0)
    private val prevProgress = AtomicInteger(0)

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun render(): BufferedImage = coroutineScope {
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

    private fun rayAt(x: Int, y: Int): Ray {
        val direction = ((upperLeft + down * (y * dy) + rightDirection * (x * dx)) - cameraPosition).normalized()
        return Ray(cameraPosition, direction)
    }

    abstract fun trace(ray: Ray): Int
}
