package com.mhealth.aura.data.repository

import com.mhealth.aura.data.db.dao.DoseLogDao
import com.mhealth.aura.data.db.entity.DoseLogEntity
import kotlinx.coroutines.flow.Flow
import java.util.*

class DoseRepository(private val dao: DoseLogDao) {
    val allLogs: Flow<List<DoseLogEntity>> = dao.getAllLogs()
    val totalTaken: Flow<Int> = dao.getTotalTaken()

    fun getLogsForToday(): Flow<List<DoseLogEntity>> {
        val cal = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
        val startOfDay = cal.timeInMillis
        val endOfDay = startOfDay + 86_400_000L
        return dao.getLogsForDay(startOfDay, endOfDay)
    }

    suspend fun logDose(
        medicationId: Long,
        medicationName: String,
        label: String,
        status: String = "taken"
    ) {
        dao.insertLog(
            DoseLogEntity(
                medicationId = medicationId,
                medicationName = medicationName,
                doseLabel = label,
                status = status
            )
        )
    }

    suspend fun logDoseForDay(
        medicationId: Long,
        medicationName: String,
        label: String,
        dayStartMillis: Long,
        status: String
    ) {
        val recordedAt = if (dayStartMillis <= System.currentTimeMillis()) {
            dayStartMillis + 12 * 60 * 60_000L
        } else {
            dayStartMillis
        }
        dao.insertLog(
            DoseLogEntity(
                medicationId = medicationId,
                medicationName = medicationName,
                dateMillis = recordedAt,
                doseLabel = label,
                status = status,
                takenAtMillis = System.currentTimeMillis()
            )
        )
    }
}
