package com.natevaughan.kforest.core

import com.natevaughan.kforest.core.Table

/**
 * @author Nate Vaughan
 */
class ConfusionMatrix {
    val errors = Table<String, String, Int>()
    val successes = mutableMapOf<String, Int>()

    fun register(predicted: String, actual: String) {
        when {
            (predicted == actual) -> {
                val count = successes.get(actual) ?: 0
                successes.put(actual, count + 1)
            }
            else -> {
                val count = errors.get(predicted, actual) ?: 0
                errors.put(predicted, actual, count + 1)
            }
        }
    }

    override fun toString(): String {
        return "Errors: $errors \nSuccesses: $successes"
    }
}
