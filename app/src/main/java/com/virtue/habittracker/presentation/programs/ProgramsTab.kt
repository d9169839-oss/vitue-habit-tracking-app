package com.virtue.habittracker.presentation.programs

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.billingclient.api.ProductDetails
import com.virtue.habittracker.data.billing.PremiumBillingManager
import com.virtue.habittracker.domain.model.program.ProgramCategory
import com.virtue.habittracker.domain.model.program.ProgramEquipment
import com.virtue.habittracker.domain.model.program.ProgramExperience
import com.virtue.habittracker.domain.model.program.ProgramPreferences
import com.virtue.habittracker.domain.model.program.ProgramProgress
import com.virtue.habittracker.domain.model.program.ProgramStatus
import com.virtue.habittracker.domain.model.program.WorkActivityLevel
import java.time.LocalDate

private enum class ProgramRoute { CATALOG, DETAIL, PERSONALIZE, REVIEW, ACTIVE, ADJUST_PERSONALIZE, ADJUST_REVIEW }

@Composable
fun ProgramsTab(
    modifier: Modifier,
    visible: Boolean,
    onFullScreenChange: (Boolean) -> Unit,
    vm: ProgramsViewModel = hiltViewModel(),
    billingViewModel: ProgramsBillingViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val manager = billingViewModel.manager
    val products by manager.products.collectAsState()
    val billingMessage by manager.message.collectAsState()
    val context = LocalContext.current
    var route by remember { mutableStateOf(ProgramRoute.CATALOG) }
    var selectedTemplateId by remember { mutableStateOf<String?>(null) }
    var selectedEnrollmentId by remember { mutableStateOf<String?>(null) }
    var draftPreferences by remember { mutableStateOf(ProgramPreferences()) }
    var draftStartEpochDay by remember { mutableStateOf(LocalDate.now().toEpochDay()) }
    var showPremiumDialog by remember { mutableStateOf(false) }

    val selectedTemplate = vm.catalog.firstOrNull { it.id == selectedTemplateId }
    val selectedProgress = state.programs.firstOrNull { it.enrollment.id == selectedEnrollmentId }

    LaunchedEffect(Unit) { manager.connect() }
    LaunchedEffect(route) { onFullScreenChange(route != ProgramRoute.CATALOG) }

    BackHandler(enabled = route != ProgramRoute.CATALOG) {
        route = when (route) {
            ProgramRoute.DETAIL -> ProgramRoute.CATALOG
            ProgramRoute.PERSONALIZE -> ProgramRoute.DETAIL
            ProgramRoute.REVIEW -> ProgramRoute.PERSONALIZE
            ProgramRoute.ACTIVE -> ProgramRoute.CATALOG
            ProgramRoute.ADJUST_PERSONALIZE -> ProgramRoute.ACTIVE
            ProgramRoute.ADJUST_REVIEW -> ProgramRoute.ADJUST_PERSONALIZE
            ProgramRoute.CATALOG -> ProgramRoute.CATALOG
        }
    }

    Column(
        modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .then(if (visible) Modifier else Modifier.padding(start = 10_000.dp))
    ) {
        when (route) {
            ProgramRoute.CATALOG -> ProgramCatalogScreen(
                state = state,
                onCategory = vm::selectCategory,
                onOpenTemplate = { template ->
                    selectedTemplateId = template.id
                    route = ProgramRoute.DETAIL
                },
                onOpenProgram = { progress ->
                    selectedEnrollmentId = progress.enrollment.id
                    route = ProgramRoute.ACTIVE
                },
                onPremium = { showPremiumDialog = true }
            )
            ProgramRoute.DETAIL -> selectedTemplate?.let { template ->
                ProgramDetailScreen(
                    template = template,
                    isPremium = state.isPremium,
                    onBack = { route = ProgramRoute.CATALOG },
                    onPersonalize = {
                        draftPreferences = ProgramPreferences(
                            availableMinutesPerDay = template.defaultMinutesPerDay.coerceIn(5, 90),
                            availableDaysPerWeek = 5,
                            experience = ProgramExperience.BEGINNER,
                            workActivityLevel = WorkActivityLevel.MIXED,
                            equipment = template.equipment
                        )
                        draftStartEpochDay = LocalDate.now().toEpochDay()
                        route = ProgramRoute.PERSONALIZE
                    },
                    onPremium = { showPremiumDialog = true }
                )
            }
            ProgramRoute.PERSONALIZE, ProgramRoute.ADJUST_PERSONALIZE -> selectedTemplate?.let { template ->
                ProgramPersonalizationScreen(
                    template = template,
                    initialPreferences = draftPreferences,
                    initialStartEpochDay = draftStartEpochDay,
                    isAdjusting = route == ProgramRoute.ADJUST_PERSONALIZE,
                    onBack = {
                        route = if (route == ProgramRoute.ADJUST_PERSONALIZE) ProgramRoute.ACTIVE else ProgramRoute.DETAIL
                    },
                    onContinue = { preferences, startEpochDay, _ ->
                        draftPreferences = preferences
                        draftStartEpochDay = startEpochDay
                        route = if (route == ProgramRoute.ADJUST_PERSONALIZE) ProgramRoute.ADJUST_REVIEW else ProgramRoute.REVIEW
                    }
                )
            }
            ProgramRoute.REVIEW, ProgramRoute.ADJUST_REVIEW -> selectedTemplate?.let { template ->
                val generatedPreview = vm.previewSchedule(template, draftStartEpochDay, draftPreferences)
                val preview = if (route == ProgramRoute.ADJUST_REVIEW) {
                    generatedPreview.filter { it.epochDay >= LocalDate.now().toEpochDay() }.take(7)
                } else generatedPreview.take(7)
                ProgramReviewScreen(
                    template = template,
                    preferences = draftPreferences,
                    startEpochDay = draftStartEpochDay,
                    previewActivities = preview,
                    isAdjusting = route == ProgramRoute.ADJUST_REVIEW,
                    isSaving = state.isStarting,
                    onBack = {
                        route = if (route == ProgramRoute.ADJUST_REVIEW) ProgramRoute.ADJUST_PERSONALIZE else ProgramRoute.PERSONALIZE
                    },
                    onConfirm = {
                        if (route == ProgramRoute.ADJUST_REVIEW && selectedEnrollmentId != null) {
                            vm.replanProgram(selectedEnrollmentId!!, draftPreferences)
                            route = ProgramRoute.ACTIVE
                        } else {
                            vm.startProgram(template.id, draftStartEpochDay, draftPreferences)
                            selectedTemplateId = template.id
                            route = ProgramRoute.CATALOG
                        }
                    }
                )
            }
            ProgramRoute.ACTIVE -> selectedProgress?.let { progress ->
                val template = vm.catalog.firstOrNull { it.id == progress.enrollment.templateId }
                if (template == null) {
                    Text("This program is no longer available.", Modifier.padding(20.dp))
                } else {
                    ActiveProgramScreen(
                        progress = progress,
                        onBack = { route = ProgramRoute.CATALOG },
                        onAdjust = {
                            selectedTemplateId = template.id
                            draftPreferences = progress.enrollment.preferences
                            draftStartEpochDay = progress.enrollment.startEpochDay
                            route = ProgramRoute.ADJUST_PERSONALIZE
                        },
                        onActivityStatus = vm::setActivityStatus,
                        onProgramStatus = vm::setEnrollmentStatus,
                        onDelete = {
                            vm.deleteEnrollment(progress.enrollment.id)
                            selectedEnrollmentId = null
                            route = ProgramRoute.CATALOG
                        }
                    )
                }
            }
        }
    }

    if (showPremiumDialog) {
        PremiumDialog(
            products = products,
            message = billingMessage,
            onDismiss = { showPremiumDialog = false },
            onRestore = manager::refreshPurchases,
            onPurchase = { product ->
                (context as? Activity)?.let { manager.launchPurchase(it, product) }
            }
        )
    }

    state.message?.let { message ->
        AlertDialog(
            onDismissRequest = vm::clearMessage,
            title = { Text("Programs") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = vm::clearMessage) { Text("OK") } }
        )
    }
}

@Composable
private fun ProgramCatalogScreen(
    state: ProgramsUiState,
    onCategory: (ProgramCategory?) -> Unit,
    onOpenTemplate: (com.virtue.habittracker.domain.model.program.ProgramTemplate) -> Unit,
    onOpenProgram: (ProgramProgress) -> Unit,
    onPremium: () -> Unit
) {
    val visiblePrograms = state.programs
    val catalog = ProgramCatalogForUi.all
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("VITUE PROGRAMS", color = ProgramLavender, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text("Become who you choose.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Small, repeatable actions. A plan that fits your real life.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = ProgramCard)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("YOUR NEXT CHAPTER", color = ProgramLavender, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text("A little progress, repeated.", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Choose a guided fitness, grooming, or mindfulness journey. Your plan is saved on this device first and works offline.", color = ProgramMuted)
                if (!state.isPremium) OutlinedButton(onClick = onPremium) { Text("Explore Vitue Premium") }
                else Text("Premium active", color = androidx.compose.ui.graphics.Color(0xFF34D399), fontWeight = FontWeight.SemiBold)
            }
        }
        if (visiblePrograms.isNotEmpty()) {
            Text("YOUR PROGRAMS", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            visiblePrograms.forEach { progress -> ActiveProgramSummaryCard(progress, onOpenProgram) }
        }
        Text("DISCOVER", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = state.selectedCategory == null, onClick = { onCategory(null) }, label = { Text("All") })
            FilterChip(selected = state.selectedCategory == ProgramCategory.FITNESS, onClick = { onCategory(ProgramCategory.FITNESS) }, label = { Text("Fitness") })
            FilterChip(selected = state.selectedCategory == ProgramCategory.SELF_GROOMING, onClick = { onCategory(ProgramCategory.SELF_GROOMING) }, label = { Text("Grooming") })
            FilterChip(selected = state.selectedCategory == ProgramCategory.MINDFULNESS, onClick = { onCategory(ProgramCategory.MINDFULNESS) }, label = { Text("Mindfulness") })
        }
        catalog.filter { state.selectedCategory == null || it.category == state.selectedCategory }.forEach { template ->
            Card(onClick = { onOpenTemplate(template) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = ProgramCard)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(template.category.displayName(), color = ProgramLavender, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text(if (template.isPremium) "PREMIUM" else "FREE", color = if (template.isPremium) ProgramLavender else androidx.compose.ui.graphics.Color(0xFF34D399), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    Text(template.title, color = androidx.compose.ui.graphics.Color(0xFFF8F5FF), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(template.description, color = ProgramMuted, style = MaterialTheme.typography.bodyMedium)
                    Text(template.durationDays.toString() + " days · " + template.defaultMinutesPerDay + " min/day · " + template.difficulty.displayName(), color = ProgramMuted, style = MaterialTheme.typography.labelMedium)
                    Text("View program details →", color = ProgramLavender, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        Text("Fitness programs are general-wellness guidance, not medical advice. Content should be reviewed by a qualified professional before public release.", color = ProgramMuted, style = MaterialTheme.typography.bodySmall)
    }
}

private object ProgramCatalogForUi {
    val all get() = com.virtue.habittracker.domain.model.program.ProgramCatalog.all
}

@Composable
private fun ActiveProgramSummaryCard(progress: ProgramProgress, onOpen: (ProgramProgress) -> Unit) {
    Card(onClick = { onOpen(progress) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(progress.enrollment.titleSnapshot, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(progress.progressPercent.toString() + "%", color = ProgramLavender, fontWeight = FontWeight.Bold)
            }
            Text(progress.enrollment.status.name.lowercase().replaceFirstChar { it.uppercase() }, color = ProgramMuted, style = MaterialTheme.typography.bodySmall)
            Text("Open your plan →", color = ProgramLavender, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun PremiumDialog(
    products: List<ProductDetails>,
    message: String?,
    onDismiss: () -> Unit,
    onRestore: () -> Unit,
    onPurchase: (ProductDetails) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Unlock Vitue Premium") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Get access to premium guided programs while keeping your existing progress and history.")
                if (products.isEmpty()) Text("Subscription options will appear when Google Play Billing is connected and your products are available for this tester.", color = ProgramMuted)
                products.forEach { product ->
                    val offer = product.subscriptionOfferDetails?.firstOrNull()
                    val price = offer?.pricingPhases?.pricingPhaseList?.lastOrNull()?.formattedPrice ?: "View price in Google Play"
                    OutlinedButton(onClick = { onPurchase(product) }, modifier = Modifier.fillMaxWidth()) {
                        Text(product.name + " · " + price)
                    }
                }
                if (message != null) Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = onRestore, modifier = Modifier.fillMaxWidth()) { Text("Restore purchase") }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Not now") } }
    )
}

@dagger.hilt.android.lifecycle.HiltViewModel
class ProgramsBillingViewModel @javax.inject.Inject constructor(val manager: PremiumBillingManager) : androidx.lifecycle.ViewModel()
