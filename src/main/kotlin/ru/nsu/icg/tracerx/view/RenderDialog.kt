package ru.nsu.icg.tracerx.view

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JProgressBar

class RenderDialog(
    frame: JFrame,
    onClose: () -> Unit
) : JDialog(frame, "Render Progress", false) {

    private val progressBar = JProgressBar(0, 100)
    val progressSetter: (Int) -> Unit = {
        progressBar.value = it
        progressLabel.text = "$it%"
        repaint()
    }
    private val progressLabel = JLabel("0%")

    private var thread: Thread? = null

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE
        size = Dimension(400, 200)
        preferredSize = Dimension(400, 200)
        setLocationRelativeTo(null)

        add(JLabel("Progress:"), BorderLayout.NORTH)
        add(progressBar, BorderLayout.CENTER)
        add(progressLabel, BorderLayout.SOUTH)

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                thread?.interrupt()
                frame.isEnabled = true
                frame.isVisible = true
                onClose()
            }
        })

        isResizable = false
        pack()
    }

    fun startRender(action: () -> Unit) {
        progressBar.value = 0
        isVisible = true
        thread = Thread(action)
        thread?.start()
    }
}