package it.unibo.predict

import it.unibo.Intention
import it.unibo.describe.Describe.Companion.clauseToString
import java.io.File

class Predict : Intention {
    constructor(d: Intention?) : super(d, false) {}
    constructor() : super(null, false) {}

    val against: MutableSet<String> = mutableSetOf()
    val using: MutableSet<String> = mutableSetOf()
    var nullify: Int = 0
    var accuracysize: Int = 20

    fun concat(c: Collection<String>, sep: String =", "): String {
        if (c.isEmpty()) {
            return ""
        }
        return c.toList().sorted().reduce { a, b -> "$a$sep$b"}
    }

    override fun toPythonCommand(commandPath: String, path: String): String {
        val sessionStep = getSessionStep()
        val filename = getFilename()
        statistics["execution_id"] = id
        return (commandPath.replace("/", File.separator) //
                + " --path " + (if (path.contains(" ")) "\"" else "") + path.replace("\\", "/") + (if (path.contains(" ")) "\"" else "") //
                + " --file " + filename //
                + " --session_step " + sessionStep //
                + " --measure " + measures.minus(against).first() //
                + " --execution_id " + id
                + " --cube " + json.toString().replace(" ", "__")
                + " --using " + concat(using, sep = ",")
                + " --accuracy_size " + accuracysize
                + " --nullify " + nullify)
    }

    override fun toString(): String {
        return "with $cubeSyn" +
                " predict ${concat(measures)}" +
                " by ${concat(attributes)}" +
                if (clauses.isEmpty()) { "" } else { " for ${concat(clauses.map { clauseToString(it) }, sep = " and ")}" } +
                " against " + if (against.isEmpty()) { measures } else { concat(against) } +
                " using " + if (using.isEmpty()) { concat(listOf("CrossCorrelation", "Multireg", "Polyfit")) } else { concat(using) }
    }
}