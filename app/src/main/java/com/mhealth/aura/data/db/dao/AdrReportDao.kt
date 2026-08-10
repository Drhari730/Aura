package com.mhealth.aura.data.db.dao

import androidx.room.*
import com.mhealth.aura.data.db.entity.AdrReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AdrReportDao {
    @Query("SELECT * FROM adr_reports ORDER BY dateMillis DESC")
    fun getAll(): Flow<List<AdrReportEntity>>

    @Insert
    suspend fun insert(report: AdrReportEntity)
}
