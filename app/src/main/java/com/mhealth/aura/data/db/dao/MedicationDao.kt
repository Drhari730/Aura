package com.mhealth.aura.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mhealth.aura.data.db.entity.MedicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications ORDER BY isActive DESC, startDateMillis DESC, id DESC")
    fun getAll(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications ORDER BY isActive DESC, startDateMillis DESC, id DESC")
    suspend fun getAllSnapshot(): List<MedicationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medication: MedicationEntity): Long

    @Update
    suspend fun update(medication: MedicationEntity)

    @Delete
    suspend fun delete(medication: MedicationEntity)
}
