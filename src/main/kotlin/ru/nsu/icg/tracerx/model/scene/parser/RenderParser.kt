package ru.nsu.icg.tracerx.model.scene.parser

import ru.nsu.icg.tracerx.model.common.Vector3D
import ru.nsu.icg.tracerx.model.scene.Render
import java.io.Reader
import java.util.*
import kotlin.jvm.Throws

class RenderParser(private val reader: Reader) {
    @Throws(SceneParseException::class)
    fun parse(): Render {
        val sceneStr = combineLines(reader.readLines())
        val scanner = Scanner(sceneStr).useLocale(Locale.US)
        val parsed = Render(
            backgroundColor = readColor(scanner, "invalid background color"),
            gamma = readFloat(scanner, "invalid gamma value"),
            renderDepth = readInt(scanner, "invalid depth value"),
            quality = readQuality(scanner),
            cameraPosition = readPoint3D(scanner, "invalid camera position"),
            observationPosition = readPoint3D(scanner, "invalid observation position"),
            up = readPoint3D(scanner, "invalid up vector"),
            zNear = readFloat(scanner, "invalid near clipping plane distance"),
            zFar = readFloat(scanner, "invalid far clipping plane distance"),
            screenWidth = readFloat(scanner, "invalid screen width"),
            screenHeight = readFloat(scanner, "invalid screen height")
        )
        return parsed.copy(
            up = canonizeUpVector(parsed.observationPosition, parsed.cameraPosition, parsed.up)
        )
    }

    private fun canonizeUpVector(view: Vector3D, eye: Vector3D, up: Vector3D): Vector3D {
        val z = view - eye
        return ((z * up) * z).normalized()
    }
}