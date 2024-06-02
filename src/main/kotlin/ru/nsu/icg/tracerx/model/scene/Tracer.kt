package ru.nsu.icg.tracerx.model.scene

import ru.nsu.icg.tracerx.model.common.Vector3D
import ru.nsu.icg.tracerx.model.primitive.Primitive3D
import java.awt.Dimension
import java.awt.image.BufferedImage

class Tracer {
    var progressSetter: (Int) -> Unit = {}

    fun render(
        primitives: List<Primitive3D>,
        lightSources: List<LightSource>,
        cameraPosition: Vector3D,
        viewDirection: Vector3D,
        screenDistance: Float,
        up: Vector3D,
        screenWidth: Float, screenHeight: Float,
        screenDimension: Dimension,
        gamma: Float,
    ): BufferedImage {
        Thread.sleep(1000)
        progressSetter(50)

        Thread.sleep(1000)
        progressSetter(100)

        val result = BufferedImage(screenDimension.width, screenDimension.height, BufferedImage.TYPE_INT_RGB)
        return result
    }
}