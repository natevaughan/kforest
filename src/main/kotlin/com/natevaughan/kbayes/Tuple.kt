package com.natevaughan.kbayes

/**
 * @author Nate Vaughan
 */
data class Tuple2<L, R>(val left: L, val right: R) {
    override fun toString(): String {
        return "[$left, $right]"
    }
}

data class Tuple3<L, C, R>(val left: L, val center: C, val right: R) {
    override fun toString(): String {
        return "[$left, $center, $right]"
    }
}
