package com.example

import com.example.core.util.FuzzyMatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FuzzyMatcherTest {

    @Test
    fun testExactAndPrefixMatch() {
        val exactScore = FuzzyMatcher.score("chrome", "Chrome")
        val prefixScore = FuzzyMatcher.score("chr", "Chrome")
        assertTrue(exactScore >= 100)
        assertTrue(prefixScore in 70..99)
    }

    @Test
    fun testAcronymMatch() {
        val score = FuzzyMatcher.score("ps", "Play Store")
        assertTrue("Acronym score should be > 30", score > 30)
    }

    @Test
    fun testDidYouMeanSuggestion() {
        val candidates = listOf("battery", "brightness", "bluetooth", "clear", "config")
        val suggestion = FuzzyMatcher.findDidYouMean("batry", candidates)
        assertNotNull(suggestion)
        assertEquals("battery", suggestion)
    }

    @Test
    fun testLevenshteinDistance() {
        assertEquals(0, FuzzyMatcher.levenshtein("terminal", "terminal"))
        assertEquals(1, FuzzyMatcher.levenshtein("term", "termn"))
        assertEquals(3, FuzzyMatcher.levenshtein("kitten", "sitting"))
    }
}
