package com.natevaughan.kforest

/**
 * @author Nate Vaughan
 */
typealias Dataset = Collection<Collection<Tuple2<String, String>>>
typealias Matrix = Map<String, Map<String, Collection<Tuple2<String, Int>>>>