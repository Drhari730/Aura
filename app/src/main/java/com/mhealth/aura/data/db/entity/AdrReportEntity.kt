package com.mhealth.aura.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "adr_reports")
data class AdrReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateMillis: Long = System.currentTimeMillis(),
    val medication: String = "",
    val symptoms: String = "",
    val severity: String = "Mild",
    val notes: String = ""
)
