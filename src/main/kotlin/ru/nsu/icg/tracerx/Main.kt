package ru.nsu.icg.tracerx

import ru.nsu.icg.tracerx.model.scene.parser.RenderParser
import ru.nsu.icg.tracerx.model.scene.parser.SceneParser
import java.nio.file.Files
import java.nio.file.Path

fun main() {
    println(SceneParser(Files.newBufferedReader(Path.of("scene/StandfordBunny.scene"))).parse())
    println(RenderParser(Files.newBufferedReader(Path.of("scene/StandfordBunny.render"))).parse())
}