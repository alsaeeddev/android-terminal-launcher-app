package com.example.domain.command

data class ParsedCommand(
    val name: String,
    val args: List<String>,
    val flags: Map<String, String?>,
    val raw: String
)

enum class ChainOperator {
    NONE,
    AND,       // &&
    SEMICOLON // ;
}

data class PipelineStage(
    val commands: List<ParsedCommand>, // e.g. cmd1 | cmd2
    val operatorAfter: ChainOperator = ChainOperator.NONE
)

object CommandParser {

    /**
     * Splits a raw line into tokens considering quotes (' and ") and backslash escapes.
     */
    fun tokenize(input: String): List<String> {
        val tokens = mutableListOf<String>()
        val current = StringBuilder()
        var inSingleQuote = false
        var inDoubleQuote = false
        var isEscaped = false

        for (c in input) {
            if (isEscaped) {
                current.append(c)
                isEscaped = false
                continue
            }
            if (c == '\\') {
                isEscaped = true
                continue
            }
            if (c == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote
                continue
            }
            if (c == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote
                continue
            }
            if (c.isWhitespace() && !inSingleQuote && !inDoubleQuote) {
                if (current.isNotEmpty()) {
                    tokens.add(current.toString())
                    current.clear()
                }
                continue
            }
            current.append(c)
        }

        if (current.isNotEmpty()) {
            tokens.add(current.toString())
        }

        return tokens
    }

    /**
     * Parses a single command segment into name, positional args, and flags map.
     */
    fun parseSingle(segment: String): ParsedCommand {
        val tokens = tokenize(segment)
        if (tokens.isEmpty()) {
            return ParsedCommand("", emptyList(), emptyMap(), segment)
        }

        val name = tokens[0]
        val args = mutableListOf<String>()
        val flags = mutableMapOf<String, String?>()

        var i = 1
        while (i < tokens.size) {
            val token = tokens[i]
            if (token.startsWith("--") && token.length > 2) {
                val flagContent = token.substring(2)
                if (flagContent.contains("=")) {
                    val parts = flagContent.split("=", limit = 2)
                    flags[parts[0]] = parts[1]
                } else {
                    flags[flagContent] = "true"
                }
            } else if (token.startsWith("-") && token.length > 1 && !token.toDoubleOrNull().isNumeric()) {
                val flagChars = token.substring(1)
                for (ch in flagChars) {
                    flags[ch.toString()] = "true"
                }
            } else {
                args.add(token)
            }
            i++
        }

        return ParsedCommand(name, args, flags, segment)
    }

    private fun Double?.isNumeric(): Boolean = this != null

    /**
     * Parses an entire command line supporting chaining (&&, ;) and pipelines (|)
     */
    fun parseLine(rawInput: String): List<PipelineStage> {
        val stages = mutableListOf<PipelineStage>()
        if (rawInput.isBlank()) return stages

        // Split by chained operators (&& or ;)
        // We tokenize with awareness of delimiters outside quotes
        val segments = splitByChainOperators(rawInput)

        for ((segmentText, op) in segments) {
            // Split segmentText by pipe '|'
            val pipeTokens = splitByPipe(segmentText)
            val parsedCommands = pipeTokens.map { parseSingle(it.trim()) }
            if (parsedCommands.isNotEmpty() && parsedCommands.any { it.name.isNotEmpty() }) {
                stages.add(PipelineStage(parsedCommands, op))
            }
        }

        return stages
    }

    private fun splitByChainOperators(input: String): List<Pair<String, ChainOperator>> {
        val result = mutableListOf<Pair<String, ChainOperator>>()
        val current = StringBuilder()
        var inSingle = false
        var inDouble = false
        var i = 0

        while (i < input.length) {
            val c = input[i]
            if (c == '\'' && !inDouble) {
                inSingle = !inSingle
                current.append(c)
                i++
                continue
            }
            if (c == '"' && !inSingle) {
                inDouble = !inDouble
                current.append(c)
                i++
                continue
            }
            if (!inSingle && !inDouble) {
                if (c == '&' && i + 1 < input.length && input[i + 1] == '&') {
                    result.add(Pair(current.toString().trim(), ChainOperator.AND))
                    current.clear()
                    i += 2
                    continue
                }
                if (c == ';') {
                    result.add(Pair(current.toString().trim(), ChainOperator.SEMICOLON))
                    current.clear()
                    i++
                    continue
                }
            }
            current.append(c)
            i++
        }

        if (current.isNotEmpty()) {
            result.add(Pair(current.toString().trim(), ChainOperator.NONE))
        }

        return result
    }

    private fun splitByPipe(input: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inSingle = false
        var inDouble = false

        for (c in input) {
            if (c == '\'' && !inDouble) {
                inSingle = !inSingle
                current.append(c)
                continue
            }
            if (c == '"' && !inSingle) {
                inDouble = !inDouble
                current.append(c)
                continue
            }
            if (c == '|' && !inSingle && !inDouble) {
                result.add(current.toString())
                current.clear()
                continue
            }
            current.append(c)
        }
        if (current.isNotEmpty()) {
            result.add(current.toString())
        }
        return result
    }
}
