package com.example.domain.command.impl

import com.example.core.util.DateTimeUtil
import com.example.core.util.DeviceInfoHelper
import com.example.data.system.SystemActionController
import com.example.domain.command.Command
import com.example.domain.command.CommandCategory
import com.example.domain.command.CommandContext
import com.example.domain.model.OutputLine
import com.example.domain.model.OutputLineType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BatteryCommand : Command {
    override val name = "battery"
    override val aliases = listOf("batt", "power")
    override val description = "Show current battery level and charging status"
    override val usage = "battery"
    override val category = CommandCategory.SYSTEM

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        val status = DeviceInfoHelper.getBatteryStatus(context.context)
        val icon = if (status.isCharging) "⚡" else "🔋"
        emit(OutputLine("$icon Battery Level: ${status.percentage}% [${status.statusString}]", OutputLineType.INFO))
    }
}

class TimeCommand : Command {
    override val name = "time"
    override val description = "Show current system time"
    override val usage = "time"
    override val category = CommandCategory.SYSTEM

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        emit(OutputLine("🕒 ${DateTimeUtil.formatCurrentTime()}", OutputLineType.INFO))
    }
}

class DateCommand : Command {
    override val name = "date"
    override val description = "Show current system date"
    override val usage = "date"
    override val category = CommandCategory.SYSTEM

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        emit(OutputLine("📅 ${DateTimeUtil.formatCurrentDate()}", OutputLineType.INFO))
    }
}

class UptimeCommand : Command {
    override val name = "uptime"
    override val description = "Show device uptime since last boot"
    override val usage = "uptime"
    override val category = CommandCategory.SYSTEM

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        emit(OutputLine("⏱ Uptime: ${DeviceInfoHelper.getUptimeString()}", OutputLineType.INFO))
    }
}

class DeviceCommand : Command {
    override val name = "device"
    override val aliases = listOf("neofetch", "sysinfo", "fetch")
    override val description = "Display system overview and Neofetch ASCII banner"
    override val usage = "device | neofetch"
    override val category = CommandCategory.SYSTEM

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        val banner = DeviceInfoHelper.getNeofetchHeader(context.context)
        for (line in banner) {
            emit(OutputLine(line, OutputLineType.ASCII_ART))
        }
    }
}

class StorageCommand : Command {
    override val name = "storage"
    override val aliases = listOf("df", "disk")
    override val description = "Show internal storage capacity and usage"
    override val usage = "storage"
    override val category = CommandCategory.SYSTEM

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        val (avail, total) = DeviceInfoHelper.getStorageInfo()
        val used = total - avail
        val pct = if (total > 0) ((used * 100) / total).toInt() else 0
        emit(OutputLine("💾 Storage Usage: $pct%", OutputLineType.HEADER))
        emit(OutputLine("Used: ${DeviceInfoHelper.formatBytes(used)} / Total: ${DeviceInfoHelper.formatBytes(total)} (${DeviceInfoHelper.formatBytes(avail)} free)", OutputLineType.INFO))
    }
}

class RamCommand : Command {
    override val name = "ram"
    override val aliases = listOf("mem", "free")
    override val description = "Show current system RAM statistics"
    override val usage = "ram"
    override val category = CommandCategory.SYSTEM

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        val (avail, total) = DeviceInfoHelper.getRamInfo(context.context)
        val used = total - avail
        val pct = if (total > 0) ((used * 100) / total).toInt() else 0
        emit(OutputLine("🧠 Memory (RAM): $pct% utilized", OutputLineType.HEADER))
        emit(OutputLine("Used: ${DeviceInfoHelper.formatBytes(used)} / Total: ${DeviceInfoHelper.formatBytes(total)} (${DeviceInfoHelper.formatBytes(avail)} free)", OutputLineType.INFO))
    }
}

class NetworkCommand : Command {
    override val name = "network"
    override val aliases = listOf("wifi", "netstat")
    override val description = "Show network status and connection type"
    override val usage = "network"
    override val category = CommandCategory.SYSTEM

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        val net = DeviceInfoHelper.getNetworkType(context.context)
        emit(OutputLine("📶 Network: $net", OutputLineType.INFO))
    }
}

class TorchCommand(
    private val systemActionController: SystemActionController
) : Command {
    override val name = "torch"
    override val aliases = listOf("flashlight")
    override val description = "Control device flashlight (torch on | torch off)"
    override val usage = "torch on | torch off"
    override val category = CommandCategory.SYSTEM

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        val action = args.firstOrNull()?.lowercase() ?: "on"
        val enable = action != "off" && action != "0"
        val result = systemActionController.setTorch(enable)
        if (result.isSuccess) {
            emit(OutputLine("🔦 ${result.getOrNull()}", OutputLineType.SUCCESS))
        } else {
            emit(OutputLine("Torch error: ${result.exceptionOrNull()?.message}", OutputLineType.ERROR))
        }
    }
}

class VolumeCommand(
    private val systemActionController: SystemActionController
) : Command {
    override val name = "volume"
    override val aliases = listOf("vol")
    override val description = "View or adjust system audio volume"
    override val usage = "volume [media|ring|alarm] [0-100]"
    override val category = CommandCategory.SYSTEM

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        if (args.isEmpty()) {
            emit(OutputLine("🔊 ${systemActionController.getVolumeSummary()}", OutputLineType.INFO))
            return@flow
        }
        val stream = if (args.size >= 2) args[0] else "media"
        val levelStr = if (args.size >= 2) args[1] else args[0]
        val level = levelStr.replace("%", "").toIntOrNull()
        if (level == null) {
            emit(OutputLine("Usage: volume [media|ring|alarm] [0-100]", OutputLineType.WARNING))
            return@flow
        }
        val result = systemActionController.setVolume(stream, level)
        if (result.isSuccess) {
            emit(OutputLine("🔊 ${result.getOrNull()}", OutputLineType.SUCCESS))
        } else {
            emit(OutputLine("Volume error: ${result.exceptionOrNull()?.message}", OutputLineType.ERROR))
        }
    }
}

class LockCommand(
    private val systemActionController: SystemActionController
) : Command {
    override val name = "lock"
    override val description = "Lock device screen using Accessibility Service"
    override val usage = "lock"
    override val category = CommandCategory.SYSTEM

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        val result = systemActionController.lockScreen()
        if (result.isSuccess) {
            emit(OutputLine("🔒 ${result.getOrNull()}", OutputLineType.SUCCESS))
        } else {
            emit(OutputLine("Lock error: ${result.exceptionOrNull()?.message}", OutputLineType.ERROR))
            systemActionController.openAccessibilitySettings()
        }
    }
}

class SettingsCommand(
    private val systemActionController: SystemActionController
) : Command {
    override val name = "settings"
    override val aliases = listOf("sysset", "bt", "dnd", "brightness")
    override val description = "Open Android Settings or specific section (wifi, bt, display, sound, apps, dnd)"
    override val usage = "settings [wifi|bt|display|sound|apps|battery|storage|dnd|loc]"
    override val category = CommandCategory.SYSTEM

    override suspend fun execute(
        context: CommandContext,
        args: List<String>,
        flags: Map<String, String?>
    ): Flow<OutputLine> = flow {
        val section = args.firstOrNull() ?: "all"
        val result = systemActionController.openSettingsSection(section)
        if (result.isSuccess) {
            emit(OutputLine("⚙ ${result.getOrNull()}", OutputLineType.SUCCESS))
        } else {
            emit(OutputLine("Error opening settings: ${result.exceptionOrNull()?.message}", OutputLineType.ERROR))
        }
    }
}
