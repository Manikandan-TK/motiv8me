package com.example.motiv8me.ui.features.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.motiv8me.R
import com.example.motiv8me.ui.theme.Motiv8MeTheme
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import com.example.motiv8me.util.Constants
import androidx.compose.ui.graphics.luminance
import com.example.motiv8me.ui.navigation.ThemeToggleActions

/**
 * Placeholder Home screen. In the final app, this might show status
 * or simply not be used directly if the main interaction is background work.
 *
 * @param onNavigateToSettings Callback to navigate to the settings screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.background.luminance() < 0.5f
    val backgroundColor = if (isDark) colorScheme.surface else colorScheme.background
    val quote = remember { mutableStateOf(Constants.MOTIVATIONAL_QUOTES.random()) }.value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.primary
                ),
                actions = {
                    ThemeToggleActions()
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.home_active_title),
                style = MaterialTheme.typography.headlineSmall.copy(color = colorScheme.secondary),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.home_active_description),
                style = MaterialTheme.typography.bodyMedium.copy(color = colorScheme.onSurface),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Surface(
                color = if (isDark) colorScheme.tertiary else colorScheme.primary,
                shape = MaterialTheme.shapes.large,
                shadowElevation = 8.dp
            ) {
                Text(
                    text = quote,
                    style = MaterialTheme.typography.titleMedium.copy(color = colorScheme.onPrimary),
                    modifier = Modifier.padding(24.dp),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onNavigateToSettings,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(stringResource(id = R.string.settings_title))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    Motiv8MeTheme {
        HomeScreen(onNavigateToSettings = {})
    }
}

// TODO: Add string resources for the text used above if desired.