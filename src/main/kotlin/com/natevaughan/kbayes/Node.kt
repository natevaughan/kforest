package com.natevaughan.kbayes

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
}