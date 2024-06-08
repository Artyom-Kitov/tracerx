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
    private val rendererPopupList = RendererPopupList()
    private val colorSetter = JPanel()
    private val colorPanel = JPanel()
    private val picker = ColorPickerDialog(this)

    var lightSourcesShownConsumer: (Boolean) -> Unit = {}

    private val gammaSetter = ParameterPanel("gamma", 0.1f, 10f, 0.1f)
    private val depthSetter = ParameterPanel("depth", 1f, 10f, 1f)
    private val nThreadsSetter = ParameterPanel("Number of threads", 2f, 16f, 2f)
    private val sourceShownSetter = JCheckBox("Show light sources")

    init {
        layout = FlowLayout()
        size = DEFAULT_SIZE
        preferredSize = DEFAULT_SIZE

        colorSetter.add(JLabel("background color"))
        colorPanel.size = Dimension(70, 70)
        colorSetter.add(colorPanel)
        colorPanel.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                picker.isVisible = true
            }
        })
        picker.onChoose = { colorPanel.background = it }
        sourceShownSetter.isSelected = true

        add(JLabel("Model"))
        add(rendererPopupList)
        add(colorSetter)
        add(gammaSetter)
        add(depthSetter)
        add(nThreadsSetter)
        add(sourceShownSetter)

        addWindowListener(object : WindowAdapter() {
            override fun windowOpened(e: WindowEvent?) {
                picker.color = sceneController.backgroundColor
                gammaSetter.parameterValue = sceneController.gamma
                depthSetter.parameterValue = sceneController.depth.toFloat()
                nThreadsSetter.parameterValue = sceneController.nThreads.toFloat()
                rendererPopupList.selectedItem = sceneController.rendererSupplier.first
            }
        })
        val saveButton = JButton("Save")
        val cancelButton = JButton("Cancel")

        saveButton.addActionListener {
            sceneController.gamma = gammaSetter.parameterValue
            sceneController.backgroundColor = colorPanel.background
            sceneController.depth = depthSetter.parameterValue.toInt()
            sceneController.nThreads = nThreadsSetter.parameterValue.toInt()
            lightSourcesShownConsumer(sourceShownSetter.isSelected)
            sceneController.rendererSupplier = rendererPopupList.selectedItem!!.toString() to rendererPopupList.supplier

            isVisible = false
            parent.repaint()
        }
        cancelButton.addActionListener { isVisible = false }

        val buttonPanel = JPanel()
        buttonPanel.preferredSize = Dimension(DEFAULT_SIZE.width, 80)
        buttonPanel.add(saveButton)
        buttonPanel.add(cancelButton)
        add(buttonPanel, BorderLayout.SOUTH)

        pack()
    }

    companion object {
        private val DEFAULT_SIZE = Dimension(480, 350)
    }
}