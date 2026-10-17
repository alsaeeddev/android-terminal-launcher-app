package com.example.di

import android.content.Context
import com.example.core.common.DefaultDispatcherProvider
import com.example.core.common.DispatcherProvider
import com.example.data.local.datastore.SettingsDataStore
import com.example.data.local.db.AppDatabase
import com.example.data.repository.AppRepositoryImpl
import com.example.data.repository.ProductivityRepositoryImpl
import com.example.data.repository.SettingsRepositoryImpl
import com.example.data.system.LauncherAppsManager
import com.example.data.system.SystemActionController
import com.example.domain.command.CommandRegistry
import com.example.domain.command.impl.*
import com.example.domain.repository.AppRepository
import com.example.domain.repository.ProductivityRepository
import com.example.domain.repository.SettingsRepository
import com.example.domain.usecase.ExecuteCommandUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class AppContainer(private val applicationContext: Context) {

    val dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider()
    val appScope = CoroutineScope(SupervisorJob() + dispatcherProvider.default)

    val appDatabase: AppDatabase by lazy {
        AppDatabase.getInstance(applicationContext)
    }

    val settingsDataStore: SettingsDataStore by lazy {
        SettingsDataStore(applicationContext)
    }

    val launcherAppsManager: LauncherAppsManager by lazy {
        LauncherAppsManager(applicationContext)
    }

    val systemActionController: SystemActionController by lazy {
        SystemActionController(applicationContext)
    }

    val appRepository: AppRepository by lazy {
        AppRepositoryImpl(
            launcherAppsManager = launcherAppsManager,
            appDao = appDatabase.appDao(),
            dispatcherProvider = dispatcherProvider,
            externalScope = appScope
        )
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(
            dataStore = settingsDataStore,
            dispatcherProvider = dispatcherProvider
        )
    }

    val productivityRepository: ProductivityRepository by lazy {
        ProductivityRepositoryImpl(
            noteDao = appDatabase.noteDao(),
            todoDao = appDatabase.todoDao(),
            aliasDao = appDatabase.aliasDao(),
            historyDao = appDatabase.historyDao(),
            dispatcherProvider = dispatcherProvider
        )
    }

    val commandRegistry: CommandRegistry by lazy {
        val registry = CommandRegistry()

        // App commands
        registry.register(LsCommand(appRepository))
        registry.register(OpenCommand(appRepository))
        registry.register(InfoCommand(appRepository))
        registry.register(UninstallCommand(appRepository))
        registry.register(StoreCommand(appRepository))
        registry.register(FavCommand(appRepository))
        registry.register(HideCommand(appRepository))
        registry.register(UnhideCommand(appRepository))
        registry.register(RecentCommand(appRepository))
        registry.register(TopCommand(appRepository))
        registry.register(FindCommand(appRepository))
        registry.register(SearchCommand(settingsRepository))
        registry.register(ShortcutsCommand(appRepository))

        // System commands
        registry.register(BatteryCommand())
        registry.register(TimeCommand())
        registry.register(DateCommand())
        registry.register(UptimeCommand())
        registry.register(DeviceCommand())
        registry.register(StorageCommand())
        registry.register(RamCommand())
        registry.register(NetworkCommand())
        registry.register(TorchCommand(systemActionController))
        registry.register(VolumeCommand(systemActionController))
        registry.register(LockCommand(systemActionController))
        registry.register(SettingsCommand(systemActionController))

        // Productivity commands
        registry.register(CalcCommand())
        registry.register(NoteCommand(productivityRepository))
        registry.register(TodoCommand(productivityRepository))
        registry.register(CalCommand())
        registry.register(WeatherCommand())
        registry.register(CallCommand())
        registry.register(SmsCommand())
        registry.register(AlarmCommand())
        registry.register(EventsCommand())
        registry.register(NetCheckCommand())
        registry.register(ClipCommand())
        registry.register(EchoCommand())
        registry.register(CatCommand(productivityRepository))

        // Launcher control commands
        registry.register(ThemeCommand(settingsRepository))
        registry.register(FontCommand(settingsRepository))
        registry.register(ModeCommand(settingsRepository))
        registry.register(WallpaperCommand(settingsRepository))
        registry.register(AliasCommand(productivityRepository))
        registry.register(UnaliasCommand(productivityRepository))
        registry.register(HistoryCommand(productivityRepository))
        registry.register(ClearCommand())
        registry.register(HelpCommand { commandRegistry })
        registry.register(AboutCommand())
        registry.register(ConfigCommand(settingsRepository))

        // Fun commands
        registry.register(SudoCommand())
        registry.register(CowsayCommand())
        registry.register(FortuneCommand())
        registry.register(MatrixCommand())

        registry
    }

    val executeCommandUseCase: ExecuteCommandUseCase by lazy {
        ExecuteCommandUseCase(
            commandRegistry = commandRegistry,
            appRepository = appRepository,
            productivityRepository = productivityRepository,
            dispatcherProvider = dispatcherProvider
        )
    }
}
