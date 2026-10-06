package com.virtue.habittracker.presentation.programs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.virtue.habittracker.domain.model.program.ProgramCategory
import com.virtue.habittracker.domain.model.program.ProgramEquipment
import com.virtue.habittracker.domain.model.program.ProgramExperience
import com.virtue.habittracker.domain.model.program.ProgramPreferences
import com.virtue.habittracker.domain.model.program.ProgramTemplate
import com.virtue.habittracker.domain.model.program.WorkActivityLevel
import com.virtue.habittracker.domain.service.BmiCalculator
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
internal fun ProgramPersonalizationScreen(
    template: ProgramTemplate,
    initialPreferences: ProgramPreferences,
    initialStartEpochDay: Long,
    isAdjusting: Boolean,
    onBack: () -> Unit,
    onContinue: (ProgramPreferences, Long, Boolean) -> Unit
) {
    var minutes by remember(template.id, initialPreferences) { mutableStateOf(initialPreferences.availableMinutesPerDay) }
    var days by remember(template.id, initialPreferences) { mutableStateOf(initialPreferences.availableDaysPerWeek) }
    var experience by remember(template.id, initialPreferences) { mutableStateOf(initialPreferences.experience) }
    var equipment by remember(template.id, initialPreferences) { mutableStateOf(initialPreferences.equipment) }
    var workActivity by remember(template.id, initialPreferences) { mutableStateOf(initialPreferences.workActivityLevel) }
    var startEpochDay by remember(template.id, initialStartEpochDay) { mutableStateOf(initialStartEpochDay) }
    var showDatePicker by remember { mutableStateOf(false) }
    var safetyAcknowledged by remember(template.id, isAdjusting) { mutableStateOf(isAdjusting || template.category != ProgramCategory.FITNESS) }
    var age by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(if (isAdjusting) "Adjust your plan" else "Personalize your plan") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
        )
    }) { padding ->
        Column(Modifier.padding(padding).padding(horizontal = 18.dp, vertical = 12.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text(
                if (isAdjusting) "Change the remaining schedule. Completed history stays intact."
                else "Answer only what is relevant. These settings create a deterministic schedule saved locally when you start.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SectionTitle("1 · Time and availability")
            Text("Minutes per day: " + minutes, fontWeight = FontWeight.SemiBold)
            ChoiceRow(listOf(10, 15, 20, 30, 45, 60).map { it to it.toString() }, minutes) { minutes = it }
            Text("Available days per week: " + days, fontWeight = FontWeight.SemiBold)
            ChoiceRow(listOf(3, 4, 5, 6, 7).map { it to it.toString() }, days) { days = it }
            if (!isAdjusting) {
                Text("Preferred start date", fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = LocalDate.ofEpochDay(startEpochDay).format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy")),
                    onValueChange = {}, readOnly = true, modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { TextButton(onClick = { showDatePicker = true }) { Text("Change") } }
                )
            }
            SectionTitle("2 · Experience and equipment")
            Text("Experience", fontWeight = FontWeight.SemiBold)
            ChoiceRow(listOf(ProgramExperience.BEGINNER to "Beginner", ProgramExperience.SOME_EXPERIENCE to "Some", ProgramExperience.EXPERIENCED to "Experienced"), experience) { experience = it }
            Text("Equipment", fontWeight = FontWeight.SemiBold)
            ChoiceRow(listOf(ProgramEquipment.NONE to "No equipment", ProgramEquipment.HOME_BASIC to "Home", ProgramEquipment.GYM to "Gym"), equipment) { equipment = it }

            if (template.category == ProgramCategory.FITNESS) {
                SectionTitle("3 · Daily work activity")
                ChoiceRow(listOf(WorkActivityLevel.MOSTLY_SEATED to "Mostly seated", WorkActivityLevel.MIXED to "Mixed", WorkActivityLevel.PHYSICALLY_ACTIVE to "Physically active"), workActivity) { workActivity = it }
                if (workActivity == WorkActivityLevel.PHYSICALLY_ACTIVE) {
                    Text("Your work already adds physical load, so the planner limits scheduled activity days to protect recovery.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Optional adult BMI screening", fontWeight = FontWeight.SemiBold)
                        Text("Informational only. Values are calculated on-device, not saved or uploaded, and never set exercise intensity or calorie targets.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = age, onValueChange = { age = it.filter(Char::isDigit).take(3) }, label = { Text("Age") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = height, onValueChange = { height = it.filter { c -> c.isDigit() || c == '.' }.take(6) }, label = { Text("Height cm") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = weight, onValueChange = { weight = it.filter { c -> c.isDigit() || c == '.' }.take(6) }, label = { Text("Weight kg") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.weight(1f))
                        }
                        val bmi = BmiCalculator.calculateAdultBmi(age.toIntOrNull(), height.toDoubleOrNull(), weight.toDoubleOrNull())
                        if (bmi != null) {
                            Text("Estimated adult BMI: " + "%.1f".format(java.util.Locale.US, bmi), color = ProgramLavender, fontWeight = FontWeight.SemiBold)
                            Text("BMI is a screening value, not a diagnosis or body-composition measure.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Row(verticalAlignment = Alignment.Top) {
                    Checkbox(checked = safetyAcknowledged, onCheckedChange = { safetyAcknowledged = it })
                    Text("I understand this is general-wellness guidance and not medical advice. I will use my judgment and seek professional advice when appropriate.", Modifier.padding(top = 12.dp), style = MaterialTheme.typography.bodySmall)
                }
            }
            Button(
                onClick = { onContinue(ProgramPreferences(minutes, days, experience, workActivity, equipment), startEpochDay, safetyAcknowledged) },
                modifier = Modifier.fillMaxWidth(),
                enabled = template.category != ProgramCategory.FITNESS || safetyAcknowledged
            ) { Text(if (isAdjusting) "Review changes" else "Review plan") }
            Spacer(Modifier.height(18.dp))
        }
    }
    if (showDatePicker) {
        val picker = rememberDatePickerState(initialSelectedDateMillis = LocalDate.ofEpochDay(startEpochDay).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { picker.selectedDateMillis?.let { startEpochDay = maxOf(LocalDate.now().toEpochDay(), Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()) }; showDatePicker = false }) { Text("Use date") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = picker) }
    }
}

@Composable
private fun SectionTitle(text: String) { Text(text, color = ProgramLavender, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold) }

@Composable
private fun <T> ChoiceRow(choices: List<Pair<T, String>>, selected: T, onSelected: (T) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) { choices.forEach { (value, label) -> FilterChip(selected = selected == value, onClick = { onSelected(value) }, label = { Text(label) }) } }
}
