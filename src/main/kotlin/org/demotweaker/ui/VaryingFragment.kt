package org.demotweaker.ui

import javafx.geometry.Pos
import javafx.scene.layout.HBox
import org.demotweaker.Variable
import tornadofx.Fragment
import tornadofx.hbox

/**
 *
 */
class VaryingFragment(val variable: Variable): Fragment() {

    val steps: HBox = HBox()

    val fields = ArrayList<NumberField>()

    init {
        // TODO: Optimize by using a table instead

        for (i in 0 until variable.tweaker.steps) {
            val field = NumberField()
            field.prefWidth = UiSettings.cellWidth
            field.prefHeight = UiSettings.rowHeight
            field.alignment = Pos.BASELINE_RIGHT
            field.textProperty().addListener { observable, oldValue, newValue ->
                if (newValue == null || newValue.isBlank()) variable.clearStep(i)
                else variable.set(i, newValue.toFloatOrNull() ?: 0f)
            }

            fields.add(field)
            steps.add(field)
        }

    }

    override val root: HBox = hbox {
        alignment = Pos.CENTER_LEFT
        spacing = UiSettings.gap
        add(steps)
        updateEditorValues()
    }


    fun updateEditorValues() {
        for (i in 0 until fields.size) {
            fields[i].text = variable.get(i)?.toString() ?: ""
        }
    }


}