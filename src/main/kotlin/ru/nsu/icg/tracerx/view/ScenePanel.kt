package ru.nsu.icg.tracerx.view

import ru.nsu.icg.tracerx.controller.SceneController
import ru.nsu.icg.tracerx.model.common.Vector3D
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Point
import java.awt.event.*
import java.awt.image.BufferedImage
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities

class ScenePanel(
    private val controller: SceneController
) : JPanel() {
    private var scaleFactor = 10

    private var screenWidth by controller::screenWidth
    private val screenHeight by controller::screenHeight

    private var origin: Point = Point(0, 0)
    private var isMousePressed = false

    var drawLightSources = true
        set(value) {
            field = value
            repaint()
        }

    var rendered: BufferedImage? = null
        set(value) {
            val frame = SwingUtilities.getWindowAncestor(this) as JFrame
            frame.isResizable = value == null
            field = value
            repaint()
        }

    val image: BufferedImage
        get() {
            val img = rendered
            if (img != null) return img

            val result = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
            paintComponent(result.createGraphics())
            return result
        }

    init {
        background = controller.backgroundColor
        layout = null
        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                correctSize()
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
                isMousePressed = false
            }
        })
        addMouseMotionListener(object : MouseAdapter() {
            override fun mouseDragged(e: MouseEvent?) {
                if (e == null) return
                val sensitivity = 0.1f / scaleFactor
                controller.rotate((origin.x - e.x) * sensitivity, (origin.y - e.y) * sensitivity, 0f)
                origin = e.point
                repaint()
            }
        })

        isDoubleBuffered = true
    }

    fun correctSize() {
        if (screenWidth / screenHeight != width.toFloat() / height.toFloat()) {
            scaleFactor = (height.toFloat() / screenHeight).toInt()
            screenWidth = width.toFloat() / scaleFactor
            repaint()
        }
    }

    override fun paintComponent(g: Graphics?) {
        if (g == null) return

        if (rendered != null) {
            g.drawImage(rendered, 0, 0, this)
            return
        }

        val bkg = controller.backgroundColor
        background = bkg
        super.paintComponent(g)

        g.color = Color(255 - bkg.red, 255 - bkg.green, 255 - bkg.blue)
        val lines = controller.calculateProjection()
        for (line in lines) {
            if (line.isEmpty()) continue
            var prev = line[0]
            for (i in 1..line.lastIndex) {
                val next = line[i]

                val p1 = projectionToScreen(prev)
                val p2 = projectionToScreen(next)
                g.drawLine(p1.x, p1.y, p2.x, p2.y)
                prev = next
            }
        }

        if (!drawLightSources) return
        for (lightSource in controller.lightSources) {
            g.color = lightSource.color
            val p = projectionToScreen(lightSource.position)
            g.fillOval(p.x - SOURCE_RADIUS, p.y - SOURCE_RADIUS, SOURCE_RADIUS * 2, SOURCE_RADIUS * 2)
        }
    }

    private fun projectionToScreen(v: Vector3D): Point {
        val x = (width / 2 - scaleFactor.toFloat() * v.y).toInt()
        val y = (height / 2 - scaleFactor.toFloat() * v.z).toInt()
        return Point(x, y)
    }

    companion object {
        private const val SOURCE_RADIUS = 5
    }
}