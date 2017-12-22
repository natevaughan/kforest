package com.natevaughan.kbayes

import com.natevaughan.kbayes.LearningUtils.entropy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @author Nate Vaughan
 */
class LearningUtilsTest {

    @Test
    fun itCalculatesSimpleEntropy() {
        val entropy = entropy(listOf(3L, 3L))
        assertTrue(entropy == 1.0)
    }

    @Test
    fun itCalculatesMoreEntropy() {
        val entropy = entropy(listOf(4L, 1L, 0L))
        assertEquals(entropy, 0.72, 0.01)
    }
}