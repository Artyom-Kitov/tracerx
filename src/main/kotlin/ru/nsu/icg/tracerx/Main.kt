package ru.nsu.icg.tracerx

import com.formdev.flatlaf.FlatDarkLaf
import ru.nsu.icg.tracerx.controller.FileManagerController
import ru.nsu.icg.tracerx.controller.SceneController
import ru.nsu.icg.tracerx.model.Context
import ru.nsu.icg.tracerx.view.SceneFrame
import javax.swing.UIManager

fun main() {
    try {
        UIManager.setLookAndFeel(FlatDarkLaf())
    } catch (e: ClassNotFoundException) {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    }
    val context = Context()
    SceneFrame(
        fileManagerController = FileManagerController(context),
        sceneController = SceneController(context)
    )
}