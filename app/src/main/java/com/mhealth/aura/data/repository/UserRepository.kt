package com.mhealth.aura.data.repository

import com.mhealth.aura.data.db.dao.UserDao
import com.mhealth.aura.data.db.entity.UserEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(private val dao: UserDao) {
    val user: Flow<UserEntity?> = dao.getUser()

    suspend fun saveUser(user: UserEntity) = dao.saveUser(user)
    suspend fun updateUser(user: UserEntity) = dao.updateUser(user)
}
