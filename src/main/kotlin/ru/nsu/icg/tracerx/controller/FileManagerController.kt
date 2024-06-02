package ru.nsu.icg.tracerx.controller

import ru.nsu.icg.tracerx.model.Context
import ru.nsu.icg.tracerx.model.common.Vector3D
import ru.nsu.icg.tracerx.model.scene.Render
import ru.nsu.icg.tracerx.model.scene.Scene
import ru.nsu.icg.tracerx.model.scene.parser.RenderParser
import ru.nsu.icg.tracerx.model.scene.parser.SceneParseException
import ru.nsu.icg.tracerx.model.scene.parser.SceneParser
import java.awt.Color
import java.io.File

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
        return RenderParser(stream.reader()).parse()
    }

    fun loadFullScene(file: File): Pair<Scene, List<Pair<String, Render>>> {
        val scene = SceneParser(file.reader()).parse()
        val folder = file.parentFile

        val children = folder.listFiles()
        val renders: MutableList<Pair<String, Render>> = mutableListOf()
        if (children != null) {
            for (child in children) {
                if (child.extension.lowercase() == "render") {
                    renders.add(child.name to RenderParser(child.reader()).parse())
                }
            }
        }
        return scene to renders
    }
}