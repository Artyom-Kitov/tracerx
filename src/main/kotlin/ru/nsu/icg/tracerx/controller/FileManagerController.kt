package ru.nsu.icg.tracerx.controller

import ru.nsu.icg.tracerx.model.Context
import ru.nsu.icg.tracerx.model.scene.Render
import ru.nsu.icg.tracerx.model.scene.Scene
import ru.nsu.icg.tracerx.model.scene.parser.RenderParser
import ru.nsu.icg.tracerx.model.scene.parser.SceneParser

class FileManagerController(
    private val context: Context
) {
    fun loadDefaultScene(): Scene {
        val stream = object {}.javaClass.getResourceAsStream("/scene/StandfordBunny.scene")
            ?: throw IllegalStateException("unable to access default scene")
        return SceneParser(stream.reader()).parse()
    }

    fun loadDefaultRender(): Render {
        val stream = object {}.javaClass.getResourceAsStream("/scene/StandfordBunny.render")
            ?: throw IllegalStateException("unable to access default render")
        val render = RenderParser(stream.reader()).parse()

        val view = render.observationPosition
        val eye = render.cameraPosition
        val up = render.up
        val z = view - eye
        return render.copy(
            up = ((z * up) * z).normalized()
        )
    }
}