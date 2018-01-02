package com.natevaughan.kforest

import com.natevaughan.kforest.LearningUtils.entropy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @author Nate Vaughan
 */
class LearningUtilsTest {

    @Test
    fun itCalculatesSimpleEntropy() {
        val entropy = entropy(listOf(3, 3))
        assertTrue(entropy == 1.0)
    }

    @Test
    fun itCalculatesMoreEntropy() {
        val entropy = entropy(listOf(4, 1, 0))
        assertEquals(entropy, 0.72, 0.01)
    }
}