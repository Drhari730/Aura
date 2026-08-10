package com.mhealth.aura.ui.screens.features

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mhealth.aura.data.db.entity.AdrReportEntity
import com.mhealth.aura.data.db.entity.DoseLogEntity
import com.mhealth.aura.data.db.entity.MedicationEntity
import com.mhealth.aura.data.db.entity.UserEntity
import com.mhealth.aura.domain.DoseSchedule
import com.mhealth.aura.domain.GamificationRules
import com.mhealth.aura.ui.components.AdherenceRingChart
import com.mhealth.aura.ui.components.AppScreen
import com.mhealth.aura.ui.components.AuraCard
import com.mhealth.aura.ui.components.AuraChip
import com.mhealth.aura.ui.components.SectionLabel
import com.mhealth.aura.ui.theme.AmberLight
import com.mhealth.aura.ui.theme.BlueLight
import com.mhealth.aura.ui.theme.BluePrimary
import com.mhealth.aura.ui.theme.GreenLight
import com.mhealth.aura.ui.theme.GreenSuccess
import com.mhealth.aura.ui.theme.PurpleAccent
import com.mhealth.aura.ui.theme.PurpleLight
import com.mhealth.aura.ui.theme.RedLight
import com.mhealth.aura.ui.theme.TextMedium
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdrScreen(
    user: UserEntity?,
    reports: List<AdrReportEntity>,
    onBack: () -> Unit,
    onSubmit: (AdrReportEntity) -> Unit
) {
    var symptoms by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf("Mild") }
    var notes by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    AppScreen(
        title = "Report a Side Effect",
        subtitle = "Save an adverse drug reaction in your Aura record",
        onBack = onBack
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AuraCard {
                SectionLabel("Current antibiotic")
                Text(user?.antibiotic?.ifBlank { "Not configured" } ?: "Not configured", fontWeight = FontWeight.Bold)
                Text(user?.dose.orEmpty(), style = MaterialTheme.typography.bodySmall)
            }
            AuraCard {
                SectionLabel("What happened?")
                OutlinedTextField(
                    value = symptoms,
                    onValueChange = { symptoms = it; submitted = false },
                    label = { Text("Symptoms, e.g. rash, nausea, swelling") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Mild", "Moderate", "Severe").forEach {
                        AuraChip(label = it, selected = severity == it, onClick = { severity = it })
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Additional notes") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                if (severity == "Severe") {
                    Text(
                        "Severe breathing difficulty, facial swelling, fainting, or widespread blistering needs urgent medical care. Do not rely only on this app.",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.background(RedLight, RoundedCornerShape(10.dp)).padding(10.dp)
                    )
                }
                Button(
                    onClick = {
                        onSubmit(
                            AdrReportEntity(
                                medication = user?.antibiotic.orEmpty(),
                                symptoms = symptoms,
                                severity = severity,
                                notes = notes
                            )
                        )
                        symptoms = ""
                        notes = ""
                        submitted = true
                    },
                    enabled = symptoms.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BluePrimary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                ) {
                    Text("Save ADR report")
                }
                if (submitted) {
                    Text(
                        "Report saved to your diary.",
                        color = GreenSuccess,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            if (reports.isNotEmpty()) {
                AuraCard {
                    SectionLabel("Previous reports")
                    reports.take(10).forEach { report ->
                        Text(
                            "${date(report.dateMillis)} - ${report.severity}",
                            fontWeight = FontWeight.Bold
                        )
                        Text(report.symptoms, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressScreen(
    user: UserEntity?,
    medications: List<MedicationEntity>,
    logs: List<DoseLogEntity>,
    onBack: () -> Unit
) {
    val planned = medications.sumOf { medication ->
        val days = (
            ((medication.endDateMillis - medication.startDateMillis)
                .coerceAtLeast(0L) / 86_400_000L) + 1
            ).toInt()
        days * DoseSchedule.slotsFor(medication.frequency, medication.doseTimesCsv).size
    }
    val taken = logs.count { it.status == "taken" }
    val missed = logs.count { it.status == "missed" }
    val adherence = if (planned == 0) 0f else taken.toFloat() / planned
    val rewards = GamificationRules.calculate(medications, logs)

    AppScreen(title = "Course Progress", onBack = onBack) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AuraCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AdherenceRingChart(
                        percent = adherence.coerceIn(0f, 1f),
                        label = "${(adherence * 100).toInt()}%",
                        sublabel = "adherence"
                    )
                    Column(Modifier.padding(start = 20.dp)) {
                        Text(
                            medications.firstOrNull()?.name ?: user?.antibiotic.orEmpty(),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text("${medications.size} antibiotic course(s)")
                        Text("$taken taken", color = GreenSuccess, fontWeight = FontWeight.Bold)
                        Text("$missed missed", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("Planned", planned.toString(), BlueLight, Modifier.weight(1f))
                MetricCard("Taken", taken.toString(), GreenLight, Modifier.weight(1f))
                MetricCard("Missed", missed.toString(), RedLight, Modifier.weight(1f))
            }
            AuraCard(backgroundColor = PurpleLight) {
                SectionLabel("Safety rewards")
                Text(
                    "${rewards.safetyPoints} safety points",
                    style = MaterialTheme.typography.titleLarge,
                    color = PurpleAccent
                )
                Text(
                    "Badges reward correct schedule logging and AMR learning, never extra antibiotic intake.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMedium),
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )
                rewards.badges.forEach { badge ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(badge.title, fontWeight = FontWeight.SemiBold)
                            Text(
                                badge.description,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextMedium)
                            )
                        }
                        Text(
                            if (badge.earned) "Earned" else "Locked",
                            color = if (badge.earned) GreenSuccess else TextMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            AuraCard {
                SectionLabel("Recent dose history")
                if (logs.isEmpty()) Text("No doses have been recorded yet.")
                logs.take(20).forEach { log ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(log.doseLabel, fontWeight = FontWeight.SemiBold)
                            Text(dateTime(log.dateMillis), style = MaterialTheme.typography.bodySmall)
                        }
                        Text(
                            log.status.replaceFirstChar(Char::uppercase),
                            color = if (log.status == "taken") GreenSuccess else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LearnScreen(
    user: UserEntity?,
    medications: List<MedicationEntity>,
    onBack: () -> Unit
) {
    val lessons = listOf(
        "What is antimicrobial resistance?" to
            "AMR occurs when bacteria and other microbes change so medicines become less effective. Resistant infections can be harder and more expensive to treat.",
        "Why finishing the course matters" to
            "Stopping early may leave harder-to-kill bacteria behind. Follow the duration prescribed by your clinician.",
        "Why timing and dose matter" to
            "Taking the correct dose at the prescribed times helps maintain an effective medicine level. Do not change the schedule without clinical advice.",
        "Never share leftover antibiotics" to
            "The wrong antibiotic, dose, or duration can delay care and contribute to antimicrobial resistance.",
        "Missed dose safety" to
            "Use your clinician or medicine label instructions. Do not double a dose unless specifically directed.",
        "Common digestive effects" to
            "Nausea, loose stools, stomach discomfort, or altered taste can occur. Record symptoms and contact a clinician if they are persistent or severe.",
        "Allergy warning signs" to
            "Facial or throat swelling, breathing difficulty, fainting, or a widespread blistering rash requires urgent medical help.",
        "Antibiotic-associated diarrhoea" to
            "Frequent watery diarrhoea, blood, fever, or severe abdominal pain during or after antibiotics needs prompt medical assessment.",
        "Food and drink instructions" to
            "Some antibiotics should be taken with food and others on an empty stomach. Follow the pharmacy label for each medicine.",
        "Alcohol and antibiotics" to
            "Alcohol interactions differ by medicine. Avoid alcohol when the label advises it and ask a pharmacist when uncertain.",
        "Other medicines and supplements" to
            "Antacids, iron, calcium, blood thinners, and other products can interact with some antibiotics. Share your complete medicine list with your clinician.",
        "Pregnancy and breastfeeding" to
            "Tell your prescriber if you are pregnant, planning pregnancy, or breastfeeding so they can confirm the antibiotic is appropriate.",
        "Kidney or liver disease" to
            "Some antibiotic doses need adjustment when kidney or liver function is reduced. Never adjust the dose yourself.",
        "How to store antibiotics" to
            "Follow the label. Some liquid antibiotics require refrigeration, while others should remain at room temperature and away from moisture.",
        "Do not keep leftovers" to
            "Do not save unused antibiotics for a future illness. Ask a pharmacist about safe disposal.",
        "When to seek urgent help" to
            "Breathing difficulty, facial swelling, fainting, severe rash, persistent vomiting, or worsening infection needs prompt medical assessment.",
        "Antibiotics do not treat viruses" to
            "Colds, influenza, and most viral infections do not improve with antibiotics. Use them only when prescribed.",
        "Preventing infection" to
            "Hand hygiene, recommended vaccines, safe food handling, and appropriate wound care reduce infections and antibiotic use."
    )
    AppScreen(title = "AMR Learning", subtitle = "Practical antibiotic safety", onBack = onBack) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AuraCard(backgroundColor = AmberLight) {
                SectionLabel("Your current dose schedule")
                if (medications.isEmpty()) {
                    Text("No antibiotic schedule has been added.")
                }
                medications.forEach { medication ->
                    Text(
                        "${medication.name} ${medication.dose}",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        DoseSchedule.slotsFor(
                            medication.frequency,
                            medication.doseTimesCsv
                        ).joinToString { "${it.label}: ${it.displayTime()}" },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                user?.condition?.takeIf(String::isNotBlank)?.let {
                    Text("Recorded condition: $it", modifier = Modifier.padding(top = 8.dp))
                }
            }
            lessons.forEachIndexed { index, lesson ->
                AuraCard(backgroundColor = if (index % 2 == 0) BlueLight else GreenLight) {
                    Text(lesson.first, style = MaterialTheme.typography.titleMedium)
                    Text(lesson.second, modifier = Modifier.padding(top = 6.dp))
                }
            }
        }
    }
}

@Composable
fun AskAuraScreen(
    user: UserEntity?,
    medications: List<MedicationEntity>,
    onBack: () -> Unit
) {
    var question by remember { mutableStateOf("") }
    var answer by remember {
        mutableStateOf("Ask about your antibiotic, missed doses, food, or common side effects.")
    }
    val suggestions = listOf(
        "What is my current dose schedule?",
        "What if I miss a dose?",
        "Can I stop when I feel better?",
        "What common side effects can occur?",
        "Which allergy symptoms are an emergency?",
        "Should I take my antibiotic with food?",
        "Can I drink alcohol?",
        "Can I take antacids, iron or calcium?",
        "What if I vomit after a dose?",
        "What if I have diarrhoea?",
        "Can I share antibiotics with family?",
        "Can I use leftover antibiotics later?",
        "Why must doses be taken on time?",
        "What does OD, BD, TID and QID mean?",
        "How should I store my antibiotic?",
        "What if I am pregnant or breastfeeding?",
        "What if I have kidney or liver disease?",
        "How do antibiotics cause resistance?",
        "When should I contact my clinician?",
        "Do antibiotics work for cold or flu?"
    )

    AppScreen(title = "Ask Aura", subtitle = "Antibiotic guidance", onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 14.dp, top = 14.dp, end = 14.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AuraCard(backgroundColor = BlueLight) {
                Text("Aura", fontWeight = FontWeight.Bold, color = BluePrimary)
                Text(answer, modifier = Modifier.padding(top = 6.dp))
            }
            suggestions.forEach { suggestion ->
                Text(
                    suggestion,
                    color = BluePrimary,
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(20.dp))
                        .clickable {
                            question = suggestion
                            answer = answerFor(suggestion, user, medications)
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
            OutlinedTextField(
                value = question,
                onValueChange = { question = it },
                label = { Text("Ask a question") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { answer = answerFor(question, user, medications) },
                enabled = question.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ask Aura")
            }
            Text(
                "Aura provides general education, not a diagnosis or emergency service.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, color: Color, modifier: Modifier) {
    Column(
        modifier = modifier.background(color, RoundedCornerShape(14.dp)).padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

private fun answerFor(
    question: String,
    user: UserEntity?,
    medications: List<MedicationEntity>
): String {
    val q = question.lowercase()
    return when {
        "current" in q && "schedule" in q -> {
            if (medications.isEmpty()) {
                "No antibiotic schedule is saved. Open Medication to add the prescribed medicine and times."
            } else {
                medications.joinToString("\n") { medication ->
                    val times = DoseSchedule.slotsFor(
                        medication.frequency,
                        medication.doseTimesCsv
                    ).joinToString { "${it.label} ${it.displayTime()}" }
                    "${medication.name} ${medication.dose}: $times"
                }
            }
        }
        "miss" in q || "skip" in q ->
            "Take the missed dose when remembered unless it is nearly time for the next one. Do not double up. Check the medicine label or contact your clinician for advice specific to ${user?.antibiotic?.ifBlank { "your antibiotic" } ?: "your antibiotic"}."
        "stop" in q || "better" in q ->
            "Do not stop early just because you feel better. Complete the dates prescribed by your clinician unless they tell you to change the course."
        "side" in q || "rash" in q || "nausea" in q ->
            "Mild nausea or diarrhoea can occur with some antibiotics. Use Report ADR to record it. Seek urgent care for breathing difficulty, facial swelling, fainting, or a severe blistering rash."
        "food" in q || "meal" in q ->
            "Food instructions differ by antibiotic. Follow the pharmacy label for ${user?.antibiotic?.ifBlank { "your medicine" } ?: "your medicine"} and ask your pharmacist if the label is unclear."
        "resistance" in q || "amr" in q ->
            "AMR happens when microbes change so medicines no longer work well. Correct antibiotic choice, dose, timing, and duration help reduce this risk."
        "allerg" in q || "emergency" in q ->
            "Breathing difficulty, facial or throat swelling, fainting, or a widespread blistering rash needs urgent medical care."
        "alcohol" in q ->
            "Alcohol advice differs by antibiotic. Follow each medicine label and ask your pharmacist. Avoid alcohol if the label warns against it or if you feel unwell."
        "antacid" in q || "iron" in q || "calcium" in q ->
            "Antacids, iron, and calcium can reduce absorption of some antibiotics. Check the pharmacy label or ask a pharmacist how many hours to separate them."
        "vomit" in q ->
            "Whether to repeat a dose depends on how soon vomiting occurred and the medicine. Do not automatically repeat it; contact a pharmacist or clinician."
        "diarr" in q ->
            "Mild diarrhoea can occur. Frequent watery diarrhoea, blood, fever, or severe abdominal pain during or after antibiotics needs prompt medical advice."
        "share" in q || "leftover" in q ->
            "Never share antibiotics or save leftovers for another illness. The medicine, dose, and duration may be wrong and can delay proper treatment."
        "on time" in q || "timing" in q ->
            "Regular timing helps maintain effective medicine levels. Use the saved reminders and ask your clinician before changing the prescribed schedule."
        "od" in q || "bd" in q || "tid" in q || "qid" in q ->
            "OD usually means once daily, BD twice daily, TID three times daily, and QID four times daily. Follow the exact times on your prescription."
        "store" in q || "refriger" in q ->
            "Follow the pharmacy label. Storage differs, especially for liquid antibiotics. Keep medicines away from children, heat, and moisture."
        "pregnan" in q || "breast" in q ->
            "Tell your prescriber or pharmacist if you are pregnant, planning pregnancy, or breastfeeding so they can confirm the medicine is appropriate."
        "kidney" in q || "liver" in q ->
            "Kidney or liver disease can require a different antibiotic or dose. Do not adjust it yourself; contact the prescriber."
        "cold" in q || "flu" in q || "virus" in q ->
            "Antibiotics treat bacterial infections, not colds, influenza, or most viral infections."
        "contact" in q || "clinician" in q || "worse" in q ->
            "Contact a clinician if symptoms worsen, fever persists, you cannot keep doses down, side effects are significant, or you are not improving as expected."
        else ->
            "For a safe answer about ${user?.antibiotic?.ifBlank { "your antibiotic" } ?: "your antibiotic"}, include the exact symptom or timing issue. For worsening illness or severe symptoms, contact a clinician."
    }
}

private fun date(value: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(value))

private fun dateTime(value: Long): String =
    SimpleDateFormat("dd MMM, h:mm a", Locale.getDefault()).format(Date(value))
