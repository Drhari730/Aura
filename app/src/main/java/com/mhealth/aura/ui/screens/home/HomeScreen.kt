package com.mhealth.aura.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mhealth.aura.domain.DoseSchedule
import com.mhealth.aura.domain.GamificationRules
import com.mhealth.aura.domain.GamificationSummary
import com.mhealth.aura.ui.components.AdherenceRingChart
import com.mhealth.aura.ui.components.AuraCard
import com.mhealth.aura.ui.components.SectionLabel
import com.mhealth.aura.ui.components.StreakBadge
import com.mhealth.aura.ui.navigation.Screen
import com.mhealth.aura.ui.theme.AmberLight
import com.mhealth.aura.ui.theme.AmberWarning
import com.mhealth.aura.ui.theme.BackgroundApp
import com.mhealth.aura.ui.theme.BlueLight
import com.mhealth.aura.ui.theme.BluePrimary
import com.mhealth.aura.ui.theme.CardWhite
import com.mhealth.aura.ui.theme.GreenLight
import com.mhealth.aura.ui.theme.GreenSuccess
import com.mhealth.aura.ui.theme.PurpleAccent
import com.mhealth.aura.ui.theme.PurpleLight
import com.mhealth.aura.ui.theme.RedDanger
import com.mhealth.aura.ui.theme.RedLight
import com.mhealth.aura.ui.theme.TealLight
import com.mhealth.aura.ui.theme.TealPrimary
import com.mhealth.aura.ui.theme.TextDark
import com.mhealth.aura.ui.theme.TextLight
import com.mhealth.aura.ui.theme.TextMedium

@Composable
fun HomeScreen(viewModel: HomeViewModel, onNavigateTo: (String) -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val user = state.user
    val activeMedications = state.medications.filter { it.isActive }
    val primaryMedication = activeMedications.firstOrNull() ?: state.medications.firstOrNull()
    val primaryDuration = primaryMedication?.let {
        (((it.endDateMillis - it.startDateMillis).coerceAtLeast(0L) / 86_400_000L) + 1)
            .toInt()
    } ?: 1

    Column(modifier = Modifier.fillMaxSize().background(BackgroundApp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardWhite)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(state.todayLabel, style = MaterialTheme.typography.bodySmall)
                Text(
                    "Good morning, ${user?.name?.ifEmpty { "there" } ?: "there"}",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Brush.linearGradient(listOf(BluePrimary, TealPrimary)),
                        CircleShape
                    )
                    .clickable { onNavigateTo(Screen.Settings.route) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    user?.name?.take(2)?.uppercase() ?: "AU",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 14.dp, top = 12.dp, end = 14.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(BluePrimary, TealPrimary)))
                    .padding(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AdherenceRingChart(
                        percent = state.courseProgressPct,
                        label = "${state.dayNumber}/$primaryDuration",
                        sublabel = "days",
                        size = 84.dp,
                        ringColor = Color.White.copy(alpha = 0.95f),
                        trackColor = Color.White.copy(alpha = 0.25f)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "ACTIVE COURSE",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.65f)
                        )
                        Text(
                            primaryMedication?.name ?: "No antibiotic added",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "${primaryMedication?.frequency.orEmpty()} - " +
                                "${activeMedications.size} active medicine(s)",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { state.courseProgressPct },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )
                        Text(
                            "${(primaryDuration - state.dayNumber).coerceAtLeast(0)} days left",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.65f)
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StreakBadge(count = state.streakDays)
                AuraCard(modifier = Modifier.weight(1f)) {
                    Text(
                        "${(state.adherencePct * 100).toInt()}%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenSuccess
                    )
                    Text("Adherence rate", style = MaterialTheme.typography.bodySmall)
                }
            }

            SafetyRewardsCard(
                summary = state.gamification,
                totalTaken = state.totalTaken,
                onOpenLearn = { onNavigateTo(Screen.Learn.route) }
            )

            AuraCard {
                SectionLabel("Today's Doses")
                if (activeMedications.isEmpty()) {
                    Text("No active antibiotic. Open Medication to add one.")
                }
                activeMedications.forEach { medication ->
                    DoseSchedule.slotsFor(
                        medication.frequency,
                        medication.doseTimesCsv
                    ).forEach { slot ->
                        val taken = state.todayLogs.any {
                            it.medicationId == medication.id &&
                                it.doseLabel == slot.label &&
                                it.status == "taken"
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (taken) GreenLight else BlueLight)
                                .border(
                                    1.dp,
                                    if (taken) GreenSuccess.copy(0.3f)
                                    else BluePrimary.copy(0.2f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        if (taken) GreenSuccess else BluePrimary,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (taken) "OK" else "Rx", color = Color.White, fontSize = 11.sp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    slot.label,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (taken) GreenSuccess else TextDark
                                )
                                Text(
                                    "${slot.displayTime()} - ${medication.name} ${medication.dose}",
                                    fontSize = 11.sp,
                                    color = TextLight
                                )
                            }
                            if (!taken) {
                                TextButton(
                                    onClick = {
                                        viewModel.markDoseTaken(medication, slot.label)
                                    },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = BluePrimary
                                    )
                                ) {
                                    Text("Take Now", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            AuraCard(
                backgroundColor = TealLight,
                modifier = Modifier.clickable { onNavigateTo(Screen.Settings.route) }
            ) {
                Text(
                    if (user?.doseRemindersEnabled == true) {
                        "SMART REMINDERS ACTIVE"
                    } else {
                        "REMINDERS PAUSED"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary
                )
                Text(
                    "Tap to edit notification rules. Medication times are edited per antibiotic.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            val menuItems = listOf(
                Triple("Rx", "Medication", "Add, edit or delete") to Screen.Diary.route,
                Triple("%", "Progress", "Adherence") to Screen.Progress.route,
                Triple("ADR", "Report ADR", "Side effects") to Screen.Adr.route,
                Triple("AMR", "Learn", "Patient education") to Screen.Learn.route,
                Triple("Q", "Ask Aura", "20 quick questions") to Screen.Ask.route,
                Triple("SET", "Settings", "Notifications") to Screen.Settings.route
            )
            val bgColors =
                listOf(BlueLight, GreenLight, RedLight, TealLight, PurpleLight, AmberLight)
            val fgColors =
                listOf(BluePrimary, GreenSuccess, RedDanger, TealPrimary, PurpleAccent, AmberWarning)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                menuItems.chunked(2).forEachIndexed { rowIndex, rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        rowItems.forEachIndexed { colIndex, (item, route) ->
                            val index = rowIndex * 2 + colIndex
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(bgColors[index])
                                    .border(
                                        1.dp,
                                        fgColors[index].copy(0.15f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable { onNavigateTo(route) }
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        item.first,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = fgColors[index]
                                    )
                                    Text(
                                        item.second,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )
                                    Text(item.third, fontSize = 11.sp, color = TextMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SafetyRewardsCard(
    summary: GamificationSummary,
    totalTaken: Int,
    onOpenLearn: () -> Unit
) {
    val milestoneProgress = if (summary.totalBadges == 0) {
        0f
    } else {
        summary.earnedBadges.toFloat() / summary.totalBadges.toFloat()
    }
    val nextBadge = summary.badges.firstOrNull { !it.earned }

    AuraCard(backgroundColor = PurpleLight) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MilestoneDonut(
                progress = milestoneProgress,
                centerText = "${summary.earnedBadges}/${summary.totalBadges}",
                modifier = Modifier.size(104.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Safety Milestones",
                    fontWeight = FontWeight.Bold,
                    color = PurpleAccent
                )
                Text(
                    if (nextBadge == null) "All milestones unlocked" else "Next: ${nextBadge.title}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    "${summary.safetyPoints} safety score from correct logging and learning",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMedium)
                )
                TextButton(
                    onClick = onOpenLearn,
                    colors = ButtonDefaults.textButtonColors(contentColor = PurpleAccent)
                ) {
                    Text("Open AMR learning")
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            summary.badges.forEachIndexed { index, badge ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (badge.earned) CardWhite else BackgroundApp,
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            if (badge.earned) PurpleAccent.copy(alpha = 0.25f) else TextLight.copy(alpha = 0.25f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(if (badge.earned) PurpleAccent else TextLight.copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (badge.earned) "OK" else "${index + 1}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            badge.title,
                            color = if (badge.earned) PurpleAccent else TextMedium,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            badge.description,
                            color = TextMedium,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        Text(
            GamificationRules.safetyNote(totalTaken),
            style = MaterialTheme.typography.bodySmall.copy(color = TextMedium),
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}

@Composable
private fun MilestoneDonut(
    progress: Float,
    centerText: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 14.dp.toPx()
            val diameter = size.minDimension - stroke
            val topLeft = androidx.compose.ui.geometry.Offset(stroke / 2f, stroke / 2f)
            val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)
            drawArc(
                color = Color.White.copy(alpha = 0.85f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                brush = Brush.sweepGradient(listOf(PurpleAccent, BluePrimary, GreenSuccess, PurpleAccent)),
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(centerText, fontWeight = FontWeight.Bold, color = TextDark, fontSize = 19.sp)
            Text("done", color = TextMedium, fontSize = 10.sp)
        }
    }
}
