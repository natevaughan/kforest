@file:JvmName("ID3TreeBuilder")
package com.natevaughan.kforest.tree

import com.natevaughan.kforest.CategoricalNode
import com.natevaughan.kforest.LearningUtils.entropy
import com.natevaughan.kforest.Node
import com.natevaughan.kforest.TerminalNode
import com.natevaughan.kforest.Dataset
import com.natevaughan.kforest.TrainingException
import com.natevaughan.kforest.Tuple2

/**
 * @author Nate Vaughan
 */
fun buildTree(dataset: Dataset, targetVariableName: String, excludedVariables: List<String>, remainingLayers: Int): Node {
    if (dataset.isEmpty()) {
        throw TrainingException("Cannot train on an empty dataset")
    }
    val matrix = getCounts(dataset, targetVariableName, excludedVariables)
    if (matrix.isEmpty()) {
        throw TrainingException("All variables excluded")
    }
    val entropies = getEntropyTuples(matrix, dataset.size.toLong())
    val best = entropies.minBy { it.right }!!

    if (remainingLayers == 0 || entropies.size == 1) {
        val rootNode = buildTerminalNodes(matrix.get(best.left)!!, best.left)
        return rootNode
    }

    val goldenChildren = matrix.get(best.left)!!.filter { it.value.size == 1 }

    val subsets = subset(dataset, best.left)
    val newExcludedVariables = excludedVariables + best.left
    val ungoldenChildren = subsets.filter { !goldenChildren.containsKey(it.key) }

    val children = ungoldenChildren.map { subset ->
        subset.key to buildTree(subset.value, targetVariableName, newExcludedVariables, remainingLayers - 1)
    }.toMap()

    return CategoricalNode(best.left, children + goldenChildren.map { it.key to terminalNode(it.key, it.value) })
}

fun subset(dataset: Dataset, variableName: String): Map<String, Collection<Collection<Tuple2<String, String>>>> {
    val datasets = mutableMapOf<String, MutableList<Collection<Tuple2<String, String>>>>()
    for (row in dataset) {
        val value = row.find { it.left == variableName }?.right ?: throw TrainingException("Row was missing value $variableName")
        val subset = datasets.get(value)
        val filteredRow: Collection<Tuple2<String, String>> = row.filter { it.left != variableName }
        if (subset == null) {
            val list = mutableListOf(filteredRow)
            datasets.put(value, list)
        } else {
            subset.add(filteredRow)
        }
    }
    return datasets
}

fun buildTerminalNodes(counts: Map<String, Collection<Tuple2<String, Long>>>, nodeVariableName: String): Node {
    val children = counts.map { it.key to terminalNode(it.key, it.value) }.toMap()
    return CategoricalNode(nodeVariableName, children)
}

private fun terminalNode(name: String, values: Collection<Tuple2<String, Long>>): TerminalNode {
    return TerminalNode(values.maxBy { target ->
        target.right
    }!!.left)
}

fun getEntropyTuples(matrix: Map<String, Map<String, Collection<Tuple2<String, Long>>>>, datasetSize: Long): Collection<Tuple2<String, Double>> {
    return matrix.entries.map {
        val entropy = calculateEntropy(it.value, datasetSize)
        Tuple2(it.key, entropy)
    }
}

fun getCounts(
        dataset: Dataset,
        targetVariableName: String,
        ignoredVariables: Collection<String>
): Map<String, Map<String, Collection<Tuple2<String, Long>>>> {

    val countMap = mutableMapOf<String, MutableMap<String, MutableList<Tuple2<String, Long>>>>()

    for (coll in dataset) {

        val target = coll.find { it.left == targetVariableName } ?: throw RuntimeException("target not found in row")

        for (tuple in coll) {
            if (tuple.left == targetVariableName || ignoredVariables.contains(tuple.left)) continue

            val column = countMap[tuple.left]
            val cell = column?.get(tuple.right)
            val counts = cell?.find { it.left == target.right }

            when {
                (column == null) -> countMap.put(tuple.left, mutableMapOf(tuple.right to mutableListOf(Tuple2(target.right, 1L))))
                (cell == null) -> column.put(tuple.right, mutableListOf(Tuple2(target.right, 1L)))
                (counts == null) -> cell.add(Tuple2(target.right, 1L))
                else -> {
                    cell.remove(counts)
                    cell.add(counts.copy(right = counts.right + 1))
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