package com.virtue.habittracker.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.virtue.habittracker.R
import com.virtue.habittracker.presentation.auth.AuthStateViewModel
import com.virtue.habittracker.presentation.auth.AuthViewModel
import com.virtue.habittracker.presentation.auth.SessionState
import com.virtue.habittracker.presentation.history.HistoryViewModel
import com.virtue.habittracker.presentation.home.HomeViewModel
import com.virtue.habittracker.presentation.onboarding.OnboardingPreferences
import com.virtue.habittracker.presentation.onboarding.VirtueOnboardingScreen
import com.virtue.habittracker.presentation.programs.ProgramsTab
import com.virtue.habittracker.presentation.theme.VirtueHabitTheme
import kotlinx.coroutines.delay

private enum class AppTab { HOME, PROGRAMS, HISTORY, PROFILE }

/**
 * App-level composition only: owns tab selection, shared banners and bottom navigation.
 * Each feature screen and the connectivity observer live in focused files.
 */
@Composable
fun App(
    onSignOut: () -> Unit,
    homeViewModel: HomeViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    authViewModel: AuthViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    historyViewModel: HistoryViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    sessionViewModel: AuthStateViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val context = LocalContext.current
    // HomeTab remains composed underneath the splash so its Room Flow can emit the first snapshot.
    val hasLoadedInitialHomeData by homeViewModel.hasLoadedInitialData.collectAsStateWithLifecycle()
    val habits by homeViewModel.habits.collectAsStateWithLifecycle()
    val sessionState by sessionViewModel.state.collectAsStateWithLifecycle()
    val currentUser = (sessionState as? SessionState.SignedIn)?.user
    val userId = currentUser?.id
    val displayName = currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "Virtue Member"

    var showOnboarding by remember(userId) {
        mutableStateOf(!OnboardingPreferences.hasCompletedOnboarding(context, userId))
    }

    var selectedTab by remember { mutableStateOf(AppTab.HOME) }
    var programsFullScreen by remember { mutableStateOf(false) }
    var targetActiveEnrollmentId by remember { mutableStateOf<String?>(null) }
    val isOnline = rememberInternetConnectivity()
    var showOnlineBanner by remember { mutableStateOf(false) }
    var hasBeenOffline by remember { mutableStateOf(!isOnline) }

    // Whenever the user is on any tab other than HOME, pressing Back returns to HOME.
    // When on HOME, BackHandler is disabled so system Back exits/closes the app.
    BackHandler(enabled = selectedTab != AppTab.HOME && !showOnboarding) {
        selectedTab = AppTab.HOME
        programsFullScreen = false
        targetActiveEnrollmentId = null
    }

    LaunchedEffect(isOnline) {
        if (!isOnline) {
            hasBeenOffline = true
            showOnlineBanner = false
        } else if (hasBeenOffline) {
            showOnlineBanner = true
            delay(2_500)
            showOnlineBanner = false
            hasBeenOffline = false
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .background(Color(0xFF12111A))
                .statusBarsPadding()
                .padding(top = 2.dp)
        ) {
            // Smoothly animated offline / online connectivity banners
            AnimatedVisibility(
                visible = !isOnline,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                ConnectivityBanner(
                    text = "Offline mode • Local changes will sync automatically",
                    background = Color(0xFF381418),
                    accent = Color(0xFFFF5252)
                )
            }
            AnimatedVisibility(
                visible = isOnline && showOnlineBanner,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                ConnectivityBanner(
                    text = "Back online • Cloud sync active",
                    background = Color(0xFF0D3325),
                    accent = Color(0xFF00DC82)
                )
            }

            // All destinations stay composed to preserve their state and fast tab switching.
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                HomeTab(
                    modifier = Modifier.widthIn(max = 1100.dp).fillMaxSize(),
                    visible = selectedTab == AppTab.HOME,
                    vm = homeViewModel,
                    historyViewModel = historyViewModel,
                    onOpenHistory = { selectedTab = AppTab.HISTORY },
                    onOpenPrograms = {
                        targetActiveEnrollmentId = null
                        selectedTab = AppTab.PROGRAMS
                    },
                    onOpenActiveProgram = { enrollmentId ->
                        targetActiveEnrollmentId = enrollmentId
                        programsFullScreen = true
                        selectedTab = AppTab.PROGRAMS
                    }
                )
                HistoryTab(
                    modifier = Modifier.widthIn(max = 1100.dp).fillMaxSize(),
                    visible = selectedTab == AppTab.HISTORY,
                    vm = historyViewModel
                )
                ProgramsTab(
                    modifier = Modifier.widthIn(max = 1100.dp).fillMaxSize(),
                    visible = selectedTab == AppTab.PROGRAMS,
                    onFullScreenChange = { programsFullScreen = it },
                    openActiveEnrollmentId = targetActiveEnrollmentId,
                    onConsumedOpenActiveEnrollment = { targetActiveEnrollmentId = null },
                    onBackToHome = {
                        programsFullScreen = false
                        targetActiveEnrollmentId = null
                        selectedTab = AppTab.HOME
                    }
                )
                ProfileTab(
                    modifier = Modifier.widthIn(max = 1100.dp).fillMaxSize(),
                    visible = selectedTab == AppTab.PROFILE,
                    onSignOut = { authViewModel.signOut(onComplete = onSignOut) },
                    onOpenHistory = { selectedTab = AppTab.HISTORY },
                    onOpenPrograms = { selectedTab = AppTab.PROGRAMS },
                    onOpenOnboardingTour = { showOnboarding = true },
                    authViewModel = authViewModel
                )
            }

            if (!programsFullScreen) {
                AppBottomNavigation(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
        }
        // Cover the complete app (including navigation) only for the first local data load.
        if (!hasLoadedInitialHomeData) {
            VitueSplashScreen()
        } else if (showOnboarding) {
            VirtueOnboardingScreen(
                userDisplayName = displayName,
                existingHabitCount = habits.size,
                onCreateStarterHabit = { title, description ->
                    val alreadyExists = habits.any { it.habit.title.equals(title, ignoreCase = true) }
                    if (!alreadyExists) {
                        homeViewModel.addHabit(
                            title = title,
                            description = description,
                            onError = {}
                        )
                    }
                },
                onFinishOnboarding = { openProgramsTab ->
                    OnboardingPreferences.markOnboardingCompleted(context, userId)
                    showOnboarding = false
                    if (openProgramsTab) {
                        selectedTab = AppTab.PROGRAMS
                    }
                }
            )
        }
    }
}

@Composable
private fun AppBottomNavigation(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = Color(0xFFC4B5FD),
        selectedTextColor = Color(0xFFC4B5FD),
        indicatorColor = Color(0xFF2E2448),
        unselectedIconColor = Color(0xFF8E8AA7),
        unselectedTextColor = Color(0xFF8E8AA7)
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
        color = Color(0xFF1A1825),
        border = BorderStroke(1.dp, Color(0xFFA78BFA).copy(alpha = 0.22f)),
        tonalElevation = 6.dp
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            val itemModifier = Modifier.height(72.dp)
            NavigationBarItem(
                modifier = itemModifier,
                selected = selectedTab == AppTab.HOME,
                onClick = { onTabSelected(AppTab.HOME) },
                colors = itemColors,
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_home),
                        contentDescription = "Home",
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        "Home",
                        fontSize = 11.sp,
                        fontWeight = if (selectedTab == AppTab.HOME) FontWeight.Bold else FontWeight.Medium
                    )
                }
            )
            NavigationBarItem(
                modifier = itemModifier,
                selected = selectedTab == AppTab.PROGRAMS,
                onClick = { onTabSelected(AppTab.PROGRAMS) },
                colors = itemColors,
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_program),
                        contentDescription = "Programs",
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        "Programs",
                        fontSize = 11.sp,
                        fontWeight = if (selectedTab == AppTab.PROGRAMS) FontWeight.Bold else FontWeight.Medium
                    )
                }
            )
            NavigationBarItem(
                modifier = itemModifier,
                selected = selectedTab == AppTab.HISTORY,
                onClick = { onTabSelected(AppTab.HISTORY) },
                colors = itemColors,
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_history),
                        contentDescription = "History",
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        "History",
                        fontSize = 11.sp,
                        fontWeight = if (selectedTab == AppTab.HISTORY) FontWeight.Bold else FontWeight.Medium
                    )
                }
            )
            NavigationBarItem(
                modifier = itemModifier,
                selected = selectedTab == AppTab.PROFILE,
                onClick = { onTabSelected(AppTab.PROFILE) },
                colors = itemColors,
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_profile),
                        contentDescription = "Profile",
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        "Profile",
                        fontSize = 11.sp,
                        fontWeight = if (selectedTab == AppTab.PROFILE) FontWeight.Bold else FontWeight.Medium
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF12111A)
@Composable
private fun AppBottomNavigationPreview() {
    VirtueHabitTheme {
        AppBottomNavigation(
            selectedTab = AppTab.HOME,
            onTabSelected = {}
        )
    }
}
