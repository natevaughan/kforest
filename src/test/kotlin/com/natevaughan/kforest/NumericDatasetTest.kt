package com.natevaughan.kforest

import com.natevaughan.kforest.numeric.preprocessNumeric
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * @author Nate Vaughan
 */
class NumericDatasetTest {

    @Test
    fun testNumericUtils() {
        val dataset = getNumericDataset()
        val ds = preprocessNumeric(dataset)
        assertNotNull(ds)
    }

    fun getNumericDataset(): Collection<Collection<Tuple2<String, Any>>> {
        return listOf(
                listOf(Tuple2("size", 0.1), Tuple2("target", "true")) as Collection<Tuple2<String, Any>>,
                listOf(Tuple2("size", 0.2), Tuple2("target", "true")) as Collection<Tuple2<String, Any>>,
                listOf(Tuple2("size", 0.5), Tuple2("target", "true")) as Collection<Tuple2<String, Any>>,
                listOf(Tuple2("size", 0.6), Tuple2("target", "true")) as Collection<Tuple2<String, Any>>
        )
    }
}