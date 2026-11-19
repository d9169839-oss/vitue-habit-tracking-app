package com.virtue.habittracker.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.virtue.habittracker.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private val AuthBg = Color(0xFF12111A)
private val AuthCard = Color(0xFF1E1C2A)
private val AuthBorder = Color(0xFF36324A)
private val AuthViolet = Color(0xFFA78BFA)
private val AuthText = Color(0xFFF8F5FF)
private val AuthMuted = Color(0xFF9E98B3)

/** Login UI only forwards user actions; Firebase work remains in AuthViewModel/use cases. */
@Composable
fun LoginScreen(
    state: AuthUiState,
    vm: AuthViewModel,
    onRegister: () -> Unit,
    onForgot: () -> Unit,
    onGoogle: (String) -> Unit,
    credentialProvider: GoogleCredentialProvider? = null
) {
    AuthScaffold(
        eyebrow = "WELCOME BACK",
        title = "Become who you choose.",
        subtitle = "Small promises. Kept daily. Pick up where you left off.",
        state = state,
        email = vm::onEmailChanged,
        password = vm::onPasswordChanged,
        onSubmit = vm::signIn
    ) {
        Button(
            onClick = vm::signIn,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AuthViolet,
                contentColor = Color(0xFF140E21),
                disabledContainerColor = AuthViolet.copy(alpha = 0.5f),
                disabledContentColor = Color(0xFF140E21)
            )
        ) {
            if (state.isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF140E21),
                        strokeWidth = 2.5.dp
                    )
                    Text("Signing in…", fontWeight = FontWeight.Bold)
                }
            } else {
                Text("Sign in", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
        TextButton(onClick = onForgot, modifier = Modifier.align(Alignment.End)) {
            Text("Forgot password?", color = Color(0xFFC4B5FD), fontWeight = FontWeight.SemiBold)
        }
        AuthDivider()
        GoogleButton(state.isLoading, onGoogle, vm::showError, credentialProvider)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("New to Virtue? ", color = AuthMuted)
            TextButton(onClick = onRegister) {
                Text("Create account", color = Color(0xFF00DC82), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RegisterScreen(
    state: AuthUiState,
    vm: AuthViewModel,
    onBack: () -> Unit,
    onGoogle: (String) -> Unit,
    credentialProvider: GoogleCredentialProvider? = null
) {
    AuthScaffold(
        eyebrow = "A FRESH START",
        title = "Build your next chapter.",
        subtitle = "Create your account and make room for better daily habits.",
        state = state,
        email = vm::onEmailChanged,
        password = vm::onPasswordChanged,
        confirmPassword = vm::onConfirmPasswordChanged,
        name = vm::onNameChanged,
        onSubmit = vm::register
    ) {
        Button(
            onClick = vm::register,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AuthViolet,
                contentColor = Color(0xFF140E21),
                disabledContainerColor = AuthViolet.copy(alpha = 0.5f),
                disabledContentColor = Color(0xFF140E21)
            )
        ) {
            if (state.isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF140E21),
                        strokeWidth = 2.5.dp
                    )
                    Text("Creating account…", fontWeight = FontWeight.Bold)
                }
            } else {
                Text("Create account", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
        AuthDivider()
        GoogleButton(state.isLoading, onGoogle, vm::showError, credentialProvider)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Already have an account? ", color = AuthMuted)
            TextButton(onClick = onBack) {
                Text("Sign in", color = Color(0xFFC4B5FD), fontWeight = FontWeight.Bold)
            }
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
        email = vm::onEmailChanged,
        onSubmit = vm::sendPasswordReset
    ) {
        Button(
            onClick = vm::sendPasswordReset,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AuthViolet,
                contentColor = Color(0xFF140E21),
                disabledContainerColor = AuthViolet.copy(alpha = 0.5f),
                disabledContentColor = Color(0xFF140E21)
            )
        ) {
            if (state.isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF140E21),
                        strokeWidth = 2.5.dp
                    )
                    Text("Sending reset link…", fontWeight = FontWeight.Bold)
                }
            } else {
                Text("Send reset email", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
        TextButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Back to sign in", color = Color(0xFFC4B5FD), fontWeight = FontWeight.SemiBold)
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
    onSubmit: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthBg)
            .statusBarsPadding()
            .padding(top = 8.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(AuthViolet.copy(alpha = 0.35f), Color.Transparent)
                            ),
                            shape = CircleShape
                        )
                )
                Image(
                    painter = painterResource(R.drawable.ic_virtue_logo),
                    contentDescription = "Virtue Logo",
                    modifier = Modifier.size(48.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("VIRTUE", color = AuthText, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 4.sp)
                Text("HABIT TRACKER", color = Color(0xFFC4B5FD), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp)
            }
        }

        Spacer(Modifier.height(28.dp))
        Surface(
            shape = RoundedCornerShape(50),
            color = Color(0xFF28203E),
            border = BorderStroke(1.dp, AuthViolet.copy(alpha = 0.4f))
        ) {
            Text(
                text = eyebrow,
                color = Color(0xFFC4B5FD),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(title, color = AuthText, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, lineHeight = 38.sp)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, color = AuthMuted, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)
        Spacer(Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AuthCard),
            border = BorderStroke(1.dp, AuthViolet.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (name != null) {
                    AuthField(
                        value = state.name,
                        onValueChange = name,
                        label = "Full name",
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                }
                AuthField(
                    value = state.email,
                    onValueChange = email,
                    label = "Email address",
                    keyboardType = KeyboardType.Email,
                    imeAction = if (password != null) ImeAction.Next else ImeAction.Done,
                    onDone = if (password == null) onSubmit else null
                )
                if (password != null) {
                    AuthField(
                        value = state.password,
                        onValueChange = password,
                        label = "Password",
                        keyboardType = KeyboardType.Password,
                        secret = true,
                        imeAction = if (confirmPassword != null) ImeAction.Next else ImeAction.Done,
                        onDone = if (confirmPassword == null) onSubmit else null
                    )
                }
                if (confirmPassword != null) {
                    AuthField(
                        value = state.confirmPassword,
                        onValueChange = confirmPassword,
                        label = "Confirm password",
                        keyboardType = KeyboardType.Password,
                        secret = true,
                        imeAction = ImeAction.Done,
                        onDone = onSubmit
                    )
                }

                // Glowing Failure Banner
                AnimatedVisibility(visible = state.errorMessage != null) {
                    state.errorMessage?.let { msg ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF381418),
                            border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.55f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ErrorOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFFF5252),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = msg,
                                    color = Color(0xFFFF8A8A),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Glowing Success / Info Banner
                AnimatedVisibility(visible = state.infoMessage != null) {
                    state.infoMessage?.let { msg ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0D3325),
                            border = BorderStroke(1.dp, Color(0xFF00DC82).copy(alpha = 0.55f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF00DC82),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = msg,
                                    color = Color(0xFFB8F7DF),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
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
    secret: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
    onDone: (() -> Unit)? = null
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onDone = { onDone?.invoke() }
        ),
        visualTransformation = if (secret && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        trailingIcon = if (secret) {
            {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = AuthMuted
                    )
                }
            }
        } else null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = AuthText,
            unfocusedTextColor = AuthText,
            focusedBorderColor = AuthViolet,
            unfocusedBorderColor = AuthBorder,
            focusedLabelColor = Color(0xFFC4B5FD),
            unfocusedLabelColor = AuthMuted,
            cursorColor = AuthViolet,
            focusedContainerColor = Color(0xFF161421),
            unfocusedContainerColor = Color(0xFF161421)
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
private fun GoogleButton(
    loading: Boolean,
    onGoogle: (String) -> Unit,
    onError: (String) -> Unit = {},
    credentialProvider: GoogleCredentialProvider? = null
) {
    val context = LocalContext.current
    val provider = credentialProvider ?: remember(context) { GoogleCredentialClient(context) }
    val scope = rememberCoroutineScope()
    OutlinedButton(
        onClick = {
            scope.launch {
                try { onGoogle(provider.getIdToken()) }
                catch (error: CancellationException) { throw error }
                catch (error: Exception) { onError(error.localizedMessage ?: "Google sign-in failed. Check Firebase and OAuth setup.") }
            }
        },
        enabled = !loading,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color(0xFF262336),
            contentColor = AuthText
        ),
        border = BorderStroke(1.dp, AuthBorder)
    ) {
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Icon(painter = painterResource(R.drawable.ic_google_logo), contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.Unspecified)
            Spacer(Modifier.width(12.dp))
            Text("Continue with Google", fontWeight = FontWeight.SemiBold)
        }
    }
}
