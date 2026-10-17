package com.example.core.util

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Safe, zero-eval recursive-descent parser for mathematical expressions.
 * Grammar:
 * Expression = Term (('+' | '-') Term)*
 * Term       = Factor (('*' | '/' | '%') Factor)*
 * Factor     = Power ('^' Power)*
 * Power      = ('+' | '-')? Primary
 * Primary    = NUMBER | IDENTIFIER ('(' Expression ')')? | '(' Expression ')'
 */
class CalculatorParser(private val input: String) {
    private var pos = 0
    private val length = input.length

    private fun peek(): Char = if (pos < length) input[pos] else '\u0000'

    private fun get(): Char = if (pos < length) input[pos++] else '\u0000'

    private fun skipWhitespace() {
        while (pos < length && input[pos].isWhitespace()) {
            pos++
        }
    }

    fun parse(): Double {
        pos = 0
        skipWhitespace()
        if (pos >= length) throw IllegalArgumentException("Empty expression")
        val result = parseExpression()
        skipWhitespace()
        if (pos < length) {
            throw IllegalArgumentException("Unexpected character '${peek()}' at index $pos")
        }
        return result
    }

    private fun parseExpression(): Double {
        var result = parseTerm()
        while (true) {
            skipWhitespace()
            when (peek()) {
                '+' -> {
                    get()
                    result += parseTerm()
                }
                '-' -> {
                    get()
                    result -= parseTerm()
                }
                else -> return result
            }
        }
    }

    private fun parseTerm(): Double {
        var result = parseFactor()
        while (true) {
            skipWhitespace()
            when (peek()) {
                '*' -> {
                    get()
                    result *= parseFactor()
                }
                '/' -> {
                    get()
                    val divisor = parseFactor()
                    if (divisor == 0.0) throw ArithmeticException("Division by zero")
                    result /= divisor
                }
                '%' -> {
                    get()
                    val divisor = parseFactor()
                    if (divisor == 0.0) throw ArithmeticException("Modulo by zero")
                    result %= divisor
                }
                else -> return result
            }
        }
    }

    private fun parseFactor(): Double {
        var base = parseUnary()
        skipWhitespace()
        if (peek() == '^') {
            get()
            val exponent = parseFactor() // right-associative
            base = base.pow(exponent)
        }
        return base
    }

    private fun parseUnary(): Double {
        skipWhitespace()
        if (peek() == '+') {
            get()
            return parseUnary()
        }
        if (peek() == '-') {
            get()
            return -parseUnary()
        }
        return parsePrimary()
    }

    private fun parsePrimary(): Double {
        skipWhitespace()
        val c = peek()

        if (c == '(') {
            get() // consume '('
            val res = parseExpression()
            skipWhitespace()
            if (peek() != ')') {
                throw IllegalArgumentException("Missing closing parenthesis")
            }
            get() // consume ')'
            return res
        }

        if (c.isDigit() || c == '.') {
            return parseNumber()
        }

        if (c.isLetter()) {
            return parseIdentifierOrFunction()
        }

        throw IllegalArgumentException("Unexpected token '${c}' at index $pos")
    }

    private fun parseNumber(): Double {
        val start = pos
        var hasDot = false
        while (pos < length) {
            val ch = input[pos]
            if (ch == '.') {
                if (hasDot) break
                hasDot = true
                pos++
            } else if (ch.isDigit()) {
                pos++
            } else {
                break
            }
        }
        val numStr = input.substring(start, pos)
        return numStr.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number: $numStr")
    }

    private fun parseIdentifierOrFunction(): Double {
        val start = pos
        while (pos < length && (input[pos].isLetterOrDigit() || input[pos] == '_')) {
            pos++
        }
        val id = input.substring(start, pos).lowercase()
        skipWhitespace()

        // Check if function call
        if (peek() == '(') {
            get() // consume '('
            val arg = parseExpression()
            skipWhitespace()
            if (peek() != ')') throw IllegalArgumentException("Missing ')' after function $id")
            get() // consume ')'
            return when (id) {
                "sin" -> sin(Math.toRadians(arg))
                "cos" -> cos(Math.toRadians(arg))
                "tan" -> tan(Math.toRadians(arg))
                "asin" -> Math.toDegrees(kotlin.math.asin(arg))
                "acos" -> Math.toDegrees(kotlin.math.acos(arg))
                "atan" -> Math.toDegrees(kotlin.math.atan(arg))
                "sqrt" -> {
                    if (arg < 0) throw ArithmeticException("Square root of negative number")
                    sqrt(arg)
                }
                "abs" -> abs(arg)
                "ln" -> {
                    if (arg <= 0) throw ArithmeticException("Logarithm of non-positive number")
                    ln(arg)
                }
                "log", "log10" -> {
                    if (arg <= 0) throw ArithmeticException("Logarithm of non-positive number")
                    log10(arg)
                }
                else -> throw IllegalArgumentException("Unknown function: $id")
            }
        }

        // Constants
        return when (id) {
            "pi" -> Math.PI
            "e" -> Math.E
            else -> throw IllegalArgumentException("Unknown identifier: $id")
        }
    }
}
