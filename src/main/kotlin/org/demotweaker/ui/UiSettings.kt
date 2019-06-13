package org.demotweaker.ui

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.Node
import javafx.scene.control.ScrollPane


/**
 * Contains various layout settings and methods.
 */
object UiSettings {

    val smallGap = 4.0
    val gap = 8.0
    val largeGap = 16.0

    val rowHeight = 24.0

    val cellWidth = 64.0


    val appWidth = 1000.0
    val appHeight = 800.0

    val autosaveOnPlay = SimpleBooleanProperty(true)
    val followTime = SimpleBooleanProperty(true)
}


/**
 * Make sure specified node is visible in the specified scrollPane
 */
fun ScrollPane.ensureVisible(node: Node) {
    val width = content.boundsInLocal.width
    val height = content.boundsInLocal.height

    val x = node.boundsInParent.minX
    val y = node.boundsInParent.minY

    // scrolling values range from 0 to 1
    vvalue = y / height
    hvalue = x / width
}