package com.virtue.habittracker.presentation

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.virtue.habittracker.BuildConfig
import com.virtue.habittracker.presentation.auth.AuthStateViewModel
import com.virtue.habittracker.presentation.auth.AuthViewModel
import com.virtue.habittracker.presentation.auth.SessionState
import com.virtue.habittracker.reminders.HabitReminderScheduler

@Composable
fun ProfileTab(
    modifier: Modifier,
    visible: Boolean,
    onSignOut: () -> Unit,
    onOpenHistory: () -> Unit = {},
    onOpenPrograms: () -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel(),
    sessionViewModel: AuthStateViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val sessionState by sessionViewModel.state.collectAsStateWithLifecycle()
    val user = (sessionState as? SessionState.SignedIn)?.user
    val displayName = user?.displayName?.takeIf { it.isNotBlank() } ?: "Virtue Member"
    val email = user?.email?.takeIf { it.isNotBlank() } ?: "Synced with Virtue Cloud"

    val reminderScheduler = remember(context) { HabitReminderScheduler(context.applicationContext) }
    var remindersEnabled by remember { mutableStateOf(reminderScheduler.isRemindersEnabled()) }
    var reminderHour by remember { mutableIntStateOf(reminderScheduler.getReminderTime().hour) }

    var showSignOutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showReminderSettingsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .offset(x = if (visible) 0.dp else 10_000.dp)
            .background(Color(0xFF12111A))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFF28203E),
                border = BorderStroke(1.dp, Color(0xFFA78BFA).copy(alpha = 0.4f))
            ) {
                Text(
                    text = "ACCOUNT & SYNC",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFC4B5FD),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
            Text(
                text = "Your Profile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Keep showing up for yourself. Every day counts.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9E98B3)
            )
        }

        // Glowing User Identity Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, Color(0xFFA78BFA).copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF393344),
                                Color(0xFF2B253A),
                                Color(0xFF1E1C2A)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(900f, 900f)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFA78BFA).copy(alpha = 0.45f),
                                        Color(0xFF2E2448)
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "User profile avatar",
                            tint = Color(0xFFF8F5FF),
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFC4B5FD)
                        )
                    }
                }

                // Cloud Sync Status Pill
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0D3325),
                    border = BorderStroke(1.dp, Color(0xFF00DC82).copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CloudDone,
                            contentDescription = "Cloud sync active",
                            tint = Color(0xFF00DC82),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Offline-first Room cache & Cloud sync active",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFB8F7DF),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Quick Workspace Shortcuts
        Text(
            text = "QUICK ACCESS & SETTINGS",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF9E98B3),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        ProfileFeatureRow(
            icon = Icons.Filled.Insights,
            iconTint = Color(0xFF00DC82),
            title = "Habit History & Archived Habits",
            subtitle = "Inspect consistency charts or restore archived habits",
            onClick = onOpenHistory
        )

        ProfileFeatureRow(
            icon = Icons.Filled.FitnessCenter,
            iconTint = Color(0xFFC4B5FD),
            title = "Guided Fitness & Growth Programs",
            subtitle = "Calibrate your schedule and daily training load",
            onClick = onOpenPrograms
        )

        val reminderLabel = if (!remindersEnabled) {
            "Off • Tap to configure daily reminders"
        } else {
            val formattedHour = when (reminderHour) {
                0 -> "12:00 AM"
                in 1..11 -> "$reminderHour:00 AM"
                12 -> "12:00 PM"
                else -> "${reminderHour - 12}:00 PM"
            }
            "Active at $formattedHour • Tap to change"
        }

        ProfileFeatureRow(
            icon = Icons.Filled.NotificationsActive,
            iconTint = Color(0xFFFBBF24),
            title = "Daily Smart Reminders",
            subtitle = reminderLabel,
            onClick = { showReminderSettingsDialog = true }
        )

        ProfileFeatureRow(
            icon = Icons.Filled.Policy,
            iconTint = Color(0xFF60A5FA),
            title = "Privacy Policy & Health Disclaimer",
            subtitle = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) • Data safety & medical notice",
            onClick = { showPrivacyDialog = true }
        )

        authState.errorMessage?.let { errorMsg ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF381418)),
                border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f))
            ) {
                Text(
                    text = errorMsg,
                    modifier = Modifier.padding(14.dp),
                    color = Color(0xFFFFB4AB),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // Sign Out Button
        Button(
            onClick = {
                authViewModel.clearMessages()
                showSignOutDialog = true
            },
            enabled = !authState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF28203E),
                contentColor = Color(0xFFC4B5FD)
            ),
            border = BorderStroke(1.dp, Color(0xFFA78BFA).copy(alpha = 0.45f))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = "Sign out",
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Sign out of Virtue", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        // Delete Account Button (Google Play Account Deletion Policy compliance)
        OutlinedButton(
            onClick = {
                authViewModel.clearMessages()
                showDeleteAccountDialog = true
            },
            enabled = !authState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color(0xFF241217),
                contentColor = Color(0xFFFF5252)
            ),
            border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.45f))
        ) {
            Icon(
                imageVector = Icons.Filled.DeleteForever,
                contentDescription = "Delete account",
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                if (authState.isLoading) "Deleting account..." else "Delete Account Permanently",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(Modifier.height(12.dp))
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            containerColor = Color(0xFF1E1C2A),
            titleContentColor = Color.White,
            textContentColor = Color(0xFF9E98B3),
            title = { Text("Sign out?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Any pending changes will be synced and your habits remain safely backed up in your account.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF5252),
                        contentColor = Color.White
                    )
                ) {
                    Text("Sign out", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel", color = Color(0xFFC4B5FD))
                }
            }
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { if (!authState.isLoading) showDeleteAccountDialog = false },
            containerColor = Color(0xFF1E1C2A),
            titleContentColor = Color.White,
            textContentColor = Color(0xFF9E98B3),
            title = { Text("Delete Account Permanently?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This will permanently delete your account, all habits, check-in history, and active programs from both this device and the cloud. This action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.deleteAccount(onDeleted = {
                            showDeleteAccountDialog = false
                            onSignOut()
                        })
                    },
                    enabled = !authState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF5252),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        if (authState.isLoading) "Deleting..." else "Delete Permanently",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteAccountDialog = false },
                    enabled = !authState.isLoading
                ) {
                    Text("Cancel", color = Color(0xFFC4B5FD))
                }
            }
        )
    }

    if (showReminderSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showReminderSettingsDialog = false },
            containerColor = Color(0xFF1E1C2A),
            titleContentColor = Color.White,
            textContentColor = Color(0xFF9E98B3),
            title = { Text("Daily Smart Reminders", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable daily reminder", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = remindersEnabled,
                            onCheckedChange = { enabled ->
                                remindersEnabled = enabled
                                reminderScheduler.setRemindersEnabled(enabled)
                            }
                        )
                    }
                    if (remindersEnabled) {
                        Text("Preferred reminder time:", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(8 to "8 AM", 14 to "2 PM", 20 to "8 PM", 22 to "10 PM").forEach { (hour, label) ->
                                FilterChip(
                                    selected = reminderHour == hour,
                                    onClick = {
                                        reminderHour = hour
                                        reminderScheduler.setReminderTime(hour, 0)
                                    },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF2E2448),
                                        selectedLabelColor = Color(0xFFC4B5FD)
                                    )
                                )
                            }
                        }
                    }
                    TextButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            runCatching { context.startActivity(intent) }
                        }
                    ) {
                        Text("Open Android Notification Settings", color = Color(0xFFC4B5FD))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showReminderSettingsDialog = false }) {
                    Text("Done", color = Color(0xFFC4B5FD), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            containerColor = Color(0xFF1E1C2A),
            titleContentColor = Color.White,
            textContentColor = Color(0xFF9E98B3),
            title = { Text("Privacy & Health Notice", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Data & Privacy: Virtue Habit Tracker stores your habits, daily check-ins, and program enrollments locally on your device and syncs them securely over HTTPS to your private Firebase account so you can access them across devices. Biometric calibration inputs (age, sex, height, weight) are used solely to calculate your personalized program parameters. You can permanently delete your account and all cloud data at any time from this Profile tab.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "Health & Fitness Disclaimer: Program plans, BMR/TDEE calorie estimates, and activity guidelines are for educational and informational purposes only and do not constitute medical advice. Consult a qualified healthcare professional before beginning any new fitness or nutrition program.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Close", color = Color(0xFFC4B5FD), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun ProfileFeatureRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1C2A)),
        border = BorderStroke(1.dp, Color(0xFF2E2C3D))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(iconTint.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9E98B3)
                )
            }
            if (onClick != null) {
                Text(
                    text = "→",
                    color = Color(0xFFC4B5FD),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
