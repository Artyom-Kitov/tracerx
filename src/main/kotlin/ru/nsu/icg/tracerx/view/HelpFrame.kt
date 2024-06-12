package ru.nsu.icg.tracerx.view

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JScrollPane
import javax.swing.JTextArea

object HelpFrame : JFrame("Help") {
    private fun readResolve(): Any = HelpFrame

    private val SIZE = Dimension(640, 480)

    init {
        size = SIZE
        preferredSize = SIZE
        defaultCloseOperation = DISPOSE_ON_CLOSE

        layout = BorderLayout()

        val title = JLabel("Controls", JLabel.CENTER)
        title.font = Font("Arial", Font.BOLD, 24)
        add(title)

        val text = """
            WASD: move around scene.
            
            Left/Right: same as AD
            
            HN/Up/Down: move up/down
            
            QE: tile left/right
            
            Scroll: zoom
            
            Ctrl + Scroll: same as W/S
            
            Enter: start render
            
            Backspace: return from rendered to wire
        """.trimIndent()

        val helpArea = JTextArea(text)
        helpArea.font = Font("Arial", Font.PLAIN, 20)
        helpArea.isEditable = false
        helpArea.lineWrap = true
        helpArea.wrapStyleWord = true

        val scrollPane = JScrollPane(helpArea)
        add(scrollPane, BorderLayout.CENTER)
    }
}