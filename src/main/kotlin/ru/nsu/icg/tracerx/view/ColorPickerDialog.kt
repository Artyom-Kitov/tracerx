package ru.nsu.icg.tracerx.view

import java.awt.Color
import java.awt.Dimension
import javax.swing.JColorChooser
import javax.swing.JDialog

class ColorPickerDialog(
    dialog: JDialog,
) : JDialog(dialog, "Color", true) {
    var onChoose: (Color) -> Unit = {}

    private val chooser = JColorChooser()

    var color: Color
        set(value) {
            chooser.color = value
        }
        get() = chooser.color

    init {
        preferredSize = DEFAULT_SIZE
        size = DEFAULT_SIZE
        defaultCloseOperation = DISPOSE_ON_CLOSE

        chooser.preferredSize = DEFAULT_SIZE

        chooser.selectionModel.addChangeListener {
            onChoose(chooser.color)
        }
        add(chooser)
        pack()
        isResizable = false
    }

    companion object {
        private val DEFAULT_SIZE = Dimension(640, 480)
    }
}