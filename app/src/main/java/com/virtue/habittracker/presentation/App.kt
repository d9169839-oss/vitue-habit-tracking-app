package com.virtue.habittracker.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.virtue.habittracker.R
import com.virtue.habittracker.presentation.auth.AuthViewModel
import com.virtue.habittracker.presentation.history.HistoryViewModel
import com.virtue.habittracker.presentation.home.HomeViewModel
import com.virtue.habittracker.presentation.programs.ProgramsTab
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

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
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

        if (!programsFullScreen) NavigationBar {
            NavigationBarItem(selected = selectedTab == AppTab.HOME, onClick = { selectedTab = AppTab.HOME },
                icon = { androidx.compose.material3.Icon(painterResource(R.drawable.ic_home), contentDescription = "Home") },
                label = { Text("Home") })
            NavigationBarItem(selected = selectedTab == AppTab.PROGRAMS, onClick = { selectedTab = AppTab.PROGRAMS },
                icon = { androidx.compose.material3.Icon(painterResource(R.drawable.ic_program), contentDescription = "Programs") },
                label = { Text("Programs") })
            NavigationBarItem(selected = selectedTab == AppTab.HISTORY, onClick = { selectedTab = AppTab.HISTORY },
                icon = { androidx.compose.material3.Icon(painterResource(R.drawable.ic_history), contentDescription = "History") },
                label = { Text("History") })
            NavigationBarItem(selected = selectedTab == AppTab.PROFILE, onClick = { selectedTab = AppTab.PROFILE },
                icon = { androidx.compose.material3.Icon(painterResource(R.drawable.ic_profile), contentDescription = "Profile") },
                label = { Text("Profile") })
        }
    }
}