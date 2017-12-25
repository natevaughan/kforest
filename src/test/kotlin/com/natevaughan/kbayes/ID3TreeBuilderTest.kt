package com.natevaughan.kbayes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.slf4j.LoggerFactory

/**
 * @author Nate Vaughan
 */
class ID3TreeBuilderTest {

    private val OUTLOOK     = "outlook"
    private val GOLF        = "golf"
    private val OVERCAST    = "overcast"
    private val RAINY       = "rainy"
    private val SUNNY       = "sunny"
    private val YES         = "yes"
    private val NO          = "no"

    companion object {
        val log = LoggerFactory.getLogger(ID3TreeBuilderTest::class.java)
    }

    @Test
    fun buildForestTest() {
        val dataset = makeDataset()
        val forest = mutableListOf<Node>()
        for (i in 0..14) {
            val sampled = sample(dataset, 5)
            val allVariables = dataset.first().map { it.left }
            val blacklistIndexes = getRandomInts(2, allVariables.size)
            val blacklist = allVariables.filterIndexed { index, variable -> index in blacklistIndexes }
            forest.add(buildTree(sampled, GOLF, blacklist, 3))
        }
        val forestNode = ForestNode(forest)
        makeValidationDataset().forEach { row ->
            println("==============")
            println(row)
            println("--------------")
            val classification = forestNode.classify(row)
            println(classification)
        }
    }

    private fun sample(dataset: Collection<Collection<Tuple2<String, String>>>, count: Int): Collection<Collection<Tuple2<String, String>>> {
        val rows = getRandomInts(count, dataset.size)
        return dataset.filterIndexed { index, collection ->
            index in rows
        }
    }

    @Test
    fun buildTreeTest() {
        val dataset = makeDataset()
        val root =  buildTree(dataset, GOLF, emptyList(), 3)
        assertNotNull(root)
        assertEquals("It should be $OUTLOOK", OUTLOOK, root.variable)
    }

    @Test
    fun subsetTest() {
        val dataset = makeDataset()
        val subsets = subset(dataset, OUTLOOK)
        assertNotNull(subsets)
        assertEquals("It should have 3 values", 3, subsets.size)
        assertEquals("The subsets should add up to the original dataset", dataset.size, subsets.map { it.value }.sumBy { it.size })
    }
    
    @Test
    fun getCountsTest() {
        val dataset = makeDataset()
        val matrix = getCounts(dataset, GOLF, emptyList())
        assertNotNull(matrix[OUTLOOK])
        val entropies = getEntropyTuples(matrix, dataset.size.toLong())
        assertEquals(4, entropies.size)
    }
    
    @Test
    fun buildNodeTest(){
        val matrix: Map<String, Collection<Tuple2<String, Long>>> = mapOf(
            OVERCAST to listOf(
                    Tuple2(YES, 4L)
            ),
            RAINY to listOf(
                Tuple2(YES, 2L),
                Tuple2(NO, 3L)
            )
        )
        val nodeVariableName = OUTLOOK
        val node = buildTerminalNodes(matrix, nodeVariableName)
        val prediction1 = node.classify(listOf(Tuple2(OUTLOOK, OVERCAST)))
        val prediction2 = node.classify(listOf(Tuple2(OUTLOOK, RAINY)))
        assertEquals("$OVERCAST should result in a prediction of $YES", YES, prediction1)
        assertEquals("$RAINY should result in a prediction of $NO", NO, prediction2)
    }

    fun makeDataset(): List<List<Tuple2<String, String>>> {
        val header = arrayOf("outlook","temp","humidity","windy", GOLF)
        val vals = arrayOf(
            arrayOf(RAINY,"hot","high","false",NO),
            arrayOf(RAINY,"hot","high","true",NO),
            arrayOf(OVERCAST,"hot","high","false",YES),
            arrayOf(SUNNY,"mild","high","false",YES),
            arrayOf(SUNNY,"cool","normal","false",YES),
            arrayOf(SUNNY,"cool","normal","true",NO),
            arrayOf(OVERCAST,"cool","normal","true",YES),
            arrayOf(RAINY,"mild","high","false",NO),
            arrayOf(RAINY,"cool","normal","false",YES),
            arrayOf(SUNNY,"mild","normal","false",YES),
            arrayOf(RAINY,"mild","normal","true",YES),
            arrayOf(OVERCAST,"mild","high","true",YES),
            arrayOf(OVERCAST,"hot","normal","false",YES),
            arrayOf(SUNNY,"mild","high","true",NO))
        return vals.map { it ->
            (0 until header.size).map { idx ->
                Tuple2(header[idx], it[idx])
            }
        }
    }
    fun makeValidationDataset(): List<List<Tuple2<String, String>>> {
        val header = arrayOf("outlook", "temp", "humidity", "windy", GOLF)
        val vals = arrayOf(
            arrayOf(OVERCAST, "hot", "high", "false", NO),
            arrayOf(RAINY, "hot", "high", "true", NO),
            arrayOf(SUNNY, "hot", "high", "false", NO),
            arrayOf(RAINY, "cool", "high", "true", NO),
            arrayOf(SUNNY, "cool", "high", "true", NO),
            arrayOf(RAINY, "mild", "normal", "false", NO))
        return vals.map { it ->
            (0 until header.size).map { idx ->
                Tuple2(header[idx], it[idx])
            }
        }
    }

    fun getRandomInts(count: Int, max: Int): Collection<Int> {
        return (1..count).map {
            (Math.random() * max + 0.5).toInt()
        }
    }

}