package ru.nsu.icg.tracerx.view

import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JProgressBar

class RenderDialog(frame: JFrame) : JDialog(frame, "Render Progress", false) {

    private val progressBar = JProgressBar(0, 100)
    val progressSetter: (Int) -> Unit = {
        progressBar.value = it
        progressLabel.text = "$it%"
        repaint()
    }
    private val progressLabel = JLabel("0%")

    init {
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        size = Dimension(400, 200)
        preferredSize = Dimension(400, 200)
        setLocationRelativeTo(null)

        add(JLabel("Progress:"), BorderLayout.NORTH)
        add(progressBar, BorderLayout.CENTER)
        add(progressLabel, BorderLayout.SOUTH)

        isResizable = false
        pack()
    }

    fun startRender(action: () -> Unit) {
        progressBar.value = 0
        isVisible = true
        Thread(action).start()
    }
}