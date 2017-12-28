package com.natevaughan.kforest

/**
 * @author Nate Vaughan
 */
class Table <R, C, V> {
    private val rows: MutableMap<R, MutableMap<C, V>> = mutableMapOf()

    fun put(row: R, column: C, value: V) {
        val rowMap = this.rows.get(row)
        if (rowMap == null) {
            rows.put(row, mutableMapOf(column to value))
        } else {
            rowMap.put(column, value)
        }
    }

    fun get(row: R, column: C): V? {
        return this.rows.get(row)?.get(column)
    }

    override fun toString(): String {
        return rows.map { it.key.toString() + "-> " + it.value.toString() }.toString()
    }
}