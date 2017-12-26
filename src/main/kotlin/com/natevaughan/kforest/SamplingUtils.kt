package com.natevaughan.kforest

import com.natevaughan.kforest.core.Tuple2

/**
 * @author Nate Vaughan
 */
internal object SamplingUtils {

    /**
     * Get a sample of a dataset
     */
    fun sample(dataset: Collection<Collection<Tuple2<String, String>>>, count: Int): Collection<Collection<Tuple2<String, String>>> {
        val rows = uniqueRandomInts(count, dataset.size)
        return dataset.filterIndexed { index, collection ->
            index in rows
        }
    }

    /**
     * Get a list of N random ints from 0 till max
     */
    fun randomInt(max: Int): Int {
        return (Math.random() * max + 0.5).toInt()

    }

    fun uniqueRandomInts(count: Int, max: Int): Collection<Int> {
        if (count > max) {
            throw RuntimeException("Cannot get $count unique ints with a max of $max")
        }
        val set = mutableSetOf<Int>()
        while (set.size < max - 1) {
            set.add(randomInt(max))
        }
        return set
    }

    fun <T> sampleCollection(collection: Collection<T>, sampleSize: Int): Collection<T> {
        val collectionSize = collection.size
        if (sampleSize > collectionSize) {
            throw RuntimeException("Cannot build a sample of size $sampleSize for a collection of size $collectionSize")
        }
        val indices = uniqueRandomInts(sampleSize, collectionSize)
        return collection.filterIndexed { index, variable -> index in indices }
    }
}