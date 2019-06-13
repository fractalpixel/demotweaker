package org.demotweaker

import org.mistutils.time.Time
import org.mistutils.time.TimeBase

/**
 * A time instance that can be paused, resumed, restarted, speed changed, and stepped.
 */
class ControllableTime @JvmOverloads constructor(startPaused: Boolean = false,
                       var speedFactor: Double = 1.0,
                       secondsSinceStart: Double = 0.0,
                       stepsSinceStart: Long = 0): TimeBase(secondsSinceStart, stepsSinceStart) {

    @JvmOverloads
    constructor(time: Time,
                startPaused: Boolean = false,
                speedFactor: Double = 1.0): this(startPaused, speedFactor, time.secondsSinceStart, time.stepCount)

    private var startMillis: Long = System.currentTimeMillis()
    private var prevMillis: Long = startMillis
    private var currentElapsedSeconds: Double = secondsSinceStart
    private var paused: Boolean = startPaused
    private var resuming: Boolean = false

    private var lastStepTimeStamp: Double = secondsSinceStart


    init {

    }

    /**
     * Set paused (true) or unpaused (false)
     */
    fun setPaused(newPaused: Boolean) {
        if (paused != newPaused) {
            paused = newPaused

            resuming = !paused
        }
    }

    /**
     * @returns true if currently paused
     */
    fun isPaused(): Boolean = paused

    /**
     * Change current time with the specified amount
     */
    fun changeTime(deltaSeconds: Double) {
        currentElapsedSeconds += deltaSeconds
    }

    override fun nextStep() {
        val time = currentTimeSeconds
        stepCount++
        stepDurationSeconds = (time - lastStepTimeStamp)  // Allow negative time steps
        secondsSinceStart += stepDurationSeconds
        lastStepTimeStamp = time
    }


    override val currentTimeSeconds: Double
        get() {
            val currentMillis = System.currentTimeMillis()

            if (!paused) {
                if (resuming) {
                    // Just restarted
                    resuming = false
                }
                else {
                    // Running normally
                    val timeSinceLastQueryS = (currentMillis - prevMillis) / 1000.0
                    currentElapsedSeconds += timeSinceLastQueryS * speedFactor
                }
            }

            prevMillis = currentMillis
            return currentElapsedSeconds
        }
}