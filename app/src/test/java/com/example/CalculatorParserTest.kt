package com.example

import com.example.core.util.CalculatorParser
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculatorParserTest {

    @Test
    fun testBasicArithmetic() {
        assertEquals(7.0, CalculatorParser("3 + 4").parse(), 0.0001)
        assertEquals(14.0, CalculatorParser("20 - 6").parse(), 0.0001)
        assertEquals(42.0, CalculatorParser("6 * 7").parse(), 0.0001)
        assertEquals(5.0, CalculatorParser("25 / 5").parse(), 0.0001)
    }

    @Test
    fun testOperatorPrecedenceAndParentheses() {
        assertEquals(20.0, CalculatorParser("2 + 3 * 6").parse(), 0.0001)
        assertEquals(30.0, CalculatorParser("(2 + 3) * 6").parse(), 0.0001)
        assertEquals(32.0, CalculatorParser("2 * (4 ^ 2)").parse(), 0.0001)
    }

    @Test
    fun testFunctionsAndConstants() {
        assertEquals(12.0, CalculatorParser("sqrt(144)").parse(), 0.0001)
        assertEquals(1.0, CalculatorParser("sin(90)").parse(), 0.0001)
        assertEquals(1.0, CalculatorParser("cos(0)").parse(), 0.0001)
        assertEquals(3.14159, CalculatorParser("pi").parse(), 0.001)
    }

    @Test(expected = ArithmeticException::class)
    fun testDivisionByZero() {
        CalculatorParser("10 / 0").parse()
    }
}
