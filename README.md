# Shell Home — Production Android CLI Launcher

**Shell Home** is a command-line terminal launcher for Android devices. Designed for power users, developers, sysadmins, and digital minimalists, it transforms your Android desktop into a productive UNIX-like shell while offering a modern Material 3 graphical drawer fallback.

---

## 🚀 Key Features

- **Terminal-First Launcher**: Direct command execution from the home screen (`open chrome`, `calc 2*(3+4)^2`, `note add Buy milk`, `theme set matrix_green`).
- **Direct App Launching**: Type any app name or partial name directly at the prompt (e.g. `youtube`, `camera`, `files`) without typing `open`.
- **Advanced Command Engine**:
  - Full quote parsing (`"..."`, `'...'`) and backslash escapes (`\ `).
  - Flags support: short (`-a`, `-f`), combined (`-af`), long (`--sort=usage`).
  - Command chaining (`&&`, `;`) and output piping (`|`).
  - Fuzzy typo correction (*"Did you mean: battery?"*).
  - Persistent command history (accessible via `↑` and `↓` buttons or history command).
  - Tab autocomplete for commands, apps, and aliases.
  - Interactive extra key row: `[Tab]`, `[↑]`, `[↓]`, `[/]`, `[~]`, `[|]`, `[-]`, `[&&]`.
- **Neofetch & Live Status Line**:
  - Live status line: time, battery percentage, charging indicator, network connectivity type.
  - Neofetch header: ASCII Android art, CPU/RAM stats, storage capacity, kernel, uptime.
- **Material 3 GUI Fallback Drawer**:
  - Searchable app drawer with fast fuzzy filtering.
  - Grid / List view mode toggle.
  - Pinned Favorites row.
  - Long-press menu: App Info, Play Store, Uninstall, Custom Label Rename, Hide App, and Dynamic Shortcuts.
  - Notification badge counts via `NotificationListenerService`.
- **Theme & Appearance Engine**:
  - 9 Built-in color palettes: Dracula, Solarized Dark, Gruvbox, Nord, Monokai, Matrix Green, Amber CRT, Classic Light, and Material You (Android 12+ dynamic colors).
  - Retro CRT scanline shader overlay.
  - Live wallpaper background with adjustable dimming (0.0 to 1.0).
  - Scalable monospace typography.
- **Extensive Command Ecosystem**:
  - 40+ built-in commands covering Apps, System Hardware, Productivity, Launcher Customization, and Fun easter eggs.

---

## 🏛 Clean Architecture & Tech Stack

```
com.example / com.alsaeeddev.shellhome
├── core/
│   ├── common/           # DispatcherProvider, threading utilities
│   ├── ui/theme/         # TerminalTheme, Material 3, Monospace Typography
│   └── util/             # CalculatorParser, FuzzyMatcher, DeviceInfoHelper, DateTimeUtil
├── data/
│   ├── local/
│   │   ├── db/           # Room Database (Entities: AppUsage, FavoriteApp, HiddenApp,
│   │   │                 #   AppLabelOverride, CommandAlias, CommandHistory, Note, Todo)
│   │   └── datastore/    # Jetpack Preferences DataStore (Typed reactive settings)
│   ├── system/           # LauncherAppsManager, SystemActionController
│   └── repository/       # AppRepositoryImpl, SettingsRepositoryImpl, ProductivityRepositoryImpl
├── domain/
│   ├── model/            # AppItem, OutputLine, AliasItem, NoteItem, TodoItem
│   ├── repository/       # AppRepository, SettingsRepository, ProductivityRepository interfaces
│   ├── command/          # Command interface, CommandParser, CommandRegistry
│   └── usecase/          # ExecuteCommandUseCase
├── presentation/
│   ├── home/             # TerminalHomeScreen, TerminalViewModel, TerminalStatusBar
│   ├── drawer/           # GuiDrawerScreen, GuiDrawerViewModel
│   ├── settings/         # SettingsScreen, SettingsViewModel
│   ├── onboarding/       # OnboardingScreen (Intro, Default Launcher, Permissions, Theme)
│   └── navigation/       # Type-safe routes & backstack management
└── service/              # LockAccessibilityService, ShellNotificationListenerService
```

- **Runtime**: Kotlin 2.2+, Jetpack Compose, Material 3
- **Database**: Room 2.7+ with KSP (Kotlin Symbol Processing)
- **Settings**: Jetpack Preferences DataStore
- **Concurrency**: Kotlin Coroutines + Flow (`StateFlow` with `SharingStarted.WhileSubscribed(5000)`)
- **Memory Safety**: Off-main-thread IO, in-memory app list cache, incremental `LauncherApps.Callback` observation, 16 KB page-size compatible.

---

## 📋 Command Reference Table

| Category | Command | Syntax | Description |
|---|---|---|---|
| **Apps** | `ls` / `apps` | `ls [-a] [-f] [-r] [-h] [-s <name\|usage>]` | List installed applications with flags |
| **Apps** | `open` / `launch` | `open <app>` | Launch an application by label or package |
| **Apps** | `info` | `info <app>` | Open system settings page for the app |
| **Apps** | `uninstall` | `uninstall <app>` | Request app uninstallation |
| **Apps** | `store` | `store [app]` | Open Google Play Store page |
| **Apps** | `fav` | `fav add\|rm\|ls <app>` | Add, remove, or list favorite apps |
| **Apps** | `hide` | `hide <app>` | Hide app from drawer and lists |
| **Apps** | `unhide` | `unhide <app>` | Unhide a previously hidden app |
| **Apps** | `recent` | `recent [count]` | Show recently launched apps |
| **Apps** | `top` | `top [count]` | Show most frequently used apps |
| **Apps** | `find` / `grep` | `find <query>` or `ls \| grep <query>` | Filter apps or stream output |
| **Apps** | `search` | `search <query>` | Perform web search using configured engine |
| **Apps** | `shortcuts` | `shortcuts <app>` | List dynamic shortcuts for an app |
| **System** | `battery` | `battery` | Battery level, health, and charging status |
| **System** | `time` / `date` | `time` / `date` | Current formatted date and time |
| **System** | `uptime` | `uptime` | System uptime since last boot |
| **System** | `device` / `neofetch`| `device` | Neofetch ASCII banner and hardware specs |
| **System** | `storage` / `df` | `storage` | Disk capacity, used, and free space |
| **System** | `ram` / `free` | `ram` | RAM utilization and available memory |
| **System** | `network` / `wifi` | `network` | Network connectivity and transport type |
| **System** | `torch` | `torch on\|off` | Toggle device flashlight |
| **System** | `volume` | `volume [stream] [0-100]` | Check or set audio stream volumes |
| **System** | `lock` | `lock` | Lock screen via Accessibility Service |
| **System** | `settings` | `settings [section]` | Open system settings or sections (wifi, bt, dnd) |
| **Productivity** | `calc` | `calc <expr>` | Safe recursive-descent math parser (`+`, `*`, `^`, `sqrt`, `sin`, `pi`) |
| **Productivity** | `note` | `note add <t>:<c> \| ls \| rm <id>` | Quick on-device scratchpad notes |
| **Productivity** | `todo` | `todo add <task> \| ls \| done <id> \| rm <id>` | Task list manager |
| **Productivity** | `cal` | `cal` | UNIX ASCII monthly calendar |
| **Productivity** | `weather` | `weather [city]` | Real-time weather forecast (Open-Meteo, offline-safe) |
| **Productivity** | `call` / `sms` | `call <num>` / `sms <num> [msg]` | Dial phone or compose SMS |
| **Productivity** | `alarm` / `timer` | `alarm [minutes]` | Set countdown timer or alarm |
| **Productivity** | `events` | `events` | Open calendar events |
| **Productivity** | `net` | `net check` | Ping connection test |
| **Productivity** | `clip` | `clip [text]` | Read or write to system clipboard |
| **Productivity** | `echo` | `echo <text>` | Print text or pass through pipeline |
| **Productivity** | `cat` | `cat <note_id>` | Display note content or pipeline stdin |
| **Launcher** | `theme` | `theme ls \| set <name>` | Switch color schemes live |
| **Launcher** | `font` | `font size <sp>` | Adjust terminal text size |
| **Launcher** | `mode` | `mode term \| gui` | Switch default home view |
| **Launcher** | `wallpaper` | `wallpaper dim <0.0-1.0>` | Control wallpaper dimming level |
| **Launcher** | `alias` | `alias <name>=<cmd>` | Create command shortcuts |
| **Launcher** | `unalias` | `unalias <name>` | Delete custom alias |
| **Launcher** | `history` | `history [-c]` | View or clear command history |
| **Launcher** | `clear` | `clear` | Clear terminal screen buffer |
| **Launcher** | `help` / `man` | `help [cmd]` | Command documentation manual |
| **Launcher** | `about` | `about` | Version, architecture, and credits |
| **Fun** | `sudo` | `sudo <cmd>` | Superuser lecture easter egg |
| **Fun** | `cowsay` | `cowsay [msg]` | ASCII talking cow |
| **Fun** | `fortune` | `fortune` | Developer and UNIX fortune quotes |
| **Fun** | `matrix` | `matrix` | Green digital rain animation |

---

## 🔐 Permissions & Privacy

Shell Home collects **zero telemetry, no advertising IDs, and does not send data anywhere**. All features run locally on-device.

| Permission | Purpose | Requested |
|---|---|---|
| `QUERY_ALL_PACKAGES` | Standard for home launchers to query launchable apps | Install-time (Manifest) |
| `EXPAND_STATUS_BAR` | Swipe down gesture to open notifications panel | Install-time |
| `INTERNET` | Optional weather queries (`weather`) and `net check` | Install-time |
| `ACCESS_NETWORK_STATE` | Live network indicator (Wi-Fi vs Cellular) | Install-time |
| `VIBRATE` | Haptic feedback for gestures | Install-time |
| `SET_WALLPAPER` | System wallpaper background integration | Install-time |
| `CAMERA` | Flashlight / torch control (`torch on`) | Runtime / lazy |
| `CALL_PHONE` / `SEND_SMS` | `call` and `sms` commands | Handled via system intent |
| `BIND_ACCESSIBILITY_SERVICE` | Double-tap to lock screen gesture (API 28+) | User-enabled in Accessibility Settings |
| `BIND_NOTIFICATION_LISTENER_SERVICE` | Notification badges on app icons | User-enabled in Notification Settings |

---

## 📱 Android Compatibility Matrix (Android 8 to Android 17)

Every platform feature is guarded with `Build.VERSION.SDK_INT` checks with working fallbacks across all versions:

- **Android 17 / 16 (API 36-37)**: Built with edge-to-edge support, 16 KB page-size compatibility, strict sandboxing.
- **Android 15 (API 35)**: Native edge-to-edge insets enforcement (`WindowInsets.statusBarsPadding()`, `navigationBarsPadding()`, `imePadding()`).
- **Android 14 (API 34)**: Predictive back gesture navigation (`BackHandler` cleanly popping custom stack to home).
- **Android 13 (API 33)**: Themed adaptive icon with `<monochrome>` layer support; granular media & notification permissions.
- **Android 12 (API 31-32)**: Material You dynamic theming (`dynamicDarkColorScheme` / `dynamicLightColorScheme`).
- **Android 10 (API 29)**: `RoleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)` for setting default launcher; fallback to `Settings.ACTION_HOME_SETTINGS` on Android 8 & 9.
- **Android 9 (API 28)**: `GLOBAL_ACTION_LOCK_SCREEN` for lock gesture via AccessibilityService.
- **Android 8.0 & 8.1 (API 26-27, minSdk)**: Full support for `LauncherApps`, multi-user / Work Profiles, Room database, and DataStore.
