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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.virtue.habittracker.domain.model.program.ProgramTemplate

@Composable
internal fun ProgramDetailScreen(
    template: ProgramTemplate,
    isPremium: Boolean,
    onBack: () -> Unit,
    onPersonalize: () -> Unit,
    onPremium: () -> Unit
) {
    Scaffold(topBar = {
        TopAppBar(title = { Text("Program details") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
        })
    }) { padding ->
        Column(Modifier.padding(padding).padding(horizontal = 18.dp, vertical = 12.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(template.category.displayName(), color = ProgramLavender, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text(template.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(template.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailPill(template.durationDays.toString() + " days", Modifier.weight(1f))
                DetailPill(template.defaultMinutesPerDay.toString() + " min/day", Modifier.weight(1f))
                DetailPill(template.difficulty.displayName(), Modifier.weight(1f))
            }
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = ProgramCard)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("WHAT YOU'LL DO", color = ProgramLavender, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    template.phases.forEach { phase ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Phase " + phase.number + " · Days " + phase.startDay + "–" + phase.endDay, fontWeight = FontWeight.SemiBold)
                            Text(phase.title)
                            Text(phase.instructions, color = ProgramMuted, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Safety note", fontWeight = FontWeight.SemiBold)
                    Text(template.safetyNote, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    Text("This is general-wellness guidance, not medical care. Stop if an activity causes pain or feels unsafe and seek appropriate professional advice.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
            Text("Equipment: " + template.equipment.displayName())
            Text(if (template.isPremium) "Premium program" else "Free starter program", color = if (template.isPremium) ProgramLavender else MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            if (template.isPremium && !isPremium) OutlinedButton(onClick = onPremium, Modifier.fillMaxWidth()) { Text("Explore Premium") }
            Button(onClick = onPersonalize, modifier = Modifier.fillMaxWidth(), enabled = !template.isPremium || isPremium) {
                Text(if (template.isPremium && !isPremium) "Premium required" else "Personalize this program")
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun DetailPill(text: String, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Text(text, Modifier.padding(horizontal = 10.dp, vertical = 12.dp), style = MaterialTheme.typography.labelMedium)
    }
}
