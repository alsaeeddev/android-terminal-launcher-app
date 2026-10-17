package com.example.core.util

import kotlin.math.min

object FuzzyMatcher {

    data class MatchResult<T>(
        val item: T,
        val score: Int
    )

    /**
     * Scores how well [query] matches [target]. Higher score is better.
     * 100+: Exact match
     * 80+: Prefix match
     * 60+: Substring match
     * 40+: Acronym match
     * 20+: Subsequence match
     * 0: No match
     */
    fun score(query: String, target: String): Int {
        val q = query.trim().lowercase()
        val t = target.trim().lowercase()

        if (q.isEmpty() || t.isEmpty()) return 0
        if (q == t) return 100
        if (t.startsWith(q)) return 85 - (t.length - q.length).coerceAtMost(15)
        if (t.contains(q)) return 60 - t.indexOf(q).coerceAtMost(10)

        // Acronym match (e.g. "yt" matches "YouTube")
        val words = t.split(Regex("[\\s._-]+")).filter { it.isNotEmpty() }
        val acronym = words.map { it.first() }.joinToString("")
        if (acronym.startsWith(q)) return 50
        if (acronym.contains(q)) return 40

        // Subsequence match
        var qIdx = 0
        var consecutiveBonus = 0
        var totalScore = 0
        for (i in t.indices) {
            if (qIdx < q.length && t[i] == q[qIdx]) {
                qIdx++
                consecutiveBonus += 2
                totalScore += 3 + consecutiveBonus
            } else {
                consecutiveBonus = 0
            }
        }

        return if (qIdx == q.length) (20 + totalScore).coerceAtMost(39) else 0
    }

    fun <T> filterAndSort(
        query: String,
        items: List<T>,
        selector: (T) -> String,
        limit: Int = 10
    ): List<T> {
        if (query.isBlank()) return items.take(limit)
        return items
            .map { item -> MatchResult(item, score(query, selector(item))) }
            .filter { it.score > 0 }
            .sortedByDescending { it.score }
            .take(limit)
            .map { it.item }
    }

    /**
     * Compute Levenshtein edit distance between two strings
     */
    fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j

        for (i in 1..a.length) {
            for (j in 1..b.length) {
                val cost = if (a[i - 1].equals(b[j - 1], ignoreCase = true)) 0 else 1
                dp[i][j] = min(
                    dp[i - 1][j] + 1,
                    min(dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost)
                )
            }
        }
        return dp[a.length][b.length]
    }

    /**
     * Suggest closest match from a pool of candidates if distance is within tolerance
     */
    fun findDidYouMean(query: String, candidates: Collection<String>, maxDistance: Int = 3): String? {
        val q = query.trim().lowercase()
        var bestCandidate: String? = null
        var minDistance = maxDistance + 1

        for (candidate in candidates) {
            val dist = levenshtein(q, candidate.lowercase())
            if (dist < minDistance) {
                minDistance = dist
                bestCandidate = candidate
            }
        }
        return if (minDistance <= maxDistance) bestCandidate else null
    }
}
