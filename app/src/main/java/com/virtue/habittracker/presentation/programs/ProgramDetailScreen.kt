package com.virtue.habittracker.presentation.programs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.virtue.habittracker.domain.model.program.ProgramCatalog
import com.virtue.habittracker.domain.model.program.ProgramTemplate
import com.virtue.habittracker.domain.model.program.catalog.BuildLeanMuscleProgram

/**
 * Luxury modal syllabus & daily curriculum preview for a catalogue program.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun ProgramDetailScreen(
    template: ProgramTemplate,
    isEnrolled: Boolean = false,
    onBack: () -> Unit,
    onPersonalize: () -> Unit
) {
    val definition = remember(template.id) {
        ProgramCatalog.findDefinition(template.id) ?: BuildLeanMuscleProgram
    }
    val accentColor = Color(definition.accentHex)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onBack,
        sheetState = sheetState,
        containerColor = Color(0xFF151320),
        contentColor = Color.White,
        scrimColor = Color(0xCC000000),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 720.dp)
                .heightIn(max = 780.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hero Artwork Banner with Gradient Overlay & Free Badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(176.dp)
                        .clip(RoundedCornerShape(22.dp))
                ) {
                    Image(
                        painter = painterResource(template.artworkResource()),
                        contentDescription = "${template.title} program artwork",
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x33000000),
                                        Color(0xDD151320)
                                    )
                                )
                            )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xCC181526),
                            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = template.category.displayName().uppercase(),
                                color = accentColor,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xE60D3325),
                            border = BorderStroke(1.dp, Color(0xFF00DC82).copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.VerifiedUser,
                                    contentDescription = null,
                                    tint = Color(0xFF00DC82),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "100% FREE",
                                    color = Color(0xFF00DC82),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = template.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            definition.highlightTags.forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color(0xCC231F35),
                                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
                                ) {
                                    Text(
                                        text = tag,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFE0D9FF),
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    text = template.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFB5AFC7),
                    lineHeight = 20.sp
                )

                // Key Metadata Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetailPill(
                        label = "DURATION",
                        value = "${template.durationDays} Days",
                        accentColor = accentColor,
                        modifier = Modifier.weight(1f)
                    )
                    DetailPill(
                        label = "DAILY DOSE",
                        value = "${template.defaultMinutesPerDay} Min",
                        accentColor = accentColor,
                        modifier = Modifier.weight(1f)
                    )
                    DetailPill(
                        label = "SETUP",
                        value = template.equipment.displayName(),
                        accentColor = accentColor,
                        modifier = Modifier.weight(1f)
                    )
                }

                // 3-PHASE CONNECTED TIMELINE CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1829)),
                    border = BorderStroke(1.dp, Color(0xFF2E2942))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "3-PHASE PERIODIZED PROGRESSION",
                            color = accentColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        template.phases.forEachIndexed { idx, phase ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .background(accentColor.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${phase.number}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = accentColor
                                        )
                                    }
                                    if (idx < template.phases.lastIndex) {
                                        Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .height(34.dp)
                                                .background(accentColor.copy(alpha = 0.25f))
                                        )
                                    }
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Phase ${phase.number}: ${phase.title}",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = "Days ${phase.startDay}–${phase.endDay}",
                                            color = accentColor,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = phase.activityTitle,
                                        color = Color(0xFFD5CFE8),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = phase.instructions,
                                        color = Color(0xFF9E98B5),
                                        style = MaterialTheme.typography.bodySmall,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // ROTATING DAILY MODULES PREVIEW CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1829)),
                    border = BorderStroke(1.dp, Color(0xFF2E2942))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF00DC82),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "7 ROTATING DAILY CURRICULUM MODULES",
                                color = Color(0xFF00DC82),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.9.sp
                            )
                        }
                        definition.dailyModules.take(5).forEachIndexed { index, module ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(15.dp)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                    Text(
                                        text = "Session ${index + 1}: ${module.titleSuffix}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = module.focusNote,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF9E98B5),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // Clinical / Safety Note Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF181624)),
                    border = BorderStroke(1.dp, Color(0xFF29253B))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Clinical & Safety Guidance",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFD5CFE8)
                        )
                        Text(
                            text = template.safetyNote,
                            color = Color(0xFF9E98B5),
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 17.sp
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            HorizontalDivider(color = Color(0xFF262338))

            // Fixed Bottom CTA Action Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF151320))
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onPersonalize,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEnrolled) Color(0xFF00DC82) else accentColor,
                        contentColor = if (isEnrolled) Color(0xFF042416) else Color(0xFF140E21)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isEnrolled) "Open Your Active Program" else "Personalize Free Program",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailPill(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1829)),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
