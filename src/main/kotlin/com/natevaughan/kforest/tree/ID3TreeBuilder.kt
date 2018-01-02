@file:JvmName("ID3TreeBuilder")
package com.natevaughan.kforest.tree

import com.natevaughan.kforest.CategoricalNode
import com.natevaughan.kforest.LearningUtils.entropy
import com.natevaughan.kforest.Node
import com.natevaughan.kforest.TerminalNode
import com.natevaughan.kforest.Dataset
import com.natevaughan.kforest.Matrix
import com.natevaughan.kforest.TrainingException
import com.natevaughan.kforest.Tuple2

/**
 * @author Nate Vaughan
 */
fun buildTree(dataset: Dataset, targetVariableName: String, excludedVariables: List<String>, remainingLayers: Int, minimumGain: Double): Node {
    if (dataset.isEmpty()) {
        throw TrainingException("Cannot train on an empty dataset")
    }

    val matrix = getCounts(dataset, targetVariableName, excludedVariables)
    if (matrix.isEmpty()) {
        throw TrainingException("All variables excluded")
    }

    val targetCounts = matrix[targetVariableName]!!.flatMap { it.value }
    val parentEntropy = calculateEntropy(targetCounts, dataset.size)

    val eligibleEntropies = getEntropyTuples(matrix, dataset.size).filter {
        it.left != targetVariableName && parentEntropy - it.right > minimumGain
    }

    if (eligibleEntropies.isEmpty()) {
        return terminalNode(targetCounts)
    }

    val best = eligibleEntropies.minBy { it.right }!!

    if (remainingLayers == 0 || eligibleEntropies.size == 1) {
        val rootNode = buildTerminalNodes(matrix.get(best.left)!!, best.left)
        return rootNode
    }

    val goldenChildren = matrix.get(best.left)!!.filter { it.value.size == 1 }

    val subsets = subset(dataset, best.left)
    val newExcludedVariables = excludedVariables + best.left
    val ungoldenChildren = subsets.filter { !goldenChildren.containsKey(it.key) }

    val children = ungoldenChildren.map { subset ->
        subset.key to buildTree(subset.value, targetVariableName, newExcludedVariables, remainingLayers - 1, minimumGain)
    }.toMap()

    return CategoricalNode(best.left, children + goldenChildren.map { it.key to terminalNode(it.value) })
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

fun buildTerminalNodes(counts: Map<String, Collection<Tuple2<String, Int>>>, nodeVariableName: String): Node {
    val children = counts.map { it.key to terminalNode(it.value) }.toMap()
    return CategoricalNode(nodeVariableName, children)
}

private fun terminalNode(values: Collection<Tuple2<String, Int>>): TerminalNode {
    return TerminalNode(values.maxBy { target ->
        target.right
    }!!.left, values)
}

fun getEntropyTuples(matrix: Matrix, datasetSize: Int): Collection<Tuple2<String, Double>> {
    return matrix.entries.map {
        val entropy = calculateEntropy(it.value, datasetSize)
        Tuple2(it.key, entropy)
    }
}

fun getCounts(
        dataset: Dataset,
        targetVariableName: String,
        ignoredVariables: Collection<String>
): Matrix {

    val countMap = mutableMapOf<String, MutableMap<String, MutableList<Tuple2<String, Int>>>>()

    for (coll in dataset) {

        val target = coll.find { it.left == targetVariableName } ?: throw RuntimeException("target not found in row")

        for (tuple in coll) {
            if (ignoredVariables.contains(tuple.left)) continue

            val column = countMap[tuple.left]
            val cell = column?.get(tuple.right)
            val counts = cell?.find { it.left == target.right }

            when {
                (column == null) -> countMap.put(tuple.left, mutableMapOf(tuple.right to mutableListOf(Tuple2(target.right, 1))))
                (cell == null) -> column.put(tuple.right, mutableListOf(Tuple2(target.right, 1)))
                (counts == null) -> cell.add(Tuple2(target.right, 1))
                else -> {
                    cell.remove(counts)
                    cell.add(counts.copy(right = counts.right + 1))
                }
            }
        }
    }

    return countMap
}

fun calculateEntropy(column: Map<String, Collection<Tuple2<String, Int>>>, parentCount: Int): Double {
    return column.entries.sumByDouble { entry ->
        calculateEntropy(entry.value, parentCount)
    }
}
fun calculateEntropy(value: Collection<Tuple2<String, Int>>, parentCount: Int): Double {
    val sum = value.sumByDouble { it.right.toDouble() }
    return sum * entropy(value.map { it.right }) / parentCount
}