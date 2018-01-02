package com.natevaughan.kforest

/**
 * @author Nate Vaughan
 */
interface Node {
    fun classify(values: Collection<Tuple2<String, String>>): String
}

data class TerminalNode(val value: String, val counts: Collection<Tuple2<String, Int>>): Node {
    override fun classify(values: Collection<Tuple2<String, String>>): String {
        return value
    }
    override fun toString(): String {
        return "$value from $counts"
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

data class EnsembleNode(val forest: List<Node>): Node {

    private val ERROR = "error"

    override fun classify(values: Collection<Tuple2<String, String>>): String {
        val votes = mutableMapOf<String, Int>()
        forest.forEach {
            var classification: String
            try {
                classification = it.classify(values)
            } catch (e: Exception) {
                classification = ERROR
            }
            val vote = votes[classification]
            if (vote != null) {
                votes.put(classification, vote + 1)
            } else {
                votes.put(classification, 1)
            }
        }
        val vote = votes.maxBy { it.value } ?: throw ClassificationException("Cannot classify $values: no votes registered")
        return vote.key
    }
}