package com.mhealth.aura.domain

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class DoseSlot(
    val label: String,
    val hour: Int,
    val minute: Int
) {
    fun displayTime(locale: Locale = Locale.getDefault()): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        return SimpleDateFormat("h:mm a", locale).format(calendar.time)
    }
}

object DoseSchedule {
    fun slotsFor(frequency: String, timesCsv: String = ""): List<DoseSlot> {
        val defaults = defaultSlotsFor(frequency)
        val customTimes = timesCsv.split(",")
            .map(::parseTime)
            .take(defaults.size)
        return defaults.mapIndexed { index, slot ->
            customTimes.getOrNull(index)?.let { (hour, minute) ->
                slot.copy(hour = hour, minute = minute)
            } ?: slot
        }
    }

    fun defaultTimesCsv(frequency: String): String =
        defaultSlotsFor(frequency).joinToString(",") { "%02d:%02d".format(it.hour, it.minute) }

    private fun defaultSlotsFor(frequency: String): List<DoseSlot> = when (frequency.uppercase()) {
        "OD" -> listOf(DoseSlot("Morning Dose", 8, 0))
        "BD" -> listOf(
            DoseSlot("Morning Dose", 8, 0),
            DoseSlot("Evening Dose", 20, 0)
        )
        "TID" -> listOf(
            DoseSlot("Morning Dose", 7, 0),
            DoseSlot("Afternoon Dose", 14, 0),
            DoseSlot("Night Dose", 21, 0)
        )
        "QID" -> listOf(
            DoseSlot("Dose 1", 6, 0),
            DoseSlot("Dose 2", 12, 0),
            DoseSlot("Dose 3", 18, 0),
            DoseSlot("Dose 4", 22, 0)
        )
        else -> defaultSlotsFor("BD")
    }

    fun dosesPerDay(frequency: String): Int = slotsFor(frequency).size

    private fun parseTime(value: String): Pair<Int, Int>? {
        val parts = value.trim().split(":")
        if (parts.size != 2) return null
        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null
        if (hour !in 0..23 || minute !in 0..59) return null
        return hour to minute
    }
}
