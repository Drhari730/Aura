package com.mhealth.aura.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val dose: String = "",
    val frequency: String = "BD",
    val startDateMillis: Long = System.currentTimeMillis(),
    val endDateMillis: Long = System.currentTimeMillis() + 6 * 86_400_000L,
    val doseTimesCsv: String = "08:00,20:00",
    val isActive: Boolean = true
)
