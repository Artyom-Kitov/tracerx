package ru.nsu.icg.tracerx.model

import ru.nsu.icg.tracerx.model.common.Matrix
import ru.nsu.icg.tracerx.model.common.Vector3D
import ru.nsu.icg.tracerx.model.primitive.Primitive3D
import ru.nsu.icg.tracerx.model.scene.*
import java.awt.Color
import java.awt.Dimension
import java.awt.image.BufferedImage
import kotlin.math.*

class Context {
    private val lightSources: MutableList<LightSource> = mutableListOf()
    private val primitives: MutableList<Primitive3D> = mutableListOf()

    private var cameraPosition = Vector3D(0f, 0f, 0f, 1f) // eye
    private var viewDirection = Vector3D(0f, 0f, 0f)
    private var up = Vector3D(0f, 0f, 0f, 1f) // up
    private val rightDirection
        get() = (viewDirection * up).normalized()

    var screenWidth = 0f // sw
    var screenHeight = 0f // sh

    private var screenDistance = 0f // zn
    private var viewDistance = 0f // zf

    private var diffusionColor: Color = Color.BLACK
    var backgroundColor: Color = Color.BLACK

    var depth = 4
    var gamma = 1f
    var isParallel = false

    private val lines: List<List<Vector3D>>
        get() {
            val result = mutableListOf<List<Vector3D>>()
            primitives.forEach { result.addAll(it.lines) }
            return result
        }

    fun setScene(scene: Scene) {
        diffusionColor = scene.diffusionColor
        lightSources.clear()
        primitives.clear()
        lightSources.addAll(scene.lightSources)
        primitives.addAll(scene.primitives)
    }

    fun setRender(render: Render) {
        backgroundColor = render.backgroundColor
        gamma = render.gamma
        depth = render.renderDepth

        cameraPosition = render.cameraPosition
        viewDirection = (render.observationPosition - render.cameraPosition).normalized()
        up = render.up

        screenDistance = render.zNear
        viewDistance = render.zFar

        screenWidth = render.screenWidth
        screenHeight = render.screenHeight
    }

    private var lastProjectedMatrix: Matrix? = null

    fun calculateProjection(): List<List<Vector3D>> {
        var resultingMatrix = Matrix.of(
            floatArrayOf(1f, 0f, 0f, -cameraPosition.x),
            floatArrayOf(0f, 1f, 0f, -cameraPosition.y),
            floatArrayOf(0f, 0f, 1f, -cameraPosition.z),
            floatArrayOf(0f, 0f, 0f, 1f)
        )
        var viewVector = viewDirection

        val alpha = acos(up.z)
        var rotationMatrix = rotationAroundVector((up * Vector3D(0f, 0f, 1f)).normalized(), alpha)
        viewVector = rotationMatrix * viewVector
        val beta = acos(viewVector.x)
        rotationMatrix = rotationAroundVector((viewVector * Vector3D(1f, 0f, 0f)).normalized(), beta) * rotationMatrix

        resultingMatrix = rotationMatrix * resultingMatrix

        val projectionMatrix = Matrix.of(
            floatArrayOf(1f, 0f, 0f, 0f),
            floatArrayOf(0f, screenDistance, 0f, 0f),
            floatArrayOf(0f, 0f, screenDistance, 0f),
            floatArrayOf(1f, 0f, 0f, 1f),
        )
        resultingMatrix = projectionMatrix * resultingMatrix
        lastProjectedMatrix = resultingMatrix

        val result = mutableListOf<List<Vector3D>>()
        for (line in lines) {
            val projected = mutableListOf<Vector3D>()
            var toAdd = true
            for (vector in line) {
                val v = resultingMatrix * vector
                // no limit
                if (v.x < 0) {
                    toAdd = false
                    break
                }
                projected.add(Vector3D(v.x / v.w, v.y / v.w, v.z / v.w, 1f))
            }
            if (toAdd) result.add(projected)
        }
        return result
    }

    val projectedLightSources: List<LightSource>
        get() {
            val result = mutableListOf<LightSource>()
            val projectionMatrix = lastProjectedMatrix ?: return result
            for (lightSource in lightSources) {
                val projection = projectionMatrix * lightSource.position
                if (projection.x >= 0f) {
                    result.add(LightSource(projection.scaled(), lightSource.color))
                }
            }
            return result
        }

    private fun rotationAroundVector(vector: Vector3D, angle: Float): Matrix {
        val c = cos(angle)
        val s = sin(angle)
        val (x, y, z) = vector
        return Matrix.of(
            floatArrayOf(c+(1-c)*x*x, (1-c)*x*y-s*z, (1-c)*x*z+s*y, 0f),
            floatArrayOf((1-c)*x*y+s*z, c+(1-c)*y*y, (1-c)*y*z-s*x, 0f),
            floatArrayOf((1-c)*x*z-s*y, (1-c)*y*z+s*x, c+(1-c)*z*z, 0f),
            floatArrayOf(0f, 0f, 0f, 1f),
        )
    }

    fun move(dx: Float, dy: Float, dz: Float) {
        val deltaX = viewDirection * dx
        val deltaY = rightDirection * -dy
        val deltaZ = up * dz
        val movement = deltaX + deltaY + deltaZ
        cameraPosition += movement
    }

    fun rotate(aroundVertical: Float, aroundHorizontal: Float) {
        if (aroundVertical != 0f) {
            viewDirection = rotationAroundVector(up, aroundVertical) * viewDirection
        }
        if (aroundHorizontal != 0f) {
            val rotation = rotationAroundVector(rightDirection, aroundHorizontal)
            up = rotation * up
            viewDirection = rotation * viewDirection
        }
    }

    fun zoom(negative: Boolean) {
        if (negative) {
            screenDistance *= 1.1f
        } else {
            screenDistance /= 1.1f
        }
    }

    fun setInitPosition(screenDimension: Dimension) {
        val first = primitives[0].lines[0][0]
        var (minX, minY, minZ) = first
        var (maxX, maxY, maxZ) = first
        for (primitive in primitives) {
            for (line in primitive.lines) {
                for (point in line) {
                    if (point.x > maxX) maxX = point.x
                    if (point.x < minX) minX = point.x

                    if (point.y > maxY) maxY = point.y
                    if (point.y < minY) minY = point.y

                    if (point.z > maxZ) maxZ = point.z
                    if (point.z < minZ) minZ = point.z
                }
            }
        }
        var min = Vector3D(minX, minY, minZ)
        var max = Vector3D(maxX, maxY, maxZ)
        val center = (min + max) / 2f
        min = center + ((min - center) * 1.05f)
        max = center + ((max - center) * 1.05f)

        val delta = (max.z - min.z) / (2f * tan(PI.toFloat() / 12))
        val cameraPosition = Vector3D(min.x - delta, center.y, center.z)

        val render = Render(
            backgroundColor = backgroundColor,
            gamma = gamma,
            renderDepth = 4,
            quality = RenderQuality.NORMAL,
            cameraPosition = cameraPosition.copy(w = 1f),
            observationPosition = center.copy(w = 1f),
            up = Vector3D(0f, 0f, 1f),
            zNear = delta / 2f,
            zFar = (max.x - center.x) + ((max.x - min.x) / 2f),
            screenWidth = 5f,
            screenHeight = 5f
        )
        setRender(render)
    }

    fun startRender(screenDimension: Dimension, progressSetter: (Int) -> Unit, onDone: (BufferedImage) -> Unit) {
        val tracer = Tracer(
            primitives,
            lightSources,
            depth,
            cameraPosition,
            viewDirection,
            screenDistance,
            up, screenWidth,
            screenHeight,
            screenDimension,
            gamma,
            backgroundColor,
            diffusionColor
        )
        tracer.progressSetter = progressSetter
        val image = tracer.render(isParallel)
        if (image != null) onDone(image)
    }

    fun buildRender(): Render {
        return Render(
            backgroundColor = backgroundColor,
            gamma = gamma,
            renderDepth = depth,
            quality = RenderQuality.NORMAL,
            cameraPosition = cameraPosition,
            observationPosition = (cameraPosition + viewDirection * screenDistance),
            up = up,
            zNear = screenDistance,
            zFar = viewDistance,
            screenWidth = screenWidth,
            screenHeight = screenHeight
        )
    }
}