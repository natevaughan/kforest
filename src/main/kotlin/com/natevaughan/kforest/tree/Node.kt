package com.natevaughan.kforest.tree

import com.natevaughan.kforest.SamplingUtils
import com.natevaughan.kforest.SamplingUtils.sampleCollection
import com.natevaughan.kforest.core.ClassificationException
import com.natevaughan.kforest.core.Dataset
import com.natevaughan.kforest.core.OutOfBoundsException
import com.natevaughan.kforest.core.Tuple2

/**
 * @author Nate Vaughan
 */
interface Node {
    fun classify(values: Collection<Tuple2<String, String>>): String
}

data class TerminalNode(val value: String): Node {
    override fun classify(values: Collection<Tuple2<String, String>>): String {
        return value
    }
    override fun toString(): String {
        return "->$value"
    }
}

data class CategoricalNode(val variable: String, val children: Map<String, Node>): Node {
    override fun classify(values: Collection<Tuple2<String, String>>): String {
        val thisVariable = values.find { it.left == variable }
        if (thisVariable != null) {
            val thisValue = children[thisVariable.right]
            if (thisValue != null) {
                return thisValue.classify(values)
            }
            throw OutOfBoundsException("Value ${thisVariable.right} for variable $variable")
        }
        throw ClassificationException("Variable $variable not found")
    }
    override fun toString(): String {
        return "$variable (${children.size} children)"
    }
}

data class ForestNode(
        val dataset: Dataset,
        val targetVariable: String,
        val nodes: Int,
        val sampleProportion: Double,
        val allowedDepth: Int,
        val maxVariables: Int
): Node {
    val forest = mutableListOf<Node>()

    init {
        val sampleCount = (this.dataset.size * sampleProportion).toInt()
        for (i in 0..nodes) {
            val sampled = SamplingUtils.sample(dataset, sampleCount)
            val allVariables = dataset.first().map { it.left }
            val excludedVariables = allVariables - sampleCollection(allVariables, maxVariables)
            forest.add(buildTree(sampled, targetVariable, excludedVariables, 4))
        }
    }

    override fun classify(values: Collection<Tuple2<String, String>>): String {
        val votes = mutableMapOf<String, Int>()
        forest.forEach {
            try {
                val classification = it.classify(values)
                val v = votes.get( classification )
                if (v != null) {
                    votes.put(classification, v + 1)
                } else {
                    votes.put(classification, 1)
                }
            } catch (e: OutOfBoundsException) {
            }
        }
        val vote = votes.maxBy { it.value } ?: throw ClassificationException("Cannot classify $values")
        return vote.key
    }
}