@file:JvmName("ID3TreeBuilder")
package com.natevaughan.kbayes

import com.natevaughan.kbayes.LearningUtils.entropy

/**
 * @author Nate Vaughan
 */
fun buildTree(dataset: Collection<Collection<Tuple2<String, String>>>, targetVariableName: String): Node {
    if (dataset.isEmpty()) {
        throw TrainingException("Cannot train on an empty dataset")
    }
    // assumes rectangular dataset, might want to handle different cases
    val allVariables = dataset.first().map { it.left }
    val excludedVariables = emptyList<String>()
    val matrix = getCounts(dataset, targetVariableName, excludedVariables)
    val entropies =  getEntropyTuples(matrix, dataset.size.toLong())
    val best = entropies.minBy { it.right }
    val rootNode = buildNode(matrix.get(best!!.left)!!, best.left)
    val processedVariables = excludedVariables + best.left
    for (entry in matrix.entries) {
        println("need to do something with entry $entry")
    }
    return rootNode
}

fun subset(dataset: Collection<Collection<Tuple2<String, String>>>, variableName: String): Map<String, Collection<Collection<Tuple2<String, String>>>> {
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

fun buildNode(counts: Map<String, Collection<Tuple2<String, Long>>> , nodeVariableName: String): Node {
    val children = counts.map { it.key to TerminalNode(it.value.maxBy { target ->
        target.right
    }!!.left) }.toMap()
    return CategoricalNode(nodeVariableName, children)
}

fun getEntropyTuples(matrix: Map<String, Map<String, Collection<Tuple2<String, Long>>>>, datasetSize: Long): Collection<Tuple2<String, Double>> {
    return matrix.entries.map {
        val entropy = calculateEntropy(it.value, datasetSize)
        Tuple2(it.key, entropy)
    }
}

fun getCounts(
        dataset: Collection<Collection<Tuple2<String, String>>>,
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