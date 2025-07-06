@file:OptIn(ExperimentalFoundationApi::class)

package com.example.motiv8me.ui.features.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.motiv8me.R
import com.example.motiv8me.util.Constants
import com.example.motiv8me.ui.components.FrequencySelector
import com.example.motiv8me.ui.components.HabitSelector
import com.example.motiv8me.ui.theme.Motiv8MeTheme
import com.google.accompanist.pager.HorizontalPagerIndicator
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { uiState.totalPages })
    val coroutineScope = rememberCoroutineScope()

    // Sync pager state from ViewModel
    LaunchedEffect(uiState.currentPage) {
        if (pagerState.currentPage != uiState.currentPage) {
            pagerState.animateScrollToPage(uiState.currentPage)
        }
    }

    // Sync ViewModel from pager state
    LaunchedEffect(pagerState.currentPage) {
        if (uiState.currentPage != pagerState.currentPage) {
            viewModel.setCurrentPage(pagerState.currentPage)
        }
    }

    Scaffold(
        bottomBar = {
            OnboardingBottomBar(
                uiState = uiState,
                onBack = viewModel::onBackClicked,
                onNext = viewModel::onNextClicked,
                onFinish = {
                    viewModel.saveOnboardingSelections()
                    onOnboardingComplete()
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = true
            ) { page ->
                when (page) {
                    0 -> WelcomeStep()
                    1 -> HabitStep(uiState, viewModel::onHabitSelected)
                    2 -> WallpaperFrequencyStep(uiState, viewModel::onWallpaperFrequencySelected)
                    3 -> NotificationFrequencyStep(uiState, viewModel::onNotificationFrequencySelected)
                    4 -> PermissionsStep(uiState, viewModel)
                }
            }
            HorizontalPagerIndicator(
                pagerState = pagerState,
                pageCount = uiState.totalPages,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
fun OnboardingBottomBar(
    uiState: OnboardingUiState,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit
) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        if (uiState.currentPage > 0) {
            TextButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.onboarding_back))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(id = R.string.onboarding_back))
            }
        }
        Spacer(Modifier.weight(1f))
        if (uiState.currentPage < uiState.totalPages - 1) {
            val isNextEnabled = when (uiState.currentPage) {
                1 -> uiState.selectedHabit != null
                2 -> uiState.selectedWallpaperFrequency != null
                3 -> uiState.selectedNotificationFrequencyMillis != null
                else -> true
            }
            Button(onClick = onNext, enabled = isNextEnabled) {
                Text(stringResource(id = R.string.onboarding_next))
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = stringResource(id = R.string.onboarding_next))
            }
        } else {
            Button(onClick = onFinish, enabled = uiState.canCompleteOnboarding) {
                Text(stringResource(id = R.string.onboarding_finish))
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.Done, contentDescription = stringResource(id = R.string.onboarding_finish))
            }
        }
    }
}

@Composable
fun StepCard(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        content()
    }
}

@Composable
fun WelcomeStep() {
    StepCard(title = stringResource(id = R.string.onboarding_step_welcome_title)) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.size(128.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(id = R.string.onboarding_explanation),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun HabitStep(uiState: OnboardingUiState, onHabitSelected: (String) -> Unit) {
    StepCard(title = stringResource(id = R.string.onboarding_step_habit_title)) {
        HabitSelector(
            availableHabits = uiState.availableHabits,
            selectedHabit = uiState.selectedHabit,
            onHabitSelected = onHabitSelected,
            placeholder = stringResource(id = R.string.onboarding_select_habit_button)
        )
    }
}

@Composable
fun WallpaperFrequencyStep(uiState: OnboardingUiState, onFrequencySelected: (Long) -> Unit) {
    StepCard(title = stringResource(id = R.string.onboarding_step_wallpaper_frequency_title)) {
        FrequencySelector(
            availableFrequencies = uiState.availableFrequencies.toMap(),
            selectedFrequencyMillis = uiState.selectedWallpaperFrequency,
            onFrequencySelected = onFrequencySelected,
            placeholder = stringResource(id = R.string.onboarding_select_frequency_button)
        )
    }
}

@Composable
fun NotificationFrequencyStep(uiState: OnboardingUiState, onFrequencySelected: (Long) -> Unit) {
    StepCard(title = stringResource(id = R.string.onboarding_step_notification_frequency_title)) {
        FrequencySelector(
            availableFrequencies = Constants.NOTIFICATION_FREQUENCY_OPTIONS.associateBy { it.first }.mapValues { it.value.second },
            selectedFrequencyMillis = uiState.selectedNotificationFrequencyMillis,
            onFrequencySelected = onFrequencySelected,
            placeholder = stringResource(id = R.string.onboarding_select_notification_frequency_button)
        )
    }
}

@Composable
fun PermissionsStep(uiState: OnboardingUiState, viewModel: OnboardingViewModel) {
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = viewModel::onNotificationPermissionResult
    )

    StepCard(title = stringResource(id = R.string.onboarding_step_permissions_title)) {
        Text(
            text = stringResource(id = R.string.onboarding_step_permissions_explanation),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        PermissionRow(
            icon = Icons.Default.Wallpaper,
            name = stringResource(id = R.string.permission_wallpaper),
            isGranted = uiState.isWallpaperPermissionGranted,
            buttonText = stringResource(id = R.string.onboarding_set_wallpaper_button)
        ) {
            viewModel.setInitialWallpaper()
        }

        Spacer(Modifier.height(16.dp))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionRow(
                icon = Icons.Default.Notifications,
                name = stringResource(id = R.string.permission_notifications),
                isGranted = uiState.isNotificationPermissionGranted,
                buttonText = stringResource(id = R.string.permission_grant_button)
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
fun PermissionRow(icon: ImageVector, name: String, isGranted: Boolean, buttonText: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Text(name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 16.dp))
        Spacer(Modifier.weight(1f))
        if (isGranted) {
            Text(
                stringResource(id = R.string.permission_granted),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            Button(onClick = onClick) {
                Text(buttonText)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    Motiv8MeTheme {
        OnboardingScreen(onOnboardingComplete = {})
    }
}
