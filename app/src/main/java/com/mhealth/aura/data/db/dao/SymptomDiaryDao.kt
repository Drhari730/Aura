package com.mhealth.aura.data.db.dao

import androidx.room.*
import com.mhealth.aura.data.db.entity.SymptomDiaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SymptomDiaryDao {
    @Query("SELECT * FROM symptom_diary ORDER BY dateMillis DESC")
    fun getAll(): Flow<List<SymptomDiaryEntity>>

    @Insert
    suspend fun insert(entry: SymptomDiaryEntity)
}
