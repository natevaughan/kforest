package com.natevaughan.kforest

import java.util.Random


/**
 * @author Nate Vaughan
 */
internal class SamplingUtils(seed: Long) {

    val random = Random(seed)

    /**
     * Get a sample of a dataset
     */
    fun sample(dataset: Collection<Collection<Tuple2<String, String>>>, count: Int): Collection<Collection<Tuple2<String, String>>> {
        val rows = uniqueRandomInts(count, 0, dataset.size)
        return dataset.filterIndexed { index, collection ->
            index in rows
        }
    }

    /**
     * Get a list of N random ints from 0 till max
     */
    fun randomInt(max: Int): Int {
        return (random.nextDouble() * max + 0.5).toInt()

    }

    fun uniqueRandomInts(count: Int, min: Int, max: Int): Collection<Int> {
        if (count > max) {
            throw RuntimeException("Cannot get $count unique ints with a max of $max")
        }
        val set = mutableSetOf<Int>()
        val list = (min..max).toMutableList()
        while (set.size < count) {
            set.add(list.removeAt(randomInt(list.size - 1)))
        }
        return set
    }

    fun <T> sampleCollection(collection: Collection<T>, sampleSize: Int): Collection<T> {
        val collectionSize = collection.size
        if (sampleSize > collectionSize) {
            throw RuntimeException("Cannot build a sample of size $sampleSize for a collection of size $collectionSize")
        }
        val indices = uniqueRandomInts(sampleSize, 0, collectionSize - 1)
        return collection.filterIndexed { index, variable -> index in indices }
    }
}