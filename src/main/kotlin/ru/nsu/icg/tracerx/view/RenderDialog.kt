package ru.nsu.icg.tracerx.view

import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.concurrent.atomic.AtomicLong
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JProgressBar

class RenderDialog(
    frame: JFrame,
    onClose: () -> Unit
) : JDialog(frame, "Render Progress", true) {

    private val progressBar = JProgressBar(0, 100)
    private val progressLabel = JLabel("0%")
    private var startedAt = AtomicLong(0L)

    suspend fun setProgress(value: Int) {
        withContext(Dispatchers.Swing) {
            val elapsedSeconds = (System.currentTimeMillis() - startedAt.get()).toFloat() / 1000f
            progressLabel.text = "Progress: $value%, time elapsed: " +
                    "${"%.2f".format(elapsedSeconds)}s"
            progressBar.value = value
        }
    }

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
                parent.requestFocus()
            }
        })

        isResizable = false
        pack()
    }

    fun startRender(action: suspend () -> Unit) {
        startedAt.set(System.currentTimeMillis())
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