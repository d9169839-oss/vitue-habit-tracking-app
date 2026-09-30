package com.virtue.habittracker.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val AuthCard = Color(0xFF211B30)
private val AuthBorder = Color(0xFF393047)
private val AuthViolet = Color(0xFFA78BFA)
private val AuthText = Color(0xFFF8F5FF)
private val AuthMuted = Color(0xFFAAA2BB)

/** Login UI only forwards user actions; Firebase work remains in AuthViewModel/use cases. */
@Composable
fun LoginScreen(
    state: AuthUiState,
    vm: AuthViewModel,
    onRegister: () -> Unit,
    onForgot: () -> Unit,
    onGoogle: (String) -> Unit
) {
    AuthScaffold(
        eyebrow = "WELCOME BACK",
        title = "Become who you choose.",
        subtitle = "Small promises. Kept daily. Pick up where you left off.",
        state = state,
        email = vm::onEmailChanged,
        password = vm::onPasswordChanged
    ) {
        Button(
            onClick = vm::signIn,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AuthViolet, contentColor = Color(0xFF171020))
        ) {
            if (state.isLoading) CircularProgressIndicator(color = Color(0xFF171020), strokeWidth = 2.dp)
            else Text("Sign in", fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = onForgot, modifier = Modifier.align(Alignment.End)) {
            Text("Forgot password?", color = Color(0xFFC4B5FD))
        }
        AuthDivider()
        GoogleButton(state.isLoading, onGoogle, vm::showError)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("New to Vitue? ", color = AuthMuted)
            TextButton(onClick = onRegister) { Text("Create account", color = Color(0xFFC4B5FD), fontWeight = FontWeight.SemiBold) }
        }
    }
}

@Composable
fun RegisterScreen(state: AuthUiState, vm: AuthViewModel, onBack: () -> Unit, onGoogle: (String) -> Unit) {
    AuthScaffold(
        eyebrow = "A FRESH START",
        title = "Build your next chapter.",
        subtitle = "Create your account and make room for better daily habits.",
        state = state,
        email = vm::onEmailChanged,
        password = vm::onPasswordChanged,
        confirmPassword = vm::onConfirmPasswordChanged,
        name = vm::onNameChanged
    ) {
        Button(
            onClick = vm::register,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AuthViolet, contentColor = Color(0xFF171020))
        ) {
            if (state.isLoading) CircularProgressIndicator(color = Color(0xFF171020), strokeWidth = 2.dp)
            else Text("Create account", fontWeight = FontWeight.Bold)
        }
        AuthDivider()
        GoogleButton(state.isLoading, onGoogle) 
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Already have an account? ", color = AuthMuted)
            TextButton(onClick = onBack) { Text("Sign in", color = Color(0xFFC4B5FD), fontWeight = FontWeight.SemiBold) }
        }
    }
}

@Composable
fun ForgotPasswordScreen(state: AuthUiState, vm: AuthViewModel, onBack: () -> Unit) {
    AuthScaffold(
        eyebrow = "ACCOUNT RECOVERY",
        title = "Reset your password.",
        subtitle = "Enter your email and we’ll send instructions if an account exists.",
        state = state,
        email = vm::onEmailChanged
    ) {
        Button(
            onClick = vm::sendPasswordReset,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AuthViolet, contentColor = Color(0xFF171020))
        ) {
            if (state.isLoading) CircularProgressIndicator(color = Color(0xFF171020), strokeWidth = 2.dp)
            else Text("Send reset email", fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Back to sign in", color = Color(0xFFC4B5FD))
        }
    }
}

@Composable
private fun AuthScaffold(
    eyebrow: String,
    title: String,
    subtitle: String,
    state: AuthUiState,
    email: (String) -> Unit,
    password: ((String) -> Unit)? = null,
    confirmPassword: ((String) -> Unit)? = null,
    name: ((String) -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
            .background(Color(0xFF100D18))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFA78BFA), Color(0xFF6D4CC2))),
                    shape = RoundedCornerShape(16.dp)
                ).padding(13.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("V", color = Color(0xFF171020), fontSize = 26.sp, fontWeight = FontWeight.Black)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("VITUE", color = AuthText, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 4.sp)
                Text("HABIT TRACKER", color = AuthMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp)
            }
        }

        Spacer(Modifier.height(30.dp))
        Text(eyebrow, color = Color(0xFFC4B5FD), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(Modifier.height(10.dp))
        Text(title, color = AuthText, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, lineHeight = 38.sp)
        Spacer(Modifier.height(10.dp))
        Text(subtitle, color = AuthMuted, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)
        Spacer(Modifier.height(26.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AuthCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, AuthBorder)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (name != null) {
                    AuthField(value = state.name, onValueChange = name, label = "Full name", keyboardType = KeyboardType.Text)
                }
                AuthField(value = state.email, onValueChange = email, label = "Email address", keyboardType = KeyboardType.Email)
                if (password != null) {
                    AuthField(value = state.password, onValueChange = password, label = "Password", keyboardType = KeyboardType.Password, secret = true)
                }
                if (confirmPassword != null) {
                    AuthField(value = state.confirmPassword, onValueChange = confirmPassword, label = "Confirm password", keyboardType = KeyboardType.Password, secret = true)
                }
                state.errorMessage?.let {
                    Text(it, color = Color(0xFFFB7185), style = MaterialTheme.typography.bodySmall)
                }
                state.infoMessage?.let {
                    Text(it, color = Color(0xFF34D399), style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(4.dp))
                content()
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "Your progress is personal. Your account keeps it yours.",
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF817991),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    secret: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (secret) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = AuthText,
            unfocusedTextColor = AuthText,
            focusedBorderColor = AuthViolet,
            unfocusedBorderColor = AuthBorder,
            focusedLabelColor = Color(0xFFC4B5FD),
            unfocusedLabelColor = AuthMuted,
            cursorColor = AuthViolet,
            focusedContainerColor = Color(0xFF191425),
            unfocusedContainerColor = Color(0xFF191425)
        )
    )
}

@Composable
private fun AuthDivider() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Spacer(Modifier.weight(1f).height(1.dp).background(AuthBorder))
        Text("OR CONTINUE WITH", color = AuthMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(Modifier.weight(1f).height(1.dp).background(AuthBorder))
    }
}

@Composable
private fun GoogleButton(loading: Boolean, onGoogle: (String) -> Unit, onError: (String) -> Unit = {}) {
    val context = LocalContext.current
    val client = remember(context) { GoogleCredentialClient(context) }
    val scope = rememberCoroutineScope()
    OutlinedButton(
        onClick = {
            scope.launch {
                try { onGoogle(client.getIdToken()) }
                catch (error: Exception) { onError(error.localizedMessage ?: "Google sign-in failed. Check Firebase and OAuth setup.") }
            }
        },
        enabled = !loading,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = AuthText),
        border = androidx.compose.foundation.BorderStroke(1.dp, AuthBorder)
    ) { Text("Continue with Google", fontWeight = FontWeight.SemiBold) }
}
