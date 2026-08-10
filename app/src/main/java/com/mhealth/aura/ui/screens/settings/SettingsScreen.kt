package com.mhealth.aura.ui.screens.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.mhealth.aura.data.db.entity.UserEntity
import com.mhealth.aura.ui.components.AppScreen
import com.mhealth.aura.ui.components.AuraCard
import com.mhealth.aura.ui.components.SectionLabel
import com.mhealth.aura.ui.components.TimePickerField
import com.mhealth.aura.ui.theme.BluePrimary
import com.mhealth.aura.ui.theme.GreenSuccess

@Composable
fun SettingsScreen(
    user: UserEntity?,
    onBack: () -> Unit,
    onSave: (UserEntity) -> Unit,
    onTestReminder: () -> Unit,
    onOpenMedicationDiary: () -> Unit
) {
    if (user == null) {
        AppScreen(title = "Settings", onBack = onBack) {
            Text("Complete registration first.", modifier = Modifier.padding(24.dp))
        }
        return
    }
    val context = LocalContext.current
    val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    var remindersEnabled by remember(user) { mutableStateOf(user.doseRemindersEnabled) }
    var missedEnabled by remember(user) { mutableStateOf(user.missedDoseAlertsEnabled) }
    var summaryEnabled by remember(user) { mutableStateOf(user.dailySummaryEnabled) }
    var preMinutes by remember(user) { mutableStateOf(user.preDoseMinutes.toString()) }
    var missedMinutes by remember(user) { mutableStateOf(user.missedDoseMinutes.toString()) }
    var summaryHour by remember(user) { mutableStateOf(user.summaryHour) }
    var summaryMinute by remember(user) { mutableStateOf(user.summaryMinute) }

    AppScreen(
        title = "Settings",
        subtitle = "Control your course and notification behavior",
        onBack = onBack
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AuraCard {
                SectionLabel("Notification status")
                Text(
                    if (permissionGranted) "Notifications are allowed on this device"
                    else "Notifications are blocked in Android settings",
                    color = if (permissionGranted) GreenSuccess else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Use the test button below. A notification should appear immediately with sound or vibration according to your phone settings.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp)
                )
                OutlinedButton(
                    onClick = onTestReminder,
                    enabled = permissionGranted,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                ) {
                    Text("Test notification now")
                }
            }

            AuraCard {
                SectionLabel("Medication schedules")
                Text(
                    "Each antibiotic can have its own morning, afternoon and night schedule.",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                OutlinedButton(
                    onClick = onOpenMedicationDiary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Manage antibiotics and dose times")
                }
            }

            AuraCard {
                SectionLabel("Reminder rules")
                SettingSwitch(
                    title = "Before-dose reminder",
                    description = "Alert before each scheduled antibiotic dose",
                    checked = remindersEnabled,
                    onCheckedChange = { remindersEnabled = it }
                )
                OutlinedTextField(
                    value = preMinutes,
                    onValueChange = { preMinutes = it.filter(Char::isDigit).take(3) },
                    label = { Text("Minutes before dose") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = remindersEnabled,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )

                SettingSwitch(
                    title = "Missed-dose alert",
                    description = "Alert when a dose has not been recorded",
                    checked = missedEnabled,
                    onCheckedChange = { missedEnabled = it }
                )
                OutlinedTextField(
                    value = missedMinutes,
                    onValueChange = { missedMinutes = it.filter(Char::isDigit).take(3) },
                    label = { Text("Minutes after dose") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = missedEnabled,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )

                SettingSwitch(
                    title = "Daily adherence summary",
                    description = "Show taken and missed dose totals each evening",
                    checked = summaryEnabled,
                    onCheckedChange = { summaryEnabled = it }
                )
                TimePickerField(
                    label = "Daily summary time",
                    hour = summaryHour,
                    minute = summaryMinute,
                    onSelected = { hour, minute ->
                        summaryHour = hour
                        summaryMinute = minute
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AuraCard {
                SectionLabel("Profile")
                Text(user.name.ifBlank { "Aura user" }, fontWeight = FontWeight.Bold)
                Text(user.email, style = MaterialTheme.typography.bodySmall)
                Text(
                    listOf(user.city, user.district, user.state).filter(String::isNotBlank).joinToString(", "),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    onSave(
                        user.copy(
                            doseRemindersEnabled = remindersEnabled,
                            missedDoseAlertsEnabled = missedEnabled,
                            dailySummaryEnabled = summaryEnabled,
                            preDoseMinutes = preMinutes.toIntOrNull()?.coerceIn(0, 180) ?: 15,
                            missedDoseMinutes = missedMinutes.toIntOrNull()?.coerceIn(15, 720) ?: 120,
                            summaryHour = summaryHour,
                            summaryMinute = summaryMinute
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save settings and rebuild reminders")
            }
        }
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
