package com.virtue.habittracker.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.VerifiedUser
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.virtue.habittracker.BuildConfig

object VirtuePolicyLinks {
    const val PRIVACY_POLICY_HTTPS_URL =
        "https://d9169839-oss.github.io/vitue-habit-tracking-app/privacy-policy.html"
    const val ACCOUNT_DELETION_HTTPS_URL =
        "https://d9169839-oss.github.io/vitue-habit-tracking-app/account-deletion.html"
    const val GITHUB_POLICY_MD_HTTPS_URL =
        "https://github.com/d9169839-oss/vitue-habit-tracking-app/blob/main/docs/PRIVACY_POLICY.md"
    const val SUPPORT_EMAIL = "d9169839@gmail.com"
}

@Composable
fun PrivacyAndHealthPolicyDialog(
    onDismiss: () -> Unit,
    initialSection: Int = 0
) {
    val uriHandler = LocalUriHandler.current
    var selectedTab by remember { mutableIntStateOf(initialSection) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 680.dp),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF171522)),
            border = BorderStroke(1.dp, Color(0xFFA78BFA).copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF28203E),
                        border = BorderStroke(1.dp, Color(0xFFA78BFA).copy(alpha = 0.45f))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Policy,
                            contentDescription = "Privacy & Health Policy",
                            tint = Color(0xFFC4B5FD),
                            modifier = Modifier
                                .padding(10.dp)
                                .size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Privacy & Health Policy",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Google Play Compliant • v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF9E98B3)
                        )
                    }
                }

                // Section Switcher Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        0 to "Privacy & Data",
                        1 to "Health Policy",
                        2 to "Deletion & Rights"
                    ).forEach { (index, title) ->
                        FilterChip(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            label = {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF2E2448),
                                selectedLabelColor = Color(0xFFC4B5FD),
                                containerColor = Color(0xFF211E30),
                                labelColor = Color(0xFF9E98B3)
                            )
                        )
                    }
                }

                // Scrollable Content Area
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (selectedTab) {
                        0 -> PrivacyAndDataSafetySection()
                        1 -> HealthAndMedicalPolicySection()
                        else -> AccountDeletionAndRightsSection()
                    }
                }

                // Official HTTPS Web Links Dock
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            runCatching {
                                uriHandler.openUri(VirtuePolicyLinks.PRIVACY_POLICY_HTTPS_URL)
                            }.onFailure {
                                runCatching {
                                    uriHandler.openUri(VirtuePolicyLinks.GITHUB_POLICY_MD_HTTPS_URL)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF28203E),
                            contentColor = Color(0xFFC4B5FD)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFA78BFA).copy(alpha = 0.45f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.size(8.dp))
                        Text(
                            text = "Open Official HTTPS Policy on GitHub",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                runCatching {
                                    uriHandler.openUri(VirtuePolicyLinks.ACCOUNT_DELETION_HTTPS_URL)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.4f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFFF8A8A)
                            )
                        ) {
                            Text(
                                text = "HTTPS Deletion Portal",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        TextButton(onClick = onDismiss) {
                            Text(
                                text = "Done",
                                color = Color(0xFF00DC82),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivacyAndDataSafetySection() {
    PolicyBannerCard(
        icon = Icons.Filled.VerifiedUser,
        accentColor = Color(0xFF00DC82),
        title = "100% Ad-Free & Zero Data Selling",
        body = "Virtue Habit Tracker embeds zero advertising SDKs or third-party trackers. We never sell, rent, or share your personal, behavioral, or fitness data with advertisers or data brokers."
    )

    PolicySectionCard(
        title = "1. What Data We Collect (Data Safety)",
        bullets = listOf(
            "Account Identity: Email address, Display Name, and Firebase User ID (UID) via Email/Password or Google Sign-In for authentication and cloud backup isolation.",
            "Habits & Program Progress: Your custom habit titles, daily check-in dates, enrolled program IDs, and lightweight completed/skipped day checkpoints stored locally in Room SQLite and synced over HTTPS to your private Cloud Firestore workspace.",
            "On-Device Biometric Calibration: Biological sex, age, height (cm), and weight (kg) entered during fitness onboarding are stored strictly on your device (SharedPreferences) to calculate BMR/TDEE targets. They are never uploaded to Cloud Firestore."
        )
    )

    PolicySectionCard(
        title = "2. Encryption & Cloud Security",
        bullets = listOf(
            "In-Transit & At-Rest Encryption: All network traffic uses HTTPS / TLS 1.3 and AES-256 encryption at rest in Google Cloud Firestore.",
            "Strict UID Isolation: Firestore security rules guarantee that only your authenticated session (request.auth.uid == userId) can read, write, or delete your documents.",
            "Permissions Used: POST_NOTIFICATIONS and RECEIVE_BOOT_COMPLETED are used solely to schedule optional local daily habit reminders via Android WorkManager."
        )
    )
}

@Composable
private fun HealthAndMedicalPolicySection() {
    PolicyBannerCard(
        icon = Icons.Filled.HealthAndSafety,
        accentColor = Color(0xFFFBBF24),
        title = "Google Play Health Apps Disclosure",
        body = "Virtue Habit Tracker is a general self-improvement, physical fitness, and behavioral habit-tracking app. It is NOT a medical device, clinical diagnostic tool, or licensed healthcare/psychotherapy service."
    )

    PolicySectionCard(
        title = "1. Fitness & Metabolic Calculations (BMR / TDEE)",
        bullets = listOf(
            "Caloric estimates, Basal Metabolic Rate (BMR), Total Daily Energy Expenditure (TDEE), and training volume targets are mathematical approximations computed via the peer-reviewed Mifflin-St Jeor equation.",
            "All physical training routines, metabolic targets, and nutrition guidelines are for educational and informational purposes only and do not constitute medical advice.",
            "Always consult a qualified physician or healthcare professional before starting any new exercise, strength, or nutrition program—especially if you have pre-existing cardiovascular, orthopedic, or metabolic conditions."
        )
    )

    PolicySectionCard(
        title = "2. CBT-Inspired Mindfulness, Sleep & Focus Curricula",
        bullets = listOf(
            "Our stress regulation, sleep hygiene (CBT-I principles), social confidence, and dopamine/focus programs provide evidence-informed behavioral self-help exercises (box breathing, cognitive reframing, stimulus control, somatic grounding).",
            "These programs do NOT diagnose, treat, cure, or replace professional psychiatric, psychological, or medical treatment for clinical depression, anxiety disorders, ADHD, PTSD, or chronic insomnia.",
            "Emergency Notice: If you are experiencing a medical emergency or mental health crisis, stop using the app immediately and contact emergency services (e.g., 911 or 988 in the US/Canada, 112 in Europe)."
        )
    )
}

@Composable
private fun AccountDeletionAndRightsSection() {
    PolicyBannerCard(
        icon = Icons.Filled.Lock,
        accentColor = Color(0xFFA78BFA),
        title = "Instant Self-Service Account Deletion",
        body = "You own 100% of your data. Deleting your account permanently erases your Firebase Auth profile, all Cloud Firestore collections, and local device databases with zero retention period."
    )

    PolicySectionCard(
        title = "1. Two Ways to Delete Your Account & Data",
        bullets = listOf(
            "In-App Instant Deletion: Go to the Profile tab -> Scroll down -> Tap 'Delete Account Permanently' and confirm. All cloud and local data is erased immediately.",
            "Web / Email Deletion (No App Required): Visit our HTTPS Account Deletion Portal or email ${VirtuePolicyLinks.SUPPORT_EMAIL} with subject 'Account Deletion Request — Virtue Habit Tracker'. Processed within 7 business days."
        )
    )

    PolicySectionCard(
        title = "2. GDPR, CCPA & Children's Privacy (COPPA)",
        bullets = listOf(
            "Your Statutory Rights: Under GDPR, UK GDPR, and CCPA/CPRA, you have the right to access, edit, export, or erase your personal data and withdraw consent at any time.",
            "Children's Privacy: Virtue Habit Tracker is intended for users aged 13+ (or 16+ in the EEA/UK). We do not knowingly collect data from children under 13.",
            "Data Controller Contact: ${VirtuePolicyLinks.SUPPORT_EMAIL} • Package ID: com.ytron.virtuhabit"
        )
    )
}

@Composable
private fun PolicyBannerCard(
    icon: ImageVector,
    accentColor: Color,
    title: String,
    body: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = accentColor.copy(alpha = 0.11f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(22.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE5E0F0),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun PolicySectionCard(
    title: String,
    bullets: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF201D2E), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFC4B5FD)
        )
        bullets.forEach { item ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "•",
                    color = Color(0xFF00DC82),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFD6D1E6),
                    lineHeight = 18.sp
                )
            }
        }
    }
}
