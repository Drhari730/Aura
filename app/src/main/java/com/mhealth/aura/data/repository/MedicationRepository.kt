package com.mhealth.aura.data.repository

import com.mhealth.aura.data.db.dao.MedicationDao
import com.mhealth.aura.data.db.entity.MedicationEntity
import kotlinx.coroutines.flow.Flow

class MedicationRepository(private val dao: MedicationDao) {
    val medications: Flow<List<MedicationEntity>> = dao.getAll()

    suspend fun save(medication: MedicationEntity): Long =
        if (medication.id == 0L) dao.insert(medication) else {
            dao.update(medication)
            medication.id
        }

    suspend fun delete(medication: MedicationEntity) = dao.delete(medication)
}
