package com.natevaughan.kbayes

import javafx.collections.transformation.SortedList
import java.util.Collections

/**
 * @author Nate Vaughan
 */
object LearningUtils {

    /**
     * Finds the mean of two numbers
     *
     * @param first double
     * @param second double
     * @return the mathematical mean
     */
    fun mean(first: Double, second: Double): Double {
        return (first + second) / 2
    }

    /**
     * Finds the median of a list of Doubles, allowing the caller to specify
     * whether the list has been sorted
     *
     * @param doubles the list process
     * @param sort whether the list has already been sorted
     * @return Double median value
     */
    @JvmOverloads
    fun median(doubles: List<Double>, sort: Boolean? = true): Double {
        val size = doubles.size
        if (size < 1) {
            throw IllegalArgumentException("Cannot process empty list")
        }
        if (sort!! && doubles !is SortedList<*>) {
            Collections.sort(doubles)
        }
        val middle = (size - 1) / 2
        val lower = doubles[middle]
        val upper = doubles[size - (middle + 1)]
        return mean(lower, upper)
    }

    /**
     * Entropy with all counts present
     * @param count1 count for target
     * @return double entropy
     */
    fun entropy(counts: Collection<Long>): Double {
        val sum = counts.sumByDouble { it.toDouble() }
        if (sum == 0.0) {
            return 0.0
        }
        return counts.sumByDouble { entropy(it / sum) }
    }

    /**
     * Calculates entropy of a given probability
     * @param probability double
     * @return double entropy
     */
    fun entropy(probability: Double): Double {
        return if (probability == 0.0) {
            0.0
        } else -1.0 * probability * Math.log(probability) / Math.log(2.0)
    }
}
