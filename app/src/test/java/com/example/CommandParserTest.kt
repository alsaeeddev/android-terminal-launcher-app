package com.example

import com.example.domain.command.ChainOperator
import com.example.domain.command.CommandParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CommandParserTest {

    @Test
    fun testTokenizeSimple() {
        val tokens = CommandParser.tokenize("ls -a -s name")
        assertEquals(listOf("ls", "-a", "-s", "name"), tokens)
    }

    @Test
    fun testTokenizeQuotesAndEscapes() {
        val tokens = CommandParser.tokenize("echo \"hello world\" 'single quoted' escaped\\ space")
        assertEquals(listOf("echo", "hello world", "single quoted", "escaped space"), tokens)
    }

    @Test
    fun testParseFlags() {
        val cmd = CommandParser.parseSingle("apps -af --sort=usage --all Target")
        assertEquals("apps", cmd.name)
        assertTrue(cmd.flags.containsKey("a"))
        assertTrue(cmd.flags.containsKey("f"))
        assertEquals("usage", cmd.flags["sort"])
        assertEquals("true", cmd.flags["all"])
        assertEquals(listOf("Target"), cmd.args)
    }

    @Test
    fun testParsePipelinesAndChains() {
        val stages = CommandParser.parseLine("ls -a | grep chrome && echo done ; time")
        assertEquals(3, stages.size)
        // Stage 1 has pipeline (ls | grep) and operator AND
        assertEquals(2, stages[0].commands.size)
        assertEquals("ls", stages[0].commands[0].name)
        assertEquals("grep", stages[0].commands[1].name)
        assertEquals(ChainOperator.AND, stages[0].operatorAfter)

        // Stage 2 has echo done with operator SEMICOLON
        assertEquals(1, stages[1].commands.size)
        assertEquals("echo", stages[1].commands[0].name)
        assertEquals(ChainOperator.SEMICOLON, stages[1].operatorAfter)

        // Stage 3 has time
        assertEquals("time", stages[2].commands[0].name)
    }
}
