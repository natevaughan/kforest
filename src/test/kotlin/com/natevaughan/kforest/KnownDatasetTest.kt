package com.natevaughan.kforest

import com.natevaughan.kforest.tree.RandomForestBuilder
import com.opencsv.CSVReader
import org.junit.Test
import java.io.InputStreamReader

/**
 * @author Nate Vaughan
 */
class KnownDatasetTest {
    private val TARG = "target"

    @Test
    fun itShouldPredictOnTheKnownDataset() {
        val dataset = getDataset("dataset-1.csv.test")
        val forestNode = RandomForestBuilder()
                .dataset(dataset)
                .target(TARG)
                .seed(0L)
                .variablesPerTree(2)
                .build()
        val confusionMatrix = ConfusionMatrix()
        val validation = getDataset("dataset-1-validate.csv.test")
        validation.forEach { row ->
            try {
                val classification = forestNode.classify(row)
                confusionMatrix.register(classification, row.find { it.left == TARG }!!.right)
            } catch (e: ClassificationException) {
                confusionMatrix.register("error", row.find { it.left == TARG }!!.right)
            }
        }
        println(confusionMatrix)
    }

    fun getDataset(dataset: String): Dataset {
        val inStream = KnownDatasetTest::class.java.classLoader.getResourceAsStream(dataset)
        val csv = CSVReader(InputStreamReader(inStream))
        val header = csv.readNext()
        val body = csv.readAll()
        return body.map { it ->
            (0 until header.size).map { idx ->
                Tuple2(header[idx], it[idx])
            }
        }
    }
}