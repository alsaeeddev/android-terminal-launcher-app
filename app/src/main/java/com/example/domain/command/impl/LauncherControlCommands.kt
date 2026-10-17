package com.example.domain.command.impl

import com.example.core.ui.theme.TerminalThemeType
import com.example.domain.command.Command
import com.example.domain.command.CommandCategory
import com.example.domain.command.CommandContext
import com.example.domain.command.CommandRegistry
import com.example.domain.model.OutputLine
import com.example.domain.model.OutputLineType
import com.example.domain.repository.ProductivityRepository
import com.example.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class ThemeCommand(
    private val settingsRepository: SettingsRepository
) : Command {
    override val name = "theme"
    override val description = "List or set terminal color themes (Dracula, Nord, Monokai, Matrix, Gruvbox, etc.)"
    override val usage = "theme ls | theme set <theme_name>"
    override val category = CommandCategory.LAUNCHER

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        if (args.isEmpty() || args[0].equals("ls", ignoreCase = true)) {
            val current = settingsRepository.preferencesFlow.first().theme
            emit(OutputLine("Available Themes:", OutputLineType.HEADER))
            for (t in TerminalThemeType.values()) {
                val active = if (t == current) " (Active ✔)" else ""
                emit(OutputLine("• ${t.name.lowercase()} — ${t.displayName}$active", OutputLineType.INFO))
            }
            emit(OutputLine("\nUsage: theme set <name> (e.g. theme set matrix_green)", OutputLineType.NORMAL))
            return@flow
        }

        if (args[0].equals("set", ignoreCase = true)) {
            val target = args.getOrNull(1)?.uppercase()
            if (target == null) {
                emit(OutputLine("Usage: theme set <theme_name>", OutputLineType.WARNING))
                return@flow
            }
            val matching = TerminalThemeType.values().firstOrNull {
                it.name.equals(target, ignoreCase = true) || it.name.replace("_", "").equals(target.replace("_", ""), ignoreCase = true)
            }
            if (matching != null) {
                settingsRepository.setTheme(matching)
                emit(OutputLine("Theme changed to ${matching.displayName}.", OutputLineType.SUCCESS))
            } else {
                emit(OutputLine("Unknown theme: $target. Run 'theme ls' to see choices.", OutputLineType.ERROR))
            }
        } else {
            emit(OutputLine("Usage: theme ls | theme set <theme_name>", OutputLineType.WARNING))
        }
    }
}

class FontCommand(
    private val settingsRepository: SettingsRepository
) : Command {
    override val name = "font"
    override val description = "View or customize terminal font and text size"
    override val usage = "font size <sp_size> | font ls"
    override val category = CommandCategory.LAUNCHER

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        val prefs = settingsRepository.preferencesFlow.first()
        if (args.isEmpty() || args[0].equals("ls", ignoreCase = true)) {
            emit(OutputLine("Font Configuration:", OutputLineType.HEADER))
            emit(OutputLine("Current Size: ${prefs.fontSize} sp", OutputLineType.INFO))
            emit(OutputLine("Font Family: ${prefs.fontName} (JetBrains Mono / System Monospace)", OutputLineType.NORMAL))
            emit(OutputLine("Change font size: font size <9-24>", OutputLineType.WARNING))
            return@flow
        }

        if (args[0].equals("size", ignoreCase = true)) {
            val size = args.getOrNull(1)?.toFloatOrNull()
            if (size != null && size in 8.0f..26.0f) {
                settingsRepository.setFontSize(size)
                emit(OutputLine("Font size set to ${size}sp.", OutputLineType.SUCCESS))
            } else {
                emit(OutputLine("Invalid size. Please choose between 9 and 24 sp.", OutputLineType.ERROR))
            }
        }
    }
}

class ModeCommand(
    private val settingsRepository: SettingsRepository
) : Command {
    override val name = "mode"
    override val description = "Switch launcher default mode between terminal (term) and GUI drawer (gui)"
    override val usage = "mode term | mode gui"
    override val category = CommandCategory.LAUNCHER

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        val target = args.firstOrNull()?.lowercase()
        if (target == "gui" || target == "drawer") {
            settingsRepository.setDefaultMode("gui")
            emit(OutputLine("Default launcher mode switched to GUI App Drawer.", OutputLineType.SUCCESS))
        } else if (target == "term" || target == "cli" || target == "terminal") {
            settingsRepository.setDefaultMode("term")
            emit(OutputLine("Default launcher mode switched to Terminal CLI.", OutputLineType.SUCCESS))
        } else {
            val current = settingsRepository.preferencesFlow.first().defaultMode
            emit(OutputLine("Current mode: $current. Usage: mode term | mode gui", OutputLineType.INFO))
        }
    }
}

class WallpaperCommand(
    private val settingsRepository: SettingsRepository
) : Command {
    override val name = "wallpaper"
    override val aliases = listOf("bg")
    override val description = "Adjust wallpaper background dim level (0.0 to 1.0)"
    override val usage = "wallpaper dim <0.0 - 1.0>"
    override val category = CommandCategory.LAUNCHER

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        if (args.isEmpty()) {
            val cur = settingsRepository.preferencesFlow.first().wallpaperDim
            emit(OutputLine("Current wallpaper dim: $cur (0.0 = transparent wallpaper, 1.0 = solid dark)", OutputLineType.INFO))
            return@flow
        }
        val dim = (if (args[0].equals("dim", ignoreCase = true)) args.getOrNull(1) else args[0])?.toFloatOrNull()
        if (dim != null && dim in 0.0f..1.0f) {
            settingsRepository.setWallpaperDim(dim)
            emit(OutputLine("Wallpaper dim set to $dim", OutputLineType.SUCCESS))
        } else {
            emit(OutputLine("Usage: wallpaper dim <0.0 - 1.0>", OutputLineType.WARNING))
        }
    }
}

class AliasCommand(
    private val productivityRepository: ProductivityRepository
) : Command {
    override val name = "alias"
    override val description = "Create or list command aliases"
    override val usage = "alias [name=command]"
    override val category = CommandCategory.LAUNCHER

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        if (args.isEmpty()) {
            val aliases = productivityRepository.aliasesFlow.first()
            if (aliases.isEmpty()) {
                emit(OutputLine("No custom aliases. Create one with: alias ll='ls -a'", OutputLineType.INFO))
            } else {
                emit(OutputLine("Defined Aliases (${aliases.size}):", OutputLineType.HEADER))
                for (a in aliases) {
                    emit(OutputLine("alias ${a.name}='${a.command}'", OutputLineType.NORMAL))
                }
            }
            return@flow
        }

        val full = args.joinToString(" ")
        if (full.contains("=")) {
            val parts = full.split("=", limit = 2)
            val name = parts[0].trim()
            val cmd = parts[1].trim().removeSurrounding("'", "'").removeSurrounding("\"", "\"")
            productivityRepository.setAlias(name, cmd)
            emit(OutputLine("Alias '$name' created for '$cmd'.", OutputLineType.SUCCESS))
        } else {
            emit(OutputLine("Usage: alias <name>=<command> (e.g. alias c=clear)", OutputLineType.WARNING))
        }
    }
}

class UnaliasCommand(
    private val productivityRepository: ProductivityRepository
) : Command {
    override val name = "unalias"
    override val description = "Remove a custom command alias"
    override val usage = "unalias <alias_name>"
    override val category = CommandCategory.LAUNCHER

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        if (args.isEmpty()) {
            emit(OutputLine("Usage: unalias <name>", OutputLineType.WARNING))
            return@flow
        }
        productivityRepository.removeAlias(args[0])
        emit(OutputLine("Alias '${args[0]}' removed.", OutputLineType.SUCCESS))
    }
}

class HistoryCommand(
    private val productivityRepository: ProductivityRepository
) : Command {
    override val name = "history"
    override val aliases = listOf("hist")
    override val description = "View or clear recent command history"
    override val usage = "history [-c] [limit]"
    override val category = CommandCategory.LAUNCHER

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        if (flags.containsKey("c") || args.firstOrNull() == "-c") {
            productivityRepository.clearHistory()
            emit(OutputLine("Command history cleared.", OutputLineType.SUCCESS))
            return@flow
        }
        val limit = args.firstOrNull()?.toIntOrNull() ?: 20
        val history = productivityRepository.getRecentHistory(limit)
        if (history.isEmpty()) {
            emit(OutputLine("Command history is empty.", OutputLineType.INFO))
        } else {
            emit(OutputLine("Recent Commands ($limit):", OutputLineType.HEADER))
            for ((idx, cmd) in history.reversed().withIndex()) {
                emit(OutputLine("${idx + 1}  $cmd", OutputLineType.NORMAL))
            }
        }
    }
}

class ClearCommand : Command {
    override val name = "clear"
    override val aliases = listOf("cls")
    override val description = "Clear terminal output screen"
    override val usage = "clear"
    override val category = CommandCategory.LAUNCHER

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        // Clear is intercepted in ViewModel to reset terminal lines
        emit(OutputLine("Terminal buffer cleared.", OutputLineType.INFO))
    }
}

class HelpCommand(
    private val registryProvider: () -> CommandRegistry
) : Command {
    override val name = "help"
    override val aliases = listOf("?")
    override val description = "List all available commands or get details for a command"
    override val usage = "help [command_name]"
    override val category = CommandCategory.LAUNCHER

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        val registry = registryProvider()
        if (args.isNotEmpty()) {
            val cmd = registry.get(args[0])
            if (cmd != null) {
                emit(OutputLine("=== Command: ${cmd.name} ===", OutputLineType.HEADER))
                emit(OutputLine("Description: ${cmd.description}", OutputLineType.INFO))
                emit(OutputLine("Usage: ${cmd.usage}", OutputLineType.NORMAL))
                if (cmd.aliases.isNotEmpty()) {
                    emit(OutputLine("Aliases: ${cmd.aliases.joinToString(", ")}", OutputLineType.NORMAL))
                }
            } else {
                emit(OutputLine("Command '${args[0]}' not found.", OutputLineType.ERROR))
            }
            return@flow
        }

        emit(OutputLine("╔══════════════════════════════════════════════════════╗", OutputLineType.ASCII_ART))
        emit(OutputLine("║             SHELL HOME — COMMAND MANUAL              ║", OutputLineType.ASCII_ART))
        emit(OutputLine("╚══════════════════════════════════════════════════════╝", OutputLineType.ASCII_ART))
        emit(OutputLine("Type 'help <command>' for specific usage information.\n", OutputLineType.NORMAL))

        val all = registry.getAll().distinctBy { it.name }.groupBy { it.category }
        for ((cat, cmds) in all) {
            emit(OutputLine("[${cat.title}]", OutputLineType.HEADER))
            for (c in cmds.sortedBy { it.name }) {
                val pad = (12 - c.name.length).coerceAtLeast(1)
                val spacing = " ".repeat(pad)
                emit(OutputLine("  ${c.name}$spacing - ${c.description}", OutputLineType.NORMAL))
            }
            emit(OutputLine("", OutputLineType.NORMAL))
        }
    }
}

class AboutCommand : Command {
    override val name = "about"
    override val aliases = listOf("version")
    override val description = "Show app details, architecture, and version"
    override val usage = "about"
    override val category = CommandCategory.LAUNCHER

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        emit(OutputLine("🐚 Shell Home Launcher v1.0.0", OutputLineType.HEADER))
        emit(OutputLine("• Author: Al Saeed Dev (alsaeeddev.com)", OutputLineType.INFO))
        emit(OutputLine("• Architecture: Clean MVVM + MVI, Jetpack Compose, Room, DataStore", OutputLineType.NORMAL))
        emit(OutputLine("• Android Compatibility: Android 8.0 (API 26) through Android 17 (API 37)", OutputLineType.NORMAL))
        emit(OutputLine("• 16 KB Page Size Compatible | Zero Tracking | 100% On-Device", OutputLineType.SUCCESS))
    }
}

class ConfigCommand(
    private val settingsRepository: SettingsRepository
) : Command {
    override val name = "config"
    override val description = "Get or set launcher configuration variables"
    override val usage = "config [get <key> | set <key> <val> | ls]"
    override val category = CommandCategory.LAUNCHER

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        val prefs = settingsRepository.preferencesFlow.first()
        if (args.isEmpty() || args[0].equals("ls", ignoreCase = true)) {
            emit(OutputLine("Current Configuration:", OutputLineType.HEADER))
            emit(OutputLine("theme:           ${prefs.theme.name}", OutputLineType.NORMAL))
            emit(OutputLine("fontSize:        ${prefs.fontSize}", OutputLineType.NORMAL))
            emit(OutputLine("username:        ${prefs.username}", OutputLineType.NORMAL))
            emit(OutputLine("hostname:        ${prefs.hostname}", OutputLineType.NORMAL))
            emit(OutputLine("bootAnimation:   ${prefs.bootAnimationEnabled}", OutputLineType.NORMAL))
            emit(OutputLine("autoLaunch:      ${prefs.autoLaunchSingleMatch}", OutputLineType.NORMAL))
            emit(OutputLine("crtScanlines:    ${prefs.crtScanlinesEnabled}", OutputLineType.NORMAL))
            emit(OutputLine("scrollbackLimit: ${prefs.scrollbackLimit}", OutputLineType.NORMAL))
            emit(OutputLine("defaultMode:     ${prefs.defaultMode}", OutputLineType.NORMAL))
            emit(OutputLine("searchEngine:    ${prefs.searchEngine}", OutputLineType.NORMAL))
            emit(OutputLine("wallpaperDim:    ${prefs.wallpaperDim}", OutputLineType.NORMAL))
            return@flow
        }

        val action = args[0].lowercase()
        if (action == "set" && args.size >= 3) {
            val key = args[1].lowercase()
            val value = args[2]
            when (key) {
                "user", "username" -> settingsRepository.setPrompt(value, prefs.hostname)
                "host", "hostname" -> settingsRepository.setPrompt(prefs.username, value)
                "autolaunch" -> settingsRepository.setAutoLaunch(value.toBoolean())
                "crt" -> settingsRepository.setCrtScanlines(value.toBoolean())
                "searchengine" -> settingsRepository.setSearchEngine(value)
                "bootanimation" -> settingsRepository.setBootAnimation(value.toBoolean())
                else -> {
                    emit(OutputLine("Unsupported key: $key. See 'config ls'.", OutputLineType.ERROR))
                    return@flow
                }
            }
            emit(OutputLine("Config '$key' updated to '$value'.", OutputLineType.SUCCESS))
        } else {
            emit(OutputLine("Usage: config set <key> <val>", OutputLineType.WARNING))
        }
    }
}
