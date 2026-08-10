package com.mhealth.aura.data.repository

import com.mhealth.aura.data.db.dao.SymptomDiaryDao
import com.mhealth.aura.data.db.entity.SymptomDiaryEntity
import kotlinx.coroutines.flow.Flow

class SymptomDiaryRepository(private val dao: SymptomDiaryDao) {
    val entries: Flow<List<SymptomDiaryEntity>> = dao.getAll()

    suspend fun saveEntry(entry: SymptomDiaryEntity) = dao.insert(entry)
}
