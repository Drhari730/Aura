package com.mhealth.aura.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val age: String = "",
    val gender: String = "",
    val email: String = "",
    val state: String = "",
    val district: String = "",
    val city: String = "",
    val pincode: String = "",
    val condition: String = "",
    val antibiotic: String = "",
    val dose: String = "",
    val frequency: String = "BD",
    val durationDays: Int = 7,
    val startDateMillis: Long = System.currentTimeMillis(),
    val endDateMillis: Long = System.currentTimeMillis() + 6 * 86_400_000L,
    val doseTimesCsv: String = "08:00,20:00",
    val doseRemindersEnabled: Boolean = true,
    val missedDoseAlertsEnabled: Boolean = true,
    val dailySummaryEnabled: Boolean = true,
    val preDoseMinutes: Int = 15,
    val missedDoseMinutes: Int = 120,
    val summaryHour: Int = 21,
    val summaryMinute: Int = 30,
    val doctorName: String = "",
    val hospitalName: String = "",
    val hospitalLocation: String = "",
    val language: String = "kn",
    val adherenceStreakDays: Int = 0,
    val totalXp: Int = 0,
    val lastDoseDateMillis: Long = 0L
)
