package com.virtue.habittracker.presentation.programs

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.billingclient.api.ProductDetails
import com.virtue.habittracker.data.billing.PremiumBillingManager
import com.virtue.habittracker.domain.model.program.ProgramActivityStatus
import com.virtue.habittracker.domain.model.program.ProgramCategory
import com.virtue.habittracker.domain.model.program.ProgramDifficulty
import com.virtue.habittracker.domain.model.program.ProgramEquipment
import com.virtue.habittracker.domain.model.program.ProgramPreferences
import com.virtue.habittracker.domain.model.program.ProgramProgress
import com.virtue.habittracker.domain.model.program.ProgramStatus
import java.time.LocalDate

private val ProgramViolet = Color(0xFFA78BFA)
private val ProgramLavender = Color(0xFFC4B5FD)
private val ProgramCard = Color(0xFF211B30)
private val ProgramMuted = Color(0xFFAAA2BB)

@Composable
fun ProgramsTab(
    modifier: Modifier,
    visible: Boolean,
    vm: ProgramsViewModel = hiltViewModel(),
    billingViewModel: ProgramsBillingViewModel = hiltViewModel()
) {
    val billingManager = billingViewModel.manager
    val state by vm.uiState.collectAsStateWithLifecycle()
    val billingMessage by billingManager.message.collectAsState()
    val products by billingManager.products.collectAsState()
    val context = LocalContext.current
    var selectedTemplateId by remember { mutableStateOf<String?>(null) }
    var showPremiumDialog by remember { mutableStateOf(false) }
    var availableMinutes by remember { mutableStateOf(20) }
    var availableDays by remember { mutableStateOf(5) }
    var equipment by remember { mutableStateOf(ProgramEquipment.NONE) }
    var experience by remember { mutableStateOf(com.virtue.habittracker.domain.model.program.ProgramExperience.BEGINNER) }

    LaunchedEffect(Unit) { billingManager.connect() }

    val selectedTemplate = vm.catalog.firstOrNull { it.id == selectedTemplateId }
    Column(
        modifier = modifier.alpha(if (visible) 1f else 0f)
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("VITUE PROGRAMS", color = ProgramLavender, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text("Become who you choose.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Small, repeatable actions. A plan that fits your real life.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Card(colors = CardDefaults.cardColors(containerColor = ProgramCard), shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("YOUR NEXT CHAPTER", color = ProgramLavender, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text("A little progress, repeated.", color = Color(0xFFF8F5FF), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Choose a guided fitness, grooming, or mindfulness journey. Your plan is saved on this device first and works offline.", color = ProgramMuted)
                if (!state.isPremium) {
                    OutlinedButton(onClick = { showPremiumDialog = true }) { Text("Explore Vitue Premium") }
                } else {
                    Text("Premium active", color = Color(0xFF34D399), fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (state.programs.isNotEmpty()) {
            Text("YOUR PROGRAMS", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            state.programs.forEach { progress -> ActiveProgramCard(progress, vm) }
        }

        Text("DISCOVER", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = state.selectedCategory == null, onClick = { vm.selectCategory(null) }, label = { Text("All") })
            FilterChip(selected = state.selectedCategory == ProgramCategory.FITNESS, onClick = { vm.selectCategory(ProgramCategory.FITNESS) }, label = { Text("Fitness") })
            FilterChip(selected = state.selectedCategory == ProgramCategory.SELF_GROOMING, onClick = { vm.selectCategory(ProgramCategory.SELF_GROOMING) }, label = { Text("Grooming") })
            FilterChip(selected = state.selectedCategory == ProgramCategory.MINDFULNESS, onClick = { vm.selectCategory(ProgramCategory.MINDFULNESS) }, label = { Text("Mindfulness") })
        }

        vm.catalog.filter { state.selectedCategory == null || it.category == state.selectedCategory }.forEach { template ->
            Card(
                onClick = {
                    selectedTemplateId = template.id
                    availableMinutes = template.defaultMinutesPerDay.coerceIn(5, 90)
                    availableDays = 5
                    equipment = template.equipment
                    experience = com.virtue.habittracker.domain.model.program.ProgramExperience.BEGINNER
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ProgramCard)
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(template.category.displayName(), color = ProgramLavender, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text(if (template.isPremium) "PREMIUM" else "FREE", color = if (template.isPremium) ProgramLavender else Color(0xFF34D399), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    Text(template.title, color = Color(0xFFF8F5FF), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(template.description, color = ProgramMuted, style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("${template.durationDays} days", color = ProgramLavender, style = MaterialTheme.typography.labelMedium)
                        Text("${template.defaultMinutesPerDay} min/day", color = ProgramMuted, style = MaterialTheme.typography.labelMedium)
                        Text(template.difficulty.displayName(), color = ProgramMuted, style = MaterialTheme.typography.labelMedium)
                    }
                    Text("View and personalize →", color = ProgramLavender, style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        state.message?.let { message ->
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF392333))) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(message, Modifier.weight(1f), color = Color(0xFFFFC4CF))
                    TextButton(onClick = vm::clearMessage) { Text("Dismiss") }
                }
            }
        }
        Text("Fitness programs are general-wellness guidance, not medical advice. Content must be reviewed by a qualified professional before public release.", color = ProgramMuted, style = MaterialTheme.typography.bodySmall)
    }

    if (selectedTemplate != null) {
        AlertDialog(
            onDismissRequest = { selectedTemplateId = null },
            title = { Text(selectedTemplate.title) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(selectedTemplate.description)
                    Text("Duration: ${selectedTemplate.durationDays} days · ${selectedTemplate.defaultMinutesPerDay} minutes/day")
                    Text("Phases", fontWeight = FontWeight.Bold)
                    selectedTemplate.phases.forEach { phase ->
                        Text("Days ${phase.startDay}–${phase.endDay}: ${phase.title}", fontWeight = FontWeight.SemiBold)
                        Text(phase.instructions, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("Safety note", fontWeight = FontWeight.Bold)
                    Text(selectedTemplate.safetyNote, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text("Personalize your schedule", fontWeight = FontWeight.Bold)
                    Text("Minutes per day: $availableMinutes")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(10, 15, 20, 30, 45).forEach { mins ->
                            FilterChip(selected = availableMinutes == mins, onClick = { availableMinutes = mins }, label = { Text("$mins") })
                        }
                    }
                    Text("Days per week: $availableDays")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(3, 4, 5, 6, 7).forEach { days ->
                            FilterChip(selected = availableDays == days, onClick = { availableDays = days }, label = { Text("$days") })
                        }
                    }
                    Text("Experience")
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            com.virtue.habittracker.domain.model.program.ProgramExperience.BEGINNER to "New",
                            com.virtue.habittracker.domain.model.program.ProgramExperience.SOME_EXPERIENCE to "Some",
                            com.virtue.habittracker.domain.model.program.ProgramExperience.EXPERIENCED to "Experienced"
                        ).forEach { (value, label) ->
                            FilterChip(selected = experience == value, onClick = { experience = value }, label = { Text(label) })
                        }
                    }
                    if (selectedTemplate.category == ProgramCategory.FITNESS) {
                        Text("Equipment")
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(ProgramEquipment.NONE to "No equipment", ProgramEquipment.HOME_BASIC to "Home", ProgramEquipment.GYM to "Gym").forEach { (value, label) ->
                                FilterChip(selected = equipment == value, onClick = { equipment = value }, label = { Text(label) })
                            }
                        }
                    }
                    Text("Height and weight are not required for these starter plans. We do not use BMI alone to prescribe exercise or promise weight changes.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(
                    enabled = !state.isStarting,
                    onClick = {
                        if (selectedTemplate.isPremium && !state.isPremium) {
                            selectedTemplateId = null
                            showPremiumDialog = true
                        } else {
                            vm.startProgram(
                                selectedTemplate.id,
                                LocalDate.now().toEpochDay(),
                                ProgramPreferences(availableMinutes, availableDays, experience, equipment)
                            )
                            selectedTemplateId = null
                        }
                    }
                ) { Text(if (state.isStarting) "Starting…" else if (selectedTemplate.isPremium && !state.isPremium) "Unlock Premium" else "Start program") }
            },
            dismissButton = { TextButton(onClick = { selectedTemplateId = null }) { Text("Cancel") } }
        )
    }

    if (showPremiumDialog) {
        AlertDialog(
            onDismissRequest = { showPremiumDialog = false },
            title = { Text("Unlock Vitue Premium") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Premium includes longer guided programs and additional routines. Purchases are handled by Google Play.")
                    if (products.isEmpty()) {
                        Text("Subscription products are loading. Confirm the product IDs in Play Console before testing purchases.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    products.forEach { product ->
                        val price = product.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice.orEmpty()
                        OutlinedButton(
                            onClick = {
                                (context as? Activity)?.let { billingManager.launchPurchase(it, product) }
                                    ?: run { }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("${product.name} · $price") }
                    }
                    OutlinedButton(onClick = billingManager::refreshPurchases, modifier = Modifier.fillMaxWidth()) { Text("Restore purchases") }
                    billingMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Text("Subscription IDs are placeholders until configured in Google Play Console. Purchase tokens must be verified by a trusted backend before production.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = { TextButton(onClick = { billingManager.refreshProducts() }) { Text("Refresh plans") } },
            dismissButton = { TextButton(onClick = { showPremiumDialog = false }) { Text("Close") } }
        )
    }
}

@Composable
private fun ActiveProgramCard(progress: ProgramProgress, vm: ProgramsViewModel) {
    val enrollment = progress.enrollment
    val today = LocalDate.now().toEpochDay()
    val dayIndex = (today - enrollment.startEpochDay + 1L).coerceIn(1L, enrollment.durationDays.toLong()).toInt()
    val currentActivity = progress.activities.firstOrNull { it.dayIndex == dayIndex }
    Card(colors = CardDefaults.cardColors(containerColor = ProgramCard), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(enrollment.titleSnapshot, Modifier.weight(1f), color = Color(0xFFF8F5FF), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(enrollment.status.name.lowercase().replaceFirstChar { it.uppercase() }, color = ProgramLavender, style = MaterialTheme.typography.labelSmall)
            }
            Text("Day $dayIndex of ${enrollment.durationDays} · ${progress.completedCount}/${progress.totalCount} activities complete", color = ProgramMuted, style = MaterialTheme.typography.bodySmall)
            LinearProgressIndicator(progress = { progress.progressPercent / 100f }, modifier = Modifier.fillMaxWidth().height(6.dp), color = ProgramViolet, trackColor = Color(0xFF393047))
            if (enrollment.status == ProgramStatus.ACTIVE && currentActivity != null) {
                Text("TODAY · ${currentActivity.phaseTitle}", color = ProgramLavender, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text(currentActivity.title, color = Color(0xFFF8F5FF), fontWeight = FontWeight.SemiBold)
                Text(currentActivity.instructions, color = ProgramMuted, style = MaterialTheme.typography.bodySmall)
                if (currentActivity.estimatedMinutes > 0) Text("${currentActivity.estimatedMinutes} minutes", color = ProgramMuted, style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        enabled = currentActivity.status != ProgramActivityStatus.COMPLETED,
                        onClick = { vm.setActivityStatus(currentActivity.id, ProgramActivityStatus.COMPLETED) }
                    ) { Text(if (currentActivity.status == ProgramActivityStatus.COMPLETED) "Completed" else "Complete") }
                    OutlinedButton(
                        enabled = currentActivity.status == ProgramActivityStatus.PENDING,
                        onClick = { vm.setActivityStatus(currentActivity.id, ProgramActivityStatus.SKIPPED) }
                    ) { Text("Skip") }
                }
            } else if (enrollment.status == ProgramStatus.PAUSED) {
                Text("Your plan is paused. Resume whenever you're ready.", color = ProgramMuted)
            } else if (enrollment.status == ProgramStatus.COMPLETED) {
                Text("Program completed. Great work showing up for yourself.", color = Color(0xFF34D399))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (enrollment.status == ProgramStatus.ACTIVE) {
                    OutlinedButton(onClick = { vm.setEnrollmentStatus(enrollment.id, ProgramStatus.PAUSED) }) { Text("Pause") }
                    if (dayIndex == enrollment.durationDays && progress.completedCount == progress.totalCount) {
                        Button(onClick = { vm.setEnrollmentStatus(enrollment.id, ProgramStatus.COMPLETED) }) { Text("Finish") }
                    }
                } else if (enrollment.status == ProgramStatus.PAUSED) {
                    Button(onClick = { vm.setEnrollmentStatus(enrollment.id, ProgramStatus.ACTIVE) }) { Text("Resume") }
                }
                TextButton(onClick = { vm.deleteEnrollment(enrollment.id) }) { Text("Remove") }
            }
        }
    }
}

private fun ProgramCategory.displayName() = when (this) {
    ProgramCategory.FITNESS -> "FITNESS"
    ProgramCategory.SELF_GROOMING -> "SELF-GROOMING"
    ProgramCategory.MINDFULNESS -> "MINDFULNESS"
}
private fun ProgramDifficulty.displayName() = when (this) {
    ProgramDifficulty.BEGINNER -> "Beginner"
    ProgramDifficulty.INTERMEDIATE -> "Intermediate"
}

/** Lifecycle-aware host for observing and invoking the application-scoped billing manager. */
@dagger.hilt.android.lifecycle.HiltViewModel
class ProgramsBillingViewModel @javax.inject.Inject constructor(
    val manager: PremiumBillingManager
) : androidx.lifecycle.ViewModel()
