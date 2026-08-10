package com.mhealth.aura.data.db.dao

import androidx.room.*
import com.mhealth.aura.data.db.entity.DoseLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseLogDao {
    @Query("SELECT * FROM dose_logs ORDER BY dateMillis DESC")
    fun getAllLogs(): Flow<List<DoseLogEntity>>

    @Query("SELECT * FROM dose_logs WHERE dateMillis >= :startOfDay AND dateMillis < :endOfDay")
    fun getLogsForDay(startOfDay: Long, endOfDay: Long): Flow<List<DoseLogEntity>>

    @Query("SELECT COUNT(*) FROM dose_logs WHERE status = 'taken'")
    fun getTotalTaken(): Flow<Int>

    @Query(
        "SELECT COUNT(*) FROM dose_logs " +
            "WHERE medicationId = :medicationId AND doseLabel = :label AND status = 'taken' " +
            "AND dateMillis >= :startOfDay AND dateMillis < :endOfDay"
    )
    suspend fun countTakenForDose(
        medicationId: Long,
        label: String,
        startOfDay: Long,
        endOfDay: Long
    ): Int

    @Query(
        "SELECT COUNT(*) FROM dose_logs " +
            "WHERE status = 'taken' AND dateMillis >= :startOfDay AND dateMillis < :endOfDay"
    )
    suspend fun countTakenForDay(startOfDay: Long, endOfDay: Long): Int

    @Insert
    suspend fun insertLog(log: DoseLogEntity)
}
