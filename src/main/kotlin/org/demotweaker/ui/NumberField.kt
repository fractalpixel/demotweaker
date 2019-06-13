package org.demotweaker.ui

import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import java.text.DecimalFormat
import java.text.ParsePosition





/**
 *
 */
class NumberField(): TextField() {
    companion object {
        val numberFormat = DecimalFormat("#.0")
    }

    init {
        val numberFormatter = TextFormatter<Any> { c ->
            if (c.controlNewText.isEmpty()) {
                c
            }
            else {
                val parsePosition = ParsePosition(0)
                val obj = numberFormat.parse(c.controlNewText, parsePosition)

                if (obj == null || parsePosition.index < c.controlNewText.length) {
                    null
                } else {
                    c
                }
            }

        }

        textFormatter = numberFormatter
    }
}