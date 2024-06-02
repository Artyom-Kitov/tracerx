package ru.nsu.icg.tracerx.view

import ru.nsu.icg.tracerx.controller.SceneController
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.*
import javax.swing.*

class SettingsFrame(
    frame: JFrame,
    private val sceneController: SceneController
) : JDialog(frame, "Settings", true) {
    private val colorSetter = JPanel()
    private val colorPanel = JPanel()
    private val picker = ColorPickerDialog(this)

    var lightSourcesShownConsumer: (Boolean) -> Unit = {}

    private val gammaSetter = ParameterPanel("gamma", 0.1f, 10f, 0.1f)
    private val depthSetter = ParameterPanel("depth", 1f, 10f, 1f)
    private val parallelSetter = JCheckBox("Parallel rendering")
    private val sourceShownSetter = JCheckBox("Show light sources")

    init {
        layout = FlowLayout()
        size = DEFAULT_SIZE
        preferredSize = DEFAULT_SIZE

        colorSetter.add(JLabel("background color"))
        colorPanel.size = Dimension(50, 50)
        colorSetter.add(colorPanel)
        colorPanel.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                picker.isVisible = true
            }
        })
        picker.onChoose = {
            colorPanel.background = it
        }

        add(colorSetter)
        add(gammaSetter)
        add(depthSetter)
        add(parallelSetter)
        add(sourceShownSetter)

        addWindowListener(object : WindowAdapter() {
            override fun windowOpened(e: WindowEvent?) {
                picker.color = sceneController.backgroundColor
                gammaSetter.parameterValue = sceneController.gamma
                depthSetter.parameterValue = sceneController.depth.toFloat()
                parallelSetter.isSelected = sceneController.parallel
            }
        })
        val saveButton = JButton("Save")
        val cancelButton = JButton("Cancel")

        saveButton.addActionListener {
            sceneController.gamma = gammaSetter.parameterValue
            sceneController.backgroundColor = colorPanel.background
            sceneController.depth = depthSetter.parameterValue.toInt()
            sceneController.parallel = parallelSetter.isSelected
            lightSourcesShownConsumer(sourceShownSetter.isSelected)

            isVisible = false
            parent.repaint()
        }
        cancelButton.addActionListener { isVisible = false }

        val buttonPanel = JPanel()
        buttonPanel.preferredSize = Dimension(DEFAULT_SIZE.width, 50)
        buttonPanel.add(saveButton)
        buttonPanel.add(cancelButton)
        add(buttonPanel, BorderLayout.SOUTH)

        pack()
        isResizable = false
    }

    companion object {
        private val DEFAULT_SIZE = Dimension(480, 280)
    }
}