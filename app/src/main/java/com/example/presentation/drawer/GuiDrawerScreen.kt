package com.example.presentation.drawer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AppItem
import com.example.ui.theme.LocalTerminalColors
import com.example.ui.theme.MonospaceFontFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GuiDrawerScreen(
    viewModel: GuiDrawerViewModel,
    onBackToTerminal: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val apps by viewModel.apps.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val colors = LocalTerminalColors.current
    val coroutineScope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()
    val listState = rememberLazyListState()

    BackHandler {
        onBackToTerminal()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search & Controls Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                /*IconButton(
                    onClick = onBackToTerminal,
                    modifier = Modifier.testTag("back_to_term_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Terminal",
                        tint = colors.primaryText
                    )
                }*/

                val interactionSource = remember { MutableInteractionSource() }
                val isFocused by interactionSource.collectIsFocusedAsState()
                val focusManager = LocalFocusManager.current

                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    interactionSource = interactionSource,
                    shape = RoundedCornerShape(16.dp),
                    textStyle = TextStyle(
                        fontFamily = MonospaceFontFamily,
                        fontSize = 14.sp,
                        color = colors.primaryText
                    ),
                    placeholder = {
                        Text(
                            text = "Search apps...",
                            color = colors.secondaryText.copy(alpha = 0.7f),
                            fontFamily = MonospaceFontFamily,
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = if (isFocused) colors.accent else colors.secondaryText
                        )
                    },
                    trailingIcon = {
                        AnimatedVisibility(
                            visible = uiState.searchQuery.isNotEmpty(),
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            IconButton(
                                onClick = { viewModel.onSearchQueryChange("") },
                                modifier = Modifier.testTag("drawer_search_clear")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = colors.secondaryText
                                )
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { focusManager.clearFocus() }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.secondaryText.copy(alpha = 0.25f),
                        focusedContainerColor = colors.accent.copy(alpha = 0.06f),
                        unfocusedContainerColor = colors.secondaryText.copy(alpha = 0.06f),
                        cursorColor = colors.accent,
                        focusedTextColor = colors.primaryText,
                        unfocusedTextColor = colors.primaryText
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .testTag("drawer_search_input")
                )
                IconButton(
                    onClick = { viewModel.toggleViewMode() },
                    modifier = Modifier.testTag("toggle_view_button")
                ) {
                    Icon(
                        imageVector = if (uiState.isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                        contentDescription = "Toggle Grid/List",
                        tint = colors.primaryText
                    )
                }
            }

            // Favorites Row (if any)
            if (favorites.isNotEmpty() && uiState.searchQuery.isEmpty()) {
                Text(
                    text = "FAVORITES",
                    color = colors.accent,
                    fontFamily = MonospaceFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    for (fav in favorites) {
                        FavoriteAppItem(
                            app = fav,
                            onClick = { viewModel.onAppClick(fav) },
                            onLongClick = { viewModel.onAppLongClick(fav) }
                        )
                    }
                }
            }

            Text(
                text = "ALL APPS (${apps.size})",
                color = colors.secondaryText,
                fontFamily = MonospaceFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Main Apps Grid or List
            if (uiState.isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 76.dp),
                    state = gridState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(apps, key = { it.uniqueKey }) { app ->
                        val badge = uiState.badgeCounts[app.packageName] ?: 0
                        GridAppItem(
                            app = app,
                            badgeCount = badge,
                            onClick = { viewModel.onAppClick(app) },
                            onLongClick = { viewModel.onAppLongClick(app) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(apps, key = { it.uniqueKey }) { app ->
                        val badge = uiState.badgeCounts[app.packageName] ?: 0
                        ListAppItem(
                            app = app,
                            badgeCount = badge,
                            onClick = { viewModel.onAppClick(app) },
                            onLongClick = { viewModel.onAppLongClick(app) }
                        )
                    }
                }
            }
        }

        // App Long-Press Modal Sheet
        if (uiState.selectedAppForMenu != null) {
            val app = uiState.selectedAppForMenu!!
            ModalBottomSheet(
                onDismissRequest = { viewModel.dismissAppMenu() },
                containerColor = colors.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = app.label,
                        color = colors.primaryText,
                        fontFamily = MonospaceFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = app.packageName,
                        color = colors.secondaryText,
                        fontFamily = MonospaceFontFamily,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Shortcuts (if any)
                    if (uiState.shortcutsForApp.isNotEmpty()) {
                        Text(
                            text = "SHORTCUTS",
                            color = colors.accent,
                            fontFamily = MonospaceFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        for (sc in uiState.shortcutsForApp) {
                            Text(
                                text = "⚡ $sc",
                                color = colors.primaryText,
                                fontFamily = MonospaceFontFamily,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Action buttons
                    DrawerActionRow(
                        icon = if (app.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        title = if (app.isFavorite) "Remove from Favorites" else "Add to Favorites",
                        onClick = { viewModel.toggleFavorite(app) }
                    )

                    DrawerActionRow(
                        icon = Icons.Default.VisibilityOff,
                        title = "Hide App",
                        onClick = { viewModel.hideApp(app) }
                    )

                    DrawerActionRow(
                        icon = Icons.Default.Edit,
                        title = "Rename Label",
                        onClick = { viewModel.showRename() }
                    )

                    DrawerActionRow(
                        icon = Icons.Default.Info,
                        title = "App Info",
                        onClick = { viewModel.openAppInfo(app) }
                    )

                    DrawerActionRow(
                        icon = Icons.Default.Shop,
                        title = "View in Play Store",
                        onClick = { viewModel.openPlayStore(app) }
                    )

                    DrawerActionRow(
                        icon = Icons.Default.Delete,
                        title = "Uninstall",
                        tint = colors.errorText,
                        onClick = { viewModel.uninstallApp(app) }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Rename Label Dialog
        if (uiState.showRenameDialog && uiState.selectedAppForMenu != null) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissAppMenu() },
                title = { Text("Rename App Label") },
                text = {
                    OutlinedTextField(
                        value = uiState.renameInput,
                        onValueChange = { viewModel.onRenameInputChange(it) },
                        label = { Text("Custom Label") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(onClick = { viewModel.saveRename() }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissAppMenu() }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoriteAppItem(
    app: AppItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val colors = LocalTerminalColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(64.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        DrawableIconView(
            drawable = app.icon,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = app.label,
            color = colors.primaryText,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GridAppItem(
    app: AppItem,
    badgeCount: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val colors = LocalTerminalColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(4.dp)
    ) {
        BadgedBox(
            badge = {
                if (badgeCount > 0) {
                    Badge(containerColor = colors.accent) {
                        Text(text = badgeCount.toString(), color = Color.White)
                    }
                }
            }
        ) {
            DrawableIconView(
                drawable = app.icon,
                modifier = Modifier.size(52.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = app.label,
            color = colors.primaryText,
            fontSize = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListAppItem(
    app: AppItem,
    badgeCount: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val colors = LocalTerminalColors.current
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BadgedBox(
                badge = {
                    if (badgeCount > 0) {
                        Badge(containerColor = colors.accent) {
                            Text(text = badgeCount.toString(), color = Color.White)
                        }
                    }
                }
            ) {
                DrawableIconView(
                    drawable = app.icon,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.label,
                    color = colors.primaryText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = app.packageName,
                    color = colors.secondaryText,
                    fontFamily = MonospaceFontFamily,
                    fontSize = 11.sp
                )
            }

            if (app.isFavorite) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Favorite",
                    tint = colors.accent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun DrawerActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    tint: Color = LocalTerminalColors.current.primaryText,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = tint, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, color = tint, fontSize = 15.sp)
        }
    }
}

@Composable
fun DrawableIconView(
    drawable: Drawable?,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(drawable) {
        drawableToBitmap(drawable)
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier
                .background(Color.Gray.copy(alpha = 0.3f), CircleShape)
        )
    }
}

fun drawableToBitmap(drawable: Drawable?): Bitmap? {
    if (drawable == null) return null
    if (drawable is BitmapDrawable && drawable.bitmap != null) {
        return drawable.bitmap
    }
    val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
    val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}



