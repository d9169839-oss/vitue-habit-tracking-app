package com.virtue.habittracker.presentation.onboarding

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Manages whether a given user (or device session) has completed the first-time app walkthrough.
 * Keyed per user UID so every new user account sees the onboarding guide automatically on first sign-in.
 */
object OnboardingPreferences {
    private const val PREFS_NAME = "virtue_user_onboarding_prefs"
    private const val KEY_PREFIX = "completed_onboarding_"

    fun hasCompletedOnboarding(context: Context, userId: String?): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = KEY_PREFIX + (userId?.takeIf { it.isNotBlank() } ?: "guest")
        return prefs.getBoolean(key, false)
    }

    fun markOnboardingCompleted(context: Context, userId: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = KEY_PREFIX + (userId?.takeIf { it.isNotBlank() } ?: "guest")
        prefs.edit().putBoolean(key, true).apply()
    }
}

data class StarterHabitTemplate(
    val title: String,
    val description: String,
    val tag: String
)

private val OnboardingStarterHabits = listOf(
    StarterHabitTemplate(
        title = "Morning Hydration (500ml Water)",
        description = "Rehydrate immediately after waking before coffee or screens",
        tag = "METABOLIC"
    ),
    StarterHabitTemplate(
        title = "10-Min Morning Sunlight Walk",
        description = "Anchor your circadian cortisol peak and evening melatonin onset",
        tag = "CIRCADIAN"
    ),
    StarterHabitTemplate(
        title = "90-Min Deep Work Focus Sprint",
        description = "Single-task high-value work with phone in another room",
        tag = "COGNITIVE"
    ),
    StarterHabitTemplate(
        title = "5-Min Vagal Box Breathing",
        description = "Inhale 4s, hold 4s, exhale 4s, hold 4s to lower heart rate & stress",
        tag = "CBT & CALM"
    ),
    StarterHabitTemplate(
        title = "Evening Digital Sunset (No Screens)",
        description = "Put away bright screens 45 minutes before target sleep time",
        tag = "SLEEP CBT-I"
    )
)

/**
 * Luxury 4-Stage Interactive App Onboarding Walkthrough inspired by Whoop, Oura, Fabulous & Linear.
 * Guides every new user on:
 * 1. Welcome & The Virtue System (Habits + Guided Curricula)
 * 2. How to Add & Check Off Daily Habits (+ Interactive 1-Tap Starter Habit Selector)
 * 3. How to Personalize & Run the 12 Guided Programs (4-Step Blueprint + Guided Timer)
 * 4. How to Inspect Streak Velocity, 7-Day/Monthly History Charts & Smart Reminders
 */
@Composable
fun VirtueOnboardingScreen(
    userDisplayName: String,
    existingHabitCount: Int,
    onCreateStarterHabit: (title: String, description: String) -> Unit,
    onFinishOnboarding: (openProgramsTab: Boolean) -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }
    val totalSteps = 4

    // Pre-select 2 high-impact starter habits if the new user currently has 0 habits
    val selectedStarters = remember {
        mutableStateListOf<String>().apply {
            if (existingHabitCount == 0) {
                add(OnboardingStarterHabits[0].title)
                add(OnboardingStarterHabits[1].title)
            }
        }
    }

    fun completeWalkthrough(openPrograms: Boolean) {
        selectedStarters.forEach { selectedTitle ->
            val template = OnboardingStarterHabits.firstOrNull { it.title == selectedTitle }
            if (template != null) {
                onCreateStarterHabit(template.title, template.description)
            }
        }
        onFinishOnboarding(openPrograms)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A162B),
                        Color(0xFF12111A),
                        Color(0xFF0E0D14)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        val isCompactHeight = maxHeight < 700.dp
        val horizontalPad = if (maxWidth < 360.dp) 16.dp else 22.dp

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar: Stepper Dots + Skip Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPad, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(totalSteps) { index ->
                        val isSelected = index == currentStep
                        val isCompleted = index < currentStep
                        val pillWidth by animateDpAsState(
                            targetValue = if (isSelected) 32.dp else 10.dp,
                            animationSpec = tween(250),
                            label = "onboardingDotWidth"
                        )
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(pillWidth)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    when {
                                        isSelected -> Color(0xFF00DC82)
                                        isCompleted -> Color(0xFFA78BFA)
                                        else -> Color(0xFF2E2A42)
                                    }
                                )
                                .clickable { currentStep = index }
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "STEP ${currentStep + 1}/$totalSteps",
                        color = Color(0xFFC4B5FD),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    )
                }

                TextButton(
                    onClick = { completeWalkthrough(openPrograms = false) }
                ) {
                    Text(
                        text = "Skip Tour",
                        color = Color(0xFF9E98B3),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Animated Step Body
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    fadeIn(tween(240)) togetherWith fadeOut(tween(180))
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                label = "onboardingStepContent"
            ) { step ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = horizontalPad, vertical = if (isCompactHeight) 6.dp else 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (step) {
                        0 -> WelcomeArchitectureStep(userDisplayName = userDisplayName)
                        1 -> HabitMasteryAndStarterStep(
                            selectedTitles = selectedStarters,
                            onToggleStarter = { title ->
                                if (selectedStarters.contains(title)) {
                                    selectedStarters.remove(title)
                                } else {
                                    selectedStarters.add(title)
                                }
                            }
                        )
                        2 -> GuidedProgramsTourStep()
                        else -> HistoryTelemetryAndFinishStep()
                    }
                }
            }

            // Pinned Luxury Bottom Dock
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF171524),
                border = BorderStroke(1.dp, Color(0xFFA78BFA).copy(alpha = 0.25f)),
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPad, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentStep > 0) {
                            IconButton(
                                onClick = { currentStep-- },
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(Color(0xFF242038), RoundedCornerShape(16.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Previous step",
                                    tint = Color(0xFFC4B5FD)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (currentStep < totalSteps - 1) {
                                    currentStep++
                                } else {
                                    completeWalkthrough(openPrograms = false)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentStep == totalSteps - 1) {
                                    Color(0xFF00DC82)
                                } else {
                                    Color(0xFFA78BFA)
                                },
                                contentColor = Color(0xFF110D1C)
                            )
                        ) {
                            Text(
                                text = when (currentStep) {
                                    0 -> "How Daily Habits Work"
                                    1 -> if (selectedStarters.isNotEmpty()) {
                                        "Save ${selectedStarters.size} Starter Habits & Next"
                                    } else {
                                        "Next: Guided Programs"
                                    }
                                    2 -> "Next: History & Analytics"
                                    else -> "Enter Virtue Command Center"
                                },
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (currentStep == totalSteps - 1) {
                        TextButton(
                            onClick = { completeWalkthrough(openPrograms = true) },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                text = "Or jump straight into the 12 Guided Programs →",
                                color = Color(0xFFC4B5FD),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeArchitectureStep(userDisplayName: String) {
    val firstName = userDisplayName.trim().substringBefore(" ").ifBlank { "Member" }

    OnboardingEyebrowBadge(
        text = "WELCOME TO VIRTUE • 100% FREE FOREVER",
        accentColor = Color(0xFF00DC82)
    )

    Text(
        text = "Welcome, $firstName.\nBecome who you choose.",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.ExtraBold,
        color = Color.White,
        lineHeight = 36.sp
    )

    Text(
        text = "Virtue combines effortless daily habit tracking with 12 clinical & metabolic guided programs—built on behavioral neuroscience, CBT, and exercise physiology.",
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFFB3ADD0),
        lineHeight = 22.sp
    )

    // 3 Pillars Luxury Preview Card
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
                            Color(0xFF2D2546),
                            Color(0xFF1E1B2E),
                            Color(0xFF171522)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(900f, 900f)
                    )
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "HOW VIRTUE WORKS IN 3 PILLARS",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFC4B5FD),
                letterSpacing = 1.3.sp
            )

            OnboardingFeatureRow(
                icon = Icons.Filled.CheckCircle,
                tint = Color(0xFF00DC82),
                title = "1. Home Tab — Daily Habit Command Center",
                subtitle = "Create custom habits or 1-tap starters, log daily check-ins, and launch Today's Active Program Spotlight."
            )

            OnboardingFeatureRow(
                icon = Icons.Filled.FitnessCenter,
                tint = Color(0xFFA78BFA),
                title = "2. Programs Tab — 12 Bespoke Curricula",
                subtitle = "Calibrate 28–56 day programs for Fitness, CBT Anxiety Relief, Circadian Sleep (CBT-I), and Dopamine Focus."
            )

            OnboardingFeatureRow(
                icon = Icons.Filled.Insights,
                tint = Color(0xFFFBBF24),
                title = "3. History Tab — Streak & Consistency Telemetry",
                subtitle = "Inspect your 7-day & monthly completion charts, streak velocity, and restore archived habits anytime."
            )
        }
    }
}

@Composable
private fun HabitMasteryAndStarterStep(
    selectedTitles: List<String>,
    onToggleStarter: (String) -> Unit
) {
    OnboardingEyebrowBadge(
        text = "PILLAR 1 • DAILY HABITS & CHECK-INS",
        accentColor = Color(0xFFA78BFA)
    )

    Text(
        text = "Build your daily ritual in seconds.",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.ExtraBold,
        color = Color.White,
        lineHeight = 34.sp
    )

    Text(
        text = "On the Home tab, tap the '+ New Habit' button to create any custom habit, or tap the checkmark on a habit card to record today's completion and grow your streak.",
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFFB3ADD0),
        lineHeight = 21.sp
    )

    // Interactive Starter Habit Picker
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1A2B)),
        border = BorderStroke(1.dp, Color(0xFF00DC82).copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PICK YOUR FIRST STARTER HABITS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF00DC82),
                    letterSpacing = 1.1.sp
                )
                Text(
                    text = "${selectedTitles.size} selected",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC4B5FD)
                )
            }

            Text(
                text = "Tap any science-backed habit below to pre-load it onto your Home screen right now:",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9E98B3)
            )

            OnboardingStarterHabits.forEach { template ->
                val isSelected = selectedTitles.contains(template.title)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onToggleStarter(template.title) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) Color(0xFF132F26) else Color(0xFF252236),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) Color(0xFF00DC82) else Color(0xFF35314B)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = if (isSelected) {
                                Icons.Filled.CheckCircle
                            } else {
                                Icons.Filled.AddCircleOutline
                            },
                            contentDescription = null,
                            tint = if (isSelected) Color(0xFF00DC82) else Color(0xFF9E98B3),
                            modifier = Modifier.size(22.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = template.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = template.description,
                                color = Color(0xFFB3ADD0),
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFF2E2448)
                        ) {
                            Text(
                                text = template.tag,
                                color = Color(0xFFC4B5FD),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GuidedProgramsTourStep() {
    OnboardingEyebrowBadge(
        text = "PILLAR 2 • 12 SCIENCE-BACKED GUIDED PROGRAMS",
        accentColor = Color(0xFF00DC82)
    )

    Text(
        text = "Personalized 28–56 day mind & body blueprints.",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.ExtraBold,
        color = Color.White,
        lineHeight = 34.sp
    )

    Text(
        text = "Open the Programs tab to enroll in any of our 12 guided curricula—100% free, with zero locked tiers. You can even run multiple programs simultaneously.",
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFFB3ADD0),
        lineHeight = 21.sp
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1A2B)),
        border = BorderStroke(1.dp, Color(0xFFA78BFA).copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "HOW TO SELECT & RUN A PROGRAM",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFC4B5FD),
                letterSpacing = 1.2.sp
            )

            OnboardingFeatureRow(
                icon = Icons.Filled.AutoAwesome,
                tint = Color(0xFFA78BFA),
                title = "Step 1: 4-Stage Bespoke Calibration",
                subtitle = "Answer 4 quick diagnostic steps (Baseline → Focus → Cadence → Blueprint) to compute your Mifflin-St Jeor BMR/TDEE or clinical CBT protocol."
            )

            OnboardingFeatureRow(
                icon = Icons.Filled.SelfImprovement,
                tint = Color(0xFF00DC82),
                title = "Step 2: 3-Phase Roadmap & Week-1 Preview",
                subtitle = "Inspect your entire 3-Phase curriculum (Foundation → Adaptation → Mastery) before starting Day 1."
            )

            OnboardingFeatureRow(
                icon = Icons.Filled.Timer,
                tint = Color(0xFFFBBF24),
                title = "Step 3: Live Guided Session Countdown Timer",
                subtitle = "Tap 'Start Guided Session' on any active day to launch a live countdown timer with step-by-step coaching cues."
            )
        }
    }
}

@Composable
private fun HistoryTelemetryAndFinishStep() {
    OnboardingEyebrowBadge(
        text = "PILLAR 3 • HISTORY, ANALYTICS & CLOUD SYNC",
        accentColor = Color(0xFFFBBF24)
    )

    Text(
        text = "Measure your momentum & never lose progress.",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.ExtraBold,
        color = Color.White,
        lineHeight = 34.sp
    )

    Text(
        text = "What gets measured gets mastered. Use the History and Profile tabs to inspect your consistency trends and customize daily reminders.",
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFFB3ADD0),
        lineHeight = 21.sp
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1A2B)),
        border = BorderStroke(1.dp, Color(0xFFFBBF24).copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "YOUR TELEMETRY & PRIVACY TOOLKIT",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFFBBF24),
                letterSpacing = 1.2.sp
            )

            OnboardingFeatureRow(
                icon = Icons.Filled.LocalFireDepartment,
                tint = Color(0xFFFF7A59),
                title = "7-Day & Monthly Consistency Charts",
                subtitle = "Open the History tab to see your daily completion percentages, streak velocity, and restore archived habits with 1 tap."
            )

            OnboardingFeatureRow(
                icon = Icons.Filled.Insights,
                tint = Color(0xFF00DC82),
                title = "Offline-First Speed + Private Cloud Backup",
                subtitle = "Your habits and program progress checkpoints sync automatically to your encrypted account so you can switch devices seamlessly."
            )

            OnboardingFeatureRow(
                icon = Icons.Filled.CheckCircle,
                tint = Color(0xFFC4B5FD),
                title = "Replay This Guide Anytime",
                subtitle = "You can reopen this Interactive App Walkthrough anytime from Profile → App Tour & Onboarding Guide."
            )
        }
    }
}

@Composable
private fun OnboardingEyebrowBadge(
    text: String,
    accentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = accentColor.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f))
    ) {
        Text(
            text = text,
            color = accentColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun OnboardingFeatureRow(
    icon: ImageVector,
    tint: Color,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(tint.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = subtitle,
                color = Color(0xFFB3ADD0),
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}
