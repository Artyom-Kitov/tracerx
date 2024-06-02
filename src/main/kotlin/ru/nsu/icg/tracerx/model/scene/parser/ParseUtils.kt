package ru.nsu.icg.tracerx.model.scene.parser

import ru.nsu.icg.tracerx.model.common.Vector3D
import ru.nsu.icg.tracerx.model.primitive.*
import ru.nsu.icg.tracerx.model.scene.LightSource
import ru.nsu.icg.tracerx.model.scene.RenderQuality
import java.awt.Color
import java.util.*

fun readQuality(scanner: Scanner): RenderQuality {
    val error = "invalid render quality"
    try {
        val str = scanner.next()
        return RenderQuality.valueOf(str.uppercase())
    } catch (e: NoSuchElementException) {
        throw SceneParseException(error)
    } catch (e: InputMismatchException) {
        throw SceneParseException(error)
    } catch (e: IllegalArgumentException) {
        throw SceneParseException(e.message ?: error)
    }
}

fun readPrimitives(scanner: Scanner): List<Primitive3D> {
    val result = mutableListOf<Primitive3D>()
    while (scanner.hasNext()) {
        val type = scanner.next()
        val primitive = when (type.uppercase()) {
            "SPHERE" -> readSphere(scanner)
            "BOX" -> readBox(scanner)
            "TRIANGLE" -> readTriangle(scanner)
            "QUADRANGLE" -> readQuadrangle(scanner)
            else -> throw SceneParseException("invalid primitive type: $type")
        }
        result.add(primitive)
    }
    return result
}

fun readQuadrangle(scanner: Scanner): Primitive3D = Quadrangle(
    a = readPoint3D(scanner, "invalid quadrangle first vertex"),
    b = readPoint3D(scanner, "invalid quadrangle second vertex"),
    c = readPoint3D(scanner, "invalid quadrangle third vertex"),
    d = readPoint3D(scanner, "invalid quadrangle fourth vertex"),
    optics = readOptics(scanner, "invalid quadrangle optics")
)

fun readTriangle(scanner: Scanner): Primitive3D = Triangle(
    a = readPoint3D(scanner, "invalid triangle first vertex"),
    b = readPoint3D(scanner, "invalid triangle second vertex"),
    c = readPoint3D(scanner, "invalid triangle third vertex"),
    optics = readOptics(scanner, "invalid triangle optics")
)

fun readBox(scanner: Scanner): Primitive3D = Box(
    min = readPoint3D(scanner, "invalid box min coordinates"),
    max = readPoint3D(scanner, "invalid box max coordinates"),
    optics = readOptics(scanner, "invalid box optics")
)

fun readSphere(scanner: Scanner): Primitive3D = Sphere(
    center = readPoint3D(scanner, "invalid sphere position"),
    radius = readFloat(scanner, "invalid radius"),
    optics = readOptics(scanner, "invalid sphere optics")
)

fun readOptics(scanner: Scanner, error: String): Optics = Optics(
    diffusion = readPoint3D(scanner, error),
    specularity = readPoint3D(scanner, error),
    specularityPower = readFloat(scanner, error)
)

fun readLightSources(scanner: Scanner, nSources: Int): List<LightSource> {
    val result = mutableListOf<LightSource>()
    for (i in 1..nSources) {
        val error = "invalid source[$i] coordinates"
        val position = readPoint3D(scanner, error)
        val lightSource = LightSource(position, readColor(scanner, "invalid source[$i] color"))
        result.add(lightSource)
    }
    return result
}

fun readPoint3D(scanner: Scanner, error: String) = Vector3D(
    x = readFloat(scanner, error),
    y = readFloat(scanner, error),
    z = readFloat(scanner, error),
    w = 1f
)

fun readFloat(scanner: Scanner, error: String): Float = try {
    scanner.nextFloat()
} catch (e: NoSuchElementException) {
    throw SceneParseException(error)
} catch (e: InputMismatchException) {
    throw SceneParseException(error)
}

fun readInt(scanner: Scanner, error: String): Int = try {
    scanner.nextInt()
} catch (e: NoSuchElementException) {
    throw SceneParseException(error)
} catch (e: InputMismatchException) {
    throw SceneParseException(error)
}

fun readColor(scanner: Scanner, error: String): Color {
    try {
        val r = scanner.nextInt()
        val g = scanner.nextInt()
        val b = scanner.nextInt()
        if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
            throw SceneParseException(error)
        }
        return Color(r, g, b)
    } catch (e: NoSuchElementException) {
        throw SceneParseException(error)
    } catch (e: InputMismatchException) {
        throw SceneParseException(error)
    }
}

fun combineLines(lines: List<String>): String {
    val cleanLines = removeComments(lines)
    val builder = StringBuilder()
    cleanLines.forEach { builder.append("$it ") }
    return builder.toString()
}

private const val COMMENT_SEPARATOR = "//"

private fun removeComments(lines: List<String>): List<String> {
    val result: MutableList<String> = mutableListOf()
    lines.forEach {
        val commentIndex = it.indexOf(COMMENT_SEPARATOR)
        if (commentIndex == -1) {
            result.add(it)
        } else if (commentIndex != 0) {
            result.add(it.substring(0, commentIndex))
        }
    }
    return result
}
