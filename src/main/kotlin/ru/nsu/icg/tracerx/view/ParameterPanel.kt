package ru.nsu.icg.tracerx.view

import java.lang.NumberFormatException
import java.util.*
import javax.swing.*

class ParameterPanel(
    name: String,
    private val min: Float,
    private val max: Float,
    private val step: Float
) : JPanel() {

    private val slider = JSlider(0, ((max - min) / step).toInt(), 0)
    private val textField = JTextField(10)

    var parameterValue
        set(value) {
            slider.value = ((value - min) / step).toInt()
            textField.text = formatFloat(parameterValue)
        }
        get() = min + slider.value * step

    init {
        slider.majorTickSpacing = (1f / step).toInt()
        slider.paintTicks = true

        slider.addChangeListener {
            val sliderValue = slider.value
            val value = min + sliderValue * step
            textField.text = formatFloat(value)
        }

        textField.addActionListener {
            try {
                val value = textField.text.toFloat()
                if (value < min || value > max) {
                    JOptionPane.showMessageDialog(this, "Value out of range")
                } else {
                    val sliderValue = ((value - min) / step).toInt()
                    slider.value = sliderValue
                }
            } catch (_: NumberFormatException) {
                JOptionPane.showMessageDialog(this, "Invalid number format")
            }
        }

        add(JLabel("$name: "))
        add(slider)
        add(textField)
    }

    companion object {
        private fun formatFloat(f: Float): String {
            return if (f - f.toInt() == 0f) f.toInt().toString() else "%.1f".format(Locale.ENGLISH, f)
        }
    }
}