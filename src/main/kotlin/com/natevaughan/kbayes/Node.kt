package com.natevaughan.kbayes

/**
 * @author Nate Vaughan
 */
interface Node {
    val variable: String
    fun classify(values: Collection<Tuple2<String, String>>): String
}

data class TerminalNode(val value: String): Node {
    override val variable = "terminal"
    override fun classify(values: Collection<Tuple2<String, String>>): String {
        return value
    }
    override fun toString(): String {
        return "->$value"
    }
}

data class CategoricalNode(override val variable: String, val children: Map<String, Node>): Node {
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

data class ForestNode(val forest: Collection<Node>): Node {
    override val variable = "forest"

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
        val vote = votes.maxBy { it.value }!!
        return vote.key
    }
}