package org.demotweaker.ui

import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.layout.*
import javafx.scene.paint.Color
import org.demotweaker.StepListener
import org.demotweaker.Tweaker
import org.mistutils.math.fastFloor
import org.mistutils.time.Time
import tornadofx.*
import java.text.DecimalFormat
import kotlin.math.roundToInt


/**
 *
 */
class TweakFrame: View() {

    lateinit var tweaker: Tweaker

    private val tweakPanel = BorderPane()

    private val names = VBox()
    private val varyingNames = ArrayList<String>()
    private val currentValueLables = ArrayList<Label>()

    private val statusLabel = Label("")

    private val headerStepLabels = ArrayList<Label>()

    private var currentStep = -1

    private val dataRows = VBox()
    private val hScroll = ScrollPane(dataRows)

    val timeFormat = DecimalFormat("#.0")
    val valueFormat = DecimalFormat("#.00")

    init {
        tweaker = TweakAppLauncher.tweakerToShow ?: throw IllegalStateException("Need to call TweakAppLauncher.openUI()")

        val propertyLabel = Label("Properties")
        propertyLabel.prefHeight = UiSettings.rowHeight
        names.add(propertyLabel)


        val stepLabels = HBox()
        //stepLabels.spacing = UiSettings.smallGap
        for (i in 0 until tweaker.steps) {
            val stepLabel = Label("$i")
            stepLabel.alignment =Pos.BASELINE_CENTER
            stepLabel.prefWidth = UiSettings.cellWidth
            stepLabel.prefHeight = UiSettings.rowHeight
            headerStepLabels.add(stepLabel)
            stepLabels.add(stepLabel)
        }
        dataRows.add(stepLabels)

        for (varying in tweaker.varyings.values) {
            val nameLabel = Label(varying.name)
            nameLabel.prefHeight = UiSettings.rowHeight
            val currentValueLabel = Label(valueFormat.format(varying.value))
            varyingNames.add(varying.name)
            currentValueLables.add(currentValueLabel)
            names.add(HBox(nameLabel, currentValueLabel))
            dataRows.add(VaryingFragment(varying))
        }

        names.spacing = UiSettings.smallGap


        hScroll.hbarPolicy = ScrollPane.ScrollBarPolicy.ALWAYS
        hScroll.vbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
        hScroll.isFitToHeight = true

        val combined = BorderPane(hScroll, null, null, null, names)
        val combinedScroll = ScrollPane(combined)
        combinedScroll.isFitToWidth = true
        combinedScroll.hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
        combinedScroll.vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED

        tweakPanel.center = combinedScroll

        tweaker.addListener(object: StepListener {
            override fun onUpdate(time: Time) {
                updateStatusLabel()
            }

            override fun onRestarted(time: Time) {
                updateStatusLabel()
            }

            override fun onPaused(time: Time) {
                updateStatusLabel()
            }

            override fun onPlaying(time: Time) {
                updateStatusLabel()
            }
        })
    }


    fun updateStatusLabel() {
        val step = (tweaker.time.currentStepElapsedSeconds * tweaker.stepsPerSecond).fastFloor()
        var status = "Time: "
        status += timeFormat.format(tweaker.time.currentStepElapsedSeconds) + "s"
        status += " Step: "
        status += step
        status += " FPS: "
        status += tweaker.time.stepsPerSecond.roundToInt()
        status += if (tweaker.isPaused()) " [Paused] " else " "
        status += "Steps/second: ${tweaker.stepsPerSecond.roundToInt()} "
        if (tweaker.time.speedFactor != 1.0) status += " Speed: ${tweaker.time.speedFactor}"
        Platform.runLater {
            statusLabel.text = status
            highlightLabel(step)
            updateValues()
        }
    }

    fun updateValues() {
        for (i in 0 until currentValueLables.size) {
            val value = tweaker.varyings[varyingNames[i]]?.value
            currentValueLables[i].text = if (value != null) valueFormat.format(value) else ""
        }
    }

    fun highlightLabel(step: Int) {
        if (currentStep >= 0 && currentStep < headerStepLabels.size) {
            headerStepLabels[currentStep].background = Background.EMPTY
        }

        currentStep = step

        if (step >= 0 && step < headerStepLabels.size) {
            headerStepLabels[currentStep].background = Background(BackgroundFill(Color(0.8, 0.6, 0.1, 1.0), CornerRadii.EMPTY, Insets.EMPTY))

            // Follow if we are running and follow is on
            if (UiSettings.followTime.get() && !tweaker.isPaused()) {
                hScroll.ensureVisible(headerStepLabels[currentStep])
            }
        }
    }

    override val root: Parent = borderpane {
            prefWidth = UiSettings.appWidth
            prefHeight = UiSettings.appHeight
            center = tweakPanel

            val upperToolbar = hbox {
                this.spacing = UiSettings.gap

                button("Save") {
                    onAction = EventHandler<ActionEvent?> {
                       tweaker.save()
                    }
                }

                checkbox("Autosave on Play", UiSettings.autosaveOnPlay)

                checkbox("Follow", UiSettings.followTime)

                button("Restart") {
                    onAction = EventHandler<ActionEvent?> {
                        if (UiSettings.autosaveOnPlay.get()) tweaker.save()
                        tweaker.start()
                    }
                }

                button("Play/Pause") {
                    onAction = EventHandler<ActionEvent?> {
                        if (tweaker.isPaused()) {
                            if (UiSettings.autosaveOnPlay.get()) tweaker.save()
                            tweaker.play()
                        }
                        else {
                            tweaker.pause()
                        }
                    }
                }

                button("<<") {
                    onAction = EventHandler<ActionEvent?> {
                        tweaker.time.changeTime(-10.0)
                    }
                }
                button("<") {
                    onAction = EventHandler<ActionEvent?> {
                        tweaker.time.changeTime(-1.0)
                    }
                }

                button(">") {
                    onAction = EventHandler<ActionEvent?> {
                        tweaker.time.changeTime(1.0)
                    }
                }
                button(">>") {
                    onAction = EventHandler<ActionEvent?> {
                        tweaker.time.changeTime(10.0)
                    }
                }

            }

        val lowerToolbar = statusLabel

        top = VBox(upperToolbar, lowerToolbar) }

}