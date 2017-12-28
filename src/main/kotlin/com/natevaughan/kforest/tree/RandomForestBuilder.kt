package com.natevaughan.kforest.tree

import com.natevaughan.kforest.EnsembleNode
import com.natevaughan.kforest.Node
import com.natevaughan.kforest.SamplingUtils
import com.natevaughan.kforest.Dataset

/**
 * @author Nate Vaughan
 */
class RandomForestBuilder {
    var dataset: Dataset? = null
    var target: String? = null
    var nodes: Int = 100
    var sampleProportion: Double = 0.3
    var depth: Int = 5
    var variablesPerTree: Int = 5
    var seed: Long = 0L

    fun dataset(value: Dataset): RandomForestBuilder {
        dataset = value
        return this
    }
    fun target(value: String): RandomForestBuilder {
        target = value
        return this
    }
    fun nodes(value: Int): RandomForestBuilder {
        nodes = value
        return this
    }
    fun sampleProportion(value: Double): RandomForestBuilder {
        sampleProportion = value
        return this
    }
    fun depth(value: Int): RandomForestBuilder {
        depth = value
        return this
    }
    fun variablesPerTree(value: Int): RandomForestBuilder {
        variablesPerTree = value
        return this
    }
    fun seed(value: Long): RandomForestBuilder {
        seed = value
        return this
    }
    fun build(): EnsembleNode {
        val datasetSnapshot = dataset ?: throw RuntimeException("Cannot build forest with null dataset")
        val targetSnapshot = target ?: throw RuntimeException("Cannot build forest with null target")

        val sampleUtils = SamplingUtils(seed)
        val sampleCount = (datasetSnapshot.size * sampleProportion).toInt()
        val forest = mutableListOf<Node>()
        for (i in 0..nodes) {
            val sampled = sampleUtils.sample(datasetSnapshot, sampleCount)
            val allVariables = datasetSnapshot.first().map { it.left }.filter { it != targetSnapshot }
            val excludedVariables = allVariables - sampleUtils.sampleCollection(allVariables, variablesPerTree)
            forest.add(buildTree(sampled, targetSnapshot, excludedVariables, 4))
        }
        return EnsembleNode(forest)
    }
}
