package ru.nsu.icg.tracerx.view

import ru.nsu.icg.tracerx.controller.FileManagerController
import ru.nsu.icg.tracerx.controller.SceneController
import java.awt.Dimension
import javax.swing.*
import kotlin.system.exitProcess

class SceneFrame(
    private val fileManagerController: FileManagerController,
    private val sceneController: SceneController
) : JFrame("Tracer X") {
    init {
        minimumSize = MINIMUM_SIZE
        defaultCloseOperation = EXIT_ON_CLOSE
        preferredSize = DEFAULT_SIZE

        setupMenu()
        add(ScenePanel(
            scene = fileManagerController.loadDefaultScene(),
            render = fileManagerController.loadDefaultRender()
        ))

        pack()
        isVisible = true
    }

    private fun setupMenu() {
        val menu = JMenuBar()

        val open = JMenuItem("Open scene")
        val save = JMenuItem("Save image")
        menu.add(open)
        menu.add(save)

        val group = ButtonGroup()
        val wire = JRadioButtonMenuItem("Wire")
        group.add(wire)
        wire.isSelected = true
        menu.add(wire)
        val render = JRadioButtonMenuItem("Render")
        group.add(render)
        menu.add(render)

        val renderMenu = JMenu("Render settings")
        val loadRender = JMenuItem("Load settings")
        val saveRender = JMenuItem("Save settings")
        val settings = JMenuItem("Edit")
        renderMenu.add(loadRender)
        renderMenu.add(saveRender)
        renderMenu.add(settings)
        menu.add(renderMenu)

        val init = JMenuItem("Init")
        menu.add(init)

        val exit = JMenuItem("Exit")
        exit.addActionListener { exitProcess(0) }
        menu.add(exit)

        jMenuBar = menu
    }

    companion object {
        private val MINIMUM_SIZE = Dimension(640, 480)
        private val DEFAULT_SIZE = Dimension(1280, 720)
    }
}