package ru.nsu.icg.tracerx.model

import ru.nsu.icg.tracerx.model.common.Vector3D
import ru.nsu.icg.tracerx.model.primitive.Primitive3D
import ru.nsu.icg.tracerx.model.scene.LightSource
import ru.nsu.icg.tracerx.model.scene.Render
import ru.nsu.icg.tracerx.model.scene.Scene
import java.awt.Color

class Context {
    private val lightSources: MutableList<LightSource> = mutableListOf()
    private val primitives: MutableList<Primitive3D> = mutableListOf()

    private var cameraPosition = Vector3D(0f, 0f, 0f) // eye
    private var observationPosition = Vector3D(0f, 0f, 0f) // view
    private var up = Vector3D(0f, 0f, 0f) // up

    private var screenWidth = 0f // sw
    private var screenHeight = 0f // sh

    private var screenDistance = 0f // zn
    private var viewDistance = 0f // zf

    private var diffusionColor = Color.BLACK
    private var backgroundColor = Color.BLACK

    fun setContextParameters(scene: Scene, render: Render) {
        lightSources.clear()
        primitives.clear()
        lightSources.addAll(scene.lightSources)
        primitives.addAll(scene.primitives)

        cameraPosition = render.cameraPosition
        observationPosition = render.observationPosition
        up = render.up
        screenWidth = render.screenWidth
        screenHeight = render.screenHeight
        screenDistance = render.zNear
        viewDistance = render.zFar
    }
}