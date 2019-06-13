package org.demotweaker

import org.mistutils.time.Time

/**
 * Listens to movements in the demo.
 */
interface StepListener {

    fun onUpdate(time: Time) {}

    fun onRestarted(time: Time) {}

    fun onPaused(time: Time) {}

    fun onPlaying(time: Time) {}
}