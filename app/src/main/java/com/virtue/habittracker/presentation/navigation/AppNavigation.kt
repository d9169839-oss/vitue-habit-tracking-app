package com.virtue.habittracker.presentation.navigation
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.virtue.habittracker.presentation.auth.AuthEvent
import com.virtue.habittracker.presentation.auth.AuthViewModel
import com.virtue.habittracker.presentation.auth.ForgotPasswordScreen
import com.virtue.habittracker.presentation.auth.LoginScreen
import com.virtue.habittracker.presentation.auth.RegisterScreen
import com.virtue.habittracker.presentation.home.HomeScreen
private object Routes { const val LOGIN = "login"; const val REGISTER = "register"; const val FORGOT = "forgot"; const val HOME = "home" }
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    Surface(Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = Routes.LOGIN) {
            composable(Routes.LOGIN) {
                val vm: AuthViewModel = hiltViewModel()
                val state by vm.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(vm) { vm.events.collect { if (it == AuthEvent.Authenticated) navController.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } } } }
                LoginScreen(state, vm, { navController.navigate(Routes.REGISTER) }, { navController.navigate(Routes.FORGOT) }, vm::signInWithGoogle)
            }
            composable(Routes.REGISTER) {
                val vm: AuthViewModel = hiltViewModel()
                val state by vm.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(vm) { vm.events.collect { if (it == AuthEvent.Authenticated) navController.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } } } }
                RegisterScreen(state, vm, { navController.popBackStack() }, vm::signInWithGoogle)
            }
            composable(Routes.FORGOT) {
                val vm: AuthViewModel = hiltViewModel()
                val state by vm.uiState.collectAsStateWithLifecycle()
                ForgotPasswordScreen(state, vm) { navController.popBackStack() }
            }
            composable(Routes.HOME) {
                HomeScreen(onSignOut = { navController.navigate(Routes.LOGIN) { popUpTo(Routes.HOME) { inclusive = true } } })
            }
        }
    }
}
