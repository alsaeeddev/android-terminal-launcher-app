package com.example.domain.command

import com.example.core.util.FuzzyMatcher

class CommandRegistry(commands: List<Command> = emptyList()) {

    private val commandMap = mutableMapOf<String, Command>()
    private val allCommandsList = mutableListOf<Command>()

    init {
        for (cmd in commands) {
            register(cmd)
        }
    }

    fun register(command: Command) {
        allCommandsList.add(command)
        commandMap[command.name.lowercase()] = command
        for (alias in command.aliases) {
            commandMap[alias.lowercase()] = command
        }
    }

    fun get(name: String): Command? {
        return commandMap[name.lowercase()]
    }

    fun getAll(): List<Command> = allCommandsList

    fun getAllNamesAndAliases(): List<String> = commandMap.keys.toList()

    fun suggest(input: String): String? {
        return FuzzyMatcher.findDidYouMean(input, commandMap.keys)
    }
}
