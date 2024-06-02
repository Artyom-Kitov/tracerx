package ru.nsu.icg.tracerx.view

import ru.nsu.icg.tracerx.controller.FileManagerController
import ru.nsu.icg.tracerx.controller.SceneController
import ru.nsu.icg.tracerx.model.scene.Render
import java.awt.Dimension
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.*
import javax.swing.event.PopupMenuEvent
import javax.swing.event.PopupMenuListener
import kotlin.system.exitProcess

class SceneFrame(
    fileManagerController: FileManagerController,
    private val sceneController: SceneController
) : JFrame("Tracer X") {

    private val fileManager = FileManager(fileManagerController)
    private val views = JComboBox<String>()
    private val renders = mutableListOf<Render>()
    private val renderDialog = RenderDialog(this)
    private val panel = ScenePanel(sceneController)

    init {
        minimumSize = MINIMUM_SIZE
        defaultCloseOperation = EXIT_ON_CLOSE
        preferredSize = DEFAULT_SIZE

        setupMenu()
        val defaultScene = fileManagerController.loadDefaultScene()
        val defaultRender = fileManagerController.loadDefaultRender()
        sceneController.setScene(defaultScene)
        sceneController.setRender(defaultRender)
        renders.add(defaultRender)
        views.addItem("StandfordBunny")
        views.selectedIndex = 0
        views.isFocusable = false

        add(panel)

        addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent?) {
                if (e == null) return
                val sensitivity = 0.5f
                when (e.keyCode) {
                    KeyEvent.VK_LEFT, KeyEvent.VK_A -> sceneController.move(0f, sensitivity, 0f)
                    KeyEvent.VK_RIGHT, KeyEvent.VK_D -> sceneController.move(0f, -sensitivity, 0f)
                    KeyEvent.VK_UP, KeyEvent.VK_H -> sceneController.move(0f, 0f, sensitivity)
                    KeyEvent.VK_DOWN, KeyEvent.VK_N -> sceneController.move(0f, 0f, -sensitivity)
                    KeyEvent.VK_W -> sceneController.move(sensitivity, 0f, 0f)
                    KeyEvent.VK_S -> sceneController.move(-sensitivity, 0f, 0f)
                }
                repaint()
            }
        })
        views.addPopupMenuListener(object : PopupMenuListener {
            override fun popupMenuWillBecomeVisible(e: PopupMenuEvent?) {
                // nothing to do
            }

            override fun popupMenuWillBecomeInvisible(e: PopupMenuEvent?) {
                val render = renders[views.selectedIndex]
                sceneController.setRender(render)
                repaint()
            }

            override fun popupMenuCanceled(e: PopupMenuEvent?) {
                // nothing to do
            }
        })

        pack()
        isVisible = true
    }

    private fun setupMenu() {
        val menu = JMenuBar()

        val open = JMenuItem("Open scene")
        val save = JMenuItem("Save image")
        save.addActionListener { fileManager.saveImage(panel.image) }
        open.addActionListener {
            val fullScene = fileManager.openScene()
            if (fullScene != null) {
                sceneController.setScene(fullScene.first)
                views.removeAllItems()
                renders.clear()
                if (fullScene.second.isEmpty()) {
                    sceneController.init()
                } else {
                    sceneController.setRender(fullScene.second[0].second)
                    fullScene.second.forEach {
                        views.addItem(it.first)
                        renders.add(it.second)
                    }
                    views.selectedIndex = 0
                }
            }
            repaint()
        }
        menu.add(open)
        menu.add(save)

        menu.add(views)

        val group = ButtonGroup()
        val wire = JRadioButtonMenuItem("Wire")
        wire.addActionListener {
            panel.rendered = null
            repaint()
        }
        group.add(wire)
        wire.isSelected = true
        menu.add(wire)
        val render = JRadioButtonMenuItem("Render")
        render.addActionListener { render() }
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
        init.addActionListener {
            sceneController.init()
            repaint()
        }
        menu.add(init)

        val drawSources = JCheckBox("Draw light sources")
        drawSources.isFocusable = false
        drawSources.addActionListener { panel.drawLightSources = drawSources.isSelected }
        menu.add(drawSources)

        val exit = JMenuItem("Exit")
        exit.addActionListener { exitProcess(0) }
        menu.add(exit)

        jMenuBar = menu
    }

    private fun render() {
        isEnabled = false
        renderDialog.startRender {
            sceneController.startRender(panel.size, renderDialog.progressSetter) {
                panel.rendered = it
                isEnabled = true
                isVisible = true
                renderDialog.isVisible = false
            }
        }
    }

    companion object {
        private val MINIMUM_SIZE = Dimension(640, 480)
        private val DEFAULT_SIZE = Dimension(1280, 720)
    }
}