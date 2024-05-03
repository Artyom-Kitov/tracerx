package ru.nsu.icg.tracerx.model.scene

import ru.nsu.icg.tracerx.model.primitive.Primitive3D
import java.awt.Color

data class Scene(
    val diffusionColor: Color,
    val lightSources: List<LightSource>,
    val primitives: List<Primitive3D>
)