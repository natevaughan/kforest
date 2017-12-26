package com.natevaughan.kforest

import com.natevaughan.kforest.core.Tuple2
import com.natevaughan.kforest.tree.CategoricalNode
import com.natevaughan.kforest.tree.ForestNode
import com.natevaughan.kforest.tree.buildTerminalNodes
import com.natevaughan.kforest.tree.buildTree
import com.natevaughan.kforest.tree.getCounts
import com.natevaughan.kforest.tree.getEntropyTuples
import com.natevaughan.kforest.tree.subset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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
        val forestNode = ForestNode(dataset, GOLF, 3, 0.5, 3, 3)
        for (row in makeValidationDataset()) {
            val prediction = forestNode.classify(row)
            assertTrue(setOf(YES, NO).contains(prediction))
        }
    }

    @Test
    fun buildTreeTest() {
        val dataset = makeDataset()
        val root: CategoricalNode = buildTree(dataset, GOLF, emptyList(), 3) as CategoricalNode
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
}