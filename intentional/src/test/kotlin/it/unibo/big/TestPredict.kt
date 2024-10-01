package it.unibo.big

import it.unibo.Intention
import it.unibo.predict.PredictExecute
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths

class TestPredict {

    val path = "resources/intention/output/"

    fun execute(i: String) {
        try {
            val d = PredictExecute.parse(i)
            val ret = PredictExecute.execute(d, path)
            assertTrue(ret.second.nrow > 0)
            assertTrue(ret.third.nrow > 0)
        } catch (e: Exception) {
            e.printStackTrace()
            fail<String>(e.message)
        }
    }

    @BeforeEach
    fun before() {
        Intention.DEBUG = true
    }

    @Test
    fun `test cimice 8`() {
        Intention.DEBUG = false
        listOf("['2022-10', '2022-42']", "['2021-10', '2022-42']", "['2020-10', '2022-42']").forEachIndexed { i, v ->
            execute("with CIMICE predict adults for week between $v and province in ('BO') by week, province from small_instars, total_captures accuracysize 10 executionid Cimice-202-$i")
        }
    }

    @Test
    fun `test cimice 7`() {
        Intention.DEBUG = false
        val measures = listOf("adults", "cum_degree_days")
        measures.forEachIndexed { i, _ ->
            execute("with CIMICE predict adults for province in ('BO') by week, province from ${measures.subList(0, i + 1).reduce { a, b -> "$a, $b" }} accuracysize 20 executionid Cimice-200-$i")
        }
    }

    @Test
    fun `test cimice 6`() {
        Intention.DEBUG = false
        val measures = listOf("adults", "small_instars", "large_instars", "total_captures")
        measures.forEachIndexed { i, _ ->
            execute("with CIMICE predict adults for province in ('BO') by week, province from ${measures.subList(0, i + 1).reduce { a, b -> "$a, $b" }} accuracysize 20 executionid Cimice-117-$i")
        }
    }

    @Test
    fun `test cimice 5`() {
        Intention.DEBUG = false
        val provinces = listOf("'BO'", "'RA'", "'FC'")
        provinces.forEachIndexed { i, _ ->
            execute("with CIMICE predict adults for province in (${provinces.subList(0, i + 1).reduce { a, b -> "$a, $b" }}) by week, province from small_instars, total_captures accuracysize 20 executionid Cimice-119-$i")
        }
    }

    @Test
    fun `test cimice 4`() {
        execute("with CIMICE predict adults for month between ['2021-05', '2021-09'] and province in ('BO', 'RA') by week, province from small_instars, total_captures executionid Cimice-114-Test4")
    }

    @Test
    fun `test cimice 3`() {
        execute("with CIMICE predict adults for month between ['2021-05', '2021-09'] by week, province from small_instars, total_captures")
    }

    @Test
    fun `test cimice2`() {
        execute("with CIMICE predict adults for month between ['2021-05', '2021-09'] by week from small_instars, total_captures executionid Cimice-113-Test2")
    }

    @Test
    fun `test cimice 1`() {
        execute("with CIMICE predict adults by week")
    }

    @Test
    fun `test watering`() {
        Intention.DEBUG = false
        val sensors = listOf(
                "Sensor-0.25_0_-0.6", "Sensor-0.25_0_-0.4", "Sensor-0.25_0_-0.2",
                "Sensor-0.5_0_-0.2", "Sensor-0.5_0_-0.4", "Sensor-0.5_0_-0.6",
                "Sensor-0.8_0_-0.2", "Sensor-0.8_0_-0.4", "Sensor-0.8_0_-0.6",
                "Sensor-0_0_-0.2", "Sensor-0_0_-0.4", "Sensor-0_0_-0.6"
        )

        listOf(1, 2).forEach { seed ->
            var i = 0
            listOf(
                    "2022-07-05 10:00:00", "2022-07-10 10:00:00", "2022-07-15 10:00:00", "2022-07-20 10:00:00",
                    "2022-07-25 10:00:00", "2022-07-30 10:00:00", "2022-08-14 10:00:00" /*, "2022-08-24 10:00:00" */
            ).forEach { timestamp ->
                execute("with WATERING predict value " +
                        "by hour, agent " +
                        "for agent in (${sensors.subList(0, sensors.size).map { "'${it}'" }.reduce { a, b -> "${a}, ${b}" }}) " +
                        "and hour between ['2022-07-01 10:00:00', '${timestamp}'] " +
                        "and measurement_type in ('GROUND_WATER_POTENTIAL') " +
                        "and field='Field-1f032c308c' " +
                        "using timeDecisionTree, timeRandomForest, decisionTree, randomForest, univariateTS " + //
                        "nullify 5 " +
                        "executionid I-120-12-${i++}")
            }
            i = 0
            listOf(2, 3, 4, 5, 6, 9, 12).forEach { idx ->
                listOf("2022-07-04 10:00:00").forEach { timestamp ->
                    execute("with WATERING predict value " +
                            "by hour, agent " +
                            "for agent in (${sensors.subList(0, idx).map { "'${it}'" }.reduce { a, b -> "${a}, ${b}" }}) " +
                            "and hour between ['2022-07-01 10:00:00', '${timestamp}'] " +
                            "and measurement_type in ('GROUND_WATER_POTENTIAL') " +
                            "and field='Field-1f032c308c' " +
                            (if (idx <= 6) {
                                ""
                            } else {
                                "using timeDecisionTree, timeRandomForest, decisionTree, randomForest, univariateTS "
                            }) +
                            "nullify 5 " +
                            "executionid I-122-$idx-${i++}")
                }
            }
        }
    }

    @Test
    fun `test models`() {
        execute("with sales_fact_1997 predict unit_sales by the_date")
    }

    @Test
    fun `test models 2`() {
        execute("with sales_fact_1997 predict unit_sales by the_date using univariateTS")
        // execute("with sales_fact_1997 predict unit_sales by the_month using multivariateTS")
        execute("with sales_fact_1997 predict unit_sales by the_month using timeDecisionTree")
        execute("with sales_fact_1997 predict unit_sales by the_month using timeRandomForest, univariateTS")
    }

//    @Test
//    fun testScalability() {
//        val writer = Files.newBufferedWriter(Paths.get("resources/intention/predict_time.csv"))
//        val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT)
//        var first = true
//
//        for (t in 0..9) {
//            listOf(
//                    "with sales predict unit_sales by the_month", // 12
//                    "with sales predict unit_sales by product_family, the_month", // 36
//                    "with sales predict unit_sales by the_date", // 323
//                    "with sales predict unit_sales by product_category, the_month", // 540
//                    "with sales predict unit_sales by product_subcategory, the_month", // 1224
//                    "with sales predict unit_sales by product_category, the_date", // 12113
//                    "with sales predict unit_sales by customer_id, the_month", // 16949
//                    "with sales predict unit_sales by product_id, the_month", // 18492
//                    "with sales predict unit_sales by the_date, customer_id", // 20k
//                    "with sales predict unit_sales by the_date, product_id", // 77k
//                    "with sales predict unit_sales by the_date, customer_id, product_id" // 87k
//            ).forEach { s ->
//                println("\n--- $s ---\n")
//                val d = PredictExecute.parse(s)
//                PredictExecute.execute(d, path)
//                if (first) csvPrinter.printRecord(d.statistics.keys.sorted())
//                first = false
//                csvPrinter.printRecord(d.statistics.keys.sorted().map { d.statistics[it] })
//                csvPrinter.flush()
//            }
//        }
//    }
}