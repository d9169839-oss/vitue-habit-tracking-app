package com.virtue.habittracker.presentation.navigation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.virtue.habittracker.presentation.auth.AuthEvent
import com.virtue.habittracker.presentation.auth.AuthStateViewModel
import com.virtue.habittracker.presentation.auth.AuthViewModel
import com.virtue.habittracker.presentation.auth.ForgotPasswordScreen
import com.virtue.habittracker.presentation.auth.LoginScreen
import com.virtue.habittracker.presentation.auth.RegisterScreen
import com.virtue.habittracker.presentation.auth.SessionState
import com.virtue.habittracker.presentation.App

private object Routes { const val SPLASH = "session_check"; const val LOGIN = "login"; const val REGISTER = "register"; const val FORGOT = "forgot"; const val HOME = "home" }

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val sessionViewModel: AuthStateViewModel = hiltViewModel()
    val session by sessionViewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(session) {
        val destination = when (session) {
            SessionState.Loading -> null
            SessionState.SignedOut -> Routes.LOGIN
            is SessionState.SignedIn -> Routes.HOME
        }
        if (destination != null && navController.currentDestination?.route != destination) {
            navController.navigate(destination) {
                popUpTo(Routes.SPLASH) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
    Surface(Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = Routes.SPLASH) {
            composable(Routes.SPLASH) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
            composable(Routes.LOGIN) {
                val vm: AuthViewModel = hiltViewModel()
                val state by vm.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(vm) { vm.events.collect { if (it == AuthEvent.Authenticated) navController.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true }; launchSingleTop = true } } }
                LoginScreen(state, vm, { navController.navigate(Routes.REGISTER) }, { navController.navigate(Routes.FORGOT) }, vm::signInWithGoogle)
            }
            composable(Routes.REGISTER) {
                val vm: AuthViewModel = hiltViewModel()
                val state by vm.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(vm) { vm.events.collect { if (it == AuthEvent.Authenticated) navController.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true }; launchSingleTop = true } } }
                RegisterScreen(state, vm, { navController.popBackStack() }, vm::signInWithGoogle)
            }
            composable(Routes.FORGOT) {
                val vm: AuthViewModel = hiltViewModel()
                val state by vm.uiState.collectAsStateWithLifecycle()
                ForgotPasswordScreen(state, vm) { navController.popBackStack() }
            }
            composable(Routes.HOME) {
                App(onSignOut = { navController.navigate(Routes.LOGIN) { popUpTo(Routes.HOME) { inclusive = true }; launchSingleTop = true } })
            }
        }
    }
}
