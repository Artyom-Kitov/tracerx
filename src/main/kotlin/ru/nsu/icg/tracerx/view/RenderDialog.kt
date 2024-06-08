package ru.nsu.icg.tracerx.view

import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
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
) : JDialog(frame, "Render Progress", true) {

    private val progressBar = JProgressBar(0, 100)
    suspend fun progressSetter(value: Int) {
        withContext(Dispatchers.Swing) {
            progressLabel.text = "$value%"
            progressBar.value = value
        }
    }
    private val progressLabel = JLabel("0%")

    private var job: Job? = null

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
                job?.cancel()
                onClose()
                parent.isEnabled = true
                parent.isVisible = true
                isVisible = false
            }
        })

        isResizable = false
        pack()
    }

    fun startRender(action: suspend () -> Unit) {
        job = CoroutineScope(Dispatchers.Swing).launch {
            progressBar.value = 0
            parent.isEnabled = false
            try {
                action()
            } finally {
                parent.isEnabled = true
                parent.isVisible = true
                isVisible = false
            }
        }
        isVisible = true
    }
}