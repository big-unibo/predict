package it.unibo.predict

import it.unibo.antlr.gen.PredictBaseVisitor
import it.unibo.antlr.gen.PredictParser
import it.unibo.antlr.gen.PredictParser.PredictContext
import org.antlr.v4.runtime.Token
import org.apache.commons.lang3.tuple.Triple
import org.json.JSONObject
import java.util.stream.Collectors

/** How to interpret the Predict syntax. */
class CustomPredictVisitor(val explain: Predict) : PredictBaseVisitor<JSONObject?>() {

    override fun visitPredict(ctx: PredictContext): JSONObject? {
        explain.setCube(ctx.cube.name)
        explain.addAttribute(true, *ctx.gc.stream().map { it.name }.toArray())
        if (ctx.sc != null) {
            visit(ctx.sc)
        }
        explain.addMeasures(ctx.mc.text.lowercase())
        explain.against += ctx.against.map { it.text.lowercase() }
        explain.using += ctx.using.map { it.name }
        explain.nullify = if (ctx.nullify != null) ctx.nullify.text.toInt() else explain.nullify
        explain.accuracysize = if (ctx.accuracysize != null) ctx.accuracysize.text.toInt() else explain.accuracysize
        if (ctx.executionid != null) explain.setId(ctx.executionid.text)
        return null
    }

    override fun visitCondition(ctx: PredictParser.ConditionContext): JSONObject? {
        val op: Token? = ctx.`in`
        val text: String = if (op != null) op.text else ctx.op.text
        explain.addClause(
            Triple.of(ctx.attr.text, text, ctx.`val`.stream().map { obj: PredictParser.ValueContext -> obj.text }.collect(Collectors.toList()))
        )
        return null
    }
}