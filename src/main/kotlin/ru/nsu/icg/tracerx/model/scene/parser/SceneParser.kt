package ru.nsu.icg.tracerx.model.scene.parser

import ru.nsu.icg.tracerx.model.scene.Scene
import java.io.Reader
import java.util.*
import kotlin.jvm.Throws

class SceneParser(private val reader: Reader) {
    @Throws(SceneParseException::class)
    fun parse(): Scene {
        val sceneStr = combineLines(reader.readLines())
        val scanner = Scanner(sceneStr).useLocale(Locale.US)

        val backgroundColor = readColor(scanner, "invalid background color")
        val nSources = readInt(scanner, "invalid amount of light sources")
        val lightSources = readLightSources(scanner, nSources)
        val primitives = readPrimitives(scanner)
        return Scene(
            backgroundColor = backgroundColor,
            lightSources = lightSources,
            primitives = primitives
        )
    }
}