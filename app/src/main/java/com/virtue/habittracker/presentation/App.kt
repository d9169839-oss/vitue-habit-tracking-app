package com.virtue.habittracker.presentation

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.virtue.habittracker.R
import com.virtue.habittracker.presentation.auth.AuthViewModel
import com.virtue.habittracker.presentation.history.HistoryViewModel
import com.virtue.habittracker.presentation.home.HomeViewModel
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
    historyViewModel: HistoryViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    // HomeTab remains composed underneath the splash so its Room Flow can emit the first snapshot.
    val hasLoadedInitialHomeData by homeViewModel.hasLoadedInitialData.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(AppTab.HOME) }
    var programsFullScreen by remember { mutableStateOf(false) }
    val isOnline = rememberInternetConnectivity()
    var showOnlineBanner by remember { mutableStateOf(false) }
    var hasBeenOffline by remember { mutableStateOf(!isOnline) }

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
              .background(MaterialTheme.colorScheme.background)
              .statusBarsPadding()
              .padding(top = 3.dp)
      ) {
        // Keep the offline banner persistent; briefly confirm reconnection as before.
        if (!isOnline) {
            ConnectivityBanner(text = "Device offline", background = androidx.compose.ui.graphics.Color(0xFFC62828))
        } else if (showOnlineBanner) {
            ConnectivityBanner(text = "Internet available", background = androidx.compose.ui.graphics.Color(0xFF2E7D32))
        }

        // All destinations stay composed to preserve their state and fast tab switching.
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            HomeTab(
                modifier = Modifier.widthIn(max = 1100.dp).fillMaxSize().alpha(if (selectedTab == AppTab.HOME) 1f else 0f),
                visible = selectedTab == AppTab.HOME,
                vm = homeViewModel,
                authViewModel = authViewModel,
                historyViewModel = historyViewModel,
                onOpenHistory = { selectedTab = AppTab.HISTORY },
                onSignOut = onSignOut
            )
            HistoryTab(
                modifier = Modifier.widthIn(max = 1100.dp).fillMaxSize().alpha(if (selectedTab == AppTab.HISTORY) 1f else 0f),
                visible = selectedTab == AppTab.HISTORY,
                vm = historyViewModel
            )
            ProgramsTab(
                modifier = Modifier.widthIn(max = 1100.dp).fillMaxSize().alpha(if (selectedTab == AppTab.PROGRAMS) 1f else 0f),
                visible = selectedTab == AppTab.PROGRAMS,
                onFullScreenChange = { programsFullScreen = it }
            )
            ProfileTab(
                modifier = Modifier.widthIn(max = 1100.dp).fillMaxSize().alpha(if (selectedTab == AppTab.PROFILE) 1f else 0f),
                visible = selectedTab == AppTab.PROFILE,
                onSignOut = { authViewModel.signOut(); onSignOut() }
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
      if (!hasLoadedInitialHomeData) VitueSplashScreen()
    }
}

@Composable
private fun AppBottomNavigation(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
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
                icon = {
                    androidx.compose.material3.Icon(
                        painter = painterResource(R.drawable.ic_home),
                        contentDescription = "Home",
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        "Home",
                        fontSize = 11.sp,
                        fontWeight = if (selectedTab == AppTab.HOME) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            )
            NavigationBarItem(
                modifier = itemModifier,
                selected = selectedTab == AppTab.PROGRAMS,
                onClick = { onTabSelected(AppTab.PROGRAMS) },
                icon = {
                    androidx.compose.material3.Icon(
                        painter = painterResource(R.drawable.ic_program),
                        contentDescription = "Programs",
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        "Programs",
                        fontSize = 11.sp,
                        fontWeight = if (selectedTab == AppTab.PROGRAMS) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            )
            NavigationBarItem(
                modifier = itemModifier,
                selected = selectedTab == AppTab.HISTORY,
                onClick = { onTabSelected(AppTab.HISTORY) },
                icon = {
                    androidx.compose.material3.Icon(
                        painter = painterResource(R.drawable.ic_history),
                        contentDescription = "History",
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        "History",
                        fontSize = 11.sp,
                        fontWeight = if (selectedTab == AppTab.HISTORY) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            )
            NavigationBarItem(
                modifier = itemModifier,
                selected = selectedTab == AppTab.PROFILE,
                onClick = { onTabSelected(AppTab.PROFILE) },
                icon = {
                    androidx.compose.material3.Icon(
                        painter = painterResource(R.drawable.ic_profile),
                        contentDescription = "Profile",
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        "Profile",
                        fontSize = 11.sp,
                        fontWeight = if (selectedTab == AppTab.PROFILE) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF100D18)
@Composable
private fun AppBottomNavigationPreview() {
    VirtueHabitTheme {
        AppBottomNavigation(
            selectedTab = AppTab.HOME,
            onTabSelected = {}
        )
    }
}
