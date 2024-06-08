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
    private val renderDialog = RenderDialog(this) { wireButton.doClick() }
    private val panel = ScenePanel(sceneController)
    private val settingsDialog = SettingsFrame(this, sceneController)

    private var renderButton = JRadioButtonMenuItem()
    private var wireButton = JRadioButtonMenuItem()

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

        settingsDialog.lightSourcesShownConsumer = { panel.drawLightSources = it }

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
                    KeyEvent.VK_ENTER -> renderButton.doClick()
                    KeyEvent.VK_BACK_SPACE -> wireButton.doClick()
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
                    sceneController.init(panel.size)
                    panel.correctSize()
                } else {
                    sceneController.setRender(fullScene.second[0].second)
                    fullScene.second.forEach {
                        views.addItem(it.first)
                        renders.add(it.second)
                    }
                    views.selectedIndex = 0
                }
            }
            panel.correctSize()
            repaint()
        }
        menu.add(open)
        menu.add(save)

        menu.add(views)

        val group = ButtonGroup()
        wireButton = JRadioButtonMenuItem("Wire")
        wireButton.addActionListener {
            panel.rendered = null
            repaint()
        }
        group.add(wireButton)
        wireButton.isSelected = true
        menu.add(wireButton)
        renderButton = JRadioButtonMenuItem("Render")
        group.add(renderButton)
        menu.add(renderButton)

        val renderMenu = JMenu("Render settings")
        val loadRender = JMenuItem("Load settings")
        val saveRender = JMenuItem("Save settings")
        val settings = JMenuItem("Edit")
        renderMenu.add(loadRender)
        renderMenu.add(saveRender)
        renderMenu.add(settings)
        menu.add(renderMenu)

        loadRender.addActionListener {
            val render = fileManager.openRender()
            if (render != null) sceneController.setRender(render)
            repaint()
        }
        saveRender.addActionListener { fileManager.saveRender(sceneController.buildRender()) }
        settings.addActionListener { settingsDialog.isVisible = true }

        val init = JMenuItem("Init")
        init.addActionListener {
            sceneController.init(panel.size)
            panel.correctSize()
            repaint()
        }
        menu.add(init)

        renderButton.addActionListener { render() }

        val exit = JMenuItem("Exit")
        exit.addActionListener { exitProcess(0) }
        menu.add(exit)

        jMenuBar = menu
    }

    private fun render() {
        renderDialog.startRender {
            sceneController.startRender(panel.size, renderDialog::progressSetter) {
                panel.rendered = it
            }
        }
    }

    companion object {
        private val MINIMUM_SIZE = Dimension(640, 480)
        private val DEFAULT_SIZE = Dimension(1280, 720)
    }
}