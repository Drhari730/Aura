package com.mhealth.aura.data.repository

import com.mhealth.aura.data.db.dao.AdrReportDao
import com.mhealth.aura.data.db.entity.AdrReportEntity
import kotlinx.coroutines.flow.Flow

class AdrRepository(private val dao: AdrReportDao) {
    val reports: Flow<List<AdrReportEntity>> = dao.getAll()

    suspend fun submitReport(report: AdrReportEntity) = dao.insert(report)
}
