package com.mhealth.aura.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dose_logs")
data class DoseLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: Long = 0,
    val medicationName: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val doseLabel: String = "",
    val status: String = "taken",
    val takenAtMillis: Long = System.currentTimeMillis()
)
