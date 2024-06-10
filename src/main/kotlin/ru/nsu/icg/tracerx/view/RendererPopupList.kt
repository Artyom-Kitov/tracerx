package ru.nsu.icg.tracerx.view

import ru.nsu.icg.tracerx.model.render.GlobalIlluminationRenderer
import javax.swing.JComboBox

class RendererPopupList : JComboBox<String>(RENDERER_LIST.keys.toTypedArray()) {

    val supplier get() = RENDERER_LIST[selectedItem] ?: throw NullPointerException("invalid item")

    companion object {
        private val RENDERER_LIST = mapOf(
            "Global illumination" to ::GlobalIlluminationRenderer,
        )
    }
}