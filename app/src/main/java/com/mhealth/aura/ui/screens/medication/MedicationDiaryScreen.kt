package com.mhealth.aura.ui.screens.medication

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mhealth.aura.data.db.entity.DoseLogEntity
import com.mhealth.aura.data.db.entity.MedicationEntity
import com.mhealth.aura.data.db.entity.UserEntity
import com.mhealth.aura.domain.DoseSchedule
import com.mhealth.aura.ui.components.AppScreen
import com.mhealth.aura.ui.components.AuraCard
import com.mhealth.aura.ui.components.AuraChip
import com.mhealth.aura.ui.components.DatePickerField
import com.mhealth.aura.ui.components.SectionLabel
import com.mhealth.aura.ui.components.TimePickerField
import com.mhealth.aura.ui.theme.BlueLight
import com.mhealth.aura.ui.theme.BluePrimary
import com.mhealth.aura.ui.theme.BorderColor
import com.mhealth.aura.ui.theme.GreenLight
import com.mhealth.aura.ui.theme.GreenSuccess
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun MedicationDiaryScreen(
    user: UserEntity?,
    medications: List<MedicationEntity>,
    logs: List<DoseLogEntity>,
    onBack: () -> Unit,
    onSave: (MedicationEntity) -> Unit,
    onDelete: (MedicationEntity) -> Unit,
    onMarkDose: (MedicationEntity, String, Long, String) -> Unit
) {
    if (user == null) {
        AppScreen(title = "Medication Diary", onBack = onBack) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Complete registration to create an antibiotic course.")
            }
        }
        return
    }

    var selectedId by remember(medications) {
        mutableStateOf(medications.firstOrNull()?.id)
    }
    var editorMedication by remember { mutableStateOf<MedicationEntity?>(null) }
    val selected = medications.firstOrNull { it.id == selectedId }
        ?: medications.firstOrNull()

    AppScreen(
        title = "Medication Diary",
        subtitle = "Add, edit, delete and track each antibiotic",
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 14.dp, top = 14.dp, end = 14.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    editorMedication = MedicationEntity(
                        startDateMillis = startOfDay(System.currentTimeMillis()),
                        endDateMillis = startOfDay(System.currentTimeMillis()) + 6 * 86_400_000L
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add another antibiotic")
            }

            if (medications.isEmpty()) {
                AuraCard {
                    Text("No antibiotics added yet.")
                    Text(
                        "Add each prescribed antibiotic separately so every medicine can have its own schedule.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            medications.forEach { medication ->
                val isSelected = medication.id == selected?.id
                AuraCard(
                    backgroundColor = if (isSelected) BlueLight else Color.White,
                    modifier = Modifier
                        .border(
                            1.dp,
                            if (isSelected) BluePrimary else BorderColor,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { selectedId = medication.id }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                medication.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${medication.dose} - ${medication.frequency} - " +
                                    DoseSchedule.slotsFor(
                                        medication.frequency,
                                        medication.doseTimesCsv
                                    ).joinToString { it.displayTime() },
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "${date(medication.startDateMillis)} to " +
                                    date(medication.endDateMillis),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            if (medication.isActive) "Active" else "Paused",
                            color = if (medication.isActive) GreenSuccess else Color.Gray,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { editorMedication = medication },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Edit")
                        }
                        OutlinedButton(
                            onClick = {
                                onDelete(medication)
                                if (selectedId == medication.id) selectedId = null
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            editorMedication?.let { medication ->
                key(medication.id, medication.name, medication.startDateMillis) {
                    MedicationEditor(
                        medication = medication,
                        onCancel = { editorMedication = null },
                        onSave = {
                            onSave(it)
                            editorMedication = null
                            if (it.id != 0L) selectedId = it.id
                        }
                    )
                }
            }

            selected?.let { medication ->
                MedicationTracking(
                    medication = medication,
                    logs = logs.filter { it.medicationId == medication.id },
                    onMarkDose = { label, dayStart, status ->
                        onMarkDose(medication, label, dayStart, status)
                    }
                )
            }
        }
    }
}

@Composable
private fun MedicationEditor(
    medication: MedicationEntity,
    onCancel: () -> Unit,
    onSave: (MedicationEntity) -> Unit
) {
    var name by remember { mutableStateOf(medication.name) }
    var dose by remember { mutableStateOf(medication.dose) }
    var frequency by remember { mutableStateOf(medication.frequency) }
    var startDate by remember { mutableStateOf(medication.startDateMillis) }
    var endDate by remember { mutableStateOf(medication.endDateMillis) }
    var active by remember { mutableStateOf(medication.isActive) }
    var slots by remember {
        mutableStateOf(DoseSchedule.slotsFor(medication.frequency, medication.doseTimesCsv))
    }

    fun changeFrequency(value: String) {
        frequency = value
        slots = DoseSchedule.slotsFor(value)
    }

    AuraCard {
        SectionLabel(if (medication.id == 0L) "New antibiotic" else "Modify antibiotic")
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Antibiotic name") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
        )
        OutlinedTextField(
            value = dose,
            onValueChange = { dose = it },
            label = { Text("Dose, e.g. 500 mg") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("OD", "BD", "TID", "QID").forEach { value ->
                AuraChip(
                    label = value,
                    selected = frequency == value,
                    onClick = { changeFrequency(value) }
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DatePickerField(
                label = "Start date",
                valueMillis = startDate,
                onSelected = {
                    startDate = it
                    if (endDate < it) endDate = it
                },
                modifier = Modifier.weight(1f)
            )
            DatePickerField(
                label = "End date",
                valueMillis = endDate,
                minimumMillis = startDate,
                onSelected = { endDate = it },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Set each prescribed time independently. TID supports morning, afternoon and night.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        slots.forEachIndexed { index, slot ->
            TimePickerField(
                label = slot.label,
                hour = slot.hour,
                minute = slot.minute,
                onSelected = { hour, minute ->
                    slots = slots.toMutableList().also {
                        it[index] = slot.copy(hour = hour, minute = minute)
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
        }
        AuraChip(
            label = if (active) "Reminders active" else "Course paused",
            selected = active,
            onClick = { active = !active }
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    onSave(
                        medication.copy(
                            name = name.trim(),
                            dose = dose.trim(),
                            frequency = frequency,
                            startDateMillis = startDate,
                            endDateMillis = endDate,
                            doseTimesCsv = slots.joinToString(",") {
                                "%02d:%02d".format(it.hour, it.minute)
                            },
                            isActive = active
                        )
                    )
                },
                enabled = name.isNotBlank() && dose.isNotBlank() && endDate >= startDate,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
private fun MedicationTracking(
    medication: MedicationEntity,
    logs: List<DoseLogEntity>,
    onMarkDose: (String, Long, String) -> Unit
) {
    val slots = DoseSchedule.slotsFor(medication.frequency, medication.doseTimesCsv)
    val days = remember(medication.startDateMillis, medication.endDateMillis) {
        dateRange(medication.startDateMillis, medication.endDateMillis)
    }
    var selectedDay by remember(medication.id) {
        mutableStateOf(
            System.currentTimeMillis().coerceIn(
                medication.startDateMillis,
                medication.endDateMillis
            )
        )
    }
    val selectedStart = startOfDay(selectedDay)
    val selectedEnd = selectedStart + 86_400_000L

    AuraCard {
        SectionLabel("${medication.name} date columns")
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            days.forEach { day ->
                val dayStart = startOfDay(day)
                val selected = dayStart == selectedStart
                val taken = logs.count {
                    it.status == "taken" &&
                        it.dateMillis >= dayStart &&
                        it.dateMillis < dayStart + 86_400_000L
                }
                Column(
                    modifier = Modifier
                        .width(72.dp)
                        .background(
                            if (selected) BluePrimary else BlueLight,
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            if (selected) BluePrimary else BorderColor,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedDay = day }
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        SimpleDateFormat("EEE", Locale.getDefault()).format(Date(day)),
                        color = if (selected) Color.White else BluePrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(day)),
                        color = if (selected) Color.White else Color.DarkGray
                    )
                    Text(
                        "$taken/${slots.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selected) Color.White else GreenSuccess
                    )
                }
            }
        }
    }

    AuraCard {
        SectionLabel("Doses for ${date(selectedDay)}")
        slots.forEach { slot ->
            val existing = logs.firstOrNull {
                it.doseLabel == slot.label &&
                    it.dateMillis >= selectedStart &&
                    it.dateMillis < selectedEnd
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .background(
                        if (existing?.status == "taken") GreenLight else BlueLight,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(slot.label, fontWeight = FontWeight.Bold)
                    Text(
                        "${slot.displayTime()} - ${medication.name} ${medication.dose}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    existing?.let {
                        Text(
                            "Recorded as ${it.status}",
                            color = if (it.status == "taken") {
                                GreenSuccess
                            } else {
                                MaterialTheme.colorScheme.error
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                if (existing == null) {
                    Column {
                        Text(
                            "Taken",
                            color = GreenSuccess,
                            modifier = Modifier
                                .clickable {
                                    onMarkDose(slot.label, selectedStart, "taken")
                                }
                                .padding(6.dp)
                        )
                        Text(
                            "Missed",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .clickable {
                                    onMarkDose(slot.label, selectedStart, "missed")
                                }
                                .padding(6.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun dateRange(startMillis: Long, endMillis: Long): List<Long> {
    val start = startOfDay(startMillis)
    val end = startOfDay(endMillis)
    if (end < start) return listOf(start)
    val count = (((end - start) / 86_400_000L) + 1).toInt().coerceIn(1, 90)
    return List(count) { start + it * 86_400_000L }
}

private fun date(value: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(value))

private fun startOfDay(value: Long): Long = Calendar.getInstance().apply {
    timeInMillis = value
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis
