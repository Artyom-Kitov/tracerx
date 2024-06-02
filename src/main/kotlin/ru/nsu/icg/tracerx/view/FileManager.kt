package ru.nsu.icg.tracerx.view

import ru.nsu.icg.tracerx.controller.FileManagerController
import ru.nsu.icg.tracerx.model.scene.Render
import ru.nsu.icg.tracerx.model.scene.Scene
import ru.nsu.icg.tracerx.model.scene.parser.SceneParseException
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.filechooser.FileNameExtensionFilter

class FileManager(private val controller: FileManagerController) : JFileChooser() {
    private val sceneFilter = FileNameExtensionFilter(SCENE_DESCRIPTION, SCENE_FORMAT)
    private val imageFilter = FileNameExtensionFilter(IMAGE_DESCRIPTION, IMAGE_FORMAT)

    init {
        isMultiSelectionEnabled = false
        dragEnabled = true
    }

    fun openScene(): Pair<Scene, List<Pair<String, Render>>>? {
        fileFilter = sceneFilter
        dialogTitle = OPEN_TITLE
        dialogType = OPEN_DIALOG

        val result = showOpenDialog(parent)
        return when (result) {
            APPROVE_OPTION -> {
                try {
                    controller.loadFullScene(selectedFile)
                } catch (e: SceneParseException) {
                    JOptionPane.showMessageDialog(this,
                        "Invalid scene file",
                        "Error", JOptionPane.ERROR_MESSAGE)
                    null
                } catch (e: IOException) {
                    JOptionPane.showMessageDialog(this,
                        "Something went wrong, please try again",
                        "Error", JOptionPane.ERROR_MESSAGE)
                    null
                }
            }
            ERROR_OPTION -> {
                JOptionPane.showMessageDialog(this,
                    "Something went wrong, please try again",
                    "Error", JOptionPane.ERROR_MESSAGE)
                null
            }
            else -> null
        }
    }

    fun saveImage(image: BufferedImage) {
        fileFilter = imageFilter
        dialogTitle = SAVE_TITLE
        dialogType = SAVE_DIALOG

        val result = showSaveDialog(parent)
        when (result) {
            APPROVE_OPTION -> {
                try {
                    if (!selectedFile.name.endsWith(".$IMAGE_FORMAT")) {
                        selectedFile = File("${selectedFile.absolutePath}.$IMAGE_FORMAT")
                    }
                    ImageIO.write(image, IMAGE_FORMAT, selectedFile)
                } catch (e: IOException) {
                    JOptionPane.showMessageDialog(this, "Error saving the image: ${e.message}",
                        "Error", JOptionPane.ERROR_MESSAGE)
                }
            }
            ERROR_OPTION -> {
                JOptionPane.showMessageDialog(this, "Something went wrong while saving the image",
                    "Error", JOptionPane.ERROR_MESSAGE)
            }
        }
    }

    companion object {
        private const val SCENE_DESCRIPTION = "format: .scene"
        private const val SCENE_FORMAT = "scene"

        private const val IMAGE_DESCRIPTION = "format: .png"
        private const val IMAGE_FORMAT = "png"

        private const val SAVE_TITLE = "Save as"
        private const val OPEN_TITLE = "Open"
    }
}