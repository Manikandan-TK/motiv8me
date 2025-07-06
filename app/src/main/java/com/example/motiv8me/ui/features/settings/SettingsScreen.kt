package com.example.motiv8me.ui.features.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.motiv8me.R
import com.example.motiv8me.ui.theme.Motiv8MeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToHabitSelection: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
    onNavigateToPro: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.refreshPermissions()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_navigate_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    PersonalizationSection(
                        uiState = uiState,
                        onHabitClick = onNavigateToHabitSelection,
                        onWallpaperFrequencyChanged = viewModel::onWallpaperFrequencyChanged,
                        onNotificationClick = onNavigateToNotificationSettings,
                        onThemeChanged = viewModel::onThemeChanged
                    )
                }
                item {
                    PermissionsSection(
                        uiState = uiState,
                        context = context
                    )
                }
                item {
                    UpgradeSection(onClick = onNavigateToPro)
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            content()
        }
    }
}

@Composable
private fun PersonalizationSection(
    uiState: SettingsUiState,
    onHabitClick: () -> Unit,
    onWallpaperFrequencyChanged: (Long) -> Unit,
    onNotificationClick: () -> Unit,
    onThemeChanged: (String) -> Unit
) {
    SettingsSectionCard(
        title = stringResource(R.string.settings_section_personalization),
        icon = Icons.Default.Palette
    ) {
        SettingsItemRow(
            title = stringResource(R.string.settings_label_focus_habit),
            subtitle = uiState.currentHabit,
            icon = Icons.Default.Style,
            onClick = onHabitClick
        )
        HorizontalDivider()
        WallpaperFrequencyDropdown(
            uiState = uiState,
            onFrequencyChanged = onWallpaperFrequencyChanged
        )
        HorizontalDivider()
        ThemeDropdown(
            uiState = uiState,
            onThemeChanged = onThemeChanged
        )
        HorizontalDivider()
        SettingsItemRow(
            title = stringResource(R.string.settings_label_notification_frequency),
            subtitle = uiState.notificationFrequencyDisplayName,
            icon = Icons.Default.Notifications,
            onClick = onNotificationClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeDropdown(
    uiState: SettingsUiState,
    onThemeChanged: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = expanded }
    ) {
        SettingsItemRow(
            title = "Theme", // Using hardcoded string for reliability
            subtitle = uiState.selectedTheme,
            icon = Icons.Default.Style,
            modifier = Modifier.menuAnchor(),
            onClick = { expanded = true }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            uiState.availableThemes.forEach { theme ->
                DropdownMenuItem(
                    text = { Text(theme) },
                    onClick = {
                        onThemeChanged(theme)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WallpaperFrequencyDropdown(
    uiState: SettingsUiState,
    onFrequencyChanged: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        SettingsItemRow(
            title = stringResource(R.string.settings_label_wallpaper_frequency),
            subtitle = uiState.wallpaperFrequencyDisplayName,
            icon = Icons.Default.Timer,
            modifier = Modifier.menuAnchor(),
            onClick = { expanded = true }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            uiState.availableWallpaperFrequencies.forEach { (name, value) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onFrequencyChanged(value)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}


@Composable
private fun PermissionsSection(
    uiState: SettingsUiState,
    context: Context
) {
    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", context.packageName, null)
        context.startActivity(intent)
    }

    SettingsSectionCard(
        title = stringResource(R.string.settings_section_permissions),
        icon = Icons.Default.Security
    ) {
        PermissionRow(
            label = stringResource(R.string.settings_label_wallpaper_permission),
            isGranted = uiState.hasWallpaperPermission,
            onClick = ::openAppSettings
        )
        HorizontalDivider()
        PermissionRow(
            label = stringResource(R.string.settings_label_notification_permission),
            isGranted = uiState.hasNotificationPermission,
            onClick = ::openAppSettings
        )
    }
}

@Composable
private fun UpgradeSection(onClick: () -> Unit) {
    SettingsSectionCard(
        title = stringResource(R.string.settings_section_upgrade),
        icon = Icons.Default.Star
    ) {
        SettingsItemRow(
            title = stringResource(R.string.settings_label_pro_features),
            subtitle = stringResource(R.string.settings_value_pro_features),
            icon = Icons.Default.Star,
            onClick = onClick
        )
    }
}

@Composable
private fun SettingsItemRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val rowModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Row(
        modifier = rowModifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PermissionRow(label: String, isGranted: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = if (isGranted) stringResource(R.string.permission_granted) else stringResource(R.string.permission_denied),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    Motiv8MeTheme {
        // This is a simplified preview and won't reflect the ViewModel's state.
        // For a full preview, a PreviewParameterProvider would be needed.
        Scaffold { padding ->
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    SettingsSectionCard(title = "Personalization", icon = Icons.Default.Palette) {
                        SettingsItemRow(title = "Focus Habit", subtitle = "Exercise More", icon = Icons.Default.Style, onClick = {})
                        HorizontalDivider()
                        SettingsItemRow(title = "Wallpaper Frequency", subtitle = "Every 6 hours", icon = Icons.Default.Timer, onClick = {})
                        HorizontalDivider()
                        SettingsItemRow(title = "Notification Frequency", subtitle = "Once a day", icon = Icons.Default.Notifications, onClick = {})
                    }
                }
            }
        }
    }
}