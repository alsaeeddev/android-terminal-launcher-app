package com.example.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.OutputLine
import com.example.domain.model.OutputLineType
import com.example.ui.theme.LocalTerminalColors
import com.example.ui.theme.MonospaceFontFamily
import kotlinx.coroutines.delay

@Composable
fun TerminalHomeScreen(
    viewModel: TerminalViewModel,
    onNavigateToDrawer: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val terminalColors = LocalTerminalColors.current
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }

    // Auto scroll on new output
    LaunchedEffect(state.lines.size) {
        if (state.lines.isNotEmpty()) {
            listState.animateScrollToItem(state.lines.size - 1)
        }
    }

    // Auto-focus terminal input
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = state.wallpaperDim))
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        viewModel.onDoubleTap()
                    },
                    onTap = {
                        focusRequester.requestFocus()
                    }
                )
            }
    ) {
        // Optional CRT Scanline effect
        if (state.crtScanlines) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val step = 4.dp.toPx()
                var y = 0f
                while (y < size.height) {
                    drawLine(
                        color = Color.Black.copy(alpha = 0.18f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                    y += step
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Live Status Header Bar
            TerminalStatusBar(
                username = state.username,
                hostname = state.hostname,
                liveStatus = state.liveStatus,
                onDrawerClick = onNavigateToDrawer,
                onSettingsClick = onNavigateToSettings
            )

            // Main Terminal Output
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                itemsIndexed(
                    items = state.lines,
                    key = { index, line -> "${line.timestamp}_$index" }
                ) { _, line ->
                    TerminalLineItem(
                        line = line,
                        fontSize = state.fontSize,
                        onAppClick = { pkg -> viewModel.onAppClick(pkg) }
                    )
                }
            }

            // Suggestions bar
            if (state.suggestions.isNotEmpty()) {
                SuggestionRow(
                    suggestions = state.suggestions,
                    onSuggestionClick = { item -> viewModel.onSuggestionClick(item, context) }
                )
            }

            // Interactive Command Prompt Input
            CommandLineInputRow(
                prompt = state.prompt,
                input = state.input,
                fontSize = state.fontSize,
                focusRequester = focusRequester,
                onInputChange = { viewModel.onInputChange(it) },
                onExecute = { viewModel.onExecute(context) }
            )

            // Extra Terminal Keys Bar (Tab, arrows, shortcuts)
            ExtraTerminalKeyBar(
                onTab = { viewModel.onTabPress() },
                onUp = { viewModel.onHistoryUp() },
                onDown = { viewModel.onHistoryDown() },
                onKey = { viewModel.onAppendExtraKey(it) },
                onExecute = { viewModel.onExecute(context) }
            )
        }
    }
}

@Composable
fun TerminalStatusBar(
    username: String,
    hostname: String,
    liveStatus: LiveStatusInfo,
    onDrawerClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val colors = LocalTerminalColors.current
    Surface(
        color = colors.surface.copy(alpha = 0.85f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$username@$hostname",
                    color = colors.promptHost,
                    fontFamily = MonospaceFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                if (liveStatus.time.isNotEmpty()) {
                    Text(
                        text = " • ${liveStatus.time}",
                        color = colors.secondaryText,
                        fontFamily = MonospaceFontFamily,
                        fontSize = 12.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onDrawerClick,
                    modifier = Modifier.size(36.dp).testTag("drawer_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = "App Drawer",
                        tint = colors.accent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.size(36.dp).testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = colors.secondaryText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TerminalLineItem(
    line: OutputLine,
    fontSize: Float,
    onAppClick: (String) -> Unit
) {
    val colors = LocalTerminalColors.current

    val textColor = when (line.type) {
        OutputLineType.COMMAND_ECHO -> colors.commandText
        OutputLineType.SUCCESS -> colors.successText
        OutputLineType.ERROR -> colors.errorText
        OutputLineType.WARNING -> colors.warningText
        OutputLineType.INFO -> colors.infoText
        OutputLineType.HEADER -> colors.accent
        OutputLineType.APP_LINK -> colors.appLinkText
        OutputLineType.ASCII_ART -> colors.primaryText
        OutputLineType.NORMAL -> colors.primaryText
    }

    val isClickable = line.appPackage != null

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .then(
                if (isClickable) {
                    Modifier.clickable { onAppClick(line.appPackage!!) }
                } else Modifier
            )
    ) {
        Text(
            text = line.text,
            color = textColor,
            fontFamily = MonospaceFontFamily,
            fontSize = fontSize.sp,
            lineHeight = (fontSize * 1.35f).sp,
            textDecoration = if (isClickable) TextDecoration.Underline else TextDecoration.None
        )
    }
}

@Composable
fun SuggestionRow(
    suggestions: List<SuggestionItem>,
    onSuggestionClick: (SuggestionItem) -> Unit
) {
    val colors = LocalTerminalColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (item in suggestions) {
            Surface(
                color = colors.suggestionBg,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.clickable { onSuggestionClick(item) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.text,
                        color = colors.suggestionText,
                        fontFamily = MonospaceFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.badge,
                        color = colors.accent,
                        fontFamily = MonospaceFontFamily,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CommandLineInputRow(
    prompt: String,
    input: String,
    fontSize: Float,
    focusRequester: FocusRequester,
    onInputChange: (String) -> Unit,
    onExecute: () -> Unit
) {
    val colors = LocalTerminalColors.current
    var cursorVisible by remember { mutableStateOf(true) }

    // Blinking cursor
    LaunchedEffect(Unit) {
        while (true) {
            cursorVisible = !cursorVisible
            delay(500)
        }
    }

    Surface(
        color = colors.surface.copy(alpha = 0.95f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = prompt,
                color = colors.promptUser,
                fontFamily = MonospaceFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize.sp
            )

            Box(modifier = Modifier.weight(1f)) {
                BasicTextField(
                    value = input,
                    onValueChange = onInputChange,
                    textStyle = TextStyle(
                        color = colors.primaryText,
                        fontFamily = MonospaceFontFamily,
                        fontSize = fontSize.sp
                    ),
                    cursorBrush = SolidColor(if (cursorVisible) colors.cursorColor else Color.Transparent),
                    keyboardOptions = KeyboardOptions(
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Go
                    ),
                    keyboardActions = KeyboardActions(
                        onGo = { onExecute() }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .testTag("terminal_input")
                )

                if (input.isEmpty()) {
                    Text(
                        text = "type app",
                        color = colors.secondaryText.copy(alpha = 0.6f),
                        fontFamily = MonospaceFontFamily,
                        fontSize = fontSize.sp
                    )
                }
            }

            IconButton(
                onClick = onExecute,
                modifier = Modifier.size(36.dp).testTag("execute_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Execute",
                    tint = colors.accent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ExtraTerminalKeyBar(
    onTab: () -> Unit,
    onUp: () -> Unit,
    onDown: () -> Unit,
    onKey: (String) -> Unit,
    onExecute: () -> Unit
) {
    val colors = LocalTerminalColors.current
    val keys = listOf("Tab", "↑", "↓", "/", "~", "|", "-", "&&")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (key in keys) {
            Surface(
                color = colors.surface,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.clickable {
                    when (key) {
                        "Tab" -> onTab()
                        "↑" -> onUp()
                        "↓" -> onDown()
                        "&&" -> onKey(" && ")
                        "|" -> onKey(" | ")
                        else -> onKey(key)
                    }
                }
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = key,
                        color = colors.primaryText,
                        fontFamily = MonospaceFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
