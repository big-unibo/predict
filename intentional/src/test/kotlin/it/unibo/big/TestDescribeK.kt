package it.unibo.big

import it.unibo.Intention
import it.unibo.assessext.AssessExecuteExt
import it.unibo.describe.Describe
import it.unibo.describe.DescribeExecute
import it.unibo.describe.DescribeExecute.Vcoord
import it.unibo.describe.DescribeExecute.Vmemb
import krangl.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.math.roundToInt

class TestDescribeK {

    val path = "resources/intention/output/"
    
    @BeforeEach
    fun before() {
        Intention.DEBUG = true
        Vcoord.clear()
        Vmemb.clear()
    }

    @Test
    fun testAdd() {
        assertEquals(42, Integer.sum(19, 23))
    }

    @Test
    fun `test cube content`() {
        try {
            var d = DescribeExecute.parse("with sales_fact_1997 describe unit_sales by the_month for country = 'USA' and store_country = 'USA'")
            d.writeMultidimensionalCube(path)
            var cube = DataFrame.readCSV(path + "debug_0.csv")
            cube = cube.rename(*cube.names.map { it to it.lowercase() }.toTypedArray())
            var c = dataFrameOf("unit_sales", "the_month")(
                    23775, "1997-01",
                    22357, "1997-02",
                    24406, "1997-03",
                    22222, "1997-04",
                    22178, "1997-05",
                    25500, "1997-06",
                    25363, "1997-07",
                    23497, "1997-08",
                    23236, "1997-09",
                    21505, "1997-10",
                    28967, "1997-11",
                    31744, "1997-12")
            assertEquals(c, cube)
            d = DescribeExecute.parse("with sales_fact_1997 describe unit_sales by the_year for country = 'USA' and store_country = 'USA'")
            d.writeMultidimensionalCube(path)
            cube = DataFrame.readCSV(path + "debug_0.csv")
            cube = cube.rename(*cube.names.map { it to it.lowercase() }.toTypedArray())
            c = dataFrameOf("unit_sales", "the_year")(294750, 1997)
            assertEquals(c, cube)
        } catch (e: Exception) {
            e.printStackTrace()
            fail<String>(e.message)
        }
    }

    @Test
    fun `test selection`() {
        try {
            var d: Describe
            var cube: DataFrame
            var c: DataFrame

            d = DescribeExecute.parse("with sales_fact_1997 describe unit_sales by product_subcategory for product_subcategory in ('Bagels', 'Beer', 'Bologna')")
            cube = DescribeExecute.execute(d, path).second
            cube = cube.sortedBy("product_subcategory")
            c = dataFrameOf("unit_sales", "product_subcategory", "zscore_unit_sales")(
                    815,    "Bagels",    -0.779,
                    27183,  "Beer",       1.412,
                    2588,   "Bologna",   -0.632,
            )
            c.names.forEach { assertEquals(c[it], cube[it], it) }

            d = DescribeExecute.parse(d, "with sales_fact_1997 describe unit_sales by product_subcategory for product_subcategory in ('Bagels', 'Beer', 'Wine')", false)
            cube = DescribeExecute.execute(d, path).second
            cube = cube.sortedBy("product_subcategory")
            c = dataFrameOf("unit_sales", "product_subcategory", "zscore_unit_sales")(
                    815,    "Bagels",   -0.108,
                    27183,  "Beer",     -0.015,
                    5155,   "Wine",     -0.511,
            )
            c.names.forEach { assertEquals(c[it], cube[it], it) }
        } catch (e: Exception) {
            e.printStackTrace()
            fail<String>(e.message)
        }
    }

    @Test
    fun `test drill down`() {
        try {
            var d: Describe
            var cube: DataFrame

            d = DescribeExecute.parse("with sales_fact_1997 describe unit_sales by the_year")
            cube = DescribeExecute.execute(d, path).second
            assertEquals(1, cube.nrow)
            // attr, mea, zscore, 3 models (no skyline/clustering), peculiarity, label
            assertEquals(8, cube.ncol, cube.names.toString())

            d = DescribeExecute.parse(d, "with sales_fact_1997 describe unit_sales by the_month", false)
            cube = DescribeExecute.execute(d, path).second
            assertEquals(12, cube.nrow)
            // attr, mea, zscore, 4 models (no skyline), peculiarity, label
            assertEquals(9, cube.ncol, cube.names.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            fail<String>(e.message)
        }
    }

    @Test
    fun `test rollup`() {
        try {
            var d: Describe
            var cube: DataFrame

            d = DescribeExecute.parse("with sales_fact_1997 describe unit_sales by the_month")
            cube = DescribeExecute.execute(d, path).second
            assertEquals(12, cube.nrow)
            // attr, mea, zscore, 4 models (no skyline), peculiarity, label
            assertEquals(9, cube.ncol, cube.names.toString())

            d = DescribeExecute.parse(d, "with sales_fact_1997 describe unit_sales by the_year", false)
            cube = DescribeExecute.execute(d, path).second
            assertEquals(1, cube.nrow)
            // attr, mea, zscore, 3 models (no skyline/clustering), peculiarity, label
            assertEquals(8, cube.ncol, cube.names.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            fail<String>(e.message)
        }
    }

    @Test
    fun `test drill anywhere`() {
        try {
            var d: Describe
            var cube: DataFrame
            var c: DataFrame

            // -- 3 ----------------------------------------------------------------------------------------------------
            d = DescribeExecute.parse(
                    "with sales_fact_1997 describe unit_sales" +
                            " by product_subcategory, city" +
                            " for product_category in ('Bread', 'Beer and Wine') and country = 'USA' and store_country = 'USA' and city in ('Altadena', 'Albany')", false)
            cube = DescribeExecute.execute(d, path).second
            cube = cube.sortedBy("city", "product_subcategory")
            c = dataFrameOf("unit_sales", "city", "product_subcategory", "zscore_unit_sales")(
                    16, "Albany", "Bagels", -0.588,
                    528, "Albany", "Beer", 2.868,
                    124, "Albany", "Muffins", 0.141,
                    99, "Albany", "Sliced Bread", -0.028,
                    130, "Albany", "Wine", 0.182,
                    7, "Altadena", "Bagels", -0.649,
                    12, "Altadena", "Beer", -0.615,
                    33, "Altadena", "Muffins", -0.473,
                    43, "Altadena", "Sliced Bread", -0.406,
                    39, "Altadena", "Wine", -0.433,
            )
            c.names.forEach { assertEquals(c[it], cube[it], it) }

            d = DescribeExecute.parse(d,
                    "with sales_fact_1997 describe unit_sales" +
                            " by product_category, customer_id" +
                            " for product_category in ('Bread', 'Beer and Wine') and country = 'USA' and store_country = 'USA' and customer_id in (7283,8823,5674)", false)
            cube = DescribeExecute.execute(d, path).second
            cube = cube.sortedBy("customer_id", "product_category")
            c = dataFrameOf("unit_sales", "customer_id", "product_category", "zscore_unit_sales", "peculiarity")(
                    3, 5674, "Beer and Wine", -0.466, -0.466,
                    8, 5674, "Bread", 1.64, 1.64,
                    8, 7283, "Beer and Wine", -0.394, -0.394,
                    7, 7283, "Bread", 0.865, 0.865,
                    3, 8823, "Beer and Wine", -0.466, -0.466,
                    3, 8823, "Bread", -0.481, -0.481,
            )
            c.names.forEach { assertEquals(c[it], cube[it], it) }

            // -- 2 ----------------------------------------------------------------------------------------------------
            d = DescribeExecute.parse("with sales_fact_1997 describe unit_sales by the_month", false)
            cube = DescribeExecute.execute(d, path).second
            assertEquals(12, cube.nrow)

            d = DescribeExecute.parse(d, "with sales_fact_1997 describe unit_sales by product_category", false)
            cube = DescribeExecute.execute(d, path).second
            assertEquals(45, cube.nrow)

            // -- 1 ----------------------------------------------------------------------------------------------------
            d = DescribeExecute.parse("with sales_fact_1997 describe unit_sales by the_year, product_subcategory")
            cube = DescribeExecute.execute(d, path).second
            assertEquals(102, cube.nrow)

            d = DescribeExecute.parse(d, "with sales_fact_1997 describe unit_sales by the_month, product_category", false)
            cube = DescribeExecute.execute(d, path).second
            assertEquals(540, cube.nrow)
        } catch (e: Exception) {
            e.printStackTrace()
            fail<String>(e.message)
        }
    }

    @Test
    fun `test paper old interestingness`() {
        try {
            var d: Describe
            var cube: DataFrame
            var c: DataFrame

            d = DescribeExecute.parse("with sales_fact_1997 describe unit_sales by product_subcategory for the_month = '1997-04' and product_category in ('Bread', 'Beer and Wine', 'Fruit', 'Meat')", false)
            cube = DescribeExecute.execute(d, path).second
            cube = cube.sortedBy("product_subcategory")
            c = dataFrameOf("unit_sales", "product_subcategory", "zscore_unit_sales")(
                    48, "Bagels", -0.634,
                    2116, "Beer", 3.244,
                    192, "Bologna", -0.364,
                    138, "Canned Fruit", -0.465,
                    211, "Deli Meats", -0.328,
                    64, "Fresh Chicken", -0.604,
                    798, "Fresh Fruit", 0.773,
                    237, "Frozen Chicken", -0.279,
                    141, "Hamburger", -0.459,
                    154, "Hot Dogs", -0.435,
                    205, "Muffins", -0.339,
                    266, "Sliced Bread", -0.225,
                    448, "Wine", 0.116,
                    // 48,  "Bagels",	          -0.969,
                    // 116, "Beer",	          -0.611,
                    // 192, "Bologna",	      -0.211,
                    // 138, "Canned Fruit",	  -0.495,
                    // 211, "Deli Meats",	      -0.111,
                    // 64,  "Fresh Chicken",    -0.885,
                    // 798, "Fresh Fruit",	   2.977,
                    // 237, "Frozen Chicken",    0.025,
                    // 141, "Hamburger",	      -0.480,
                    // 154, "Hot Dogs",	      -0.411,
                    // 205, "Muffins",	      -0.143,
                    // 266, "Sliced Bread",	   0.178,
                    // 448, "Wine",	           1.136,
            )
            c.names.forEach { assertEquals(c[it], cube[it], it) }

            d = DescribeExecute.parse(d, "with sales_fact_1997 describe unit_sales by gender, product_category for the_month = '1997-04' and product_category in ('Bread', 'Beer and Wine', 'Fruit', 'Meat')", false)
            cube = DescribeExecute.execute(d, path).second
            cube = cube.sortedBy("gender", "product_category")
            cube.print()
            c = dataFrameOf("unit_sales", "gender", "product_category", "peculiarity")(
                    1766, "F", "Beer and Wine", 0.803,
                    278, "F", "Bread", -0.362,
                    440, "F", "Fruit", -0.562,
                    477, "F", "Meat", 0.083,
                    798, "M", "Beer and Wine", -1.308,
                    241, "M", "Bread", -0.443,
                    496, "M", "Fruit", -0.44,
                    522, "M", "Meat", 0.182,
                    // 266, "F", "Beer and Wine", -1.277,
                    // 278, "F", "Bread",	      -0.595,
                    // 440, "F", "Fruit",	      -0.668,
                    // 477, "F", "Meat",	       1.257,
                    // 298, "M", "Beer and Wine", -0.985,
                    // 241, "M", "Bread",	      -0.933,
                    // 496, "M", "Fruit",	      -0.157,
                    // 522, "M", "Meat",	       1.666,
            )
            c.names.forEach { assertEquals(c[it], cube[it], it) }

            d = DescribeExecute.parse(d, "with sales_fact_1997 describe unit_sales by product_category for the_month = '1997-04' and product_category in ('Bread', 'Beer and Wine', 'Fruit', 'Meat')", false)
            cube = DescribeExecute.execute(d, path).second
            // c = dataFrameOf("unit_sales", "product_category", "peculiarity")(
            //         564, "Beer and Wine", -0.018,
            //         519, "Bread",	       -0.022,
            //         936, "Fruit",	        0.017,
            //         999, "Meat",	        0.023,
            // )
            // c.names.forEach { assertEquals(c[it], cube[it], it) }
        } catch (e: Exception) {
            e.printStackTrace()
            fail<String>(e.message)
        }
    }

    fun equals(c: DataFrame, cube: DataFrame) {
        c.names.forEach { it ->
            if (c[it].values()[0].toString().toDoubleOrNull() == null) {
                assertEquals(c[it], cube[it], it)
            } else {
                assertEquals(c[it].map<Double> { 1.0 * (it * 1000).roundToInt() / 1000.0 }, cube[it].map<Double> { 1.0 * (it * 1000).roundToInt() / 1000.0 }, it)
            }
        }
    }

    @Test
    fun `test covid countries`() {
        var d: Describe
        var cube: DataFrame
        var c: DataFrame

        d = DescribeExecute.parse("with COVID-19 describe deaths by month, country for country in ('United States Of America', 'Italy') and month in ('2020-11', '2020-12')", false)
        cube = DescribeExecute.execute(d, path, oldInterest = false).second
        cube = cube.sortedBy("month", "country")
        c = dataFrameOf("deaths", "month", "country", "novelty", "surprise", "zscore_deaths", "peculiarity")(
//                16583,	"2020-11",	"Italy",	                1.000,	1.000,	-0.558, 0.425,
//                37165,	"2020-11",	"United States Of America",	1.000,	1.000,	 1.313, 1.000,
//                 8483,	"2020-12",	"Italy",	                1.000,	1.000,	-1.294, 0.986,
//                28649,	"2020-12",	"United States Of America",	1.000,	1.000,	 0.539, 0.411,
            20249, "2020-11",	"Italy",	                1.000,	1.000,	-0.789, 0.533, //
            44738, "2020-11",	"United States Of America",	1.000,	1.000,	 0.344, 0.233, //
            14940, "2020-12",	"Italy",	                1.000,	1.000,	-1.035, 0.700, //
            69280, "2020-12",	"United States Of America",	1.000,	1.000,	 1.479, 1.000, //
        )
        equals(c, cube)

        d = DescribeExecute.parse(d, "with COVID-19 describe deaths by month, country for country in ('United States Of America', 'Italy', 'Spain') and month in ('2020-11', '2020-12')", false)
        cube = DescribeExecute.execute(d, path, oldInterest = false).second
        cube = cube.sortedBy("month", "country")
        c = dataFrameOf("deaths", "month", "country", "novelty", "surprise", "peculiarity")(
            20249, "2020-11", "Italy",                    0.0, 0.0, 0.512, //
             7957, "2020-11", "Spain",                    1.0, 0.5, 0.866, //
            44738, "2020-11", "United States Of America", 0.0, 0.0, 0.447, //
            14940, "2020-12", "Italy",                    0.0, 0.0, 0.526, //
             5002, "2020-12", "Spain",                    1.0, 0.5, 1.000, //
            69280, "2020-12", "United States Of America", 0.0, 0.0, 0.382, //
        )
        equals(c, cube)
    }

    @Test
    fun `test paper new interestingness`() {
        try {
            var d: Describe
            var cube: DataFrame
            var c: DataFrame

            d = DescribeExecute.parse("with sales_fact_1997 describe unit_sales by product_subcategory for country = 'USA' and store_country = 'USA' and the_month = '1997-04' and product_category in ('Bread', 'Beer and Wine', 'Fruit', 'Meat')", false)
            cube = DescribeExecute.execute(d, path, oldInterest = false).second
            cube = cube.sortedBy("product_subcategory")
            c = dataFrameOf("unit_sales", "product_subcategory", "zscore_unit_sales", "peculiarity", "novelty", "surprise")(
                    48, "Bagels", -0.634, 0.195, 1, 1,
                    2116, "Beer", 3.244, 1.000, 1, 1,
                    192, "Bologna", -0.364, 0.112, 1, 1,
                    138, "Canned Fruit", -0.465, 0.143, 1, 1,
                    211, "Deli Meats", -0.328, 0.101, 1, 1,
                    64, "Fresh Chicken", -0.604, 0.186, 1, 1,
                    798, "Fresh Fruit", 0.773, 0.238, 1, 1,
                    237, "Frozen Chicken", -0.279, 0.086, 1, 1,
                    141, "Hamburger", -0.459, 0.141, 1, 1,
                    154, "Hot Dogs", -0.435, 0.134, 1, 1,
                    205, "Muffins", -0.339, 0.105, 1, 1,
                    266, "Sliced Bread", -0.225, 0.069, 1, 1,
                    448, "Wine", 0.116, 0.036, 1, 1,
            )
            equals(c, cube)

            d = DescribeExecute.parse(d, "with sales_fact_1997 describe unit_sales by gender, product_category for country = 'USA' and store_country = 'USA' and the_month = '1997-04' and product_category in ('Bread', 'Beer and Wine', 'Fruit', 'Meat')", false)
            cube = DescribeExecute.execute(d, path, oldInterest = false).second
            cube = cube.sortedBy("gender", "product_category")
            cube.print()
            c = dataFrameOf("unit_sales", "gender", "product_category", "peculiarity", "novelty", "surprise")(
                    1766, "F", "Beer and Wine", 0.614, 1, 1,
                    278, "F", "Bread", 0.277, 1, 1,
                    440, "F", "Fruit", 0.430, 1, 1,
                    477, "F", "Meat", 0.063, 1, 1,
                    798, "M", "Beer and Wine", 1.000, 1, 1,
                    241, "M", "Bread", 0.339, 1, 1,
                    496, "M", "Fruit", 0.336, 1, 1,
                    522, "M", "Meat", 0.139, 1, 1,
            )
            equals(c, cube)

            d = DescribeExecute.parse(d, "with sales_fact_1997 describe unit_sales by product_category for country = 'USA' and store_country = 'USA' and the_month = '1997-04' and product_category in ('Bread', 'Beer and Wine', 'Fruit', 'Meat')", false)
            cube = DescribeExecute.execute(d, path, oldInterest = false).second
            cube = cube.sortedBy("product_category")
            c = dataFrameOf("unit_sales", "product_category", "peculiarity", "novelty", "surprise")(
                    2564, "Beer and Wine", 1.000, 1, 0.5,
                    519, "Bread", 0.280, 1, 0.5,
                    936, "Fruit", 0.048, 1, 0.5,
                    999, "Meat", 0.238, 1, 0.5,
            )
            equals(c, cube)
        } catch (e: Exception) {
            e.printStackTrace()
            fail<String>(e.message)
        }
    }

    @Test
    fun `test paper selection`() {
        try {
            var d: Describe
            var cube: DataFrame
            var c: DataFrame

            d = DescribeExecute.parse("with sales_fact_1997 describe unit_sales by product_name for product_subcategory in ('Beer') and product_name in ('Good Chablis Wine', 'Good Chardonnay', 'Good Chardonnay Wine', 'Good Imported Beer', 'Good Light Beer', 'Pearl Imported Beer')", false)
            cube = DescribeExecute.execute(d, path).second
            c = dataFrameOf("unit_sales", "product_name", "zscore_unit_sales")(
                    25654, "Good Imported Beer", 1.414,
                    115, "Good Light Beer", -0.71,
                    175, "Pearl Imported Beer", -0.705,
                    // 154, "Good Imported Beer", 0.241,
                    // 115, "Good Light Beer", -1.327,
                    // 175, "Pearl Imported Beer", 1.086,
            )
            c.names.forEach { assertEquals(c[it], cube[it], it) }

            d = DescribeExecute.parse(d, "with sales_fact_1997 describe unit_sales by product_name for product_category in ('Beer and Wine') and product_name in ('Good Chablis Wine', 'Good Chardonnay', 'Good Chardonnay Wine', 'Good Imported Beer', 'Good Light Beer', 'Pearl Imported Beer')", false)
            cube = DescribeExecute.execute(d, path).second
            cube = cube.sortedBy("product_name")
            c = dataFrameOf("unit_sales", "product_name", "peculiarity")(
                    163, "Good Chablis Wine", -0.447,
                    192, "Good Chardonnay", -0.444,
                    146, "Good Chardonnay Wine", -0.448,
                    25654, "Good Imported Beer", 0.822,
                    115, "Good Light Beer", 0.258,
                    175, "Pearl Imported Beer", 0.26,
                    // 163, "Good Chablis Wine", 0.228,
                    // 192, "Good Chardonnay", 1.433,
                    // 146, "Good Chardonnay Wine", -0.478,
                    // 154, "Good Imported Beer", -0.386,
                    // 115, "Good Light Beer", -0.438,
                    // 175, "Pearl Imported Beer", -0.359,
            )
            c.names.forEach { assertEquals(c[it], cube[it], it) }
        } catch (e: Exception) {
            e.printStackTrace()
            fail<String>(e.message)
        }
    }

    @Test
    fun `test result`() {
        try {
            var d: Describe
            var cube: DataFrame

            d = DescribeExecute.parse("with sales_fact_1997 describe unit_sales, store_sales by the_month for country = 'USA' and store_country = 'USA' using top-k size 3")
            cube = DescribeExecute.execute(d, path).second
            assertEquals(12, cube.nrow)
            // attr, 2 * mea, 2 * zscore, 1 model * 2 mea, peculiarity, label
            assertEquals(9, cube.ncol, cube.names.toString())
            cube.select("the_month", "peculiarity", "model_top_unit_sales", "model_top_store_sales").print(maxRows = 12)
            var c = dataFrameOf("store_sales", "unit_sales", "the_month", "zscore_store_sales", "zscore_unit_sales", "peculiarity", "model_top_unit_sales", "model_top_store_sales")(
                    47687.41, 23775, "1997-01", -0.34,  -0.271, -0.271, false, false,//
                    45458.79, 22357, "1997-02", -0.774, -0.759, -0.759, false, false,//
                    50729.87, 24406, "1997-03",  0.253, -0.054,  0.253, false, false,//
                    44905.23, 22222, "1997-04", -0.882, -0.805, -0.805, false, false,//
                    45551.19, 22178, "1997-05", -0.756, -0.82,  -0.756, false, false,//
                    49481.73, 25500, "1997-06",  0.01,   0.322,  0.322, true,  false,//
                    51846.88, 25363, "1997-07",  0.47,   0.275,  0.47,  false, true,//
                    47999.04, 23497, "1997-08", -0.279, -0.367, -0.279, false, false,//
                    46670.33, 23236, "1997-09", -0.538, -0.456, -0.456, false, false,//
                    43885.76, 21505, "1997-10", -1.08,  -1.052, -1.052, false, false,//
                    57057.14, 28967, "1997-11",  1.486,  1.515,  1.515, true,  true,//
                    61909.12, 31744, "1997-12",  2.431,  2.47,   2.47,  true,  true)
            c.names.forEach { assertEquals(c[it], cube[it], it) }

            d = DescribeExecute.parse(d, "with sales_fact_1997 describe unit_sales, store_sales by the_year for country = 'USA' and store_country = 'USA' using top-k size 3", false)
            cube = DescribeExecute.execute(d, path).second
            assertEquals(1, cube.nrow)
            // "zscore_store_sales", "zscore_unit_sales", "peculiarity",     -0, 0, 0,
            c = dataFrameOf("store_sales", "unit_sales", "the_year", "model_top_unit_sales", "model_top_store_sales")(593182.49, 294750, "1997", true, true)
            c.names.forEach { assertEquals(c[it], cube[it], it) }
        } catch (e: Exception) {
            e.printStackTrace()
            fail<String>(e.message)
        }
    }

    @Test
    fun `test covid`() {
        try {
            var d: Describe?
            var c: DataFrame?

            d = DescribeExecute.parse("with COVID-19 describe deaths by country, month")
            c = DescribeExecute.execute(d, path).second

            d = DescribeExecute.parse("with COVID-19 describe deaths by month for country = 'United States Of America'")
            c = DescribeExecute.execute(d, path).second
            assertFalse(c.names.contains("model_skyline"), c.names.toString())
            d = DescribeExecute.parse(d, "with COVID-19 describe deaths, cases by month, country for country = 'United States Of America'", false)
            val o = DescribeExecute.execute(d, path)
            c = o.second
            assertTrue(c.names.contains("model_skyline"), c.names.toString())

            d = DescribeExecute.parse("with covid describe cases by month", false)
            DescribeExecute.execute(d, path)
            d = DescribeExecute.parse("with covid describe cases by country", false)
            DescribeExecute.execute(d, path)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            fail<Any>(e.message)
        }
    }

    @Test
    fun `test interestingness`() {
        try {
            var d: Describe?
            var c: DataFrame?

            d = DescribeExecute.parse("with sales describe unit_sales by product_subcategory for product_category = 'Bread'", false)
            c = DescribeExecute.execute(d, path, oldInterest = false).second
            assertEquals(3, Vcoord.size)
            assertEquals(3, Vmemb.size)
            assertEquals(mutableMapOf(
                    "Sliced Bread" to 1.0,
                    "Muffins" to 1.0,
                    "Bagels" to 1.0,
            ), Vmemb)

            d = DescribeExecute.parse(d, "with sales describe unit_sales by product_subcategory, the_year for product_category = 'Bread'", false)
            c = DescribeExecute.execute(d, path, oldInterest = false).second
            assertEquals(6, Vcoord.size)
            assertEquals(4, Vmemb.size)
            assertEquals(mutableMapOf(
                    "Sliced Bread" to 2.0,
                    "Muffins" to 2.0,
                    "Bagels" to 2.0,
                    "1997" to 1.0,
            ), Vmemb)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            fail<Any>(e.message)
        }
    }

    @Test
    fun drill() {
        try {
            var d: Describe = DescribeExecute.parse("with sales_fact_1997 describe unit_sales, store_sales by product_category")
            DescribeExecute.execute(d, path)
            d = DescribeExecute.parse(d, "with sales_fact_1997 describe unit_sales, store_sales by product_subcategory")
            DescribeExecute.execute(d, path)
            d = DescribeExecute.parse(d, "with sales_fact_1997 describe unit_sales, store_sales by store_country")
            DescribeExecute.execute(d, path)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            fail<Any>(e.message)
        }
    }

    @Test
    fun rollup() {
        try {
            var d: Describe = DescribeExecute.parse("with sales_fact_1997 describe unit_sales, store_sales by product_subcategory")
            DescribeExecute.execute(d, path)
            d = DescribeExecute.parse(d, "with sales_fact_1997 describe unit_sales, store_sales by product_category")
            DescribeExecute.execute(d, path)
            d = DescribeExecute.parse(d, "with sales_fact_1997 describe unit_sales, store_sales by store_country")
            DescribeExecute.execute(d, path)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            fail<Any>(e.message)
        }
    }

    @Test
    fun test01() {
        try {
            var d: Describe = DescribeExecute.parse("with sales_fact_1997 describe unit_sales, store_sales by product_category")
            DescribeExecute.execute(d, path)
            d = DescribeExecute.parse(d, "with sales_fact_1997 describe unit_sales, store_sales for product_category = 'Bread' by product_category")
            DescribeExecute.execute(d, path)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            fail<Any>(e.message)
        }
    }

    @Test
    fun test02() {
        try {
            var d: Describe = DescribeExecute.parse("with sales_fact_1997 describe unit_sales, store_sales by product_category")
            DescribeExecute.execute(d, path)
            d = DescribeExecute.parse(d, "with sales_fact_1997 describe unit_sales, store_sales by product_family size 3")
            DescribeExecute.execute(d, path)
            d = DescribeExecute.parse(d, "with sales_fact_1997 describe unit_sales, store_sales by product_family size 4")
            DescribeExecute.execute(d, path)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            fail<Any>(e.message)
        }
    }

    @Test
    fun test03() {
        try {
            var d: Describe = DescribeExecute.parse("with sales_fact_1997 describe unit_sales, store_sales by the_month")
            DescribeExecute.execute(d, path)
            d = DescribeExecute.parse(d, "with sales_fact_1997 describe unit_sales, store_sales by the_date")
            DescribeExecute.execute(d, path)
            d = DescribeExecute.parse(d, "with sales_fact_1997 describe unit_sales, store_sales by the_year")
            DescribeExecute.execute(d, path)
            d = DescribeExecute.parse(d, "with sales_fact_1997 describe unit_sales, store_sales by the_date")
            DescribeExecute.execute(d, path)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            fail<Any>(e.message)
        }
    }

//  @Test
//  public void test04() {
//    try {
//      Describe d = DescribeExecute.parse("with sales_fact_1997 describe unit_sales, store_sales for quarter in ('Q1', 'Q2') by the_month");
//      DescribeExecute.execute(d, path);
//      d = DescribeExecute.parse("with sales_fact_1997 describe unit_sales, store_sales for the_date in ('1997-01-01', '1997-01-02', '1997-02-01') by the_date");
//      DescribeExecute.execute(d, path);
//      d = DescribeExecute.parse(d,
//          "with sales_fact_1997 describe unit_sales, store_sales for the_date in ('1997-01-01', '1997-01-02', '1997-02-01') by gender");
//      DescribeExecute.execute(d, path);
//    } catch (final Exception e) {
//      e.printStackTrace();
//      fail(e.getMessage());
//    }
//  }

//  @Test
//  public void test05() {
//    try {
//      Describe d = DescribeExecute.parse(
//          "with sales_fact_1997 describe unit_sales for product_category in ('Fruit', 'Pizza') and gender in ('M', 'F') by product_subcategory size 2");
//      DescribeExecute.execute(d, path);
//      d = DescribeExecute.parse(d,
//          "with sales_fact_1997 describe unit_sales for product_category in ('Fruit', 'Pizza') and gender in ('M', 'F') by product_category size 2");
//      DescribeExecute.execute(d, path);
//      d = DescribeExecute.parse(d,
//          "with sales_fact_1997 describe unit_sales for product_category in ('Fruit', 'Pizza') and gender in ('M', 'F') by gender size 2");
//      DescribeExecute.execute(d, path);
//    } catch (final Exception e) {
//      e.printStackTrace();
//      fail(e.getMessage());
//    }
//  }

//  @Test
//  public void test06() {
//    try {
//      Describe d = DescribeExecute.parse("with sales_fact_1997 describe unit_sales for product_category in ('Fruit', 'Pizza') and gender in ('M', 'F') by product_category size 2");
//      DescribeExecute.execute(d, path);
//      d = DescribeExecute.parse(d, "with sales_fact_1997 describe unit_sales for product_category in ('Fruit', 'Pizza') and gender in ('M', 'F') by product_subcategory size 2");
//      DescribeExecute.execute(d, path);
//      d = DescribeExecute.parse(d, "with sales_fact_1997 describe unit_sales for product_category in ('Fruit', 'Pizza') and gender in ('M', 'F') by gender size 2");
//      DescribeExecute.execute(d, path);
//    } catch (final Exception e) {
//      e.printStackTrace();
//      fail(e.getMessage());
//    }
//  }

    // @Test
    // public void test07() {
    //   try {
    //     Describe d = DescribeExecute.parse("with sales_fact_1997 describe unit_sales by product_subcategory size 2");
    //     DescribeExecute.execute(d, path);
    //     assertEquals(0, d.getClauses().size());
    //     d = DescribeExecute.parse(d, "with sales_fact_1997 describe unit_sales by gender size 2");
    //     DescribeExecute.execute(d, path);
    //     assertEquals(0, d.getClauses().size());
    //     d = DescribeExecute.parse(d, "with sales_fact_1997 describe unit_sales for product_category in ('Pizza', 'Fruit') by gender size 2");
    //     assertEquals(1, d.getClauses().size());
    //     DescribeExecute.execute(d, path);
    //     d = DescribeExecute.parse(d,
    //         "with sales_fact_1997 describe unit_sales for product_subcategory in ('Pizza', 'Canned Fruit', 'Canned-Fruit') by gender size 2");
    //     DescribeExecute.execute(d, path);
    //     assertEquals(1, d.getClauses().size());
    //     d = DescribeExecute.parse(d, "with sales_fact_1997 describe unit_sales for gender in ('M') by gender size 2");
    //     DescribeExecute.execute(d, path);
    //     assertEquals(2, d.getClauses().size());
    //   } catch (final Exception e) {
    //     e.printStackTrace();
    //     fail(e.getMessage());
    //   }
    // }

//  @Test
//  public void test08() {
//    try {
//      Describe d = DescribeExecute.parse("with sales_fact_1997 describe unit_sales for the_month='1997-04' and product_category in ('Beer and Wine','Fruit','Meat','Bread') by product_subcategory");
//      DescribeExecute.execute(d, path);
//      d = DescribeExecute.parse(d, "with sales_fact_1997 describe unit_sales for the_month='1997-04' and product_category in ('Beer and Wine','Fruit','Meat','Bread') by product_category");
//      DescribeExecute.execute(d, path);
//    } catch (final Exception e) {
//      e.printStackTrace();
//      fail(e.getMessage());
//    }
//  }

    //  @Test
    //  public void test04() {
    //    try {
    //      Describe d = DescribeExecute.parse("with sales_fact_1997 describe unit_sales, store_sales for quarter in ('Q1', 'Q2') by the_month");
    //      DescribeExecute.execute(d, path);
    //      d = DescribeExecute.parse("with sales_fact_1997 describe unit_sales, store_sales for the_date in ('1997-01-01', '1997-01-02', '1997-02-01') by the_date");
    //      DescribeExecute.execute(d, path);
    //      d = DescribeExecute.parse(d,
    //          "with sales_fact_1997 describe unit_sales, store_sales for the_date in ('1997-01-01', '1997-01-02', '1997-02-01') by gender");
    //      DescribeExecute.execute(d, path);
    //    } catch (final Exception e) {
    //      e.printStackTrace();
    //      fail(e.getMessage());
    //    }
    //  }
    //  @Test
    //  public void test05() {
    //    try {
    //      Describe d = DescribeExecute.parse(
    //          "with sales_fact_1997 describe unit_sales for product_category in ('Fruit', 'Pizza') and gender in ('M', 'F') by product_subcategory size 2");
    //      DescribeExecute.execute(d, path);
    //      d = DescribeExecute.parse(d,
    //          "with sales_fact_1997 describe unit_sales for product_category in ('Fruit', 'Pizza') and gender in ('M', 'F') by product_category size 2");
    //      DescribeExecute.execute(d, path);
    //      d = DescribeExecute.parse(d,
    //          "with sales_fact_1997 describe unit_sales for product_category in ('Fruit', 'Pizza') and gender in ('M', 'F') by gender size 2");
    //      DescribeExecute.execute(d, path);
    //    } catch (final Exception e) {
    //      e.printStackTrace();
    //      fail(e.getMessage());
    //    }
    //  }
    //  @Test
    //  public void test06() {
    //    try {
    //      Describe d = DescribeExecute.parse("with sales_fact_1997 describe unit_sales for product_category in ('Fruit', 'Pizza') and gender in ('M', 'F') by product_category size 2");
    //      DescribeExecute.execute(d, path);
    //      d = DescribeExecute.parse(d, "with sales_fact_1997 describe unit_sales for product_category in ('Fruit', 'Pizza') and gender in ('M', 'F') by product_subcategory size 2");
    //      DescribeExecute.execute(d, path);
    //      d = DescribeExecute.parse(d, "with sales_fact_1997 describe unit_sales for product_category in ('Fruit', 'Pizza') and gender in ('M', 'F') by gender size 2");
    //      DescribeExecute.execute(d, path);
    //    } catch (final Exception e) {
    //      e.printStackTrace();
    //      fail(e.getMessage());
    //    }
    //  }
    // @Test
    // public void test07() {
    //   try {
    //     Describe d = DescribeExecute.parse("with sales_fact_1997 describe unit_sales by product_subcategory size 2");
    //     DescribeExecute.execute(d, path);
    //     assertEquals(0, d.getClauses().size());
    //     d = DescribeExecute.parse(d, "with sales_fact_1997 describe unit_sales by gender size 2");
    //     DescribeExecute.execute(d, path);
    //     assertEquals(0, d.getClauses().size());
    //     d = DescribeExecute.parse(d, "with sales_fact_1997 describe unit_sales for product_category in ('Pizza', 'Fruit') by gender size 2");
    //     assertEquals(1, d.getClauses().size());
    //     DescribeExecute.execute(d, path);
    //     d = DescribeExecute.parse(d,
    //         "with sales_fact_1997 describe unit_sales for product_subcategory in ('Pizza', 'Canned Fruit', 'Canned-Fruit') by gender size 2");
    //     DescribeExecute.execute(d, path);
    //     assertEquals(1, d.getClauses().size());
    //     d = DescribeExecute.parse(d, "with sales_fact_1997 describe unit_sales for gender in ('M') by gender size 2");
    //     DescribeExecute.execute(d, path);
    //     assertEquals(2, d.getClauses().size());
    //   } catch (final Exception e) {
    //     e.printStackTrace();
    //     fail(e.getMessage());
    //   }
    // }
    //  @Test
    //  public void test08() {
    //    try {
    //      Describe d = DescribeExecute.parse("with sales_fact_1997 describe unit_sales for the_month='1997-04' and product_category in ('Beer and Wine','Fruit','Meat','Bread') by product_subcategory");
    //      DescribeExecute.execute(d, path);
    //      d = DescribeExecute.parse(d, "with sales_fact_1997 describe unit_sales for the_month='1997-04' and product_category in ('Beer and Wine','Fruit','Meat','Bread') by product_category");
    //      DescribeExecute.execute(d, path);
    //    } catch (final Exception e) {
    //      e.printStackTrace();
    //      fail(e.getMessage());
    //    }
    //  }
    @Test
    fun test09() {
        try {
//      Describe d;
//
//      d = DescribeExecute.parse("with COVID-19 describe deaths100k by country");
//      DescribeExecute.execute(d, path);
//
//      d = DescribeExecute.parse("with COVID-19 describe dEaThs for CoNtiNenTExp='Africa' by DaTeReP");
//      DescribeExecute.execute(d, path);
//      d = DescribeExecute.parse(d, "with COVID-19 describe dEaThs for CoNtiNenTExp='Africa' by MoNtH");
//      DescribeExecute.execute(d, path);
//
//      d = DescribeExecute.parse("with COVID-19 describe cases, deaths for daterep >= '04/01/2020' by month");
//      DescribeExecute.execute(d, path);
//
//
//      d = DescribeExecute.parse("with COVID-19 describe dEaThs for CoNtiNenTExp='Africa' by MoNtH");
//      DescribeExecute.execute(d, path);
//      d = DescribeExecute.parse(d, "with COVID-19 describe dEaThs for CoNtiNenTExp='Africa' by DaTeReP");
//      DescribeExecute.execute(d, path);
//
//      d = DescribeExecute.parse("with COVID-19 describe deaths for country = 'France' by month");
//      DescribeExecute.execute(d, path);
//      d = DescribeExecute.parse("with COVID-19 describe deaths for country = 'France' by country");
//      DescribeExecute.execute(d, path);
//
//      d = DescribeExecute.parse("with COVID-19 describe deaths, cases by month");
//      DescribeExecute.execute(d, path);
//      d = DescribeExecute.parse(d, "with COVID-19 describe deaths, cases by geoId");
//      DescribeExecute.execute(d, path);
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            fail<Any>(e.message)
        }
    }

    @Test
    fun testGrammar() {
        DescribeExecute.parse("with sales describe ma")
        DescribeExecute.parse("with sales describe ma, mb")
        DescribeExecute.parse("with sales describe ma, mb for c = -1")
        DescribeExecute.parse("with sales describe ma, mb for c = a")
        DescribeExecute.parse("with sales describe ma, mb for c >= 01/02/2001")
        DescribeExecute.parse("with sales describe ma, mb for c >= '01/02/2001'")
        DescribeExecute.parse("with sales describe ma, mb for c >= 01-01-2001")
        DescribeExecute.parse("with sales describe ma, mb for c >= '01-02-2001'")
        DescribeExecute.parse("with sales describe ma, mb for c >= -1.0")
        DescribeExecute.parse("with sales describe ma, mb for c >= a")
        DescribeExecute.parse("with sales describe ma, mb for c = TRUE")
        DescribeExecute.parse("with sales describe ma, mb for c = 1 by a")
        DescribeExecute.parse("with sales describe ma, mb for c = 1 by a using clustering")
        DescribeExecute.parse("with sales describe ma, mb for c = 1 by a using clustering, top-k")
        DescribeExecute.parse("with sales_fact_1997 describe unit_sales for customer_id = 10 and store_id = 11 and the_date = '1997-01-20' by customer_id")
    }

    @Test
    fun integration() {
        var i: Intention = DescribeExecute.parse("with COVID-19 describe deaths by month")
        DescribeExecute.execute(i, path, oldInterest = false)
        i = DescribeExecute.parse(i, "with COVID-19 describe deaths by year")
        DescribeExecute.execute(i, path, oldInterest = false)
        i = AssessExecuteExt.parse("with COVID-19 assess deaths by year", k=1)
        AssessExecuteExt.execute(i, path)
        i = DescribeExecute.parse(i, "with COVID-19 describe deaths by month")
        val c: DataFrame  = DescribeExecute.execute(i, path, oldInterest = false).second
        assertTrue(c["peculiarity"].sum() as Double > 0.0)
        // assertTrue(c["novelty"].sum() as Double > 0.0)
        // assertTrue(c["surprise"].sum() as Double > 0.0)
    }
}