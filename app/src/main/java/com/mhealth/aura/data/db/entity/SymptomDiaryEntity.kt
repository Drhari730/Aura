package com.mhealth.aura.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "symptom_diary")
data class SymptomDiaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateMillis: Long = System.currentTimeMillis(),
    val mood: Int = 3,
    val symptoms: String = "",
    val notes: String = ""
)
