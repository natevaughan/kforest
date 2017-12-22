package com.natevaughan.kbayes

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.slf4j.LoggerFactory

/**
 * @author Nate Vaughan
 */
class ID3TreeBuilderTest {
    companion object {
        val log = LoggerFactory.getLogger(ID3TreeBuilderTest::class.java)
    }

    @Test
    fun getCountsTest() {
        val dataset = makeDataset()
        val treeBuilder = ID3TreeBuilder()
        assertNotNull(treeBuilder)
        val matrix = treeBuilder.getCounts(dataset, "golf")
        assertNotNull(matrix["outlook"])
        val size = dataset.size
        val entropies = matrix.entries.map {
            val entropy = treeBuilder.calculateEntropy(it.value, size.toLong())
            Tuple2(it.key, entropy)
        }
        println(entropies)
    }

    fun makeDataset(): List<List<Tuple2<String, String>>> {
        val header = arrayOf("outlook","temp","humidity","windy", "golf")
        val vals = arrayOf(
                arrayOf("rainy","hot","high","false","no"),
                arrayOf("rainy","hot","high","true","no"),
                arrayOf("overcast","hot","high","false","yes"),
                arrayOf("sunny","mild","high","false","yes"),
                arrayOf("sunny","cool","normal","false","yes"),
                arrayOf("sunny","cool","normal","true","no"),
                arrayOf("overcast","cool","normal","true","yes"),
                arrayOf("rainy","mild","high","false","no"),
                arrayOf("rainy","cool","normal","false","yes"),
                arrayOf("sunny","mild","normal","false","yes"),
                arrayOf("rainy","mild","normal","true","yes"),
                arrayOf("overcast","mild","high","true","yes"),
                arrayOf("overcast","hot","normal","false","yes"),
                arrayOf("sunny","mild","high","true","no"))
        return vals.map { it ->
            (0 until header.size).map { idx ->
                Tuple2(header[idx], it[idx])
            }
        }
    }

}