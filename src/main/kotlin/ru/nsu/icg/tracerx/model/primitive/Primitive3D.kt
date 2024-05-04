package ru.nsu.icg.tracerx.model.primitive

import ru.nsu.icg.tracerx.model.common.Vector3D

abstract class Primitive3D(
    open val optics: Optics
) {
    abstract val lines: List<List<Vector3D>>
}