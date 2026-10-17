package com.example.domain.command.impl

import android.content.Intent
import android.net.Uri
import com.example.domain.command.Command
import com.example.domain.command.CommandCategory
import com.example.domain.command.CommandContext
import com.example.domain.model.AppItem
import com.example.domain.model.OutputLine
import com.example.domain.model.OutputLineType
import com.example.domain.repository.AppRepository
import com.example.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class LsCommand(
    private val appRepository: AppRepository
) : Command {
    override val name = "ls"
    override val aliases = listOf("apps", "list")
    override val description = "List installed applications"
    override val usage = "ls [-a] [-f] [-r] [-h] [-s <name|usage|date>] [-w]"
    override val category = CommandCategory.APPS

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        val showAll = flags.containsKey("a") || flags.containsKey("all")
        val favoritesOnly = flags.containsKey("f") || flags.containsKey("favorites")
        val hiddenOnly = flags.containsKey("h") || flags.containsKey("hidden")
        val reverse = flags.containsKey("r") || flags.containsKey("reverse")
        val workOnly = flags.containsKey("w") || flags.containsKey("work")
        val sortBy = flags["s"] ?: flags["sort"] ?: "name"

        var apps = appRepository.appsFlow.value

        if (!showAll && !hiddenOnly) {
            apps = apps.filter { !it.isHidden }
        } else if (hiddenOnly) {
            apps = apps.filter { it.isHidden }
        }

        if (favoritesOnly) {
            apps = apps.filter { it.isFavorite }
        }

        if (workOnly) {
            apps = apps.filter { it.isWorkProfile }
        }

        apps = when (sortBy.lowercase()) {
            "usage", "count" -> apps.sortedByDescending { it.launchCount }
            "date", "install" -> apps.sortedByDescending { it.installedTimestamp }
            else -> apps.sortedBy { it.label.lowercase() }
        }

        if (reverse) {
            apps = apps.reversed()
        }

        if (apps.isEmpty()) {
            emit(OutputLine("No applications found matching criteria.", OutputLineType.WARNING))
            return@flow
        }

        emit(OutputLine("Installed Applications (${apps.size}):", OutputLineType.HEADER))
        for (app in apps) {
            val favMarker = if (app.isFavorite) "★ " else "  "
            val workMarker = if (app.isWorkProfile) " [Work]" else ""
            val hiddenMarker = if (app.isHidden) " [Hidden]" else ""
            val countInfo = if (app.launchCount > 0) " (${app.launchCount}x)" else ""
            val text = "$favMarker${app.label}$workMarker$hiddenMarker$countInfo"
            emit(OutputLine(text = text, type = OutputLineType.APP_LINK, appPackage = app.packageName))
        }
    }
}

class OpenCommand(
    private val appRepository: AppRepository
) : Command {
    override val name = "open"
    override val aliases = listOf("launch", "run")
    override val description = "Launch an application by name or package"
    override val usage = "open <app_name | package_name>"
    override val category = CommandCategory.APPS

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        if (args.isEmpty()) {
            emit(OutputLine("Error: specify app name or package. Example: open Chrome", OutputLineType.ERROR))
            return@flow
        }

        val target = args.joinToString(" ")
        val app = appRepository.findApp(target)
        if (app != null) {
            val result = appRepository.launchApp(app.packageName, app.userHandle)
            if (result.isSuccess) {
                emit(OutputLine("Launching ${app.label}...", OutputLineType.SUCCESS))
            } else {
                emit(OutputLine("Failed to launch ${app.label}: ${result.exceptionOrNull()?.message}", OutputLineType.ERROR))
            }
        } else {
            emit(OutputLine("Application not found: '$target'. Try 'ls' to see installed apps.", OutputLineType.ERROR))
        }
    }
}

class InfoCommand(
    private val appRepository: AppRepository
) : Command {
    override val name = "info"
    override val description = "View application info or open system app settings"
    override val usage = "info <app_name>"
    override val category = CommandCategory.APPS

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        if (args.isEmpty()) {
            emit(OutputLine("Usage: info <app_name>", OutputLineType.WARNING))
            return@flow
        }
        val target = args.joinToString(" ")
        val app = appRepository.findApp(target)
        if (app != null) {
            appRepository.openAppInfo(app.packageName)
            emit(OutputLine("Opened settings for ${app.label} (${app.packageName})", OutputLineType.SUCCESS))
        } else {
            emit(OutputLine("App '$target' not found.", OutputLineType.ERROR))
        }
    }
}

class UninstallCommand(
    private val appRepository: AppRepository
) : Command {
    override val name = "uninstall"
    override val aliases = listOf("remove", "rmapp")
    override val description = "Uninstall an application"
    override val usage = "uninstall <app_name>"
    override val category = CommandCategory.APPS

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        if (args.isEmpty()) {
            emit(OutputLine("Usage: uninstall <app_name>", OutputLineType.WARNING))
            return@flow
        }
        val target = args.joinToString(" ")
        val app = appRepository.findApp(target)
        if (app != null) {
            appRepository.uninstallApp(app.packageName)
            emit(OutputLine("Prompting uninstall for ${app.label}...", OutputLineType.INFO))
        } else {
            emit(OutputLine("App '$target' not found.", OutputLineType.ERROR))
        }
    }
}

class StoreCommand(
    private val appRepository: AppRepository
) : Command {
    override val name = "store"
    override val aliases = listOf("playstore")
    override val description = "Open an app's Google Play Store page"
    override val usage = "store <app_name>"
    override val category = CommandCategory.APPS

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        if (args.isEmpty()) {
            val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.context.startActivity(storeIntent)
            emit(OutputLine("Opened Google Play Store.", OutputLineType.SUCCESS))
            return@flow
        }
        val target = args.joinToString(" ")
        val app = appRepository.findApp(target)
        if (app != null) {
            appRepository.openPlayStore(app.packageName)
            emit(OutputLine("Opening Play Store page for ${app.label}...", OutputLineType.INFO))
        } else {
            val searchIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=$target")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.context.startActivity(searchIntent)
                emit(OutputLine("Searching Play Store for '$target'...", OutputLineType.INFO))
            } catch (e: Exception) {
                emit(OutputLine("Error opening Play Store: ${e.message}", OutputLineType.ERROR))
            }
        }
    }
}

class FavCommand(
    private val appRepository: AppRepository
) : Command {
    override val name = "fav"
    override val aliases = listOf("favorite")
    override val description = "Manage favorite applications (add | rm | ls)"
    override val usage = "fav add <app> | fav rm <app> | fav ls"
    override val category = CommandCategory.APPS

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        if (args.isEmpty() || args[0].equals("ls", ignoreCase = true)) {
            val favs = appRepository.appsFlow.value.filter { it.isFavorite }
            if (favs.isEmpty()) {
                emit(OutputLine("No favorite apps set. Use 'fav add <app>' to add one.", OutputLineType.INFO))
            } else {
                emit(OutputLine("Favorite Applications (${favs.size}):", OutputLineType.HEADER))
                for (fav in favs) {
                    emit(OutputLine("★ ${fav.label} (${fav.packageName})", OutputLineType.APP_LINK, fav.packageName))
                }
            }
            return@flow
        }

        val subAction = args[0].lowercase()
        val appQuery = args.drop(1).joinToString(" ")
        if (appQuery.isBlank()) {
            emit(OutputLine("Please specify an app. Usage: fav $subAction <app_name>", OutputLineType.WARNING))
            return@flow
        }

        val app = appRepository.findApp(appQuery)
        if (app == null) {
            emit(OutputLine("App '$appQuery' not found.", OutputLineType.ERROR))
            return@flow
        }

        when (subAction) {
            "add" -> {
                appRepository.setFavorite(app.packageName, true)
                emit(OutputLine("Added '${app.label}' to favorites.", OutputLineType.SUCCESS))
            }
            "rm", "remove", "delete" -> {
                appRepository.setFavorite(app.packageName, false)
                emit(OutputLine("Removed '${app.label}' from favorites.", OutputLineType.SUCCESS))
            }
            else -> {
                emit(OutputLine("Unknown sub-command '$subAction'. Use 'add', 'rm', or 'ls'.", OutputLineType.ERROR))
            }
        }
    }
}

class HideCommand(
    private val appRepository: AppRepository
) : Command {
    override val name = "hide"
    override val description = "Hide an app from the default apps list and GUI drawer"
    override val usage = "hide <app_name>"
    override val category = CommandCategory.APPS

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        if (args.isEmpty()) {
            emit(OutputLine("Usage: hide <app_name>", OutputLineType.WARNING))
            return@flow
        }
        val appQuery = args.joinToString(" ")
        val app = appRepository.findApp(appQuery)
        if (app != null) {
            appRepository.setHidden(app.packageName, true)
            emit(OutputLine("App '${app.label}' is now hidden. (View with 'ls -h' or unhide with 'unhide ${app.label}')", OutputLineType.SUCCESS))
        } else {
            emit(OutputLine("App '$appQuery' not found.", OutputLineType.ERROR))
        }
    }
}

class UnhideCommand(
    private val appRepository: AppRepository
) : Command {
    override val name = "unhide"
    override val description = "Unhide a previously hidden application"
    override val usage = "unhide <app_name>"
    override val category = CommandCategory.APPS

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        if (args.isEmpty()) {
            emit(OutputLine("Usage: unhide <app_name>", OutputLineType.WARNING))
            return@flow
        }
        val appQuery = args.joinToString(" ")
        val app = appRepository.findApp(appQuery)
        if (app != null) {
            appRepository.setHidden(app.packageName, false)
            emit(OutputLine("App '${app.label}' is now visible.", OutputLineType.SUCCESS))
        } else {
            emit(OutputLine("App '$appQuery' not found.", OutputLineType.ERROR))
        }
    }
}

class RecentCommand(
    private val appRepository: AppRepository
) : Command {
    override val name = "recent"
    override val aliases = listOf("recents")
    override val description = "Show recently launched applications"
    override val usage = "recent [count]"
    override val category = CommandCategory.APPS

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        val count = args.firstOrNull()?.toIntOrNull() ?: 5
        val recents = appRepository.appsFlow.value
            .filter { it.lastLaunchedTimestamp > 0 }
            .sortedByDescending { it.lastLaunchedTimestamp }
            .take(count)

        if (recents.isEmpty()) {
            emit(OutputLine("No recently launched apps recorded yet.", OutputLineType.INFO))
        } else {
            emit(OutputLine("Recently Launched Apps:", OutputLineType.HEADER))
            for (app in recents) {
                emit(OutputLine("${app.label} (${app.packageName})", OutputLineType.APP_LINK, app.packageName))
            }
        }
    }
}

class TopCommand(
    private val appRepository: AppRepository
) : Command {
    override val name = "top"
    override val description = "Show top most frequently launched applications"
    override val usage = "top [count]"
    override val category = CommandCategory.APPS

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        val count = args.firstOrNull()?.toIntOrNull() ?: 5
        val tops = appRepository.appsFlow.value
            .filter { it.launchCount > 0 }
            .sortedByDescending { it.launchCount }
            .take(count)

        if (tops.isEmpty()) {
            emit(OutputLine("No app usage metrics recorded yet.", OutputLineType.INFO))
        } else {
            emit(OutputLine("Top Most Used Apps:", OutputLineType.HEADER))
            for ((index, app) in tops.withIndex()) {
                emit(OutputLine("${index + 1}. ${app.label} — ${app.launchCount} launches", OutputLineType.APP_LINK, app.packageName))
            }
        }
    }
}

class FindCommand(
    private val appRepository: AppRepository
) : Command {
    override val name = "find"
    override val aliases = listOf("grep")
    override val description = "Find apps or filter previous command output"
    override val usage = "find <query> | <command> | grep <pattern>"
    override val category = CommandCategory.APPS

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        val query = args.joinToString(" ").trim()
        if (query.isEmpty()) {
            emit(OutputLine("Usage: find <query> or pipe: ls | grep <query>", OutputLineType.WARNING))
            return@flow
        }

        // If stdin is present, act as grep filter
        if (context.stdin.isNotEmpty()) {
            val matches = context.stdin.filter { it.text.contains(query, ignoreCase = true) }
            if (matches.isEmpty()) {
                emit(OutputLine("No lines matched '$query'.", OutputLineType.INFO))
            } else {
                for (line in matches) {
                    emit(line)
                }
            }
            return@flow
        }

        // Otherwise find apps
        val apps = appRepository.appsFlow.value
            .filter { it.label.contains(query, ignoreCase = true) || it.packageName.contains(query, ignoreCase = true) }

        if (apps.isEmpty()) {
            emit(OutputLine("No apps found matching '$query'.", OutputLineType.INFO))
        } else {
            emit(OutputLine("Search results for '$query':", OutputLineType.HEADER))
            for (app in apps) {
                emit(OutputLine("• ${app.label} (${app.packageName})", OutputLineType.APP_LINK, app.packageName))
            }
        }
    }
}

class SearchCommand(
    private val settingsRepository: SettingsRepository
) : Command {
    override val name = "search"
    override val aliases = listOf("google", "web")
    override val description = "Perform a web search using configured engine"
    override val usage = "search <query>"
    override val category = CommandCategory.APPS

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        if (args.isEmpty()) {
            emit(OutputLine("Usage: search <query>", OutputLineType.WARNING))
            return@flow
        }
        val query = args.joinToString(" ")
        val prefs = settingsRepository.preferencesFlow.first()
        val url = when (prefs.searchEngine.lowercase()) {
            "duckduckgo", "ddg" -> "https://duckduckgo.com/?q=${Uri.encode(query)}"
            "bing" -> "https://www.bing.com/search?q=${Uri.encode(query)}"
            "ecosia" -> "https://www.ecosia.org/search?q=${Uri.encode(query)}"
            else -> "https://www.google.com/search?q=${Uri.encode(query)}"
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.context.startActivity(intent)
            emit(OutputLine("Searching ${prefs.searchEngine} for '$query'...", OutputLineType.SUCCESS))
        } catch (e: Exception) {
            emit(OutputLine("Failed to open browser: ${e.message}", OutputLineType.ERROR))
        }
    }
}

class ShortcutsCommand(
    private val appRepository: AppRepository
) : Command {
    override val name = "shortcuts"
    override val description = "List dynamic and manifest shortcuts for an app"
    override val usage = "shortcuts <app_name>"
    override val category = CommandCategory.APPS

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        if (args.isEmpty()) {
            emit(OutputLine("Usage: shortcuts <app_name>", OutputLineType.WARNING))
            return@flow
        }
        val target = args.joinToString(" ")
        val app = appRepository.findApp(target)
        if (app != null) {
            val list = appRepository.getAppShortcuts(app.packageName)
            if (list.isEmpty()) {
                emit(OutputLine("No shortcuts available for ${app.label}.", OutputLineType.INFO))
            } else {
                emit(OutputLine("Shortcuts for ${app.label}:", OutputLineType.HEADER))
                for (sc in list) {
                    emit(OutputLine("⚡ $sc", OutputLineType.NORMAL))
                }
            }
        } else {
            emit(OutputLine("App '$target' not found.", OutputLineType.ERROR))
        }
    }
}
