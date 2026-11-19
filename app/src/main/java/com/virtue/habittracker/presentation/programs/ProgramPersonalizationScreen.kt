package com.virtue.habittracker.presentation.programs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.virtue.habittracker.domain.model.program.BiologicalSex
import com.virtue.habittracker.domain.model.program.Biometrics
import com.virtue.habittracker.domain.model.program.DailyOccupation
import com.virtue.habittracker.domain.model.program.ProgramEquipment
import com.virtue.habittracker.domain.model.program.ProgramExperience
import com.virtue.habittracker.domain.model.program.ProgramPreferences
import com.virtue.habittracker.domain.model.program.ProgramTemplate
import com.virtue.habittracker.domain.model.program.WorkActivityLevel
import com.virtue.habittracker.domain.service.FitnessMetabolicCalculator
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ProgramPersonalizationScreen(
    template: ProgramTemplate,
    initialPreferences: ProgramPreferences,
    initialStartEpochDay: Long,
    isAdjusting: Boolean,
    onBack: () -> Unit,
    onContinue: (ProgramPreferences, Long, Boolean) -> Unit
) {
    var currentStep by remember { mutableIntStateOf(1) }

    // Step 1: Biometrics
    var sex by remember(initialPreferences) { mutableStateOf(initialPreferences.biometrics.sex) }
    var age by remember(initialPreferences) { mutableIntStateOf(initialPreferences.biometrics.age) }
    var heightCm by remember(initialPreferences) { mutableStateOf(initialPreferences.biometrics.heightCm) }
    var weightKg by remember(initialPreferences) { mutableStateOf(initialPreferences.biometrics.weightKg) }

    // Step 2: Experience & Occupation
    var experience by remember(initialPreferences) { mutableStateOf(initialPreferences.experience) }
    var occupation by remember(initialPreferences) { mutableStateOf(initialPreferences.biometrics.occupation) }

    // Step 3: Availability & Equipment
    var minutes by remember(initialPreferences) { mutableIntStateOf(initialPreferences.availableMinutesPerDay) }
    var days by remember(initialPreferences) { mutableIntStateOf(initialPreferences.availableDaysPerWeek) }
    var equipment by remember(initialPreferences) { mutableStateOf(initialPreferences.equipment) }
    var startEpochDay by remember(initialStartEpochDay) { mutableStateOf(initialStartEpochDay) }
    var showDatePicker by remember { mutableStateOf(false) }

    val biometrics = Biometrics(sex, age, heightCm, weightKg, occupation)
    val workActivityLevel = when (occupation) {
        DailyOccupation.DESK -> WorkActivityLevel.MOSTLY_SEATED
        DailyOccupation.LIGHT, DailyOccupation.ON_FEET -> WorkActivityLevel.MIXED
        DailyOccupation.PHYSICAL -> WorkActivityLevel.PHYSICALLY_ACTIVE
    }

    val currentPreferences = ProgramPreferences(
        biometrics = biometrics,
        availableMinutesPerDay = minutes,
        availableDaysPerWeek = days,
        experience = experience,
        workActivityLevel = workActivityLevel,
        equipment = equipment
    )

    val metabolicProfile = FitnessMetabolicCalculator.calculate(
        biometrics = biometrics,
        experience = experience,
        focusProgramId = template.id
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF12111A))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Top Header Bar (Back button, Title, Profile Avatar)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = {
                        if (currentStep > 1) currentStep-- else onBack()
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Fitness Goals",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    color = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF262436), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("👤", fontSize = 16.sp)
            }
        }

        // Step Badge & Progress Segmented Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFF1E1C2B),
                border = BorderStroke(1.dp, Color(0xFF2E2C3E))
            ) {
                Text(
                    text = "STEP $currentStep OF 4",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC4B5FD),
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Text(
                text = "${currentStep * 25}% Complete",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF8E8AA7)
            )
        }

        // 4-Segment Progress Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            (1..4).forEach { stepIndex ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (stepIndex <= currentStep) Color(0xFFA78BFA) else Color(0xFF282536))
                )
            }
        }

        // STEP CONTENT SWITCHER
        when (currentStep) {
            1 -> Step1AboutYou(
                sex = sex,
                onSexChange = { sex = it },
                age = age,
                onAgeChange = { age = it },
                heightCm = heightCm,
                onHeightChange = { heightCm = it },
                weightKg = weightKg,
                onWeightChange = { weightKg = it },
                onContinue = { currentStep = 2 }
            )
            2 -> Step2YourExperience(
                experience = experience,
                onExperienceChange = { experience = it },
                occupation = occupation,
                onOccupationChange = { occupation = it },
                onContinue = { currentStep = 3 }
            )
            3 -> Step3TimeAndEquipment(
                minutes = minutes,
                onMinutesChange = { minutes = it },
                days = days,
                onDaysChange = { days = it },
                equipment = equipment,
                onEquipmentChange = { equipment = it },
                startEpochDay = startEpochDay,
                onOpenDatePicker = { showDatePicker = true },
                onContinue = { currentStep = 4 }
            )
            4 -> Step4CalibratedReview(
                profile = metabolicProfile,
                preferences = currentPreferences,
                startEpochDay = startEpochDay,
                onConfirm = { onContinue(currentPreferences, startEpochDay, true) }
            )
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = LocalDate.ofEpochDay(startEpochDay).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            val selected = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                            startEpochDay = maxOf(LocalDate.now().toEpochDay(), selected.toEpochDay())
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Select Date", fontWeight = FontWeight.Bold, color = Color(0xFFA78BFA))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = Color(0xFF8E8AA7)) }
            }
        ) { DatePicker(state = pickerState) }
    }
}

// ==========================================
// STEP 1: ABOUT YOU (Matching Screenshot 1)
// ==========================================
@Composable
private fun Step1AboutYou(
    sex: BiologicalSex,
    onSexChange: (BiologicalSex) -> Unit,
    age: Int,
    onAgeChange: (Int) -> Unit,
    heightCm: Double,
    onHeightChange: (Double) -> Unit,
    weightKg: Double,
    onWeightChange: (Double) -> Unit,
    onContinue: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        // Headline & Subtitle
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "About you",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = Color.White
            )
            Text(
                text = "Your baseline biometrics help calculate resting metabolic rate and energy expenditure with clinical precision.",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFF9E8AA7),
                lineHeight = 20.sp
            )
        }

        // BIOLOGICAL SEX SECTION
        Text(
            text = "BIOLOGICAL SEX",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF8E8AA7),
            letterSpacing = 1.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Male Option Card
            SexOptionCard(
                title = "Male",
                subtitle = "Assigned at birth",
                icon = "♂",
                isSelected = sex == BiologicalSex.MALE,
                onClick = { onSexChange(BiologicalSex.MALE) },
                modifier = Modifier.weight(1f)
            )

            // Female Option Card
            SexOptionCard(
                title = "Female",
                subtitle = "Assigned at birth",
                icon = "♀",
                isSelected = sex == BiologicalSex.FEMALE,
                onClick = { onSexChange(BiologicalSex.FEMALE) },
                modifier = Modifier.weight(1f)
            )
        }

        // PHYSICAL DIMENSIONS SECTION
        Text(
            text = "PHYSICAL DIMENSIONS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF8E8AA7),
            letterSpacing = 1.sp
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Age Stepper Card
            DimensionStepperCard(
                label = "Age",
                valueText = age.toString(),
                unitText = "yrs",
                onDecrement = { if (age > 14) onAgeChange(age - 1) },
                onIncrement = { if (age < 100) onAgeChange(age + 1) }
            )

            // Height Stepper Card
            DimensionStepperCard(
                label = "Height",
                valueText = heightCm.toInt().toString(),
                unitText = "cm",
                onDecrement = { if (heightCm > 100) onHeightChange(heightCm - 1.0) },
                onIncrement = { if (heightCm < 240) onHeightChange(heightCm + 1.0) }
            )

            // Weight Stepper Card
            DimensionStepperCard(
                label = "Weight",
                valueText = "%.1f".format(java.util.Locale.US, weightKg),
                unitText = "kg",
                onDecrement = { if (weightKg > 30.0) onHeightChange(weightKg - 0.5) },
                onIncrement = { if (weightKg < 250.0) onWeightChange(weightKg + 0.5) }
            )
        }

        // Footer Note
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🔒", fontSize = 13.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Processed on-device · Used solely for metabolic calibration",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF8E8AA7)
            )
        }

        // Primary CTA Button
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFA78BFA),
                contentColor = Color(0xFF171020)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("→", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ===============================================
// STEP 2: YOUR EXPERIENCE (Matching Screenshot 2)
// ===============================================
@Composable
private fun Step2YourExperience(
    experience: ProgramExperience,
    onExperienceChange: (ProgramExperience) -> Unit,
    occupation: DailyOccupation,
    onOccupationChange: (DailyOccupation) -> Unit,
    onContinue: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        // Headline & Subtitle
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Your experience",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = Color.White
            )
            Text(
                text = "We tailor volume, exercise complexity, and caloric expenditure to your training history and daily activity.",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFF9E8AA7),
                lineHeight = 20.sp
            )
        }

        // GYM EXPERIENCE SECTION
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "GYM EXPERIENCE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8E8AA7),
                letterSpacing = 1.sp
            )
            Text(
                text = "Baseline Volume",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC4B5FD)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Never trained
            ExperienceOptionCard(
                title = "Never trained",
                subtitle = "Brand new to resistance training or starting fresh",
                icon = "🌱",
                isRecommended = false,
                isSelected = experience == ProgramExperience.BEGINNER,
                onClick = { onExperienceChange(ProgramExperience.BEGINNER) }
            )

            // Beginner (Recommended)
            ExperienceOptionCard(
                title = "Beginner",
                subtitle = "Under 1 year · Learning form, fundamental compound movements",
                icon = "🏋️",
                isRecommended = true,
                isSelected = experience == ProgramExperience.BEGINNER,
                onClick = { onExperienceChange(ProgramExperience.BEGINNER) }
            )

            // Intermediate
            ExperienceOptionCard(
                title = "Intermediate",
                subtitle = "1 to 3 years · Familiar with compound lifts and overload",
                icon = "🤸",
                isRecommended = false,
                isSelected = experience == ProgramExperience.SOME_EXPERIENCE,
                onClick = { onExperienceChange(ProgramExperience.SOME_EXPERIENCE) }
            )

            // Advanced
            ExperienceOptionCard(
                title = "Advanced",
                subtitle = "3+ years · Periodized programming, near-maximal adaptation",
                icon = "🎖️",
                isRecommended = false,
                isSelected = experience == ProgramExperience.EXPERIENCED,
                onClick = { onExperienceChange(ProgramExperience.EXPERIENCED) }
            )
        }

        // DAILY OCCUPATION SECTION
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DAILY OCCUPATION",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8E8AA7),
                letterSpacing = 1.sp
            )
            Text(
                text = "NEAT Index",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00DC82)
            )
        }

        // Segmented Tabs: Desk, Light, On feet, Physical
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1C2A), RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            DailyOccupation.entries.forEach { occ ->
                val isSelected = occupation == occ
                val label = when (occ) {
                    DailyOccupation.DESK -> "Desk"
                    DailyOccupation.LIGHT -> "Light"
                    DailyOccupation.ON_FEET -> "On feet"
                    DailyOccupation.PHYSICAL -> "Physical"
                }

                Surface(
                    onClick = { onOccupationChange(occ) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) Color(0xFFA78BFA) else Color.Transparent
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color(0xFF171020) else Color(0xFF8E8AA7)
                        )
                    }
                }
            }
        }

        // Occupation Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF171522)),
            border = BorderStroke(1.dp, Color(0xFF282538))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = when (occupation) {
                        DailyOccupation.DESK -> "🪑"
                        DailyOccupation.LIGHT -> "🚶"
                        DailyOccupation.ON_FEET -> "🏃"
                        DailyOccupation.PHYSICAL -> "🏋️"
                    },
                    fontSize = 20.sp
                )

                Text(
                    text = when (occupation) {
                        DailyOccupation.DESK -> "Mostly seated · e.g., Software, remote office, driving (~1.2× NEAT multiplier)"
                        DailyOccupation.LIGHT -> "Light activity · e.g., Teaching, retail, walking (~1.38× NEAT multiplier)"
                        DailyOccupation.ON_FEET -> "On feet all day · e.g., Nursing, hospitality, trades (~1.55× NEAT multiplier)"
                        DailyOccupation.PHYSICAL -> "Heavy physical labor · e.g., Construction, farming (~1.73× NEAT multiplier)"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFC4B5FD),
                    lineHeight = 18.sp
                )
            }
        }

        // Calibration Note Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B192A)),
            border = BorderStroke(1.dp, Color(0xFF29243E))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("✨", fontSize = 18.sp)
                Text(
                    text = "This calibration calculates initial training volume per muscle group and foundational daily caloric burn.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9E8AA7),
                    lineHeight = 18.sp
                )
            }
        }

        // Primary CTA Button
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFA78BFA),
                contentColor = Color(0xFF171020)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("→", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==================================================
// STEP 3: TIME & EQUIPMENT
// ==================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Step3TimeAndEquipment(
    minutes: Int,
    onMinutesChange: (Int) -> Unit,
    days: Int,
    onDaysChange: (Int) -> Unit,
    equipment: ProgramEquipment,
    onEquipmentChange: (ProgramEquipment) -> Unit,
    startEpochDay: Long,
    onOpenDatePicker: () -> Unit,
    onContinue: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Time & Equipment",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = Color.White
            )
            Text(
                text = "Set your daily session duration, weekly workout frequency, and equipment access.",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFF9E8AA7)
            )
        }

        Text("MINUTES PER DAY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF8E8AA7), letterSpacing = 1.sp)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(15, 20, 30, 45, 60).forEach { min ->
                Surface(
                    onClick = { onMinutesChange(min) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (minutes == min) Color(0xFFA78BFA) else Color(0xFF1E1C2A),
                    border = BorderStroke(1.dp, if (minutes == min) Color(0xFFA78BFA) else Color(0xFF2E2C3D))
                ) {
                    Text(
                        text = "$min min",
                        color = if (minutes == min) Color(0xFF171020) else Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }
        }

        Text("TRAINING DAYS PER WEEK", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF8E8AA7), letterSpacing = 1.sp)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(3, 4, 5, 6).forEach { d ->
                Surface(
                    onClick = { onDaysChange(d) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (days == d) Color(0xFFA78BFA) else Color(0xFF1E1C2A),
                    border = BorderStroke(1.dp, if (days == d) Color(0xFFA78BFA) else Color(0xFF2E2C3D))
                ) {
                    Text(
                        text = "$d days/wk",
                        color = if (days == d) Color(0xFF171020) else Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }
        }

        Text("EQUIPMENT ACCESS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF8E8AA7), letterSpacing = 1.sp)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            EquipmentOptionCard(
                title = "No equipment",
                subtitle = "Bodyweight, mobility, and movement routines",
                icon = "🪴",
                isSelected = equipment == ProgramEquipment.NONE,
                onClick = { onEquipmentChange(ProgramEquipment.NONE) }
            )
            EquipmentOptionCard(
                title = "Home Basic",
                subtitle = "Dumbbells, resistance bands, or adjustable weights",
                icon = "🏠",
                isSelected = equipment == ProgramEquipment.HOME_BASIC,
                onClick = { onEquipmentChange(ProgramEquipment.HOME_BASIC) }
            )
            EquipmentOptionCard(
                title = "Gym access",
                subtitle = "Full barbell, cable, machine, and free-weight suite",
                icon = "🏋️",
                isSelected = equipment == ProgramEquipment.GYM,
                onClick = { onEquipmentChange(ProgramEquipment.GYM) }
            )
        }

        Text("START DATE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF8E8AA7), letterSpacing = 1.sp)
        OutlinedTextField(
            value = LocalDate.ofEpochDay(startEpochDay).format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy")),
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = { TextButton(onClick = onOpenDatePicker) { Text("Change", color = Color(0xFFA78BFA)) } }
        )

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFA78BFA),
                contentColor = Color(0xFF171020)
            )
        ) {
            Text("Continue to Calibrated Review →", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ==================================================
// STEP 4: CALIBRATED REVIEW
// ==================================================
@Composable
private fun Step4CalibratedReview(
    profile: com.virtue.habittracker.domain.service.MetabolicProfile,
    preferences: ProgramPreferences,
    startEpochDay: Long,
    onConfirm: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Calibrated targets",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = Color.White
            )
            Text(
                text = "Here is your personalized metabolic breakdown and volume baseline calculated from clinical fitness equations.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9E8AA7)
            )
        }

        // METABOLIC STATS GRID
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(label = "BMR", value = "${profile.bmr} kcal", modifier = Modifier.weight(1f))
            StatCard(label = "TDEE", value = "${profile.tdee} kcal", modifier = Modifier.weight(1f))
            StatCard(label = "TARGET", value = "${profile.targetCalories} kcal", accentColor = Color(0xFF00DC82), modifier = Modifier.weight(1.2f))
        }

        // MACRONUTRIENT SPLIT CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF171522)),
            border = BorderStroke(1.dp, Color(0xFF282538))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "TARGET MACRONUTRIENTS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC4B5FD),
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MacroItem("Protein", "${profile.proteinGrams}g", Color(0xFFA78BFA))
                    MacroItem("Carbs", "${profile.carbsGrams}g", Color(0xFF80D8FF))
                    MacroItem("Fats", "${profile.fatGrams}g", Color(0xFFFFB74D))
                }
            }
        }

        // WEEKLY VOLUME CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF171522)),
            border = BorderStroke(1.dp, Color(0xFF282538))
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("WEEKLY HYPERTROPHY VOLUME", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF8E8AA7))
                    Spacer(Modifier.height(4.dp))
                    Text("${profile.weeklySetsPerMuscleGroup} sets / muscle group / week", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Text("🏋️", fontSize = 24.sp)
            }
        }

        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFA78BFA),
                contentColor = Color(0xFF171020)
            )
        ) {
            Text("Start Program →", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ==================================================
// HELPER COMPONENTS & CARDS
// ==================================================
@Composable
private fun SexOptionCard(
    title: String,
    subtitle: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171522)),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) Color(0xFFA78BFA) else Color(0xFF282538)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(if (isSelected) Color(0xFF29233A) else Color(0xFF221F30), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(icon, fontSize = 20.sp)
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color(0xFFA78BFA), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✓", color = Color(0xFF171020), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .border(1.5.dp, Color(0xFF3A354A), CircleShape)
                    )
                }
            }

            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFF8E8AA7))
        }
    }
}

@Composable
private fun DimensionStepperCard(
    label: String,
    valueText: String,
    unitText: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171522)),
        border = BorderStroke(1.dp, Color(0xFF282538))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF8E8AA7))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(valueText, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(unitText, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF8E8AA7), modifier = Modifier.padding(bottom = 4.dp))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    onClick = onDecrement,
                    shape = CircleShape,
                    color = Color(0xFF262436),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("−", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Surface(
                    onClick = onIncrement,
                    shape = CircleShape,
                    color = Color(0xFF262436),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("+", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExperienceOptionCard(
    title: String,
    subtitle: String,
    icon: String,
    isRecommended: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171522)),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) Color(0xFFA78BFA) else Color(0xFF282538)
        )
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
                    .size(44.dp)
                    .background(if (isSelected) Color(0xFF392B58) else Color(0xFF242132), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 20.sp)
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    if (isRecommended) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFF392B58)
                        ) {
                            Text(
                                text = "Recommended",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC4B5FD),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFF8E8AA7))
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(Color(0xFFA78BFA), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", color = Color(0xFF171020), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .border(1.5.dp, Color(0xFF3A354A), CircleShape)
                )
            }
        }
    }
}

@Composable
private fun EquipmentOptionCard(
    title: String,
    subtitle: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    ExperienceOptionCard(
        title = title,
        subtitle = subtitle,
        icon = icon,
        isRecommended = false,
        isSelected = isSelected,
        onClick = onClick
    )
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    accentColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171522)),
        border = BorderStroke(1.dp, Color(0xFF282538))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF8E8AA7))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = accentColor)
        }
    }
}

@Composable
private fun MacroItem(
    label: String,
    value: String,
    color: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8E8AA7))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
    }
}
