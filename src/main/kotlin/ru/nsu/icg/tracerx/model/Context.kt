package ru.nsu.icg.tracerx.model

import ru.nsu.icg.tracerx.model.common.Matrix
import ru.nsu.icg.tracerx.model.common.Vector3D
import ru.nsu.icg.tracerx.model.primitive.Primitive3D
import ru.nsu.icg.tracerx.model.scene.LightSource
import ru.nsu.icg.tracerx.model.scene.Render
import ru.nsu.icg.tracerx.model.scene.Scene
import java.awt.Color
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

    private var diffusionColor = Color.BLACK
    var backgroundColor = Color.BLACK

    private val lines: List<List<Vector3D>>
        get() {
            val result = mutableListOf<List<Vector3D>>()
            primitives.forEach { result.addAll(it.lines) }
            return result
        }

    fun setContextParameters(scene: Scene, render: Render) {
        lightSources.clear()
        primitives.clear()
        lightSources.addAll(scene.lightSources)
        primitives.addAll(scene.primitives)

        cameraPosition = render.cameraPosition
        viewDirection = (render.observationPosition - render.cameraPosition).normalized()
        up = render.up
        screenWidth = render.screenWidth
        screenHeight = render.screenHeight
        screenDistance = render.zNear
        viewDistance = render.zFar
    }

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

        val result = mutableListOf<MutableList<Vector3D>>()
        for (line in lines) {
            val projected = mutableListOf<Vector3D>()
            var toAdd = true
            for (vector in line) {
                val v = resultingMatrix * vector
                if (v.x < screenDistance) {
                    toAdd = false
                    break
                }
                projected.add(Vector3D(v.x / v.w, v.y / v.w, v.z / v.w, 1f))
            }
            if (toAdd) result.add(projected)
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
}