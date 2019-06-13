package org.demotweaker

import org.mistutils.interpolation.Interpolator
import org.mistutils.interpolation.interpolators.CosineInterpolator
import org.mistutils.math.fastFloor
import org.mistutils.math.max
import org.mistutils.math.min

/**
 * A double value that varies over time.
 */
class Variable @JvmOverloads constructor (val tweaker: Tweaker,
                                          val name: String,
                                          val defaultValue: Float = 0f,
                                          var interpolator: Interpolator = CosineInterpolator()) {

    var value: Float = defaultValue

    val stepValues = LinkedHashMap<Int, Double>()
    private var maxStep = 0
    private var minStep = 0

    fun set(step: Int, value: Float) {
        // Update bounds
        if (stepValues.isEmpty()) {
            maxStep = step
            minStep = step
        }
        else {
            maxStep = maxStep max step
            minStep = minStep min step
        }

        stepValues[step] = value.toDouble()
    }

    fun get(step: Int): Float? {
        return stepValues[step]?.toFloat()
    }

    fun hasStep(step: Int): Boolean = stepValues.containsKey(step)

    fun clearStep(step: Int) {
        stepValues.remove(step)

        // Update bounds
        if (stepValues.isEmpty()) {
            minStep = 0
            maxStep = 0
        }
        else {
            if (step <= minStep) {
                while (!hasStep(minStep)) {
                    minStep++
                }
            }

            if (step >= maxStep) {
                while (!hasStep(maxStep)) {
                    maxStep--
                }
            }
        }
    }

    fun update(currentTimeStep: Double) {
        // Get adjacent values


        if (stepValues.isEmpty()) {
            value = defaultValue
        }
        else {
            var prevStep = currentTimeStep.fastFloor()
            var nextStep = prevStep + 1

            while (!hasStep(prevStep) && prevStep > minStep) {
                prevStep--
            }
            while (!hasStep(nextStep) && nextStep < maxStep) {
                nextStep++
            }

            val prevValue = get(prevStep) ?: defaultValue
            val nextValue = get(nextStep) ?: defaultValue

            // Interpolate
            value = interpolator.interpolate(
                currentTimeStep,
                prevStep.toDouble(),
                nextStep.toDouble(),
                prevValue.toDouble(),
                nextValue.toDouble()).toFloat()
        }
    }

}