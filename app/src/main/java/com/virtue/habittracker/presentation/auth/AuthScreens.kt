package com.virtue.habittracker.presentation.auth
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(state: AuthUiState, vm: AuthViewModel, onRegister: () -> Unit, onForgot: () -> Unit, onGoogle: (String) -> Unit) {
    AuthScaffold("Build the person you want to become", "Small promises. Kept daily.", state, vm::onEmailChanged, vm::onPasswordChanged) {
        Button(onClick = vm::signIn, enabled = !state.isLoading, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            if (state.isLoading) CircularProgressIndicator() else Text("Sign in")
        }
        TextButton(onClick = onForgot, modifier = Modifier.align(Alignment.End)) { Text("Forgot password?") }
        GoogleButton(state.isLoading, onGoogle)
        Row(verticalAlignment = Alignment.CenterVertically) { Text("New here? "); TextButton(onClick = onRegister) { Text("Create account") } }
    }
}
@Composable
fun RegisterScreen(state: AuthUiState, vm: AuthViewModel, onBack: () -> Unit, onGoogle: (String) -> Unit) {
    AuthScaffold("Start with one promise", "Create your Vitue account.", state, vm::onEmailChanged, vm::onPasswordChanged, vm::onConfirmPasswordChanged) {
        Button(onClick = vm::register, enabled = !state.isLoading, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            if (state.isLoading) CircularProgressIndicator() else Text("Create account")
        }
        GoogleButton(state.isLoading, onGoogle)
        TextButton(onClick = onBack) { Text("Already have an account? Sign in") }
    }
}
@Composable
fun ForgotPasswordScreen(state: AuthUiState, vm: AuthViewModel, onBack: () -> Unit) {
    AuthScaffold("Reset your password", "We’ll send reset instructions to your email.", state, vm::onEmailChanged) {
        Button(onClick = vm::sendPasswordReset, enabled = !state.isLoading, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            if (state.isLoading) CircularProgressIndicator() else Text("Send reset email")
        }
        TextButton(onClick = onBack) { Text("Back to sign in") }
    }
}
@Composable
private fun AuthScaffold(
    title: String, subtitle: String, state: AuthUiState, email: (String) -> Unit,
    password: ((String) -> Unit)? = null, confirmPassword: ((String) -> Unit)? = null,
    content: @Composable Column.() -> Unit
) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 36.dp), verticalArrangement = Arrangement.Center) {
        Text("VITUE", color = MaterialTheme.colorScheme.primary, fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
        Spacer(Modifier.height(24.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(28.dp))
        OutlinedTextField(value = state.email, onValueChange = email, label = { Text("Email") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), singleLine = true, modifier = Modifier.fillMaxWidth())
        if (password != null) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = state.password, onValueChange = password, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), singleLine = true, modifier = Modifier.fillMaxWidth())
        }
        if (confirmPassword != null) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = state.confirmPassword, onValueChange = confirmPassword, label = { Text("Confirm password") }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), singleLine = true, modifier = Modifier.fillMaxWidth())
        }
        state.errorMessage?.let { Spacer(Modifier.height(12.dp)); Text(it, color = MaterialTheme.colorScheme.error) }
        state.infoMessage?.let { Spacer(Modifier.height(12.dp)); Text(it, color = MaterialTheme.colorScheme.primary) }
        Spacer(Modifier.height(20.dp))
        content()
    }
}
@Composable
private fun GoogleButton(loading: Boolean, onGoogle: (String) -> Unit) {
    val context = LocalContext.current
    val client = remember(context) { GoogleCredentialClient(context) }
    val scope = rememberCoroutineScope()
    Spacer(Modifier.height(12.dp))
    OutlinedButton(onClick = { scope.launch { try { onGoogle(client.getIdToken()) } catch (_: Exception) { /* Credential cancellation/configuration errors do not crash the app. */ } } },
        enabled = !loading, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp)) { Text("Continue with Google") }
}
