package com.natevaughan.kbayes

import com.natevaughan.kbayes.LearningUtils.entropy

/**
 * @author Nate Vaughan
 */
class ID3TreeBuilder {
    fun getCounts(dataset: Collection<Collection<Tuple2<String, String>>>, targetVariableName: String): Map<String, Map<String, Collection<Tuple2<String, Long>>>> {
        val countMap = mutableMapOf<String, MutableMap<String, MutableList<Tuple2<String, Long>>>>()
        for (coll in dataset) {
            val target = coll.find { it.left == targetVariableName } ?: throw RuntimeException("target not found in row")
            for (tuple in coll) {
                val column = countMap[tuple.left]
                if (column == null) {
                    countMap.put(tuple.left, mutableMapOf(tuple.right to mutableListOf(Tuple2(target.right, 1L))))
                } else {
                    val cell = column[tuple.right]
                    if (cell == null) {
                        column.put(tuple.right, mutableListOf(Tuple2(target.right, 1L)))
                    } else {
                        val counts = cell.find { it.left == target.right }
                        if (counts == null) {
                            cell.add(Tuple2(target.right, 1L))
                        } else {
                            cell.remove(counts)
                            cell.add(counts.copy(right = counts.right + 1))
                        }
                    }
                }
            }
        }
        return countMap
    }

    fun calculateEntropy(column: Map<String, Collection<Tuple2<String, Long>>>, parentCount: Long): Double {
        return column.entries.sumByDouble { entry ->
            val sum = entry.value.sumByDouble { it.right.toDouble() }
            sum * entropy(entry.value.map { it.right }) / parentCount
        }
    }
}