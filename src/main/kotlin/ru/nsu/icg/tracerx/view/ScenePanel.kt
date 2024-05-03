package ru.nsu.icg.tracerx.view

import ru.nsu.icg.tracerx.model.scene.Render
import ru.nsu.icg.tracerx.model.scene.Scene
import java.awt.Dimension
import java.awt.Graphics
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JPanel

class ScenePanel(
    private var scene: Scene,
    private var render: Render
) : JPanel() {
    private var screenWidth = render.screenWidth
    private var screenHeight = render.screenHeight
    private var scaleFactor = 10

    init {
        background = render.backgroundColor
        layout = null
        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                if (screenWidth / screenHeight != width.toFloat() / height) {
                    scaleFactor = (height.toFloat() / screenHeight).toInt()
                    screenWidth = width.toFloat() / scaleFactor
                }
            }
        })
        isDoubleBuffered = true
    }

    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)
    }
}