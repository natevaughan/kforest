package com.natevaughan.kforest.numeric

import com.natevaughan.kforest.Dataset
import com.natevaughan.kforest.LearningUtils.median
import com.natevaughan.kforest.Tuple2

/**
 * @author Nate Vaughan
 */
fun preprocessNumeric(dataset: Collection<Collection<Tuple2<String, Any>>>): Dataset {
    // assumes rectangular dataset; might not want to?
    val numerics = dataset.first().filter {
        it.right is Number
    }
    val medians = numerics.map { numeric ->
        numeric.left to median(dataset.mapNotNull { row ->
            val tuple = row.find { it.left == numeric.left }
            when {
                tuple == null -> null
                tuple.right is Number -> tuple.right.toDouble()
                else -> null
            }
        })
    }.toMap()

    return dataset.map { row ->
        row.map { tuple ->
            val median = medians[tuple.left]
            when {
                median == null -> tuple as Tuple2<String, String>
                tuple.right is Number && tuple.right.toDouble() >= median -> Tuple2(tuple.left, ">=$median")
                tuple.right is Number && tuple.right.toDouble() < median -> Tuple2(tuple.left, "<$median")
                else -> tuple as Tuple2<String, String>
            }
        }
    }
}