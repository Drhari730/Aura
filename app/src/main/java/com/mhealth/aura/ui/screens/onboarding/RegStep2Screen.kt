package com.mhealth.aura.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mhealth.aura.domain.DoseSchedule
import com.mhealth.aura.ui.components.AuraChip
import com.mhealth.aura.ui.components.DatePickerField
import com.mhealth.aura.ui.components.ProgressStepIndicator
import com.mhealth.aura.ui.components.SectionLabel
import com.mhealth.aura.ui.components.TimePickerField
import com.mhealth.aura.ui.theme.AmberLight
import com.mhealth.aura.ui.theme.AmberWarning
import com.mhealth.aura.ui.theme.BackgroundApp
import com.mhealth.aura.ui.theme.BluePrimary
import com.mhealth.aura.ui.theme.CardWhite
import com.mhealth.aura.ui.theme.TealPrimary

@Composable
fun RegStep2Screen(
    onNext: (
        condition: String,
        antibiotic: String,
        dose: String,
        frequency: String,
        startDateMillis: Long,
        endDateMillis: Long,
        doseTimesCsv: String
    ) -> Unit
) {
    var condition by remember { mutableStateOf("Typhoid Fever") }
    var antibiotic by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("BD") }
    var startDate by remember { mutableStateOf(startOfToday()) }
    var endDate by remember { mutableStateOf(startDate + 6 * 86_400_000L) }
    var doseTimes by remember { mutableStateOf(DoseSchedule.slotsFor(frequency)) }
    val quickConditions = listOf("UTI", "URTI", "Pneumonia", "Diarrhea", "Skin Infection")

    fun changeFrequency(value: String) {
        frequency = value
        doseTimes = DoseSchedule.slotsFor(value)
    }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundApp)) {
        Column(modifier = Modifier.fillMaxWidth().background(CardWhite).padding(16.dp)) {
            Text(
                "STEP 2 OF 3",
                style = MaterialTheme.typography.labelMedium.copy(color = TealPrimary),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text("Medical Information", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            ProgressStepIndicator(step = 2, total = 3)
        }

        Column(
            modifier = Modifier.padding(16.dp).weight(1f).verticalScroll(rememberScrollState())
        ) {
            SectionLabel("Condition / Diagnosis")
            OutlinedTextField(
                value = condition,
                onValueChange = { condition = it },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BluePrimary)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 14.dp)
            ) {
                quickConditions.take(4).forEach { value ->
                    AuraChip(
                        label = value,
                        selected = condition == value,
                        onClick = { condition = value }
                    )
                }
            }
            AuraChip(
                label = quickConditions.last(),
                selected = condition == quickConditions.last(),
                onClick = { condition = quickConditions.last() },
                modifier = Modifier.padding(bottom = 14.dp)
            )

            OutlinedTextField(
                value = antibiotic,
                onValueChange = { antibiotic = it },
                label = { Text("Antibiotic Prescribed") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BluePrimary)
            )
            OutlinedTextField(
                value = dose,
                onValueChange = { dose = it },
                label = { Text("Dose, e.g. 500 mg") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BluePrimary)
            )

            SectionLabel("Frequency as prescribed")
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 14.dp)
            ) {
                listOf("OD", "BD", "TID", "QID").forEach { value ->
                    AuraChip(
                        label = value,
                        selected = frequency == value,
                        onClick = { changeFrequency(value) }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DatePickerField(
                    label = "Start date",
                    valueMillis = startDate,
                    onSelected = {
                        startDate = it
                        if (endDate < it) endDate = it
                    },
                    modifier = Modifier.weight(1f).padding(bottom = 14.dp)
                )
                DatePickerField(
                    label = "End date",
                    valueMillis = endDate,
                    onSelected = { endDate = it },
                    minimumMillis = startDate,
                    modifier = Modifier.weight(1f).padding(bottom = 14.dp)
                )
            }

            SectionLabel("Dose notification times")
            doseTimes.forEachIndexed { index, slot ->
                TimePickerField(
                    label = slot.label,
                    hour = slot.hour,
                    minute = slot.minute,
                    onSelected = { hour, minute ->
                        doseTimes = doseTimes.toMutableList().also {
                            it[index] = slot.copy(hour = hour, minute = minute)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AmberLight, RoundedCornerShape(12.dp))
                    .border(1.dp, AmberWarning.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    "Take antibiotics exactly as prescribed. Do not stop early or double a missed dose unless your clinician specifically advised it.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF7C5A00),
                        fontWeight = FontWeight.Medium
                    ),
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = {
                    onNext(
                        condition,
                        antibiotic,
                        dose,
                        frequency,
                        startDate,
                        endDate,
                        doseTimes.joinToString(",") { "%02d:%02d".format(it.hour, it.minute) }
                    )
                },
                enabled = antibiotic.isNotBlank() && dose.isNotBlank() && endDate >= startDate,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Next: Doctor Details", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
    }
}

private fun startOfToday(): Long = java.util.Calendar.getInstance().apply {
    set(java.util.Calendar.HOUR_OF_DAY, 0)
    set(java.util.Calendar.MINUTE, 0)
    set(java.util.Calendar.SECOND, 0)
    set(java.util.Calendar.MILLISECOND, 0)
}.timeInMillis
