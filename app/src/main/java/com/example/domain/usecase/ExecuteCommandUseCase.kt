package com.example.domain.usecase

import android.content.Context
import com.example.core.common.DispatcherProvider
import com.example.domain.command.ChainOperator
import com.example.domain.command.CommandContext
import com.example.domain.command.CommandParser
import com.example.domain.command.CommandRegistry
import com.example.domain.model.OutputLine
import com.example.domain.model.OutputLineType
import com.example.domain.repository.AppRepository
import com.example.domain.repository.ProductivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class ExecuteCommandUseCase(
    private val commandRegistry: CommandRegistry,
    private val appRepository: AppRepository,
    private val productivityRepository: ProductivityRepository,
    private val dispatcherProvider: DispatcherProvider
) {

    suspend fun execute(
        context: Context,
        rawInput: String,
        currentPrompt: String
    ): Flow<OutputLine> = flow {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) return@flow

        // Emit command echo line
        emit(OutputLine("$currentPrompt $trimmed", OutputLineType.COMMAND_ECHO))

        // Save into command history
        productivityRepository.addHistory(trimmed)

        // Expand alias if defined
        val expandedLine = expandAliases(trimmed)

        // Parse pipelines and chains
        val stages = CommandParser.parseLine(expandedLine)
        if (stages.isEmpty()) return@flow

        var lastOutput = mutableListOf<OutputLine>()

        for (stage in stages) {
            var stageSuccess = true
            var pipeInput = emptyList<OutputLine>()

            for (parsedCmd in stage.commands) {
                if (parsedCmd.name.isEmpty()) continue

                val command = commandRegistry.get(parsedCmd.name)
                if (command != null) {
                    val cmdContext = CommandContext(context = context, stdin = pipeInput)
                    val cmdOutput = mutableListOf<OutputLine>()
                    try {
                        command.execute(cmdContext, parsedCmd.args, parsedCmd.flags).collect { line ->
                            cmdOutput.add(line)
                            emit(line)
                        }
                    } catch (e: Exception) {
                        stageSuccess = false
                        val err = OutputLine("Error executing ${parsedCmd.name}: ${e.message}", OutputLineType.ERROR)
                        cmdOutput.add(err)
                        emit(err)
                    }
                    pipeInput = cmdOutput
                    lastOutput = cmdOutput
                } else {
                    // Check if user typed an application name directly (e.g. "youtube", "camera", "chrome")
                    val matchedApp = appRepository.findApp(parsedCmd.name)
                    if (matchedApp != null) {
                        emit(OutputLine("Launching ${matchedApp.label}...", OutputLineType.SUCCESS))
                        val launchRes = appRepository.launchApp(matchedApp.packageName, matchedApp.userHandle)
                        if (launchRes.isFailure) {
                            emit(OutputLine("Failed to launch ${matchedApp.label}: ${launchRes.exceptionOrNull()?.message}", OutputLineType.ERROR))
                        }
                    } else {
                        stageSuccess = false
                        val suggestion = commandRegistry.suggest(parsedCmd.name)
                        emit(OutputLine("shell: command not found: ${parsedCmd.name}", OutputLineType.ERROR))
                        if (suggestion != null) {
                            emit(OutputLine("Did you mean: $suggestion? (Type 'help' for commands)", OutputLineType.WARNING))
                        }
                    }
                }
            }

            if (!stageSuccess && stage.operatorAfter == ChainOperator.AND) {
                break
            }
        }
    }.flowOn(dispatcherProvider.io)

    private suspend fun expandAliases(line: String): String {
        val parts = line.split(" ", limit = 2)
        val firstWord = parts[0]
        val rest = if (parts.size > 1) " " + parts[1] else ""

        val alias = productivityRepository.getAlias(firstWord)
        return if (alias != null) "$alias$rest" else line
    }
}
