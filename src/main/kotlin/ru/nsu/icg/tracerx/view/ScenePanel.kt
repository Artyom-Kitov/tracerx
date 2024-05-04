package ru.nsu.icg.tracerx.view

import ru.nsu.icg.tracerx.controller.SceneController
import java.awt.Graphics
import java.awt.Point
import java.awt.event.*
import javax.swing.JPanel
import kotlin.math.sign

class ScenePanel(
    private val controller: SceneController
) : JPanel() {
    private var scaleFactor = 10

    private var screenWidth by controller::screenWidth
    private val screenHeight by controller::screenHeight

    private var origin: Point = Point(0, 0)
    private var isMousePressed = false

    init {
        background = controller.backgroundColor
        layout = null
        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                if (screenWidth / screenHeight != width.toFloat() / height) {
                    scaleFactor = (height.toFloat() / screenHeight).toInt()
                    screenWidth = width.toFloat() / scaleFactor
                    repaint()
                }
            }
        })
        addMouseWheelListener(object : MouseAdapter() {
            override fun mouseWheelMoved(e: MouseWheelEvent?) {
                if (e == null) return

                if (e.isControlDown) {
                    val delta = if (e.wheelRotation > 0) -0.5f else 0.5f
                    controller.move(delta, 0f, 0f)
                } else {
                    controller.zoom(e.wheelRotation < 0)
                }
                repaint()
            }
        })
        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent?) {
                if (e == null) return
                origin = e.point
                isMousePressed = true
            }

            override fun mouseReleased(e: MouseEvent?) {
                if (e == null) return
                isMousePressed = false
            }
        })
        addMouseMotionListener(object : MouseAdapter() {
            override fun mouseDragged(e: MouseEvent?) {
                if (e == null) return
                val sensitivity = 0.005f
                controller.rotate((origin.x - e.x) * sensitivity,
                    (origin.y - e.y) * sensitivity)
                origin = e.point
                repaint()
            }
        })

        isDoubleBuffered = true
    }

    override fun paintComponent(g: Graphics?) {
        if (g == null) return
        super.paintComponent(g)

        val lines = controller.calculateProjection()
        for (line in lines) {
            if (line.isEmpty()) continue
            var prev = line[0]
            for (i in 1..line.lastIndex) {
                val next = line[i]
                val x0 = (width / 2 - scaleFactor.toFloat() * prev.y).toInt()
                val y0 = (height / 2 - scaleFactor.toFloat() * prev.z).toInt()

                val x1 = (width / 2 - scaleFactor.toFloat() * next.y).toInt()
                val y1 = (height / 2 - scaleFactor.toFloat() * next.z).toInt()
                g.drawLine(x0, y0, x1, y1)
                prev = next
            }
        }
    }
}