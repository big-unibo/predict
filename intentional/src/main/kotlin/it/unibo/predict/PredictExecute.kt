package it.unibo.predict

import it.unibo.antlr.gen.PredictLexer
import it.unibo.antlr.gen.PredictParser
import it.unibo.antlr.gen.ThrowingErrorListener
import it.unibo.conversational.Utils.measureName
import it.unibo.conversational.database.QueryGenerator
import krangl.DataFrame
import krangl.readCSV
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Predict intention in action.
 */
object PredictExecute {
    private val L = LoggerFactory.getLogger(PredictExecute::class.java)

    fun parse(input: String): Predict {
        val lexer = PredictLexer(ANTLRInputStream(input)) // new ANTLRInputStream(System.in);
        val tokens = CommonTokenStream(lexer) // create a buffer of tokens pulled from the lexer
        val parser = PredictParser(tokens) // create a parser that feeds off the tokens buffer
        parser.addErrorListener(ThrowingErrorListener())
        val tree: ParseTree = parser.predict() // begin parsing at init rule
        val v = CustomPredictVisitor(Predict())
        v.visit(tree)
        return v.explain
    }

    @JvmOverloads
    @Throws(Exception::class)
    fun execute(d: Predict, path: String, pythonPath: String = "src/main/python/", id: String = UUID.randomUUID().toString()): Triple<JSONObject, DataFrame, DataFrame> {
        // get the other measures to explain
        var othermeasures = if (d.against.isEmpty()) { QueryGenerator.getMeasures(d.cube) } else { d.against }
        // omit the measure to be explained
        othermeasures = othermeasures.filter { !measureName(it).equals(measureName(d.measures.first())) && !measureName(it).contains("json") }.toSet()
        d.addMeasures(*othermeasures.toTypedArray())
        // compute the query and store the result
        val timeQuery = d.writeMultidimensionalCube(path, true)
        d.setMeasures(d.measures.map { measureName(it) })
        d.against.clear()
        othermeasures = othermeasures.map { measureName(it) }.toSet()
        d.against += othermeasures.map { measureName(it) } // d.against
        L.warn("Computing models...")
        val timeModel = d.computePython(pythonPath, path, "predict.py")
        L.warn("Models computed")
        d.measures.removeIf { othermeasures.contains(it) }
        d.statistics["intention"] = d.toString()
        d.statistics["intention_characters"] = d.toString().length
        val s = "$path${d.filename}_${d.sessionStep}"
        val cube: DataFrame = DataFrame.readCSV("$s.csv") // .sortedBy(*coordinate.toTypedArray())
        val prop: DataFrame = DataFrame.readCSV("${s}_property.csv") // .sortedBy(*coordinate.toTypedArray())
        L.warn("Read $s")
        d.statistics["time_query"] = timeQuery
        d.statistics["time_model"] = timeModel
        d.statistics["cardinality"] = cube.nrow
        return Triple(JSONObject(), cube, prop)
    }
}