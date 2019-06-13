package org.demotweaker

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import org.demotweaker.ui.TweakAppLauncher
import org.mistutils.interpolation.interpolators.CosineInterpolator
import org.mistutils.interpolation.interpolators.LinearInterpolator
import org.mistutils.interpolation.interpolators.PowInterpolator
import org.mistutils.math.fastFloor
import tornadofx.FX
import java.io.File
import java.io.IOException
import java.io.StringReader

/**
 * Class for handling several Variable values, and controlling the time stepping.
 */
// TODO: Rename to warp drive?
class Tweaker @JvmOverloads constructor(
        var fileName: String = "demotweaker.json",
        var durationSeconds: Double = 3 * 60.0,
        var stepsPerSecond: Double = 1.0) {

    val varyings = LinkedHashMap<String, Variable>()
    val time = ControllableTime()

    val steps: Int get() = (durationSeconds * stepsPerSecond).fastFloor() + 1

    private val listeners = LinkedHashSet<StepListener>()

    init {
        // Try loading
        try {
            load()
        }
        catch (e: IOException) {
            println("Could not load Tweaker datafile named '$fileName', creating new. \n${e.javaClass}: ${e.message}")
        }
    }

    @JvmOverloads
    fun variable(name: String, defaultValue: Float = 0f): Variable {
        return varyings.getOrPut(name) {
            Variable(this, name, defaultValue)
        }
    }

    @JvmOverloads
    fun value(name: String, defaultValue: Float = 0f): Float {
        return variable(name, defaultValue).value
    }

    /**
     * Call this every update loop (in draw in processing)
     */
    fun update() {
        time.nextStep()

        // Calculate values for varyings
        for (varying in varyings.values) {
            varying.update(time.secondsSinceStart * stepsPerSecond)
        }

        // Notify listeners
        for (listener in listeners) {
            listener.onUpdate(time)
        }
    }

    fun start() {
        time.reset()
        time.setPaused(false)

        for (listener in listeners) {
            listener.onRestarted(time)
        }
        for (listener in listeners) {
            listener.onPlaying(time)
        }
    }

    fun playPause() {
        if (time.isPaused()) play()
        else pause()
    }

    fun play() {
        time.setPaused(false)

        for (listener in listeners) {
            listener.onPlaying(time)
        }
    }

    fun pause() {
        time.setPaused(true)

        for (listener in listeners) {
            listener.onPaused(time)
        }
    }

    fun isPaused(): Boolean = time.isPaused()


    /**
     * Opens UI for editing the variables.
     */
    fun openEditor() {
        TweakAppLauncher.showTweaker(this)
    }

    fun save() {
        val text = saveToString()
        File(fileName).writeText(text)
    }

    fun saveToString(): String {
        val root = JsonObject()
        root["format_type"] = "Tweaker"
        root["format_version"] = "1.0"
        root["format_meta_info"] =
            "Data file for the Tweaker application, " +
            "used for defining time-variable variables.  " +
            "See https://github.com/fractalpixel/demotweaker for details."

        root["stepsPerSecond"] = stepsPerSecond
        root["durationSeconds"] = durationSeconds

        val vars = JsonArray<JsonObject>()
        for (varying in varyings.values) {
            vars.add(varyingToJson(varying))
        }
        root["vars"] = vars

        return root.toJsonString(true)
    }

    private fun varyingToJson(variable: Variable): JsonObject {
        val obj = JsonObject()
        obj["name"] = variable.name
        obj["defaultValue"] = variable.defaultValue
        obj["interpolator"] = variable.interpolator.javaClass.simpleName
        
        val steps = JsonArray<JsonObject>()
        for (entry in variable.stepValues) {
            val step = JsonObject()
            step["step"] = entry.key
            step["value"] = entry.value
            steps.add(step)
        }

        obj["steps"] = steps

        return obj
    }

    fun load() {
        loadFromString(File(fileName).readText())
    }

    fun loadFromString(jsonData: String) {
        val klaxon = Klaxon()
        val root: JsonObject = klaxon.parseJsonObject(StringReader(jsonData))

        // Clear old data
        varyings.clear()

        // Read general parameters
        stepsPerSecond = root.double("stepsPerSecond") ?: 2.0
        durationSeconds = root.double("durationSeconds") ?: 5 * 60.0

        // Parse varyings
        val children = root.array<JsonObject>("vars")
        if (children != null) {
            for (child in children) {
                val varying = parseVarying(child)
                varyings[varying.name] = varying
            }
        }
    }

    private fun parseVarying(obj: JsonObject): Variable {

        // Read fields
        val name = obj.string("name") ?: "UnknownName".also{ FX.log.warning("Missing name")}
        val defaultValue = obj.float("defaultValue") ?: 0f
        val interpolatorName = obj.string("interpolator") ?: ""
        val interpolator = when (interpolatorName) {
            "CosineInterpolator" -> CosineInterpolator()
            "LinearInterpolator" -> LinearInterpolator()
            "PowInterpolator" -> PowInterpolator()
            else -> CosineInterpolator()
        }
        val varying = Variable(this, name, defaultValue, interpolator)

        // Parse steps
        val steps = obj.array<JsonObject>("steps")
        if (steps != null) {
            for (step in steps) {
                val stepNumber = step.int("step")
                val stepValue = step.float("value")
                if (stepNumber != null && stepValue != null) {
                    varying.set(stepNumber, stepValue)
                }
            }
        }
        else {
            throw IllegalStateException("steps section not found")
        }

        return varying
    }

    fun addListener(listener: StepListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: StepListener) {
        listeners.remove(listener)
    }

}