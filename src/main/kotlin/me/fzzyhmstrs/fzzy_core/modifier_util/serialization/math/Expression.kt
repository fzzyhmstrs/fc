package me.fzzyhmstrs.fzzy_core.modifier_util.serialization.math

import com.mojang.brigadier.StringReader
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.random.Random
import java.lang.StringBuilder
import kotlin.math.pow
import kotlin.reflect.typeOf

@FunctionalInterface
fun interface Expression {
    fun eval(vars: Map<Char,Double>): Double

    @Suppress("SameParameterValue")
    companion object{

        fun parse(str: String, context: String): Expression{
            try {
                val reader = StringReader(str)
                return parseExpression(reader, context,1000)
            } catch (e: Exception){
                throw IllegalStateException("Error parsing math equation [$context]: ${e.localizedMessage}")
            }
        }

        private val expressions: Map<String, NamedExpression> = mapOf(
            "sqrt" to NamedExpression { reader, context, _ -> val parentheses = parseParentheses(reader, context, false)
                val sqrt = sqrt(parentheses)
                if (reader.canRead())
                    parseExpression(reader, context,1000, sqrt)
                else
                    sqrt },
            "ceil" to NamedExpression { reader, context, _ ->
                val parentheses = parseParentheses(reader, context, false)
                val ceil = ceil(parentheses)
                if (reader.canRead())
                    parseExpression(reader, context, 1000, ceil)
                else
                    ceil },
            "floor" to NamedExpression { reader, context, _ ->
                val parentheses = parseParentheses(reader, context, false)
                val floor = floor(parentheses)
                if (reader.canRead())
                    parseExpression(reader, context, 1000, floor)
                else
                    floor },
            "round" to NamedExpression { reader, context, _ ->
                val parentheses = parseParentheses(reader, context, false)
                val round = round(parentheses)
                if (reader.canRead())
                    parseExpression(reader, context, 1000, round)
                else
                    round },
            "ln" to NamedExpression { reader, context, _ ->
            val parentheses = parseParentheses(reader, context, false)
            val ln = ln(parentheses)
            if (reader.canRead())
                parseExpression(reader, context, 1000, ln)
            else
                ln },
        "log" to NamedExpression { reader, context, _ ->
            val params = parseParenthesesMultiple(reader, context, false)
            if (params.size != 2) throw IllegalStateException("Improper number of log arguments in equation [$context]")
            val log = log(params[0], params[1])
            if (reader.canRead())
                parseExpression(reader, context, 1000, log)
            else
                log },
        "log10" to NamedExpression { reader, context, _ ->
            val param = parseParentheses(reader, context, false)
            val log = log10(param)
            if (reader.canRead())
                parseExpression(reader, context, 1000, log)
            else
                log },
        "log2" to NamedExpression { reader, context, _ ->
            val param = parseParentheses(reader, context, false)
            val log = log2(param)
            if (reader.canRead())
                parseExpression(reader, context, 1000, log)
            else
                log },
        "abs" to NamedExpression { reader, context, _ ->
            val parentheses = parseParentheses(reader, context, false)
            val abs = abs(parentheses)
            if (reader.canRead())
                parseExpression(reader, context, 1000, abs)
            else
                abs },
        "sin" to NamedExpression { reader, context, _ ->
            val parentheses = parseParentheses(reader, context, false)
            val sin = sin(parentheses)
            if (reader.canRead())
                parseExpression(reader, context, 1000, sin)
            else
                sin },
        "cos" to NamedExpression { reader, context, _ ->
            val parentheses = parseParentheses(reader, context, false)
            val cos = cos(parentheses)
            if (reader.canRead())
                parseExpression(reader, context, 1000, cos)
            else
                cos },
        "incr" to NamedExpression { reader, context, _ ->
            val parentheses = parseParenthesesMultiple(reader, context, false)
            val incr = incr(parentheses[0],parentheses[1])
            if (reader.canRead())
                parseExpression(reader, context, 1000, incr)
            else
                incr }
        )

        private fun parseExpression(reader: StringReader, context: String,order: Int, vararg inputs: Expression): Expression{
            if (reader.string.isEmpty()) throw IllegalStateException("Empty Expression found in math equation [$context]")
            reader.skipWhitespace()
            if (StringReader.isAllowedNumber(reader.peek())){
                val number1 = reader.readDouble()
                return if (reader.canRead())
                    parseExpression(reader,context,order, constant(number1))
                else
                    constant(number1)
            } else if (reader.peek() == '(') {
                val parentheses = parseParentheses(reader, context)
                return if(reader.canRead())
                    parseExpression(reader,context,1000, parentheses)
                else
                    parentheses
            }else if (reader.peek() == '^') {
                if (1 > order) return inputs[0]
                reader.read()
                val expression1 = inputs[0]
                val expression2 = parseExpression(reader, context,1)
                return if(reader.canRead())
                    parseExpression(reader,context,1000, pow(expression1,expression2))
                else
                    pow(expression1,expression2)
            }else if (reader.peek() == '*') {
                if (2 > order) return inputs[0]
                reader.read()
                val expression1 = inputs[0]
                val expression2 = parseExpression(reader, context,2)
                return times(expression1,expression2)
            } else if (reader.peek() == '/') {
                if (2 > order) return inputs[0]
                reader.read()
                val expression1 = inputs[0]
                val expression2 = parseExpression(reader, context, 2)
                return divide(expression1,expression2)
            } else if (reader.peek() == '%') {
                if (2 > order) return inputs[0]
                reader.read()
                val expression1 = inputs[0]
                val expression2 = parseExpression(reader, context, 3)
                return mod(expression1,expression2)
            } else if (reader.peek() == '+') {
                if (3 > order) return inputs[0]
                reader.read()
                val expression1 = inputs[0]
                val expression2 = parseExpression(reader, context, 3)
                return plus(expression1,expression2)
            } else if (reader.peek() == '-') {
                if (3 > order) return inputs[0]
                reader.read()
                val expression1 = inputs[0]
                val expression2 = parseExpression(reader, context, 3)
                return minus(expression1,expression2)
            }  else if (reader.peek().isLetter() && !reader.canRead(2)){
                return variable(reader.peek())
            } else if (reader.peek().isLetter() && reader.canRead(2)){
                return if (!reader.peek(1).isLetter()) {
                    val variable = variable(reader.peek())
                    reader.read()
                    parseExpression(reader, context, order, variable)
                } else {
                    val chunk = reader.readStringUntil('(').trimEnd()
                    expressions[chunk]?.get(reader, context, chunk)
                        ?: mathHelper(reader, context, chunk)
                        ?: throw IllegalStateException("Unknown expression '$chunk' in equation [$context]")
                }
            }
            throw IllegalStateException("Unknown expression '${reader.remaining}' in equation [$context]")
        }

        private fun parseParentheses(reader: StringReader, context: String, read: Boolean = true): Expression {
            val builder = StringBuilder()
            if(read) reader.read()
            var count = 1
            while (count != 0 && reader.canRead()) {
                val c = reader.read()
                if (c == '(') count++
                else if (c == ')') count--
                if (count != 0) builder.append(c)
            }
            if (count != 0) throw IllegalStateException("Unclosed parentheses found in equation [$context] from string $builder")
            return parentheses(parseExpression(StringReader(builder.toString()), context, 1000))
        }

        private fun parseParenthesesMultiple(reader: StringReader, context: String, read: Boolean = true): List<Expression> {
            val builder = StringBuilder()
            if(read) reader.read()
            var count = 1
            while (count != 0 && reader.canRead()) {
                val c = reader.read()
                if (c == '(') count++
                else if (c == ')') count--
                if (count != 0) builder.append(c)
            }
            if (count != 0) throw IllegalStateException("Unclosed parentheses found in equation [$context] from string $builder")
            val str: MutableList<String> = mutableListOf()
            var splitIndex = 0
            var count2 = 0
            val toEat = builder.toString()
            for ((i,c) in toEat.withIndex()){
                when (c) {
                    '(' -> count2++
                    ')' -> count2--
                    ',' -> {
                        if (count2 == 0) {
                            str.add(toEat.substring(splitIndex, i))
                            splitIndex = i + 1
                        }
                    }
                }
            }
            str.add(toEat.substring(splitIndex))

            return str.map { parseExpression(StringReader(it), context, 1000) }
        }


        private fun constant(constant: Double): Expression{
            return object : Expression{
                override fun eval(vars: Map<Char, Double>): Double {
                    return constant
                }
                override fun toString(): String {
                    return "$constant"
                }
            }
        }
        private fun parentheses(e1: Expression): Expression{
            return object : Expression{
                override fun eval(vars: Map<Char, Double>): Double {
                    return e1.eval(vars)
                }
                override fun toString(): String {
                    return "($e1)"
                }
            }
        }
        private fun variable(variable: Char):Expression {
            return object : Expression{
                override fun eval(vars: Map<Char, Double>): Double {
                    return vars[variable] ?: throw IllegalStateException("Expected variable '$variable', didn't find")
                }
                override fun toString(): String {
                    return variable.toString()
                }
            }
        }
        private fun plus(e1: Expression, e2: Expression): Expression{
            return object : Expression{
                override fun eval(vars: Map<Char, Double>): Double {
                    return e1.eval(vars) + e2.eval(vars)
                }
                override fun toString(): String {
                    return "$e1 + $e2"
                }
            }
        }
        private fun minus(e1: Expression, e2: Expression): Expression{
            return object : Expression{
                override fun eval(vars: Map<Char, Double>): Double {
                    return e1.eval(vars) - e2.eval(vars)
                }
                override fun toString(): String {
                    return "$e1 - $e2"
                }
            }
        }
        private fun times(e1: Expression, e2: Expression): Expression{
            return object : Expression{
                override fun eval(vars: Map<Char, Double>): Double {
                    return e1.eval(vars) * e2.eval(vars)
                }
                override fun toString(): String {
                    return "$e1 * $e2"
                }
            }
        }
        private fun divide(e1: Expression, e2: Expression): Expression{
            return object : Expression{
                override fun eval(vars: Map<Char, Double>): Double {
                    return e1.eval(vars) / e2.eval(vars)
                }
                override fun toString(): String {
                    return "$e1 / $e2"
                }
            }
        }
        private fun mod(e1: Expression, e2: Expression): Expression{
            return object : Expression{
                override fun eval(vars: Map<Char, Double>): Double {
                    return e1.eval(vars) % e2.eval(vars)
                }
                override fun toString(): String {
                    return "$e1 % $e2"
                }
            }
        }
        private fun pow(e1: Expression, e2: Expression): Expression{
            return object : Expression{
                override fun eval(vars: Map<Char, Double>): Double {
                    return e1.eval(vars).pow(e2.eval(vars))
                }
                override fun toString(): String {
                    return "$e1 ^ $e2"
                }
            }
        }
        private fun sqrt(e1: Expression): Expression{
            return object : Expression{
                override fun eval(vars: Map<Char, Double>): Double {
                    return kotlin.math.sqrt(e1.eval(vars))
                }
                override fun toString(): String {
                    return "sqrt$e1"
                }
            }
        }
        private fun ceil(e1: Expression): Expression{
            return object : Expression{
                override fun eval(vars: Map<Char, Double>): Double {
                    return kotlin.math.ceil(e1.eval(vars))
                }
                override fun toString(): String {
                    return "ceil$e1"
                }
            }
        }
        private fun floor(e1: Expression): Expression{
            return object : Expression{
                override fun eval(vars: Map<Char, Double>): Double {
                    return kotlin.math.floor(e1.eval(vars))
                }
                override fun toString(): String {
                    return "floor$e1"
                }
            }
        }
        private fun round(e1: Expression): Expression{
            return object : Expression{
                override fun eval(vars: Map<Char, Double>): Double {
                    return kotlin.math.round(e1.eval(vars))
                }
                override fun toString(): String {
                    return "round$e1"
                }
            }
        }
        private fun log(e1: Expression, power: Expression): Expression{
            return object : Expression{
                override fun eval(vars: Map<Char, Double>): Double {
                    return kotlin.math.log(e1.eval(vars),power.eval(vars))
                }
                override fun toString(): String {

                    return "log[${power.toString()}]$e1"
                }
            }
        }

        private fun log10(e1: Expression): Expression{
            return object : Expression{
                override fun eval(vars: Map<Char, Double>): Double {
                    return kotlin.math.log10(e1.eval(vars))
                }
                override fun toString(): String {

                    return "log10$e1"
                }
            }
        }

        private fun log2(e1: Expression): Expression{
            return object : Expression{
                override fun eval(vars: Map<Char, Double>): Double {
                    return kotlin.math.log2(e1.eval(vars))
                }
                override fun toString(): String {

                    return "log2$e1"
                }
            }
        }
        private fun ln(e1: Expression): Expression{
            return object : Expression{
                override fun eval(vars: Map<Char, Double>): Double {
                    return kotlin.math.ln(e1.eval(vars))
                }
                override fun toString(): String {
                    return "ln$e1"
                }
            }
        }
        private fun abs(e1: Expression): Expression{
            return object : Expression{
                override fun eval(vars: Map<Char, Double>): Double {
                    return kotlin.math.abs(e1.eval(vars))
                }
                override fun toString(): String {
                    return "abs$e1"
                }
            }
        }
        private fun sin(e1: Expression): Expression{
            return object : Expression{
                override fun eval(vars: Map<Char, Double>): Double {
                    return kotlin.math.sin(e1.eval(vars))
                }
                override fun toString(): String {
                    return "sin$e1"
                }
            }
        }
        private fun cos(e1: Expression): Expression{
            return object : Expression{
                override fun eval(vars: Map<Char, Double>): Double {
                    return kotlin.math.cos(e1.eval(vars))
                }
                override fun toString(): String {
                    return "cos$e1"
                }
            }
        }
        private fun incr(e1: Expression, e2: Expression): Expression{
            return object : Expression{
                override fun eval(vars: Map<Char, Double>): Double {
                    val base = e1.eval(vars)
                    val increment = e2.eval(vars)
                    return base - (base % increment)
                }
                override fun toString(): String {
                    return "incr($e1,$e2)"
                }
            }
        }

        private val doubleType = typeOf<Double>()
        private val randomClassifier = typeOf<Random>().classifier
        private val random = Random.createLocal()

        private fun mathHelper(reader: StringReader, context: String, chunk: String): Expression?{
            val member = MathHelper::class.members.firstOrNull { it.name == chunk && it.parameters.mapNotNull { p -> if(p.type == doubleType || p.type.classifier == randomClassifier) true else null }.isNotEmpty() }
            val params = member?.parameters
                ?: return null

            /*println(typeOf<Double>())
            println(params.map { it.type })
            println(randomType)
            println(doubleType)*/
            var numCount = 0
            val inputs: MutableList<Any> = mutableListOf()
            for (param in params){
                if (param.type == doubleType){
                    numCount++
                }
            }
            if (numCount > 0) {
                val expressions = parseParenthesesMultiple(reader, context, false)
                var j = 0
                for (param in params){
                    if (param.type == doubleType) {
                        //println("Double TYPE")
                        inputs.add(expressions[j])
                        j++
                    } else if (param.type.classifier == randomClassifier) {
                        //println("Random TYPE")
                        inputs.add(random)
                    }

                }
                if (expressions.size != numCount) {
                    //println(expressions)
                    //println("Num count doesn't match!!")
                    throw IllegalStateException("Incorrect number of parameters passed to [$chunk], expected [${numCount}] found [${expressions.size}]")
                }
                //println(inputs)
            } else {
                for (param in params){
                    //println("Random TYPE")
                    inputs.add(random)
                }
                if (reader.peek() == '(')
                    reader.read()
            }
            return object: Expression{
                override fun eval(vars: Map<Char, Double>): Double {
                    return member.call(*params.mapIndexed{index, kParameter -> if(kParameter.type.classifier == randomClassifier) inputs[index] else (inputs[index] as Expression).eval(vars) }.toTypedArray()) as Double
                }

                override fun toString(): String {
                    return "MathHelper.$chunk${inputs.toString().replace('[','(').replace(']',')')}"
                }
            }
        }

        fun interface NamedExpression{
            fun get(reader: StringReader, context: String, chunk: String): Expression
        }

    }
}