package com.mhealth.aura.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.mhealth.aura.ui.theme.BluePrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchableDropdownField(
    label: String,
    value: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var expanded by remember { mutableStateOf(false) }
    var query by remember(value) { mutableStateOf(value) }
    val filtered = remember(query, options) {
        if (query.isBlank() || query == value) options.take(100)
        else options.filter { it.contains(query, ignoreCase = true) }.take(100)
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                expanded = true
            },
            label = { Text(label) },
            enabled = enabled,
            singleLine = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            filtered.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        query = option
                        expanded = false
                        onSelected(option)
                        focusManager.clearFocus(force = true)
                        keyboardController?.hide()
                    }
                )
            }
            if (filtered.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No matching option") },
                    onClick = { expanded = false },
                    enabled = false
                )
            }
        }
    }
}

@Composable
fun DatePickerField(
    label: String,
    valueMillis: Long,
    onSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
    minimumMillis: Long? = null
) {
    val context = LocalContext.current
    val calendar = remember(valueMillis) { Calendar.getInstance().apply { timeInMillis = valueMillis } }
    val formatted = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(valueMillis))
    Box(
        modifier = modifier.clickable {
            DatePickerDialog(
                context,
                { _, year, month, day ->
                    val picked = Calendar.getInstance().apply {
                        set(year, month, day, 0, 0, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    onSelected(picked)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                minimumMillis?.let { datePicker.minDate = it }
            }.show()
        }
    ) {
        OutlinedTextField(
            value = formatted,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun TimePickerField(
    label: String,
    hour: Int,
    minute: Int,
    onSelected: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val display = remember(hour, minute) {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }.let { SimpleDateFormat("h:mm a", Locale.getDefault()).format(it.time) }
    }
    Box(
        modifier = modifier.clickable {
            TimePickerDialog(context, { _, h, m -> onSelected(h, m) }, hour, minute, false).show()
        }
    ) {
        OutlinedTextField(
            value = display,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
