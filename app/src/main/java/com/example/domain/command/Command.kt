package com.example.domain.command

import android.content.Context
import com.example.domain.model.OutputLine
import kotlinx.coroutines.flow.Flow

data class CommandContext(
    val context: Context,
    val stdin: List<OutputLine> = emptyList()
)

enum class CommandCategory(val title: String) {
    APPS("Applications"),
    SYSTEM("System & Hardware"),
    PRODUCTIVITY("Productivity & Utilities"),
    LAUNCHER("Launcher Control"),
    FUN("Fun & Extras")
}

interface Command {
    val name: String
    val aliases: List<String> get() = emptyList()
    val description: String
    val usage: String
    val category: CommandCategory

    suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine>
}
