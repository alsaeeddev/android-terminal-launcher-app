package com.example.domain.command.impl

import com.example.domain.command.Command
import com.example.domain.command.CommandCategory
import com.example.domain.command.CommandContext
import com.example.domain.model.OutputLine
import com.example.domain.model.OutputLineType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

class SudoCommand : Command {
    override val name = "sudo"
    override val description = "Execute a command with superuser privileges (harmless easter egg)"
    override val usage = "sudo <command>"
    override val category = CommandCategory.FUN

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        emit(OutputLine("[sudo] password for user: **********", OutputLineType.WARNING))
        emit(OutputLine("We trust you have received the usual lecture from the local System Administrator.", OutputLineType.NORMAL))
        emit(OutputLine("It usually boils down to these three things:", OutputLineType.NORMAL))
        emit(OutputLine("  #1) Respect the privacy of others.", OutputLineType.NORMAL))
        emit(OutputLine("  #2) Think before you type.", OutputLineType.NORMAL))
        emit(OutputLine("  #3) With great power comes great responsibility.", OutputLineType.SUCCESS))
        emit(OutputLine("user is not in the sudoers file. This incident will be reported.", OutputLineType.ERROR))
    }
}

class CowsayCommand : Command {
    override val name = "cowsay"
    override val description = "Generate an ASCII cow speaking your text"
    override val usage = "cowsay [message]"
    override val category = CommandCategory.FUN

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        val text = if (args.isNotEmpty()) args.joinToString(" ") else "Moo! Welcome to Shell Home."
        val border = "-".repeat(text.length + 2)
        emit(OutputLine(" $border", OutputLineType.ASCII_ART))
        emit(OutputLine("< $text >", OutputLineType.ASCII_ART))
        emit(OutputLine(" $border", OutputLineType.ASCII_ART))
        emit(OutputLine("        \\   ^__^", OutputLineType.ASCII_ART))
        emit(OutputLine("         \\  (oo)\\_______", OutputLineType.ASCII_ART))
        emit(OutputLine("            (__)\\       )\\/\\", OutputLineType.ASCII_ART))
        emit(OutputLine("                ||----w |", OutputLineType.ASCII_ART))
        emit(OutputLine("                ||     ||", OutputLineType.ASCII_ART))
    }
}

class FortuneCommand : Command {
    override val name = "fortune"
    override val description = "Display a wise or humorous quote"
    override val usage = "fortune"
    override val category = CommandCategory.FUN

    private val fortunes = listOf(
        "There's no place like 127.0.0.1",
        "Simplicity is prerequisite for reliability. — Edsger W. Dijkstra",
        "Unix is simple. It just takes a genius to understand its simplicity. — Dennis Ritchie",
        "Any fool can write code that a computer can understand. Good programmers write code that humans can understand. — Martin Fowler",
        "Shell Home: The terminal is your desktop.",
        "Talk is cheap. Show me the code. — Linus Torvalds",
        "Don't worry if it doesn't work right. If everything did, you'd be out of a job.",
        "To err is human, but to really foul things up you need a computer.",
        "A bug in hand is worth two in the documentation."
    )

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        val quote = fortunes.random()
        emit(OutputLine("🥠 Fortune:", OutputLineType.HEADER))
        emit(OutputLine("\"$quote\"", OutputLineType.INFO))
    }
}

class MatrixCommand : Command {
    override val name = "matrix"
    override val description = "Render an animated green matrix digital rain stream"
    override val usage = "matrix"
    override val category = CommandCategory.FUN

    private val glyphs = "0123456789ABCDEFｦｱｳｴｵｶｷｹｺｻｼｽｾｿﾀﾂﾃﾅﾆﾇﾈﾊﾋﾎﾏﾐﾑﾒﾓﾔﾕﾗﾘﾜ"

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        emit(OutputLine("Entering the Matrix... (Streamed sequence)", OutputLineType.SUCCESS))
        for (i in 0 until 12) {
            val sb = StringBuilder()
            val len = Random.nextInt(25, 45)
            for (j in 0 until len) {
                sb.append(glyphs.random())
                if (Random.nextInt(5) == 0) sb.append(" ")
            }
            emit(OutputLine(sb.toString(), OutputLineType.SUCCESS))
            delay(60)
        }
        emit(OutputLine("Wake up, Neo... Shell Home has you.", OutputLineType.HEADER))
    }
}
